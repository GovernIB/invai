package es.caib.invai.back.service.facade.maintenance.responsible.authorizationType;

import es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO.AuthorizationTypeInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO.AuthorizationTypeOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.responsible.authorizationType.AuthorizationTypeCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting AuthorizationTypes.
 *
 * @since 1.0.3
 */
public interface AuthorizationTypeService {

    /**
     * Retrieves an authorization type by its identifier.
     *
     * @param id the authorization type identifier
     * @return the matching authorization type as an output DTO
     */
    AuthorizationTypeOutputDTO getById(Long id);

    /**
     * Retrieves a paginated list of authorization types matching the given filter criteria.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching authorization types as output DTOs
     */
    Page<AuthorizationTypeOutputDTO> getAll(AuthorizationTypeCriteria filter, Pageable pageable);

    /**
     * Creates a new authorization type.
     *
     * @param inputDTO the data for the new authorization type
     * @return the created authorization type as an output DTO
     */
    AuthorizationTypeOutputDTO create(AuthorizationTypeInputDTO inputDTO);

    /**
     * Updates an existing authorization type.
     *
     * @param id the identifier of the authorization type to update
     * @param inputDTO the new data to apply
     * @return the updated authorization type as an output DTO
     */
    AuthorizationTypeOutputDTO update(Long id, AuthorizationTypeInputDTO inputDTO);

    /**
     * Logically deletes an authorization type by its identifier.
     *
     * @param id the identifier of the authorization type to delete
     */
    void delete(Long id);

    /**
     * Reactivates a previously deleted authorization type.
     *
     * @param id the identifier of the authorization type to reactivate
     * @return the reactivated authorization type as an output DTO
     */
    AuthorizationTypeOutputDTO reactivate(Long id);
}
