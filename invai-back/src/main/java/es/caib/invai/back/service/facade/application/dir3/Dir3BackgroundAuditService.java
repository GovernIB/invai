package es.caib.invai.back.service.facade.application.dir3;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.dir3.DTO.Dir3MismatchAuditStatusOutputDTO;

/**
 * Facade boundary for the background DIR3 audit of every application. Auditing hundreds of
 * applications means thousands of Soffid calls, far too slow (and too heavy for Soffid) to do inside
 * one HTTP request, so the audit runs in a background worker that goes through the applications one
 * at a time, each in its own transaction, while every Soffid call is paced by the Soffid rate
 * limiter. The worker acts with the launching user's login token, renewed as it expires. The
 * caller launches it and then polls its progress.
 *
 * @since 1.0.6
 */
public interface Dir3BackgroundAuditService {

    /**
     * Launches the audit in the background and returns immediately. If an audit is already
     * running, no second one is started and the running one's status is returned instead, so
     * several people (or double clicks) can never multiply the load on Soffid.
     *
     * @param startedBy username of the person launching the audit; the changes the audit makes are
     *                  attributed to them
     * @return the status of the audit that is running (just started, or already in progress)
     * @throws BusinessRuleException if the launching user's login session can't be renewed in the
     *                               background (no refresh token, or its token can't be obtained)
     */
    Dir3MismatchAuditStatusOutputDTO start(String startedBy);

    /**
     * @return the progress of the running audit, or the outcome of the last one; {@code IDLE} if
     * none has been launched since the application started
     */
    Dir3MismatchAuditStatusOutputDTO getStatus();
}
