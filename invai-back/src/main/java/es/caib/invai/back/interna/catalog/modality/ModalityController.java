package es.caib.invai.back.interna.catalog.modality;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.catalog.modality.DTO.ModalityOutputDTO;
import es.caib.invai.back.service.facade.catalog.modality.ModalityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Internal REST controller exposing read-only access to the Modality lookup dictionary.
 *
 * @since 1.0.3
 */
@Tag(name = "Catàleg", description = "Consulta del catàleg de modalitats de desenvolupament.")
@RestController
@Slf4j
@RequestMapping("modality")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class ModalityController {

    /** Service facade providing access to the Modality lookup dictionary. */
    private final ModalityService modalityService;

    /**
     * Creates the controller with its required service dependency.
     *
     * @param modalityService the service facade used to resolve modality lookup entries
     */
    @Autowired
    public ModalityController(ModalityService modalityService) {
        this.modalityService = modalityService;
    }

    /**
     * Resolves every registered modality lookup entry, sorted by name.
     *
     * @return a {@link ResponseEntity} wrapping the full list of mapped {@link ModalityOutputDTO} entries
     */
    @GetMapping
    public ResponseEntity<List<ModalityOutputDTO>> getAll() {
        log.info("REST: Fetching every modality lookup entry");
        return ResponseEntity.ok(modalityService.getAll());
    }
}
