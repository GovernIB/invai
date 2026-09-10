package es.caib.invai.back.persistence.repository.maintenance.security.identityProvider;

import es.caib.invai.back.service.model.maintenance.security.identityProvider.IdentityProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Core business domain outbound Port boundary interface declaring relational persistence mechanisms
 * for the IdentityProvider catalog layer.
 *
 * @since 1.0.4
 */
public interface IdentityProviderRepository {

    /**
     * Finds an identity provider by its identifier.
     *
     * @param id the identity provider identifier
     * @return the matching domain model, or {@code null} if not found
     */
    IdentityProvider findById(Long id);

    /**
     * Finds identity providers matching the given filter criteria, paginated.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching identity providers
     */
    Page<IdentityProvider> findAll(IdentityProviderCriteria filter, Pageable pageable);

    /**
     * Persists a new identity provider.
     *
     * @param identityProvider the domain model to persist
     * @return the persisted domain model
     */
    IdentityProvider create(IdentityProvider identityProvider);

    /**
     * Persists changes to an existing identity provider.
     *
     * @param identityProvider the domain model containing the updated data
     * @param id the identifier of the identity provider to update
     * @return the updated domain model
     */
    IdentityProvider update(IdentityProvider identityProvider, Long id);

    /**
     * Deletes (logically) the given identity provider.
     *
     * @param identityProvider the domain model to delete
     */
    void delete(IdentityProvider identityProvider);

    /**
     * Checks whether an active (not logically deleted) identity provider with the given name exists.
     *
     * @param name the name to check
     * @return {@code true} if a matching active record exists
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Checks whether an active (not logically deleted) identity provider with the given name exists,
     * excluding the record with the given ID.
     *
     * @param name the name to check
     * @param id the identifier to exclude from the check
     * @return {@code true} if a matching active record exists other than the one with the given ID
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}
