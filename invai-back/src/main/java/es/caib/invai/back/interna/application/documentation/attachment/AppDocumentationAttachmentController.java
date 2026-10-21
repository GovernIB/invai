package es.caib.invai.back.interna.application.documentation.attachment;

import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentVersionDownloadDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentInputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentOutputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentSimpleOutputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentUpdateInputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentVersionInputDTO;
import es.caib.invai.back.persistence.repository.application.documentation.attachment.AppDocumentationAttachmentCriteria;
import es.caib.invai.back.service.facade.application.documentation.attachment.AppDocumentationAttachmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

/**
 * Internal REST controller exposing the documents attached to applications (the "Adjunts" list of
 * the Documentació tab) and their attachments.
 *
 * @since 1.0.6
 */
@Tag(name = "Documents adjunts", description = "Gestió dels documents adjunts de les aplicacions i dels seus fitxers.")
@RestController
@Slf4j
@RequestMapping("application/documentation")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class AppDocumentationAttachmentController {

    /** Facade service handling the business use cases of the documents. */
    private final AppDocumentationAttachmentService appDocumentationAttachmentService;

    /**
     * Creates a controller instance with the given service dependency injected.
     *
     * @param appDocumentationAttachmentService the facade service used to perform document operations
     */
    @Autowired
    public AppDocumentationAttachmentController(AppDocumentationAttachmentService appDocumentationAttachmentService) {
        this.appDocumentationAttachmentService = appDocumentationAttachmentService;
    }

    /**
     * Retrieves a paginated list of the documents of an application, each one with the data of its
     * current version. It lists the deleted documents too ({@code deletedAt} tells them apart); the
     * {@code deleted} filter keeps only the deleted ones ({@code true}) or only the others ({@code false}). It can be sorted by {@code title}, {@code section}, {@code mimeType},
     * {@code category}, {@code subcategory}, {@code versionLabel} and {@code status}.
     *
     * @param applicationId identifier of the application whose documents are listed
     * @param filter        search and filter criteria
     * @param pageable      pagination and sorting information
     * @return HTTP 200 with the matching page of documents
     */
    @GetMapping
    public ResponseEntity<Page<AppDocumentationAttachmentSimpleOutputDTO>> getAll(
            @RequestParam Long applicationId,
            @ModelAttribute AppDocumentationAttachmentCriteria filter,
            @PageableDefault(sort = "title") Pageable pageable) {
        filter.setApplicationId(applicationId);
        return ResponseEntity.ok(appDocumentationAttachmentService.getAll(filter, pageable));
    }

    /**
     * Retrieves a document with all its attachments, without the files.
     *
     * @param id the identifier of the document
     * @return HTTP 200 with the document and its attachments
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppDocumentationAttachmentOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(appDocumentationAttachmentService.getById(id));
    }

    /**
     * Creates a new document along with its first version. The request is {@code multipart/form-data}
     * with a {@code data} part holding the JSON of the document and a {@code file} part.
     *
     * @param inputDTO the data of the document and of its first version
     * @param file     the file of the first version
     * @return HTTP 201 with the created document
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AppDocumentationAttachmentOutputDTO> create(
            @Valid @RequestPart("data") AppDocumentationAttachmentInputDTO inputDTO,
            @RequestPart("file") MultipartFile file) {
        return new ResponseEntity<>(appDocumentationAttachmentService.create(inputDTO, file), HttpStatus.CREATED);
    }

    /**
     * Adds a new version to a document. The request is {@code multipart/form-data} with a
     * {@code data} part holding the JSON of the version and a {@code file} part.
     *
     * @param id       the identifier of the document
     * @param inputDTO the data of the new version
     * @param file     the file of the new version
     * @return HTTP 201 with the document and all its attachments
     */
    @PostMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AppDocumentationAttachmentOutputDTO> createAttachment(
            @PathVariable Long id,
            @Valid @RequestPart("data") AppDocumentationAttachmentVersionInputDTO inputDTO,
            @RequestPart("file") MultipartFile file) {
        return new ResponseEntity<>(appDocumentationAttachmentService.createAttachment(id, inputDTO, file), HttpStatus.CREATED);
    }

    /**
     * Updates the main record of a document; its attachments and files are never modified.
     *
     * @param id       the identifier of the document
     * @param inputDTO the new data
     * @return HTTP 200 with the updated document
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppDocumentationAttachmentOutputDTO> update(@PathVariable Long id, @Valid @RequestBody AppDocumentationAttachmentUpdateInputDTO inputDTO) {
        return ResponseEntity.ok(appDocumentationAttachmentService.update(id, inputDTO));
    }

    /**
     * Logically deletes a document and all its attachments.
     *
     * @param id the identifier of the document
     * @return HTTP 204 with no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appDocumentationAttachmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Logically deletes one attachment of a document, with its metadata. If it was the current
     * one, the most recent of the remaining attachments becomes the current one; if it was the
     * only one left, the whole document is deleted.
     *
     * @param id           the identifier of the document
     * @param attachmentId the identifier of the attachment
     * @return HTTP 204 with no content
     */
    @DeleteMapping("/{id}/attachment/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id, @PathVariable Long attachmentId) {
        appDocumentationAttachmentService.deleteAttachment(id, attachmentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Sends the file of an attachment to be downloaded, and registers the download in the audit
     * history of the attachment.
     *
     * @param id           the identifier of the document
     * @param attachmentId the identifier of the attachment
     * @return HTTP 200 with the bytes of the file
     */
    @GetMapping("/{id}/attachment/{attachmentId}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id, @PathVariable Long attachmentId) {
        return fileResponse(appDocumentationAttachmentService.download(id, attachmentId, false), ContentDisposition.attachment());
    }

    /**
     * Sends the file of an attachment to be shown in the browser, and registers the view in the
     * audit history of the attachment. It is the same file as the download, only the
     * {@code Content-Disposition} header changes.
     *
     * @param id           the identifier of the document
     * @param attachmentId the identifier of the attachment
     * @return HTTP 200 with the bytes of the file
     */
    @GetMapping("/{id}/attachment/{attachmentId}/view")
    public ResponseEntity<byte[]> view(@PathVariable Long id, @PathVariable Long attachmentId) {
        return fileResponse(appDocumentationAttachmentService.download(id, attachmentId, true), ContentDisposition.inline());
    }

    /**
     * Builds the response that sends a file with its name and media type.
     *
     * @param file        the file to send
     * @param disposition whether the browser downloads the file or shows it
     * @return HTTP 200 with the bytes of the file
     */
    private ResponseEntity<byte[]> fileResponse(AppDocumentationAttachmentVersionDownloadDTO file, ContentDisposition.Builder disposition) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.filename(file.getFileName(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.parseMediaType(file.getMimeType()))
                .contentLength(file.getContent().length)
                .body(file.getContent());
    }
}
