package es.caib.invai.back.interna.catalog.status;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.catalog.status.DTO.StatusOutputDTO;
import es.caib.invai.back.service.facade.catalog.status.StatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Internal REST controller exposing read-only access to the Status lookup dictionary.
 *
 * @since 1.0.3
 */
@Tag(name = "Catàleg", description = "Consulta del catàleg d'estats.")
@RestController
@Slf4j
@RequestMapping("status")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class StatusController {

    /** Service facade providing access to the Status lookup dictionary. */
    private final StatusService statusService;

    /**
     * Creates the controller with its required service dependency.
     *
     * @param statusService the service facade used to resolve status lookup entries
     */
    @Autowired
    public StatusController(StatusService statusService) {
        this.statusService = statusService;
    }

    /**
     * Resolves every registered status lookup entry, sorted by name.
     *
     * @return a {@link ResponseEntity} wrapping the full list of mapped {@link StatusOutputDTO} entries
     */
    @GetMapping
    public ResponseEntity<List<StatusOutputDTO>> getAll() {
        log.debug("REST: Fetching every status lookup entry");
        return ResponseEntity.ok(statusService.getAll());
    }
}
