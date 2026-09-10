package es.caib.invai.back.service.facade.application.development.provider;

import es.caib.invai.back.interna.application.development.provider.DTO.AppProviderInputDTO;
import es.caib.invai.back.interna.application.development.provider.DTO.AppProviderOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.provider.AppProviderCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Facade contract for the "AppProvider" list: provider (company) assignments hanging many-to-one
 * off a single development record. See
 * {@link es.caib.invai.back.ejb.application.development.provider.AppProviderServiceFacadeBean} for
 * the implementation.
 *
 * @since 1.0.2
 */
public interface AppProviderService {

    /**
     * Fetches the providers assigned to a single development, filtered by {@code criteria} and paged.
     *
     * @param appDevelopmentId identifier of the owning development record
     * @param criteria         optional additional filters (status, search)
     * @param pageable         pagination and sorting parameters
     * @return a page of mapped provider DTOs
     */
    Page<AppProviderOutputDTO> getAll(Long appDevelopmentId, AppProviderCriteria criteria, Pageable pageable);

    /**
     * Creates a new provider assignment. Duplicates (same company/role on the same development)
     * are not rejected.
     *
     * @param inputDTO the development reference, company name, role, and contract dates
     * @return the mapped output DTO for the newly created record
     */
    AppProviderOutputDTO create(AppProviderInputDTO inputDTO);

    /**
     * Updates an existing provider record in place.
     *
     * @param id       the provider record's own identifier
     * @param inputDTO the replacement field values to merge onto the existing record
     * @return the mapped output DTO reflecting the applied changes
     */
    AppProviderOutputDTO update(Long id, AppProviderInputDTO inputDTO);

    /**
     * Soft-deletes a provider record (stamps {@code deletedAt}/{@code deletedBy}); the row itself
     * is not removed.
     *
     * @param id the provider record's own identifier
     */
    void delete(Long id);
}
