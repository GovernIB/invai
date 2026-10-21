package es.caib.invai.back.service.facade.application.dir3;

import es.caib.invai.back.interna.application.dir3.DTO.ApplicationDir3MismatchOutputDTO;
import es.caib.invai.back.service.facade.maintenance.responsible.person.PersonService;
import es.caib.invai.back.service.model.application.dir3.PrimaryGroupDir3;

import java.util.List;

/**
 * Service Facade boundary interface declaring the DIR3 audit of the applications: checking the
 * DIR3 of each person holding a responsible/authorized assignment against the administrative unit
 * of the application, and keeping each application's {@code dir3Mismatch} flag in line with the
 * result.
 *
 * @since 1.0.6
 */
public interface Dir3CheckService {

    /**
     * Audits every application in the inventory, checking each active responsible/authorized
     * assignment's DIR3 live against Soffid, against the owning application's administrative unit.
     * Only {@code NOT_APPLY} assignments are skipped outright (never checked, never changed, never
     * flagged); every other assignment ({@code VALIDATED}, {@code NOT_VALIDATED}, or {@code MANUAL}) is
     * always checked. On a match, a {@code NOT_VALIDATED} or {@code MANUAL} assignment is promoted to
     * {@code VALIDATED}; on a mismatch, only a {@code VALIDATED} assignment is downgraded to
     * {@code NOT_VALIDATED} ({@code MANUAL} is protected from the downgrade direction only). Only that
     * one downgrade transition counts towards flagging its application - a promotion, or an
     * assignment already {@code NOT_VALIDATED}/{@code MANUAL} mismatching again, isn't news.
     *
     * @return the applications with at least one assignment that just mismatched, identified by ID and name
     * @since 1.0.6
     */
    List<ApplicationDir3MismatchOutputDTO> checkDir3MismatchForAllApplications();

    /**
     * Runs the same DIR3 audit as {@link #checkDir3MismatchForAllApplications()}, but for a single
     * application: every active responsible/authorized assignment is checked live against Soffid
     * against the application's administrative unit, using the same status transitions
     * ({@code NOT_APPLY} skipped, {@code MANUAL} protected from downgrade), and the application's
     * persisted {@code dir3Mismatch} flag is refreshed to its current real state. The whole audit
     * of the application runs in its own transaction: if Soffid can't be queried for any
     * assignment, the exception propagates and nothing is changed for that application (a failed
     * check is never read as a mismatch).
     *
     * @param id the unique identifier of the application to audit
     * @return the application's ID and name together with its resulting {@code dir3Mismatch} state
     * ({@code true} if any active, Personal CAIB assignment is left {@code NOT_VALIDATED})
     * @throws es.caib.invai.back.exception.BusinessRuleException if no application exists with the given ID
     * @since 1.0.6
     */
    ApplicationDir3MismatchOutputDTO checkDir3Mismatch(Long id);

    /**
     * Same audit as {@link #checkDir3Mismatch(Long)}, but checking each person against a picture of
     * their DIR3 taken beforehand (see {@link PersonService#getPrimaryGroupDir3FromSoffid}) instead of asking
     * Soffid about them: it makes no call to Soffid at all, so a run over every application can
     * take the picture once and audit each application in its own short transaction.
     *
     * @param id       the unique identifier of the application to audit
     * @param primaryGroupDir3 the picture of the DIR3 of the people holding an assignment on it
     * @return the application's ID and name together with its resulting {@code dir3Mismatch} state
     * @throws es.caib.invai.back.exception.BusinessRuleException if no application exists with the given ID
     * @since 1.0.6
     */
    ApplicationDir3MismatchOutputDTO checkDir3Mismatch(Long id, PrimaryGroupDir3 primaryGroupDir3);

    /**
     * Lists the e-mail addresses of every Personal CAIB person holding an active responsible or
     * authorized assignment on any application: exactly the people a DIR3 audit of every application
     * needs to know about.
     *
     * @return the distinct e-mail addresses
     * @since 1.0.6
     */
    List<String> getAssignedPersonEmails();
}
