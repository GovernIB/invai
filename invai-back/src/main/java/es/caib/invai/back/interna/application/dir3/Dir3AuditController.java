package es.caib.invai.back.interna.application.dir3;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.dir3.DTO.ApplicationDir3MismatchOutputDTO;
import es.caib.invai.back.interna.application.dir3.DTO.Dir3MismatchAuditStatusOutputDTO;
import es.caib.invai.back.service.facade.application.dir3.Dir3CheckService;
import es.caib.invai.back.service.facade.application.dir3.Dir3BackgroundAuditService;
import es.caib.invai.back.utils.Utils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing the DIR3 audit of the applications: the one of every application, run
 * in the background, and the one of a single application.
 * <p>
 * Every endpoint requires the {@code ROLE_INV_SUPER} role.
 * </p>
 *
 * @since 1.0.6
 */
@Tag(name = "Auditoria DIR3", description = "Servei d'auditoria de la concordança del DIR3 de les aplicacions de l'inventari.")
@RestController
@Slf4j
@RequestMapping("application/dir3")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class Dir3AuditController {

    /** Facade implementing the single-application DIR3 audit. */
    private final Dir3CheckService dir3CheckService;

    /** Facade running the DIR3 audit of every application in the background. */
    private final Dir3BackgroundAuditService dir3BackgroundAuditService;

    /**
     * @param dir3CheckService         facade this controller delegates the single-application audit to
     * @param dir3BackgroundAuditService facade running the DIR3 audit of every application in the background
     */
    @Autowired
    public Dir3AuditController(Dir3CheckService dir3CheckService, Dir3BackgroundAuditService dir3BackgroundAuditService) {
        this.dir3CheckService = dir3CheckService;
        this.dir3BackgroundAuditService = dir3BackgroundAuditService;
    }

    /**
     * Launches, in the background, the DIR3 audit of every application in the inventory: each
     * active responsible/authorized assignment's DIR3 is checked against the owning application's
     * administrative unit (an assignment found to mismatch - and not already {@code MANUAL} or
     * {@code NOT_APPLY} - is downgraded to {@code NOT_VALIDATED}) and each application's persisted
     * {@code dir3Mismatch} flag is refreshed. The audit goes through the applications one at a
     * time, so it returns immediately and the progress is read with {@code GET
     * application/dir3/check-all/status}. If an audit is already running, no second one is started
     * and the running one's status is returned.
     *
     * @return a {@link ResponseEntity} wrapping the status of the audit, HTTP 202
     * @throws BusinessRuleException if the caller's login session can't be renewed in the background
     */
    @PutMapping("check-all")
    public ResponseEntity<Dir3MismatchAuditStatusOutputDTO> startDir3MismatchAudit() {
        log.info("REST: Request to launch the DIR3 mismatch audit of every application");
        return ResponseEntity.accepted().body(dir3BackgroundAuditService.start(Utils.resolveCurrentUsername()));
    }

    /**
     * Reports the progress of the running DIR3 audit of every application, or the outcome of the
     * last one: how many applications there are, how many have been audited, which ones are
     * mismatching, and which ones failed and were left untouched.
     *
     * @return a {@link ResponseEntity} wrapping the status of the audit, HTTP 200
     */
    @GetMapping("check-all/status")
    public ResponseEntity<Dir3MismatchAuditStatusOutputDTO> getDir3MismatchAuditStatus() {
        return ResponseEntity.ok(dir3BackgroundAuditService.getStatus());
    }

    /**
     * Audits a single application: checks each of its active responsible/authorized assignments'
     * DIR3 against the application's administrative unit, with the same status transitions as
     * {@code check-all} ({@code MANUAL} and {@code NOT_APPLY} assignments are never downgraded),
     * and refreshes the application's persisted {@code dir3Mismatch} flag.
     *
     * @param id primary key of the application to audit
     * @return a {@link ResponseEntity} wrapping the application's ID, name and resulting
     * {@code dir3Mismatch} state, HTTP 200
     * @throws BusinessRuleException if no application exists with the given ID
     */
    @PutMapping("check/{id}")
    public ResponseEntity<ApplicationDir3MismatchOutputDTO> checkDir3Mismatch(@PathVariable Long id) {
        log.info("REST: Request to audit DIR3 mismatch for application ID: {}", id);
        return ResponseEntity.ok(dir3CheckService.checkDir3Mismatch(id));
    }
}
