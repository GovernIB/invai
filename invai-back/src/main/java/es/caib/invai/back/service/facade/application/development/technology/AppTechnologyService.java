package es.caib.invai.back.service.facade.application.development.technology;

import es.caib.invai.back.interna.application.development.technology.DTO.AppTechnologyInputDTO;
import es.caib.invai.back.interna.application.development.technology.DTO.AppTechnologyOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.technology.AppTechnologyCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Facade contract for the "AppTechnology" list: technology stack entries hanging many-to-one off a
 * single development record. See
 * {@link es.caib.invai.back.ejb.application.development.technology.AppTechnologyServiceFacadeBean}
 * for the implementation.
 *
 * @since 1.0.2
 */
public interface AppTechnologyService {

    /**
     * Fetches the technology entries assigned to a single development, filtered by {@code criteria}
     * and paged.
     *
     * @param appDevelopmentId identifier of the owning development record
     * @param criteria         optional additional filters (status, search)
     * @param pageable         pagination and sorting parameters
     * @return a page of mapped technology DTOs
     */
    Page<AppTechnologyOutputDTO> getAll(Long appDevelopmentId, AppTechnologyCriteria criteria, Pageable pageable);

    /**
     * Creates a new technology stack entry. Duplicates (same layer/technology on the same
     * development) are not rejected.
     *
     * @param inputDTO the development reference, layer, technology, version and architecture
     * @return the mapped output DTO for the newly created record
     */
    AppTechnologyOutputDTO create(AppTechnologyInputDTO inputDTO);

    /**
     * Updates an existing technology record in place.
     *
     * @param id       the technology record's own identifier
     * @param inputDTO the replacement field values to merge onto the existing record
     * @return the mapped output DTO reflecting the applied changes
     */
    AppTechnologyOutputDTO update(Long id, AppTechnologyInputDTO inputDTO);

    /**
     * Soft-deletes a technology record (stamps {@code deletedAt}/{@code deletedBy}); the row itself
     * is not removed.
     *
     * @param id the technology record's own identifier
     */
    void delete(Long id);
}
