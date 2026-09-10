package es.caib.invai.back.service.facade.application.development.core;

import es.caib.invai.back.interna.application.development.core.DTO.DevelopmentInputDTO;
import es.caib.invai.back.interna.application.development.core.DTO.DevelopmentOutputDTO;

/**
 * Facade contract for the "AppDevelopment" tab: the single development record attached 1-to-1 to
 * an application. See {@link es.caib.invai.back.ejb.application.development.core.AppDevelopmentServiceFacadeBean}
 * for the implementation.
 *
 * @since 1.0.2
 */
public interface AppDevelopmentService {

    /**
     * Fetches a development record by its own primary key (not the parent application's id).
     *
     * @param id the development record's own identifier
     * @return the mapped output DTO, or {@code null} if no record has this id
     */
    DevelopmentOutputDTO getById(Long id);

    /**
     * Creates the development record for an application, rejecting the request if that application
     * already has an active one.
     *
     * @param inputDTO the application, environment and lookup references for the new record
     * @return the mapped output DTO for the newly created record
     */
    DevelopmentOutputDTO create(DevelopmentInputDTO inputDTO);

    /**
     * Updates an existing development record in place.
     *
     * @param id       the development record's own identifier
     * @param inputDTO the replacement field values to merge onto the existing record
     * @return the mapped output DTO reflecting the applied changes
     */
    DevelopmentOutputDTO update(Long id, DevelopmentInputDTO inputDTO);

    /**
     * Soft-deletes a development record (stamps {@code deletedAt}/{@code deletedBy}); the row
     * itself is not removed.
     *
     * @param id the development record's own identifier
     */
    void delete(Long id);
}
