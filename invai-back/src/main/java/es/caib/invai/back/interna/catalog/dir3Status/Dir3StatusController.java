package es.caib.invai.back.interna.catalog.dir3Status;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.catalog.dir3Status.DTO.Dir3StatusOutputDTO;
import es.caib.invai.back.service.facade.catalog.dir3Status.Dir3StatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Internal REST controller exposing read-only access to the DIR3 validation status lookup
 * dictionary.
 *
 * @since 1.0.5
 */
@Tag(name = "Catàleg", description = "Consulta del catàleg d'estats de validació DIR3.")
@RestController
@Slf4j
@RequestMapping("dir3-status")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class Dir3StatusController {

    /** Service facade providing access to the DIR3 status lookup dictionary. */
    private final Dir3StatusService dir3StatusService;

    /**
     * Creates the controller with its required service dependency.
     *
     * @param dir3StatusService the service facade used to resolve DIR3 status lookup entries
     */
    @Autowired
    public Dir3StatusController(Dir3StatusService dir3StatusService) {
        this.dir3StatusService = dir3StatusService;
    }

    /**
     * Resolves every registered DIR3 status lookup entry, sorted by name.
     *
     * @return a {@link ResponseEntity} wrapping the full list of mapped {@link Dir3StatusOutputDTO} entries
     */
    @GetMapping
    public ResponseEntity<List<Dir3StatusOutputDTO>> getAll() {
        log.debug("REST: Fetching every DIR3 status lookup entry");
        return ResponseEntity.ok(dir3StatusService.getAll());
    }
}
