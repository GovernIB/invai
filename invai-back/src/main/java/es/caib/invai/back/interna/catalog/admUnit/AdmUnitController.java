package es.caib.invai.back.interna.catalog.admUnit;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.catalog.admUnit.DTO.AdmUnitOutputDTO;
import es.caib.invai.back.service.facade.catalog.admUnit.AdmUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Internal REST controller exposing read-only access to Administrative Units. Administrative
 * units are pure external reference data mirrored live from DIR3CAIB — there is no local
 * create/update/delete lifecycle; every endpoint here reads through to DIR3CAIB (directly or via
 * a short-lived in-memory cache).
 * <p>
 * Secured at the class level via Spring Method Security permissions, restricting execution
 * exclusively to users holding the role {@code ROLE_INV_SUPER}.
 * </p>
 *
 * @since 1.0.4
 */
@Tag(name = "Unitats administratives", description = "Consulta d'unitats administratives (DIR3CAIB).")
@RestController
@RequestMapping("adm-unit")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class AdmUnitController {

    /** Service facade handling business logic for administrative unit operations. */
    @Autowired
    private AdmUnitService admUnitService;

    /**
     * Lists every administrative unit valid for direct selection on an {@code Application} — every
     * unit at department (Conselleria) hierarchy level or below — optionally narrowed down by a
     * free-text {@code search} matched against each unit's name or DIR3CAIB code. Backs the single
     * administrative unit picker on the Application form: a department and any of its descendants
     * are equally valid choices, with no separate department-first step.
     *
     * @param search   free-text filter matched against each unit's name or code, optional
     * @param pageable pagination parameters (page, size, sorting fields) supplied by the client request
     * @return a {@link ResponseEntity} wrapping a {@link Page} of {@link AdmUnitOutputDTO} elements with an HTTP 200 status
     */
    @GetMapping
    public ResponseEntity<Page<AdmUnitOutputDTO>> getAll(@RequestParam(required = false) String search, Pageable pageable) {
        return ResponseEntity.ok(admUnitService.getAll(search, pageable));
    }
}
