package es.caib.invai.back.interna.application.responsibleAuthorized.dir3;

import es.caib.invai.back.interna.application.responsibleAuthorized.dir3.DTO.Dir3ManualValidateInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.dir3.DTO.Dir3ValidationOutputDTO;
import es.caib.invai.back.service.facade.application.responsibleAuthorized.dir3.Dir3ValidationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal REST controller managing {@code Dir3Validation} records directly, independently of
 * whether they are currently linked to an {@code AppResponsible} or an {@code AppAuthorized}
 * assignment (or both, when shared across a person's active assignments on the same anchor).
 * Replaces what used to be a separate {@code dir3-manual-validate} endpoint under each of
 * {@code application/responsible} and {@code application/authorized}.
 *
 * @since 1.0.5
 */
@Slf4j
@Validated
@Tag(name = "Validació DIR3", description = "Servei de validació manual de l'estat DIR3 dels responsables i autoritzats.")
@RestController
@RequiredArgsConstructor
@RequestMapping("application/dir3-validation")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class Dir3ValidationController {

    /** Facade service handling the business logic for Dir3Validation operations. */
    private final Dir3ValidationService dir3ValidationService;

    /**
     * Promotes a DIR3 validation from "No validado" to "Manual", recording the moment, the
     * acting user, and the given justification. One-way: once "Manual", this endpoint refuses to
     * touch it again.
     *
     * @param id identifier of the DIR3 validation record to manually validate
     * @param inputDTO payload carrying the mandatory free-text reason for the manual validation
     * @return the updated DIR3 validation record
     */
    @PutMapping("manual-validate/{id}")
    public ResponseEntity<Dir3ValidationOutputDTO> validateManually(
            @PathVariable Long id,
            @Valid @RequestBody Dir3ManualValidateInputDTO inputDTO) {
        log.info("REST: Request to manually validate DIR3 validation ID: {}", id);
        return ResponseEntity.ok(dir3ValidationService.validateManually(id, inputDTO));
    }
}
