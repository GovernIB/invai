package es.caib.invai.back.interna.catalog.ensSubject;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.catalog.ensSubject.DTO.EnsSubjectOutputDTO;
import es.caib.invai.back.service.facade.catalog.ensSubject.EnsSubjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Internal REST controller exposing read-only access to the ENS subjection lookup dictionary.
 *
 * @since 1.0.4
 */
@Tag(name = "Catàleg", description = "Consulta del catàleg de subjecció a l'ENS.")
@RestController
@Slf4j
@RequestMapping("ens-subject")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class EnsSubjectController {

    /** Service facade providing access to the ENS subjection lookup dictionary. */
    private final EnsSubjectService ensSubjectService;

    /**
     * Creates the controller with its required service dependency.
     *
     * @param ensSubjectService the service facade used to resolve ENS subjection lookup entries
     */
    @Autowired
    public EnsSubjectController(EnsSubjectService ensSubjectService) {
        this.ensSubjectService = ensSubjectService;
    }

    /**
     * Resolves every registered ENS subjection lookup entry, sorted by name.
     *
     * @return a {@link ResponseEntity} wrapping the full list of mapped {@link EnsSubjectOutputDTO} entries
     */
    @GetMapping
    public ResponseEntity<List<EnsSubjectOutputDTO>> getAll() {
        log.debug("REST: Fetching every ENS subjection lookup entry");
        return ResponseEntity.ok(ensSubjectService.getAll());
    }
}
