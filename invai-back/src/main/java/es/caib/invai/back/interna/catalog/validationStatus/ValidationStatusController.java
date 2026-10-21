package es.caib.invai.back.interna.catalog.validationStatus;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.catalog.validationStatus.DTO.ValidationStatusOutputDTO;
import es.caib.invai.back.service.facade.catalog.validationStatus.ValidationStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Internal REST controller exposing read-only access to the validation status lookup
 * dictionary.
 *
 * @since 1.0.5
 */
@Tag(name = "Catàleg", description = "Consulta del catàleg d'estats de validació.")
@RestController
@Slf4j
@RequestMapping("validation-status")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class ValidationStatusController {

    /** Service facade providing access to the validation status lookup dictionary. */
    private final ValidationStatusService validationStatusService;

    /**
     * Creates the controller with its required service dependency.
     *
     * @param validationStatusService the service facade used to resolve validation status lookup entries
     */
    @Autowired
    public ValidationStatusController(ValidationStatusService validationStatusService) {
        this.validationStatusService = validationStatusService;
    }

    /**
     * Resolves every registered validation status lookup entry, sorted by name.
     *
     * @return a {@link ResponseEntity} wrapping the full list of mapped {@link ValidationStatusOutputDTO} entries
     */
    @GetMapping
    public ResponseEntity<List<ValidationStatusOutputDTO>> getAll() {
        log.debug("REST: Fetching every validation status lookup entry");
        return ResponseEntity.ok(validationStatusService.getAll());
    }
}
