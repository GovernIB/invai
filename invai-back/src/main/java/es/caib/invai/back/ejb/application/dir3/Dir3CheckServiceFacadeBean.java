package es.caib.invai.back.ejb.application.dir3;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.dir3.DTO.ApplicationDir3MismatchOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonDir3CheckOutputDTO;
import es.caib.invai.back.persistence.repository.application.core.ApplicationCriteria;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.core.AppResponsibleAuthorizedRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.dir3.Dir3ValidationRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleCriteria;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleRepository;
import es.caib.invai.back.service.facade.application.dir3.Dir3CheckService;
import es.caib.invai.back.service.facade.maintenance.responsible.person.PersonService;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.dir3.PrimaryGroupDir3;
import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.dir3.Dir3Validation;
import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import es.caib.invai.back.service.model.catalog.validationStatus.ValidationStatusEnum;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
import es.caib.invai.back.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.Objects;

/**
 * Facade service implementation of the DIR3 audit of the applications: checks the DIR3 of each
 * active responsible/authorized assignment against the administrative unit of its application,
 * updates the assignment's DIR3 validation status and keeps the application's {@code dir3Mismatch}
 * flag in line with the result.
 * <p>
 * Every audit of an application runs in its own transaction (the one opened by the call to
 * {@link #checkDir3Mismatch}), so a failure while auditing an application rolls back everything
 * done for that application.
 * </p>
 *
 * @since 1.0.6
 */
@Service
@Slf4j
@Transactional
public class Dir3CheckServiceFacadeBean implements Dir3CheckService {

    /** Outbound port repository handling persistence operations for the Application domain model. */
    @Autowired
    private ApplicationRepository applicationRepository;

    /** Repository handling persistence of the "Responsables i Autoritzats" tab linked to an application. */
    @Autowired
    private AppResponsibleAuthorizedRepository appResponsibleAuthorizedRepository;

    /** Repository used to read the active responsible assignments of an application. */
    @Autowired
    private AppResponsibleRepository appResponsibleRepository;

    /** Repository used to read the active authorized assignments of an application. */
    @Autowired
    private AppAuthorizedRepository appAuthorizedRepository;

    /** Facade used to check a person's DIR3 against an administrative unit code live against Soffid. */
    @Autowired
    private PersonService personService;

    /** Repository used to update the DIR3 validation record linked to each responsible/authorized assignment. */
    @Autowired
    private Dir3ValidationRepository dir3ValidationRepository;

    /**
     * {@inheritDoc}
     * <p>
     * Applications with a blank administrative unit, or with no "Responsables i Autoritzats" anchor
     * yet, are skipped entirely - their assignments can never mismatch (see
     * {@link ValidationStatusEnum#resolve}); if either was previously flagged as mismatching (e.g.
     * the administrative unit was cleared afterward), the stale flag is cleared. Every
     * {@code VALIDATED}, {@code NOT_VALIDATED}, or {@code MANUAL} assignment is always checked live
     * against Soffid; only {@code NOT_APPLY} is skipped outright. {@code MANUAL} is promotable to
     * {@code VALIDATED} on a match, but protected from the change status (see
     * {@link #checkDir3AndModifiedStatus}).
     * </p>
     * <p>
     * A single {@code hasMismatch} signal, computed per application from the resulting status of
     * every active, Personal CAIB assignment after the check above ({@code true} if any of them is
     * left {@code NOT_VALIDATED} - {@code MANUAL} never counts), drives both outcomes: it's persisted
     * as-is on {@code Application.dir3Mismatch} (written only when it actually differs from the
     * currently stored value, so a run that changes nothing writes nothing), and it decides whether
     * the application is included in the returned list - which therefore reports every application
     * currently mismatching, not just ones that changed in this run.
     * </p>
     */
    @Override
    public List<ApplicationDir3MismatchOutputDTO> checkDir3MismatchForAllApplications() {
        log.debug("Facade: Auditing DIR3 mismatches across every application");
        List<ApplicationDir3MismatchOutputDTO> mismatchedApplications = new ArrayList<>();
        for (Application application : applicationRepository.findAll(new ApplicationCriteria(), Pageable.unpaged())) {
            if (refreshDir3Mismatch(application, personService::checkDir3)) {
                mismatchedApplications.add(toDir3MismatchOutput(application, true));
            }
        }
        return mismatchedApplications;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Runs exactly the same per-application audit as {@link #checkDir3MismatchForAllApplications}
     * (see {@link #refreshDir3Mismatch}), so the persisted {@code dir3Mismatch} flag and every
     * assignment's status end up identical to what the bulk run would leave for this application.
     * </p>
     */
    @Override
    public ApplicationDir3MismatchOutputDTO checkDir3Mismatch(Long id) {
        return auditDir3Mismatch(id, personService::checkDir3);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationDir3MismatchOutputDTO checkDir3Mismatch(Long id, PrimaryGroupDir3 primaryGroupDir3) {
        return auditDir3Mismatch(id, primaryGroupDir3::check);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> getAssignedPersonEmails() {
        return Stream.concat(appResponsibleRepository.findAllActivePersonalCaibEmails().stream(),
                        appAuthorizedRepository.findAllActivePersonalCaibEmails().stream())
                .distinct()
                .toList();
    }

    /**
     * Audits the application with the given id, finding each person's DIR3 with the given checker -
     * live from Soffid, or from a picture of it already taken.
     */
    private ApplicationDir3MismatchOutputDTO auditDir3Mismatch(Long id, BiFunction<String, String, PersonDir3CheckOutputDTO> dir3Checker) {
        log.debug("Facade: Auditing DIR3 mismatch for application ID: {}", id);
        Application application = applicationRepository.findById(id);
        if (Objects.isNull(application)) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_FOUND);
        }
        return toDir3MismatchOutput(application, refreshDir3Mismatch(application, dir3Checker));
    }

    /**
     * Audits a single application: every active responsible/authorized assignment is checked live
     * against the application's administrative unit (see {@link #checkDir3AndModifiedStatus}), and
     * the application's persisted {@code dir3Mismatch} flag is brought in line with the result. An
     * application with a blank administrative unit, or no "Responsables i Autoritzats" anchor yet,
     * can never mismatch, so any stale flag is cleared instead. If Soffid can't be queried for any
     * assignment, the whole application's audit fails and rolls back (see
     * {@link #checkDir3AndModifiedStatus}).
     *
     * @param application the application to audit
     * @param dir3Checker finds a person's DIR3 and compares it with the administrative unit
     * @return {@code true} if at least one active, Personal CAIB assignment is left
     * {@code NOT_VALIDATED} ({@code MANUAL} never counts)
     */
    private boolean refreshDir3Mismatch(Application application, BiFunction<String, String, PersonDir3CheckOutputDTO> dir3Checker) {
        if (StringUtils.isBlank(application.getAdmUnitCode())) {
            clearDir3MismatchIfSet(application);
            return false;
        }
        AppResponsibleAuthorized anchor = appResponsibleAuthorizedRepository.findByApplicationId(application.getId());
        if (Objects.isNull(anchor)) {
            clearDir3MismatchIfSet(application);
            return false;
        }
        boolean hasMismatch = false;
        AppResponsibleCriteria activeCriteria = AppResponsibleCriteria.builder().statusId(StatusEnum.ACTIVE.getId()).build();
        for (AppResponsible responsible : appResponsibleRepository.findAll(anchor.getId(), activeCriteria, Pageable.unpaged())) {
            checkDir3AndModifiedStatus(responsible.getPerson(), responsible.getDir3Validation(), application.getAdmUnitCode(), dir3Checker);
            if (responsible.getDir3Validation().getDir3Status() == ValidationStatusEnum.NOT_VALIDATED) {
                hasMismatch = true;
            }
        }
        for (AppAuthorized authorized : appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(anchor.getId())) {
            checkDir3AndModifiedStatus(authorized.getPerson(), authorized.getDir3Validation(), application.getAdmUnitCode(), dir3Checker);
            if (authorized.getDir3Validation().getDir3Status() == ValidationStatusEnum.NOT_VALIDATED) {
                hasMismatch = true;
            }
        }
        if (application.isDir3Mismatch() != hasMismatch) {
            application.setDir3Mismatch(hasMismatch);
            applicationRepository.update(application, application.getId());
        }
        return hasMismatch;
    }

    /**
     * Builds the DIR3 audit output row identifying an application and its current mismatch state.
     */
    private ApplicationDir3MismatchOutputDTO toDir3MismatchOutput(Application application, boolean dir3Mismatch) {
        return ApplicationDir3MismatchOutputDTO.builder()
                .id(application.getId())
                .name(application.getName())
                .dir3Mismatch(dir3Mismatch)
                .build();
    }

    /**
     * Clears a previously-set {@code dir3Mismatch} flag on an application that can no longer
     * mismatch (blank administrative unit, or no "Responsables i Autoritzats" anchor yet), avoiding
     * an unnecessary write - and audit trail row - when the flag is already {@code false}.
     */
    private void clearDir3MismatchIfSet(Application application) {
        if (application.isDir3Mismatch()) {
            application.setDir3Mismatch(false);
            applicationRepository.update(application, application.getId());
        }
    }

    /**
     * Checks a single assignment's person against the given administrative unit code - a person who
     * isn't Personal CAIB, or whose assignment is already {@code NOT_APPLY}, is skipped without
     * calling Soffid at all, since neither can ever change here. Every other assignment
     * ({@code VALIDATED}, {@code NOT_VALIDATED}, or {@code MANUAL}) is always checked live against
     * Soffid: on a match, a {@code NOT_VALIDATED} or {@code MANUAL} assignment is promoted to
     * {@code VALIDATED}; on a mismatch, only a {@code VALIDATED} assignment is downgraded to
     * {@code NOT_VALIDATED} - {@code MANUAL} is protected from the downgrade direction only. Callers
     * read the outcome by inspecting {@code dir3Validation}'s status afterward (see
     * {@link #checkDir3MismatchForAllApplications}), rather than from a return value here.
     * <p>
     * A failure to query Soffid (timeout, outage, saturation) is never read as a mismatch: the
     * exception propagates, so the application being audited rolls back untouched and can simply be
     * audited again later.
     * </p>
     */
    private void checkDir3AndModifiedStatus(Person person, Dir3Validation dir3Validation, String admUnitCode,
                                            BiFunction<String, String, PersonDir3CheckOutputDTO> dir3Checker) {
        ValidationStatusEnum previousStatus = dir3Validation.getDir3Status();
        if (!person.isPersonalCaib() || previousStatus == ValidationStatusEnum.NOT_APPLY) {
            return;
        }
        PersonDir3CheckOutputDTO checkResult = dir3Checker.apply(person.getEmail(), admUnitCode);
        if (Objects.nonNull(checkResult) && checkResult.isMatches()) {
            if (previousStatus == ValidationStatusEnum.NOT_VALIDATED || previousStatus == ValidationStatusEnum.MANUAL) {
                dir3Validation.setDir3Status(ValidationStatusEnum.VALIDATED);
                dir3ValidationRepository.update(dir3Validation, dir3Validation.getId());
            }
            return;
        }
        if (previousStatus == ValidationStatusEnum.VALIDATED) {
            dir3Validation.setDir3Status(ValidationStatusEnum.NOT_VALIDATED);
            dir3ValidationRepository.update(dir3Validation, dir3Validation.getId());
        }
    }
}
