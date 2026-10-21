package es.caib.invai.back.ejb.application.dir3;

import static org.mockito.ArgumentMatchers.eq;
import es.caib.invai.back.exception.SoffidTimeoutException;
import es.caib.invai.back.service.model.application.dir3.PrimaryGroupDir3;
import es.caib.invai.back.service.facade.maintenance.responsible.person.PersonService;
import es.caib.invai.back.config.BackgroundJobAuthentication;
import es.caib.invai.back.config.UserSessionTokenProvider;
import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.exception.UserSessionTokenException;
import es.caib.invai.back.interna.application.dir3.DTO.ApplicationDir3MismatchOutputDTO;
import es.caib.invai.back.service.model.application.dir3.Dir3MismatchAuditState;
import es.caib.invai.back.interna.application.dir3.DTO.Dir3MismatchAuditStatusOutputDTO;
import es.caib.invai.back.service.facade.application.core.ApplicationService;
import es.caib.invai.back.service.facade.application.dir3.Dir3CheckService;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link Dir3BackgroundAuditServiceFacadeBean}. The worker thread is real, so the tests
 * wait (with a timeout) for the audit to leave {@code RUNNING}; every collaborator is mocked.
 */
@ExtendWith(MockitoExtension.class)
class Dir3BackgroundAuditServiceFacadeBeanTest {

    @Mock
    private ApplicationService applicationService;

    @Mock
    private Dir3CheckService dir3CheckService;

    @Mock
    private UserSessionTokenProvider userSessionTokenProvider;

    @Mock
    private PersonService personService;

    @InjectMocks
    private Dir3BackgroundAuditServiceFacadeBean service;

    private final Authentication login = new UsernamePasswordAuthenticationToken("jperez", null, List.of());

    @BeforeEach
    void logIn() {
        SecurityContextHolder.getContext().setAuthentication(login);
    }

    @AfterEach
    void stopWorker() {
        service.shutdown();
        SecurityContextHolder.clearContext();
    }

    /** The launching user's session can be renewed and yields a token. */
    private void sessionRenewable() {
        when(userSessionTokenProvider.canRenew(login)).thenReturn(true);
        when(userSessionTokenProvider.getAccessToken(login)).thenReturn("token");
    }

    private Dir3MismatchAuditStatusOutputDTO awaitEnd() throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        Dir3MismatchAuditStatusOutputDTO status = service.getStatus();
        while (status.getState() == Dir3MismatchAuditState.RUNNING) {
            if (System.nanoTime() > deadline) {
                throw new AssertionError("the audit did not finish in time: " + status.getState());
            }
            Thread.sleep(10);
            status = service.getStatus();
        }
        return status;
    }

    private static ApplicationDir3MismatchOutputDTO result(Long id, boolean mismatch) {
        return ApplicationDir3MismatchOutputDTO.builder().id(id).name("App " + id).dir3Mismatch(mismatch).build();
    }

    @Test
    void getStatus_beforeAnyAudit_isIdle() {
        Dir3MismatchAuditStatusOutputDTO status = service.getStatus();

        assertEquals(Dir3MismatchAuditState.IDLE, status.getState());
        assertNull(status.getStartedBy());
        assertNull(status.getStartedAt());
        assertEquals(0, status.getTotalApplications());
        assertTrue(status.getMismatchedApplications().isEmpty());
        assertTrue(status.getFailedApplicationIds().isEmpty());
    }

    @Test
    void start_sessionCannotBeRenewed_throwsAndLaunchesNothing() {
        when(userSessionTokenProvider.canRenew(login)).thenReturn(false);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.start("jperez"));

        assertEquals(Constants.ERR_DIR3AUDIT_SESSION, ex.getMessage());
        verify(applicationService, never()).getAllApplicationIds();
        assertEquals(Dir3MismatchAuditState.IDLE, service.getStatus().getState());
    }

    @Test
    void start_noAuthentication_throwsAndLaunchesNothing() {
        SecurityContextHolder.clearContext();

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.start("jperez"));

        assertEquals(Constants.ERR_DIR3AUDIT_SESSION, ex.getMessage());
        verify(applicationService, never()).getAllApplicationIds();
    }

    @Test
    void start_tokenUnavailable_throwsAndLaunchesNothing() {
        when(userSessionTokenProvider.canRenew(login)).thenReturn(true);
        when(userSessionTokenProvider.getAccessToken(login)).thenThrow(new UserSessionTokenException("expired"));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.start("jperez"));

        assertEquals(Constants.ERR_DIR3AUDIT_SESSION, ex.getMessage());
        verify(applicationService, never()).getAllApplicationIds();
        assertEquals(Dir3MismatchAuditState.IDLE, service.getStatus().getState());
    }

    @Test
    void start_sessionLostMidRun_stopsTheAuditAndReportsWhy() throws Exception {
        sessionRenewable();
        when(applicationService.getAllApplicationIds()).thenReturn(List.of(1L, 2L, 3L));
        when(dir3CheckService.checkDir3Mismatch(eq(1L), any())).thenReturn(result(1L, false));
        when(dir3CheckService.checkDir3Mismatch(eq(2L), any()))
                .thenThrow(new RuntimeException("wrapped", new UserSessionTokenException("session gone")));

        service.start("jperez");
        Dir3MismatchAuditStatusOutputDTO ended = awaitEnd();

        assertEquals(Dir3MismatchAuditState.FAILED, ended.getState());
        assertEquals(Constants.ERR_DIR3AUDIT_SESSION, ended.getFailureReason());
        assertEquals(1, ended.getProcessedApplications());
        assertTrue(ended.getFailedApplicationIds().isEmpty());
        verify(dir3CheckService, never()).checkDir3Mismatch(eq(3L), any());
    }

    @Test
    void start_auditsEveryApplicationAndReportsMismatchesAndFailures() throws Exception {
        sessionRenewable();
        when(applicationService.getAllApplicationIds()).thenReturn(List.of(1L, 2L, 3L));
        when(dir3CheckService.checkDir3Mismatch(eq(1L), any())).thenReturn(result(1L, false));
        when(dir3CheckService.checkDir3Mismatch(eq(2L), any())).thenReturn(result(2L, true));
        when(dir3CheckService.checkDir3Mismatch(eq(3L), any())).thenThrow(new RuntimeException("Soffid timed out"));

        Dir3MismatchAuditStatusOutputDTO started = service.start("jperez");
        Dir3MismatchAuditStatusOutputDTO ended = awaitEnd();

        assertEquals(Dir3MismatchAuditState.RUNNING, started.getState());
        assertEquals("jperez", started.getStartedBy());
        assertNotNull(started.getStartedAt());
        assertEquals(Dir3MismatchAuditState.COMPLETED, ended.getState());
        assertEquals(3, ended.getTotalApplications());
        assertEquals(3, ended.getProcessedApplications());
        assertEquals(1, ended.getMismatchedApplications().size());
        assertEquals(2L, ended.getMismatchedApplications().get(0).getId());
        assertEquals(List.of(3L), ended.getFailedApplicationIds());
        assertNotNull(ended.getFinishedAt());
    }

    @Test
    void start_oneFailingApplicationDoesNotStopTheRest() throws Exception {
        sessionRenewable();
        when(applicationService.getAllApplicationIds()).thenReturn(List.of(1L, 2L));
        when(dir3CheckService.checkDir3Mismatch(eq(1L), any())).thenThrow(new RuntimeException("boom"));
        when(dir3CheckService.checkDir3Mismatch(eq(2L), any())).thenReturn(result(2L, true));

        service.start("jperez");
        Dir3MismatchAuditStatusOutputDTO ended = awaitEnd();

        verify(dir3CheckService).checkDir3Mismatch(eq(2L), any());
        assertEquals(Dir3MismatchAuditState.COMPLETED, ended.getState());
        assertEquals(2, ended.getProcessedApplications());
    }

    @Test
    void start_whileRunning_doesNotLaunchASecondAuditAndReturnsTheRunningOne() throws Exception {
        sessionRenewable();
        when(applicationService.getAllApplicationIds()).thenReturn(List.of(1L));
        CountDownLatch insideAudit = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(dir3CheckService.checkDir3Mismatch(eq(1L), any())).thenAnswer(invocation -> {
            insideAudit.countDown();
            release.await(10, TimeUnit.SECONDS);
            return result(1L, false);
        });

        service.start("jperez");
        assertTrue(insideAudit.await(10, TimeUnit.SECONDS));
        Dir3MismatchAuditStatusOutputDTO second = service.start("another");

        assertEquals(Dir3MismatchAuditState.RUNNING, second.getState());
        assertEquals("jperez", second.getStartedBy());
        release.countDown();
        awaitEnd();
        verify(applicationService, times(1)).getAllApplicationIds();
        verify(userSessionTokenProvider, times(1)).canRenew(login);
    }

    @Test
    void start_workerRunsAsABackgroundJobOfTheLaunchingUser() throws Exception {
        sessionRenewable();
        when(applicationService.getAllApplicationIds()).thenReturn(List.of(1L));
        AtomicReference<Object> authentication = new AtomicReference<>();
        when(dir3CheckService.checkDir3Mismatch(eq(1L), any())).thenAnswer(invocation -> {
            authentication.set(SecurityContextHolder.getContext().getAuthentication());
            return result(1L, false);
        });

        service.start("jperez");
        awaitEnd();

        BackgroundJobAuthentication seen = assertInstanceOf(BackgroundJobAuthentication.class, authentication.get());
        assertEquals("jperez", seen.getPrincipal());
        assertSame(login, seen.getUserAuthentication());
    }

    @Test
    void start_applicationListUnavailable_endsFailed() throws Exception {
        sessionRenewable();
        when(applicationService.getAllApplicationIds()).thenThrow(new RuntimeException("database down"));

        service.start("jperez");
        Dir3MismatchAuditStatusOutputDTO ended = awaitEnd();

        assertEquals(Dir3MismatchAuditState.FAILED, ended.getState());
        assertEquals(0, ended.getProcessedApplications());
        assertNotNull(ended.getFinishedAt());
        verify(dir3CheckService, never()).checkDir3Mismatch(anyLong(), any());
    }

    @Test
    void start_afterAFinishedAudit_launchesANewOneWithCleanState() throws Exception {
        sessionRenewable();
        when(applicationService.getAllApplicationIds()).thenReturn(List.of(1L));
        when(dir3CheckService.checkDir3Mismatch(eq(1L), any())).thenReturn(result(1L, true), result(1L, false));

        service.start("jperez");
        Dir3MismatchAuditStatusOutputDTO first = awaitEnd();
        service.start("another");
        Dir3MismatchAuditStatusOutputDTO second = awaitEnd();

        assertEquals(1, first.getMismatchedApplications().size());
        assertEquals("another", second.getStartedBy());
        assertEquals(Dir3MismatchAuditState.COMPLETED, second.getState());
        assertTrue(second.getMismatchedApplications().isEmpty());
        assertEquals(1, second.getProcessedApplications());
    }

    // ------------------------------------------------------------------
    // the picture of the DIR3 of the people, taken once from Soffid
    // ------------------------------------------------------------------

    @Test
    void start_loadsThePrimaryGroupDir3OnceWithTheAssignedPeopleAndAuditsEveryApplicationWithIt() throws Exception {
        sessionRenewable();
        PrimaryGroupDir3 primaryGroupDir3 = new PrimaryGroupDir3(java.util.Map.of(), java.util.Map.of());
        when(applicationService.getAllApplicationIds()).thenReturn(List.of(1L, 2L));
        when(dir3CheckService.getAssignedPersonEmails()).thenReturn(List.of("ana@caib.es", "joan@caib.es"));
        when(personService.getPrimaryGroupDir3FromSoffid(List.of("ana@caib.es", "joan@caib.es"))).thenReturn(primaryGroupDir3);
        when(dir3CheckService.checkDir3Mismatch(eq(1L), any())).thenReturn(result(1L, false));
        when(dir3CheckService.checkDir3Mismatch(eq(2L), any())).thenReturn(result(2L, false));

        service.start("jperez");
        awaitEnd();

        verify(personService, times(1)).getPrimaryGroupDir3FromSoffid(any());
        verify(dir3CheckService).checkDir3Mismatch(1L, primaryGroupDir3);
        verify(dir3CheckService).checkDir3Mismatch(2L, primaryGroupDir3);
    }

    @Test
    void start_soffidCannotBeQueriedForThePrimaryGroupDir3_endsFailedWithTheSoffidReasonAndAuditsNothing() throws Exception {
        sessionRenewable();
        when(applicationService.getAllApplicationIds()).thenReturn(List.of(1L, 2L));
        when(personService.getPrimaryGroupDir3FromSoffid(any())).thenThrow(new SoffidTimeoutException());

        service.start("jperez");
        Dir3MismatchAuditStatusOutputDTO ended = awaitEnd();

        assertEquals(Dir3MismatchAuditState.FAILED, ended.getState());
        assertEquals(Constants.ERR_DIR3AUDIT_SOFFID, ended.getFailureReason());
        assertEquals(0, ended.getProcessedApplications());
        verify(dir3CheckService, never()).checkDir3Mismatch(anyLong(), any());
    }

    @Test
    void start_sessionLostWhileLoadingThePrimaryGroupDir3_endsFailedWithTheSessionReason() throws Exception {
        sessionRenewable();
        when(applicationService.getAllApplicationIds()).thenReturn(List.of(1L));
        when(personService.getPrimaryGroupDir3FromSoffid(any()))
                .thenThrow(new RuntimeException("wrapped", new UserSessionTokenException("session gone")));

        service.start("jperez");
        Dir3MismatchAuditStatusOutputDTO ended = awaitEnd();

        assertEquals(Dir3MismatchAuditState.FAILED, ended.getState());
        assertEquals(Constants.ERR_DIR3AUDIT_SESSION, ended.getFailureReason());
        verify(dir3CheckService, never()).checkDir3Mismatch(anyLong(), any());
    }

    @Test
    void start_theTotalIsKnownEvenWhenThePrimaryGroupDir3CannotBeLoaded() throws Exception {
        sessionRenewable();
        when(applicationService.getAllApplicationIds()).thenReturn(List.of(1L, 2L, 3L));
        when(personService.getPrimaryGroupDir3FromSoffid(any())).thenThrow(new SoffidTimeoutException());

        service.start("jperez");
        Dir3MismatchAuditStatusOutputDTO ended = awaitEnd();

        assertEquals(3, ended.getTotalApplications());
    }
}
