package es.caib.invai.back.interna.integrations.dir3.admUnit;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.integrations.dir3.admUnit.DTO.AdmUnitOutputDTO;
import es.caib.invai.back.service.facade.integrations.dir3.admUnit.AdmUnitService;
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
     * Lists every department (Conselleria) in the DIR3CAIB tree — the first step of the
     * Application form picker, letting the client choose a department before narrowing down to
     * one of its administrative units via {@link #getAdmUnitsByDepartment}.
     *
     * @param pageable pagination parameters (page, size, sorting fields) supplied by the client request
     * @return a {@link ResponseEntity} wrapping a {@link Page} of department-level {@link AdmUnitOutputDTO} elements with an HTTP 200 status
     */
    @GetMapping("departments")
    public ResponseEntity<Page<AdmUnitOutputDTO>> getDepartments(Pageable pageable) {
        return ResponseEntity.ok(admUnitService.getDepartments(pageable));
    }

    /**
     * Lists every descendant of a given department at any depth (the department itself is
     * deliberately excluded, since an application can never link to a department directly) — the
     * second step of the Application form picker, once a department has been chosen via
     * {@link #getDepartments}.
     *
     * @param code     the DIR3CAIB code of the chosen department
     * @param pageable pagination parameters (page, size, sorting fields) supplied by the client request
     * @return a {@link ResponseEntity} wrapping a {@link Page} of {@link AdmUnitOutputDTO} elements with an HTTP 200 status,
     * empty when {@code code} does not match any unit in the DIR3CAIB tree
     */
    @GetMapping("departments/{code}/adm-units")
    public ResponseEntity<Page<AdmUnitOutputDTO>> getAdmUnitsByDepartment(@PathVariable String code, Pageable pageable) {
        return ResponseEntity.ok(admUnitService.getAdmUnitsByDepartment(code, pageable));
    }
}
