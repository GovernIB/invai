package es.caib.invai.back.interna.maintenance.development.providerRole;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.development.providerRole.DTO.ProviderRoleInputDTO;
import es.caib.invai.back.interna.maintenance.development.providerRole.DTO.ProviderRoleOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.development.providerRole.ProviderRoleCriteria;
import es.caib.invai.back.service.facade.maintenance.development.providerRole.ProviderRoleService;
import es.caib.invai.back.utils.Constants;
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
 * <p>
 * Every endpoint that changes data requires {@code INV_SUPER}; reading is open to every role.
 * </p>
 *
 * @since 1.0.2
 */
@Tag(name = "Rols", description = "Manteniment del catàleg de rols.")
@RestController
@Slf4j
@RequestMapping("role")
@PreAuthorize(Constants.PRE_AUTH_ANY_ROLE)
public class ProviderRoleController {

    /** Facade service handling the business logic for provider Role operations. */
    private final ProviderRoleService providerRoleService;

    /**
     * Creates a new controller instance with the given service dependency.
     *
     * @param providerRoleService the facade service used to perform Role operations
     */
    @Autowired
    public ProviderRoleController(ProviderRoleService providerRoleService) {
        this.providerRoleService = providerRoleService;
    }

    /**
     * Retrieves a role by its unique identifier.
     *
     * @param id the identifier of the role to fetch
     * @return the matching {@link ProviderRoleOutputDTO}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProviderRoleOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(providerRoleService.getById(id));
    }

    /**
     * Retrieves a paginated list of roles matching the given filter criteria.
     *
     * @param filter   the search criteria used to narrow the results
     * @param pageable the pagination and sorting parameters
     * @return a page of {@link ProviderRoleOutputDTO} results
     */
    @GetMapping
    public ResponseEntity<Page<ProviderRoleOutputDTO>> getAll(
            @ModelAttribute ProviderRoleCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(providerRoleService.getAll(filter, pageable));
    }

    /**
     * Creates a new role record.
     *
     * @param inputDTO the data used to create the role
     * @return the created role, with HTTP 201 status
     */
    @PreAuthorize(Constants.PRE_AUTH_SUPER)
    @PostMapping
    public ResponseEntity<ProviderRoleOutputDTO> create(@Valid @RequestBody ProviderRoleInputDTO inputDTO) {
        return new ResponseEntity<>(providerRoleService.create(inputDTO), HttpStatus.CREATED);
    }

    /**
     * Updates an existing role record.
     *
     * @param id       the identifier of the role to update
     * @param inputDTO the data used to update the role
     * @return the updated role
     */
    @PreAuthorize(Constants.PRE_AUTH_SUPER)
    @PutMapping("/{id}")
    public ResponseEntity<ProviderRoleOutputDTO> update(@PathVariable Long id, @Valid @RequestBody ProviderRoleInputDTO inputDTO) {
        return ResponseEntity.ok(providerRoleService.update(id, inputDTO));
    }

    /**
     * Logically deletes a role record.
     *
     * @param id the identifier of the role to delete
     * @return an empty response with HTTP 204 status
     */
    @PreAuthorize(Constants.PRE_AUTH_SUPER)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        providerRoleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a logically deleted role record.
     *
     * @param id the identifier of the role to reactivate
     * @return the reactivated role
     */
    @PreAuthorize(Constants.PRE_AUTH_SUPER)
    @PutMapping("reactivate/{id}")
    public ResponseEntity<ProviderRoleOutputDTO> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(providerRoleService.reactivate(id));
    }
}
