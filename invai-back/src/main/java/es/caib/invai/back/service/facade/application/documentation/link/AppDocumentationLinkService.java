package es.caib.invai.back.service.facade.application.documentation.link;

import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkInputDTO;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkOutputDTO;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkUpdateInputDTO;
import es.caib.invai.back.persistence.repository.application.documentation.link.AppDocumentationLinkCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service facade boundary declaring the business use cases of the links of applications.
 *
 * @since 1.0.6
 */
public interface AppDocumentationLinkService {

    /**
     * Retrieves a paginated list of the links of an application matching the given filter
     * criteria. Deleted links are listed too, told apart by their deletion timestamp, unless the
     * filter excludes them.
     *
     * @param filter   filter criteria, scoped by application
     * @param pageable pagination and sorting information
     * @return a page of links
     */
    Page<AppDocumentationLinkOutputDTO> getAll(AppDocumentationLinkCriteria filter, Pageable pageable);

    /**
     * Creates a link of an application.
     *
     * @param inputDTO the data of the link
     * @return the created link
     */
    AppDocumentationLinkOutputDTO create(AppDocumentationLinkInputDTO inputDTO);

    /**
     * Updates an active link. The application it belongs to never changes.
     *
     * @param id       the identifier of the link
     * @param inputDTO the new data
     * @return the updated link
     */
    AppDocumentationLinkOutputDTO update(Long id, AppDocumentationLinkUpdateInputDTO inputDTO);

    /**
     * Logically deletes a link.
     *
     * @param id the identifier of the link
     */
    void delete(Long id);
}
