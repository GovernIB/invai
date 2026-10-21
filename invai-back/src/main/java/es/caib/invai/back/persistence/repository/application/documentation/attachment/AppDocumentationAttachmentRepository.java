package es.caib.invai.back.persistence.repository.application.documentation.attachment;

import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationAttachment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Outbound port declaring the persistence mechanisms for the documents attached to applications.
 * The attachments of a document are handled by {@link AppDocumentationAttachmentVersionRepository}.
 *
 * @since 1.0.6
 */
public interface AppDocumentationAttachmentRepository {

    /**
     * Finds a document by its identifier, active or logically deleted.
     *
     * @param id the identifier
     * @return the matching document without its attachments, or {@code null} if none is found
     */
    AppDocumentationAttachment findById(Long id);

    /**
     * Finds the documents matching the given criteria, deleted ones included unless filtered out, paginated and sorted as the pageable
     * asks (see {@link AppDocumentationAttachmentSpecification} for the sortable keys).
     *
     * @param filter   filter criteria, scoped by application
     * @param pageable pagination and sorting information
     * @return a page of matching documents, without their attachments
     */
    Page<AppDocumentationAttachment> findAll(AppDocumentationAttachmentCriteria filter, Pageable pageable);

    /**
     * Persists a new document.
     *
     * @param appDocumentationAttachment the document to create
     * @return the persisted document
     */
    AppDocumentationAttachment create(AppDocumentationAttachment appDocumentationAttachment);

    /**
     * Persists changes to the main record of an existing document.
     *
     * @param appDocumentationAttachment the data to persist
     * @param id            the identifier of the document to update
     * @return the updated document
     */
    AppDocumentationAttachment update(AppDocumentationAttachment appDocumentationAttachment, Long id);

    /**
     * Logically deletes the given document.
     *
     * @param appDocumentationAttachment the document to delete, already carrying its deletion timestamp and author
     */
    void delete(AppDocumentationAttachment appDocumentationAttachment);
}
