package es.caib.invai.back.interna.maintenance.responsible.authorizationType;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO.AuthorizationTypeInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO.AuthorizationTypeOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.responsible.authorizationType.AuthorizationTypeCriteria;
import es.caib.invai.back.service.facade.maintenance.responsible.authorizationType.AuthorizationTypeService;
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
 * Internal REST controller that manages lifecycle endpoints and routing rules for AuthorizationType assets.
 *
 * @since 1.0.3
 */
@Tag(name = "Autoritzacions", description = "Manteniment del catàleg de tipus d'autorització.")
@RestController
@Slf4j
@RequestMapping("authorization-type")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class AuthorizationTypeController {

    /** Service facade handling AuthorizationType business use cases. */
    private final AuthorizationTypeService authorizationTypeService;

    /**
     * Creates the controller with its required service dependency.
     *
     * @param authorizationTypeService the service facade to delegate business operations to
     */
    @Autowired
    public AuthorizationTypeController(AuthorizationTypeService authorizationTypeService) {
        this.authorizationTypeService = authorizationTypeService;
    }

    /**
     * Retrieves an authorization type by its identifier.
     *
     * @param id the authorization type identifier
     * @return the matching authorization type
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuthorizationTypeOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(authorizationTypeService.getById(id));
    }

    /**
     * Retrieves a paginated, filtered list of authorization types.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching authorization types
     */
    @GetMapping
    public ResponseEntity<Page<AuthorizationTypeOutputDTO>> getAll(
            @ModelAttribute AuthorizationTypeCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(authorizationTypeService.getAll(filter, pageable));
    }

    /**
     * Creates a new authorization type.
     *
     * @param inputDTO the data for the new authorization type
     * @return the created authorization type, with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<AuthorizationTypeOutputDTO> create(@Valid @RequestBody AuthorizationTypeInputDTO inputDTO) {
        return new ResponseEntity<>(authorizationTypeService.create(inputDTO), HttpStatus.CREATED);
    }

    /**
     * Updates an existing authorization type.
     *
     * @param id the identifier of the authorization type to update
     * @param inputDTO the new data to apply
     * @return the updated authorization type
     */
    @PutMapping("/{id}")
    public ResponseEntity<AuthorizationTypeOutputDTO> update(@PathVariable Long id, @Valid @RequestBody AuthorizationTypeInputDTO inputDTO) {
        return ResponseEntity.ok(authorizationTypeService.update(id, inputDTO));
    }

    /**
     * Logically deletes an authorization type by its identifier.
     *
     * @param id the identifier of the authorization type to delete
     * @return an empty response with HTTP 204 status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        authorizationTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a previously deleted authorization type.
     *
     * @param id the identifier of the authorization type to reactivate
     * @return the reactivated authorization type
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<AuthorizationTypeOutputDTO> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(authorizationTypeService.reactivate(id));
    }
}
