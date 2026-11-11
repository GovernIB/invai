package es.caib.invai.back.interna.maintenance.documentation.rolePermission;

import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentRoleCategoryOutputDTO;
import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentRolePermissionOutputDTO;
import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentRolePermissionUpdateInputDTO;
import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentUserPermissionOutputDTO;
import es.caib.invai.back.service.facade.maintenance.documentation.documentRolePermission.DocumentRolePermissionService;
import es.caib.invai.back.utils.Constants;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Internal REST controller that manages the documentation permissions of the roles of the INVAI
 * application: what each role can do with the documents of each category of the documentation module.
 *
 * @since 1.0.7
 */
@Tag(name = "Permisos documentals", description = "Manteniment dels permisos dels rols sobre els documents.")
@RestController
@Slf4j
@RequestMapping("documentation/role-permission")
@PreAuthorize(Constants.PRE_AUTH_ANY_ROLE)
public class DocumentRolePermissionController {

    /** Facade service handling the business use cases for the documentation permissions of the roles. */
    private final DocumentRolePermissionService documentRolePermissionService;

    /**
     * Creates a controller instance with the given service dependency injected.
     *
     * @param documentRolePermissionService the facade service used to perform the operations
     */
    @Autowired
    public DocumentRolePermissionController(DocumentRolePermissionService documentRolePermissionService) {
        this.documentRolePermissionService = documentRolePermissionService;
    }

    /**
     * Retrieves the documentation permissions every role has in a category.
     *
     * @param categoryId the identifier of the category
     * @return HTTP 200 with the permissions of every role in the category, sorted by role name
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<List<DocumentRolePermissionOutputDTO>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(documentRolePermissionService.getByCategory(categoryId));
    }

    /**
     * Retrieves the categories assigned to a role, each in full, and the documentation permissions
     * the role has in each of them. A category is assigned to the role when the role has some
     * permission in it.
     *
     * @param role name of the role, as Soffid reports it (for example {@code INV_SISTEMES})
     * @return HTTP 200 with the categories of the role and its permissions in them
     */
    @GetMapping("/by-role")
    public ResponseEntity<List<DocumentRoleCategoryOutputDTO>> getByRole(@RequestParam String role) {
        return ResponseEntity.ok(documentRolePermissionService.getByRole(role));
    }

    /**
     * Retrieves what the logged user can do with the documents of each of the categories assigned
     * to them, adding up the permissions of all their roles, so the front can show only the actions
     * the user is allowed. Every role of the application can ask for it.
     *
     * @return HTTP 200 with the permissions of the logged user in their categories
     */
    @GetMapping("/me")
    public ResponseEntity<List<DocumentUserPermissionOutputDTO>> getCurrentUserPermissions() {
        return ResponseEntity.ok(documentRolePermissionService.getCurrentUserPermissions());
    }

    /**
     * Sets the documentation permissions of the listed roles in a category, all or nothing.
     *
     * @param categoryId the identifier of the category
     * @param inputDTO   the permissions to set, one entry per role
     * @return HTTP 200 with the permissions of every role in the category, once updated
     */
    @PutMapping("/{categoryId}")
    @PreAuthorize(Constants.PRE_AUTH_SUPER)
    public ResponseEntity<List<DocumentRolePermissionOutputDTO>> update(
            @PathVariable Long categoryId,
            @Valid @RequestBody DocumentRolePermissionUpdateInputDTO inputDTO) {
        return ResponseEntity.ok(documentRolePermissionService.update(categoryId, inputDTO));
    }
}
