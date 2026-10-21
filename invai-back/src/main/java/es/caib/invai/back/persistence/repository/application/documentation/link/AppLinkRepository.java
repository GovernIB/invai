package es.caib.invai.back.persistence.repository.application.documentation.link;

import es.caib.invai.back.service.model.application.documentation.link.AppLink;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Outbound port declaring the persistence mechanisms for the links of applications.
 *
 * @since 1.0.6
 */
public interface AppLinkRepository {

    /**
     * Finds a link by its identifier, active or logically deleted.
     *
     * @param id the identifier
     * @return the matching link, or {@code null} if none is found
     */
    AppLink findById(Long id);

    /**
     * Finds the links matching the given criteria, deleted ones included unless filtered out,
     * paginated and sorted as the pageable asks (see {@link AppLinkSpecification} for the sortable keys).
     *
     * @param filter   filter criteria, scoped by application
     * @param pageable pagination and sorting information
     * @return a page of matching links
     */
    Page<AppLink> findAll(AppDocumentationLinkCriteria filter, Pageable pageable);

    /**
     * Tells whether an active link of the application already has the given title, section and
     * address.
     *
     * @param applicationId identifier of the application
     * @param title         title of the link
     * @param sectionId     identifier of the section
     * @param url           address of the link
     * @return {@code true} if such an active link exists
     */
    boolean existsActiveDuplicate(Long applicationId, String title, Long sectionId, String url);

    /**
     * Tells whether an active link other than the given one already has, in the same application,
     * the given title, section and address.
     *
     * @param applicationId identifier of the application
     * @param title         title of the link
     * @param sectionId     identifier of the section
     * @param url           address of the link
     * @param id            identifier of the link to leave out
     * @return {@code true} if such an active link exists
     */
    boolean existsActiveDuplicateExcluding(Long applicationId, String title, Long sectionId, String url, Long id);

    /**
     * Persists a new link.
     *
     * @param appLink the link to create
     * @return the persisted link
     */
    AppLink create(AppLink appLink);

    /**
     * Persists changes to an existing link.
     *
     * @param appLink the data to persist
     * @param id      the identifier of the link to update
     * @return the updated link
     */
    AppLink update(AppLink appLink, Long id);

    /**
     * Logically deletes the given link.
     *
     * @param appLink the link to delete, already carrying its deletion timestamp and author
     */
    void delete(AppLink appLink);
}
