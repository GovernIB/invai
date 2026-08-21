package es.caib.invai.back.interna.maintenance.development.role;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.development.role.DTO.RoleInputDTO;
import es.caib.invai.back.interna.maintenance.development.role.DTO.RoleOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.development.role.RoleCriteria;
import es.caib.invai.back.service.facade.maintenance.development.role.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Internal REST controller that manages lifecycle endpoints and routing rules for provider Role assets.
 *
 * @since 1.0.2
 */
@Tag(name = "Rols", description = "Manteniment del catàleg de rols.")
@RestController
@Slf4j
@RequestMapping("role")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class RoleController {

    /** Facade service handling the business logic for provider Role operations. */
    private final RoleService roleService;

    /**
     * Creates a new controller instance with the given service dependency.
     *
     * @param roleService the facade service used to perform Role operations
     */
    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Retrieves a role by its unique identifier.
     *
     * @param id the identifier of the role to fetch
     * @return the matching {@link RoleOutputDTO}
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getById(id));
    }

    /**
     * Retrieves a paginated list of roles matching the given filter criteria.
     *
     * @param filter   the search criteria used to narrow the results
     * @param pageable the pagination and sorting parameters
     * @return a page of {@link RoleOutputDTO} results
     */
    @GetMapping
    public ResponseEntity<Page<RoleOutputDTO>> getAll(
            @ModelAttribute RoleCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(roleService.getAll(filter, pageable));
    }

    /**
     * Creates a new role record.
     *
     * @param inputDTO the data used to create the role
     * @return the created role, with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<RoleOutputDTO> create(@Valid @RequestBody RoleInputDTO inputDTO) {
        return new ResponseEntity<>(roleService.create(inputDTO), HttpStatus.CREATED);
    }

    /**
     * Updates an existing role record.
     *
     * @param id       the identifier of the role to update
     * @param inputDTO the data used to update the role
     * @return the updated role
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoleOutputDTO> update(@PathVariable Long id, @Valid @RequestBody RoleInputDTO inputDTO) {
        return ResponseEntity.ok(roleService.update(id, inputDTO));
    }

    /**
     * Logically deletes a role record.
     *
     * @param id the identifier of the role to delete
     * @return an empty response with HTTP 204 status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a logically deleted role record.
     *
     * @param id the identifier of the role to reactivate
     * @return the reactivated role
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<RoleOutputDTO> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.reactivate(id));
    }
}
