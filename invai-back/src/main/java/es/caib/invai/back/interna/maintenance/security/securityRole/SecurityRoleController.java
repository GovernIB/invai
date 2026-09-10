package es.caib.invai.back.interna.maintenance.security.securityRole;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.security.securityRole.DTO.SecurityRoleOutputDTO;
import es.caib.invai.back.service.facade.maintenance.security.securityRole.SecurityRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Internal REST controller for the Security Role maintenance module.
 * <p>
 * Currently limited to searching/listing candidates against Soffid: local CRUD is not exposed
 * yet, pending a future iteration that adds the resolve-or-create flow feeding
 * {@code INV_APP_ROLE} assignments.
 * </p>
 * <p>
 * Secured at the class level via Spring Method Security permissions, restricting execution
 * exclusively to users holding the role {@code ROLE_INV_SUPER}.
 * </p>
 *
 * @since 1.0.4
 */
@Tag(name = "Rols de seguretat", description = "Manteniment del catàleg de rols de seguretat.")
@RestController
@RequestMapping("security-role")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class SecurityRoleController {

    /** Service facade handling business logic for security role operations. */
    @Autowired
    private SecurityRoleService securityRoleService;

    /**
     * Lists roles that can be assigned to an application (feeding {@code INV_APP_ROLE}) against
     * the Soffid SCIM API - a {@code getAll} when {@code name} is omitted, or restricted to matches when
     * given - feeding the security role typeahead. Does not touch the local {@code SecurityRole}
     * catalog.
     *
     * @param name the text to search for, matched (word by word) against the role's name, or
     * omitted to list every Soffid role in this project's namespace (name starting with
     * {@code "INV_"})
     * @param pageable the pagination parameters
     * @return the requested page of matching Soffid role candidates, mapped into
     * {@link SecurityRoleOutputDTO} with a {@code null} id
     */
    @GetMapping("soffid-search")
    public ResponseEntity<Page<SecurityRoleOutputDTO>> searchSoffid(
            @RequestParam(required = false) String name,
            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(securityRoleService.searchSoffid(name, pageable));
    }
}
