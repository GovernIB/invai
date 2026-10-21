package es.caib.invai.back.ejb.application.dir3;

import es.caib.invai.back.config.BackgroundJobAuthentication;
import es.caib.invai.back.config.UserSessionTokenProvider;
import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.exception.UserSessionTokenException;
import es.caib.invai.back.interna.application.dir3.DTO.ApplicationDir3MismatchOutputDTO;
import es.caib.invai.back.service.model.application.dir3.Dir3MismatchAuditState;
import es.caib.invai.back.interna.application.dir3.DTO.Dir3MismatchAuditStatusOutputDTO;
import es.caib.invai.back.service.facade.application.core.ApplicationService;
import es.caib.invai.back.service.facade.application.dir3.Dir3CheckService;
import es.caib.invai.back.service.facade.application.dir3.Dir3BackgroundAuditService;
import es.caib.invai.back.service.facade.maintenance.responsible.person.PersonService;
import es.caib.invai.back.service.model.application.dir3.PrimaryGroupDir3;
import es.caib.invai.back.utils.Constants;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Objects;

/**
 * Facade service implementation of the background DIR3 audit of every application. A single
 * worker thread goes through the applications one by one, delegating each to
 * {@link Dir3CheckService#checkDir3Mismatch} - so each application is audited in its own
 * transaction, and its Soffid calls are paced by the Soffid rate limiter. An application whose
 * audit fails is recorded and skipped, never retried within the same run, and never read as a
 * mismatch.
 * <p>
 * The worker has no HTTP request, so it runs under a {@link BackgroundJobAuthentication}: it
 * carries the launching user's name (for the audit fields) and their login authentication, whose
 * token - renewed as it expires - the calls to {@code invai-api-interna} are made with. The audit
 * therefore depends on that user's login session staying valid at the identity provider; if it is
 * lost, the audit stops and ends {@code FAILED} with the reason. The state lives in this
 * instance's memory, so it is per application server node and is lost on restart.
 * </p>
 *
 * @since 1.0.6
 */
@Service
@Slf4j
public class Dir3BackgroundAuditServiceFacadeBean implements Dir3BackgroundAuditService {

    /** Facade providing the application list. */
    @Autowired
    private ApplicationService applicationService;

    /** Facade providing the per-application DIR3 audit and the people to audit. */
    @Autowired
    private Dir3CheckService dir3CheckService;

    /** Takes the picture of the DIR3 of the people to audit, from Soffid, before the run. */
    @Autowired
    private PersonService personService;

    /** Source of the launching user's renewable token. */
    @Autowired
    private UserSessionTokenProvider userSessionTokenProvider;

    /** Single worker thread: audits never overlap, whoever launches them. */
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "dir3-mismatch-audit");
        thread.setDaemon(true);
        return thread;
    });

    // State of the running audit, or of the last one. Every access is synchronized on this.
    private Dir3MismatchAuditState state = Dir3MismatchAuditState.IDLE;
    private String startedBy;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String failureReason;
    private int totalApplications;
    private int processedApplications;
    private final List<ApplicationDir3MismatchOutputDTO> mismatchedApplications = new ArrayList<>();
    private final List<Long> failedApplicationIds = new ArrayList<>();

    /**
     * {@inheritDoc}
     * <p>
     * The launching user's token is requested before launching anything, so a session that can't
     * be used (or renewed) is reported right away instead of making every application fail in the
     * background.
     * </p>
     */
    @Override
    public synchronized Dir3MismatchAuditStatusOutputDTO start(String startedBy) {
        if (state == Dir3MismatchAuditState.RUNNING) {
            log.debug("Facade: DIR3 audit already running, not starting another");
            return snapshot();
        }
        Authentication userAuthentication = verifyUserSession();

        this.state = Dir3MismatchAuditState.RUNNING;
        this.startedBy = startedBy;
        this.startedAt = LocalDateTime.now();
        this.finishedAt = null;
        this.failureReason = null;
        this.totalApplications = 0;
        this.processedApplications = 0;
        this.mismatchedApplications.clear();
        this.failedApplicationIds.clear();

        log.debug("Facade: Launching the background DIR3 audit requested by {}", startedBy);
        executor.execute(() -> run(startedBy, userAuthentication));
        return snapshot();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized Dir3MismatchAuditStatusOutputDTO getStatus() {
        return snapshot();
    }

    /** Stops the worker when the application shuts down. */
    @PreDestroy
    void shutdown() {
        executor.shutdownNow();
    }

    /**
     * Checks that the calling user's login can supply a token now and keep renewing it.
     *
     * @return the calling user's login authentication
     * @throws BusinessRuleException if there is no user session, it can't be renewed, or its token can't be obtained
     */
    private Authentication verifyUserSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication) || !userSessionTokenProvider.canRenew(authentication)) {
            log.error("Facade: Cannot launch the DIR3 audit, the user's session cannot be renewed in the background");
            throw new BusinessRuleException(Constants.ERR_DIR3AUDIT_SESSION);
        }
        try {
            userSessionTokenProvider.getAccessToken(authentication);
        } catch (UserSessionTokenException ex) {
            log.error("Facade: Cannot launch the DIR3 audit, the user's token is unavailable", ex);
            throw new BusinessRuleException(Constants.ERR_DIR3AUDIT_SESSION);
        }
        return authentication;
    }

    /**
     * Body of the worker: audits every application, one at a time, under the launching user.
     */
    private void run(String username, Authentication userAuthentication) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new BackgroundJobAuthentication(username, userAuthentication));
        SecurityContextHolder.setContext(context);
        try {
            List<Long> applicationIds = applicationService.getAllApplicationIds();
            setTotalApplications(applicationIds.size());
            PrimaryGroupDir3 primaryGroupDir3 = loadPrimaryGroupDir3();
            for (Long applicationId : applicationIds) {
                if (Thread.currentThread().isInterrupted()) {
                    log.warn("Facade: DIR3 audit interrupted");
                    finish(Dir3MismatchAuditState.FAILED, null);
                    return;
                }
                auditApplication(applicationId, primaryGroupDir3);
            }
            finish(Dir3MismatchAuditState.COMPLETED, null);
        } catch (UserSessionTokenException ex) {
            log.error("Facade: DIR3 audit stopped, the user's session is no longer valid", ex);
            finish(Dir3MismatchAuditState.FAILED, Constants.ERR_DIR3AUDIT_SESSION);
        } catch (BusinessRuleException ex) {
            log.error("Facade: DIR3 audit aborted: {}", ex.getMessage());
            finish(Dir3MismatchAuditState.FAILED, ex.getMessage());
        } catch (RuntimeException ex) {
            log.error("Facade: DIR3 audit aborted", ex);
            finish(Dir3MismatchAuditState.FAILED, null);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * Takes, with a few bulk calls to Soffid, the picture of the DIR3 of everyone holding an
     * assignment on any application, so that each application is then audited without calling Soffid
     * at all. If Soffid can't be queried there is nothing to audit with, so the whole run fails.
     *
     * @return the picture to audit every application with
     * @throws UserSessionTokenException if the launching user's session was lost while asking
     * @throws BusinessRuleException     if Soffid could not be queried for any other reason
     */
    private PrimaryGroupDir3 loadPrimaryGroupDir3() {
        try {
            return personService.getPrimaryGroupDir3FromSoffid(dir3CheckService.getAssignedPersonEmails());
        } catch (RuntimeException ex) {
            UserSessionTokenException sessionLoss = findSessionLoss(ex);
            if (Objects.nonNull(sessionLoss)) {
                throw sessionLoss;
            }
            log.error("Facade: Could not load from Soffid the data to audit DIR3 with", ex);
            throw new BusinessRuleException(Constants.ERR_DIR3AUDIT_SOFFID);
        }
    }

    /**
     * Audits one application and records the outcome. A failure is recorded and never stops the
     * run - except the loss of the user's session, which makes every remaining application fail
     * too, so it is propagated to stop the run.
     */
    private void auditApplication(Long applicationId, PrimaryGroupDir3 primaryGroupDir3) {
        try {
            ApplicationDir3MismatchOutputDTO result = dir3CheckService.checkDir3Mismatch(applicationId, primaryGroupDir3);
            recordProcessed(result);
        } catch (RuntimeException ex) {
            UserSessionTokenException sessionLoss = findSessionLoss(ex);
            if (Objects.nonNull(sessionLoss)) {
                throw sessionLoss;
            }
            log.warn("Facade: DIR3 audit failed for application ID: {}, leaving it unchanged", applicationId, ex);
            recordFailed(applicationId);
        }
    }

    /**
     * @return the {@link UserSessionTokenException} in the cause chain of {@code ex}, or {@code null} if there is none
     */
    private static UserSessionTokenException findSessionLoss(Throwable ex) {
        for (Throwable cause = ex; Objects.nonNull(cause); cause = cause.getCause()) {
            if (cause instanceof UserSessionTokenException sessionLoss) {
                return sessionLoss;
            }
        }
        return null;
    }

    private synchronized void setTotalApplications(int total) {
        this.totalApplications = total;
    }

    private synchronized void recordProcessed(ApplicationDir3MismatchOutputDTO result) {
        processedApplications++;
        if (result.isDir3Mismatch()) {
            mismatchedApplications.add(result);
        }
    }

    private synchronized void recordFailed(Long applicationId) {
        processedApplications++;
        failedApplicationIds.add(applicationId);
    }

    private synchronized void finish(Dir3MismatchAuditState finalState, String reason) {
        this.state = finalState;
        this.failureReason = reason;
        this.finishedAt = LocalDateTime.now();
    }

    /** Immutable copy of the current state; callers must hold the lock on {@code this}. */
    private Dir3MismatchAuditStatusOutputDTO snapshot() {
        return Dir3MismatchAuditStatusOutputDTO.builder()
                .state(state)
                .startedBy(startedBy)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .failureReason(failureReason)
                .totalApplications(totalApplications)
                .processedApplications(processedApplications)
                .mismatchedApplications(new ArrayList<>(mismatchedApplications))
                .failedApplicationIds(new ArrayList<>(failedApplicationIds))
                .build();
    }
}
