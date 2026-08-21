package es.caib.invai.back.persistence.repository.maintenance.responsible.authorizationType;

import es.caib.invai.back.service.model.maintenance.responsible.authorizationType.AuthorizationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Core business domain outbound Port boundary interface declaring relational persistence mechanisms
 * for the AuthorizationType catalog layer.
 *
 * @since 1.0.3
 */
public interface AuthorizationTypeRepository {

    /**
     * Finds an authorization type by its identifier.
     *
     * @param id the authorization type identifier
     * @return the matching domain model, or {@code null} if not found
     */
    AuthorizationType findById(Long id);

    /**
     * Finds authorization types matching the given filter criteria, paginated.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching authorization types
     */
    Page<AuthorizationType> findAll(AuthorizationTypeCriteria filter, Pageable pageable);

    /**
     * Persists a new authorization type.
     *
     * @param authorizationType the domain model to persist
     * @return the persisted domain model
     */
    AuthorizationType create(AuthorizationType authorizationType);

    /**
     * Persists changes to an existing authorization type.
     *
     * @param authorizationType the domain model containing the updated data
     * @param id the identifier of the authorization type to update
     * @return the updated domain model
     */
    AuthorizationType update(AuthorizationType authorizationType, Long id);

    /**
     * Deletes (logically) the given authorization type.
     *
     * @param authorizationType the domain model to delete
     */
    void delete(AuthorizationType authorizationType);

    /**
     * Checks whether an active (not logically deleted) authorization type with the given name exists.
     *
     * @param name the name to check
     * @return {@code true} if a matching active record exists
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Checks whether an active (not logically deleted) authorization type with the given name exists,
     * excluding the record with the given ID.
     *
     * @param name the name to check
     * @param id the identifier to exclude from the check
     * @return {@code true} if a matching active record exists other than the one with the given ID
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}
