package es.caib.invai.back.service.facade.application.documentation.attachment;

import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentVersionDownloadDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentInputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentOutputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentSimpleOutputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentUpdateInputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentVersionInputDTO;
import es.caib.invai.back.persistence.repository.application.documentation.attachment.AppDocumentationAttachmentCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service facade boundary declaring the business use cases of the documents attached to
 * applications and their attachments.
 *
 * @since 1.0.6
 */
public interface AppDocumentationAttachmentService {

    /**
     * Retrieves a paginated list of the documents of an application matching the given filter
     * criteria, each one with the data of its current attachment and no file content. Deleted
     * documents are listed too, told apart by their deletion timestamp, unless the filter excludes them.
     *
     * @param filter   filter criteria, scoped by application
     * @param pageable pagination and sorting information
     * @return a page of documents
     */
    Page<AppDocumentationAttachmentSimpleOutputDTO> getAll(AppDocumentationAttachmentCriteria filter, Pageable pageable);

    /**
     * Retrieves a document with its attachments and metadata, without any file content. A deleted
     * document is returned too, with its deletion timestamp and the attachments and metadata it
     * had when it was deleted.
     *
     * @param id the identifier of the document
     * @return the document with its attachments, the newest first
     */
    AppDocumentationAttachmentOutputDTO getById(Long id);

    /**
     * Creates a new document along with its first version, which becomes the current one.
     *
     * @param inputDTO the data of the document and of its first version
     * @param file     the file of the first version
     * @return the created document with its version
     */
    AppDocumentationAttachmentOutputDTO create(AppDocumentationAttachmentInputDTO inputDTO, MultipartFile file);

    /**
     * Adds a new version to an existing document; it becomes the current one and the previous
     * current version stops being so.
     *
     * @param id       the identifier of the document
     * @param inputDTO the data of the new version
     * @param file     the file of the new version
     * @return the document with all its attachments
     */
    AppDocumentationAttachmentOutputDTO createAttachment(Long id, AppDocumentationAttachmentVersionInputDTO inputDTO, MultipartFile file);

    /**
     * Updates the main record of a document. Its attachments and files are never modified.
     *
     * @param id       the identifier of the document
     * @param inputDTO the new data
     * @return the updated document with its attachments
     */
    AppDocumentationAttachmentOutputDTO update(Long id, AppDocumentationAttachmentUpdateInputDTO inputDTO);

    /**
     * Logically deletes a document and all its attachments.
     *
     * @param id the identifier of the document
     */
    void delete(Long id);

    /**
     * Logically deletes one attachment of a document, with its metadata. If it was the current one,
     * the most recent of the remaining attachments becomes the current one. If it was the only
     * attachment left, the whole document is deleted instead.
     *
     * @param id           the identifier of the document
     * @param attachmentId the identifier of the attachment
     */
    void deleteAttachment(Long id, Long attachmentId);

    /**
     * Reads the file of any active version of an active document and registers the access in the
     * audit history of that version: a download, or a view when it is shown in the browser.
     *
     * @param id           the identifier of the document
     * @param attachmentId the identifier of the version
     * @param inline       whether the file is shown in the browser instead of downloaded
     * @return the file with its name and media type
     */
    AppDocumentationAttachmentVersionDownloadDTO download(Long id, Long attachmentId, boolean inline);
}
