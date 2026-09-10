package es.caib.invai.back.service.facade.maintenance.security.identityProvider;

import es.caib.invai.back.interna.maintenance.security.identityProvider.DTO.IdentityProviderInputDTO;
import es.caib.invai.back.interna.maintenance.security.identityProvider.DTO.IdentityProviderOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.security.identityProvider.IdentityProviderCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting IdentityProviders.
 *
 * @since 1.0.4
 */
public interface IdentityProviderService {

    /**
     * Retrieves an identity provider by its identifier.
     *
     * @param id the identity provider identifier
     * @return the matching identity provider as an output DTO
     */
    IdentityProviderOutputDTO getById(Long id);

    /**
     * Retrieves a paginated list of identity providers matching the given filter criteria.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching identity providers as output DTOs
     */
    Page<IdentityProviderOutputDTO> getAll(IdentityProviderCriteria filter, Pageable pageable);

    /**
     * Creates a new identity provider.
     *
     * @param inputDTO the data for the new identity provider
     * @return the created identity provider as an output DTO
     */
    IdentityProviderOutputDTO create(IdentityProviderInputDTO inputDTO);

    /**
     * Updates an existing identity provider.
     *
     * @param id the identifier of the identity provider to update
     * @param inputDTO the new data to apply
     * @return the updated identity provider as an output DTO
     */
    IdentityProviderOutputDTO update(Long id, IdentityProviderInputDTO inputDTO);

    /**
     * Logically deletes an identity provider by its identifier.
     *
     * @param id the identifier of the identity provider to delete
     */
    void delete(Long id);

    /**
     * Reactivates a previously deleted identity provider.
     *
     * @param id the identifier of the identity provider to reactivate
     * @return the reactivated identity provider as an output DTO
     */
    IdentityProviderOutputDTO reactivate(Long id);
}
