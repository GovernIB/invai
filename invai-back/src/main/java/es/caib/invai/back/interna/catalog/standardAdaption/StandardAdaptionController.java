package es.caib.invai.back.interna.catalog.standardAdaption;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.catalog.standardAdaption.DTO.StandardAdaptionOutputDTO;
import es.caib.invai.back.service.facade.catalog.standardAdaption.StandardAdaptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Internal REST controller exposing read-only access to the Standard Adaption lookup dictionary.
 *
 * @since 1.0.3
 */
@Tag(name = "Catàleg", description = "Consulta del catàleg de nivells d'adaptació als estàndards GOIB.")
@RestController
@Slf4j
@RequestMapping("standard-adaption")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class StandardAdaptionController {

    /** Service facade providing access to the Standard Adaption lookup dictionary. */
    private final StandardAdaptionService standardAdaptionService;

    /**
     * Creates the controller with its required service dependency.
     *
     * @param standardAdaptionService the service facade used to resolve standard adaption lookup entries
     */
    @Autowired
    public StandardAdaptionController(StandardAdaptionService standardAdaptionService) {
        this.standardAdaptionService = standardAdaptionService;
    }

    /**
     * Resolves every registered standard adaption lookup entry, sorted by name.
     *
     * @return a {@link ResponseEntity} wrapping the full list of mapped {@link StandardAdaptionOutputDTO} entries
     */
    @GetMapping
    public ResponseEntity<List<StandardAdaptionOutputDTO>> getAll() {
        log.info("REST: Fetching every standard adaption lookup entry");
        return ResponseEntity.ok(standardAdaptionService.getAll());
    }
}
