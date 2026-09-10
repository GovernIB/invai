package es.caib.invai.back.interna.maintenance.security.identityProvider;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.security.identityProvider.DTO.IdentityProviderInputDTO;
import es.caib.invai.back.interna.maintenance.security.identityProvider.DTO.IdentityProviderOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.security.identityProvider.IdentityProviderCriteria;
import es.caib.invai.back.service.facade.maintenance.security.identityProvider.IdentityProviderService;
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
 * Internal REST controller that manages lifecycle endpoints and routing rules for IdentityProvider assets.
 *
 * @since 1.0.4
 */
@Tag(name = "Proveïdors d'identitat", description = "Manteniment del catàleg de proveïdors d'identitat.")
@RestController
@Slf4j
@RequestMapping("identity-provider")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class IdentityProviderController {

    /** Service facade handling IdentityProvider business use cases. */
    private final IdentityProviderService identityProviderService;

    /**
     * Creates the controller with its required service dependency.
     *
     * @param identityProviderService the service facade to delegate business operations to
     */
    @Autowired
    public IdentityProviderController(IdentityProviderService identityProviderService) {
        this.identityProviderService = identityProviderService;
    }

    /**
     * Retrieves an identity provider by its identifier.
     *
     * @param id the identity provider identifier
     * @return the matching identity provider
     */
    @GetMapping("/{id}")
    public ResponseEntity<IdentityProviderOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(identityProviderService.getById(id));
    }

    /**
     * Retrieves a paginated, filtered list of identity providers.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching identity providers
     */
    @GetMapping
    public ResponseEntity<Page<IdentityProviderOutputDTO>> getAll(
            @ModelAttribute IdentityProviderCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(identityProviderService.getAll(filter, pageable));
    }

    /**
     * Creates a new identity provider.
     *
     * @param inputDTO the data for the new identity provider
     * @return the created identity provider, with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<IdentityProviderOutputDTO> create(@Valid @RequestBody IdentityProviderInputDTO inputDTO) {
        return new ResponseEntity<>(identityProviderService.create(inputDTO), HttpStatus.CREATED);
    }

    /**
     * Updates an existing identity provider.
     *
     * @param id the identifier of the identity provider to update
     * @param inputDTO the new data to apply
     * @return the updated identity provider
     */
    @PutMapping("/{id}")
    public ResponseEntity<IdentityProviderOutputDTO> update(@PathVariable Long id, @Valid @RequestBody IdentityProviderInputDTO inputDTO) {
        return ResponseEntity.ok(identityProviderService.update(id, inputDTO));
    }

    /**
     * Logically deletes an identity provider by its identifier.
     *
     * @param id the identifier of the identity provider to delete
     * @return an empty response with HTTP 204 status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        identityProviderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a previously deleted identity provider.
     *
     * @param id the identifier of the identity provider to reactivate
     * @return the reactivated identity provider
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<IdentityProviderOutputDTO> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(identityProviderService.reactivate(id));
    }
}
