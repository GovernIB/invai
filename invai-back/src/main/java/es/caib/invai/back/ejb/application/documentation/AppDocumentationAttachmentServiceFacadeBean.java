package es.caib.invai.back.ejb.application.documentation;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentVersionDownloadDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentInputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationMetadataInputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentOutputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentSimpleOutputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentUpdateInputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentVersionInputDTO;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.persistence.repository.application.documentation.attachment.AppDocumentationAttachmentCriteria;
import es.caib.invai.back.persistence.repository.application.documentation.attachment.AppDocumentationAttachmentRepository;
import es.caib.invai.back.persistence.repository.application.documentation.attachment.AppDocumentationAttachmentVersionRepository;
import es.caib.invai.back.persistence.repository.application.documentation.attachment.AppDocumentationMetadataRepository;
import es.caib.invai.back.persistence.repository.catalog.appSection.AppSectionRepository;
import es.caib.invai.back.persistence.repository.catalog.documentStatus.DocumentStatusRepository;
import es.caib.invai.back.persistence.repository.catalog.mimeType.MimeTypeRepository;
import es.caib.invai.back.persistence.repository.maintenance.documentation.documentCategory.DocumentCategoryRepository;
import es.caib.invai.back.service.facade.application.documentation.attachment.AppDocumentationAttachmentService;
import es.caib.invai.back.service.mapper.application.documentation.attachment.AppDocumentationAttachmentVersionMapper;
import es.caib.invai.back.service.mapper.application.documentation.attachment.AppDocumentationAttachmentMapper;
import es.caib.invai.back.service.mapper.application.documentation.attachment.AppDocumentationMetadataMapper;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationAttachment;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationAttachmentVersion;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationMetadata;
import es.caib.invai.back.service.model.catalog.mimeType.MimeType;
import es.caib.invai.back.service.model.maintenance.documentation.documentCategory.DocumentCategory;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Facade service implementation for the documents attached to applications and their attachments.
 * A document is made of a main record and one or more attachments, each holding an uploaded file;
 * the latest version uploaded is the current one.
 *
 * @since 1.0.6
 */
@Service
@Slf4j
@Transactional
public class AppDocumentationAttachmentServiceFacadeBean implements AppDocumentationAttachmentService {

    /** Maximum size of an uploaded file, in bytes (5 MB). */
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;

    /** Mapper used to convert between document domain models, entities, and DTOs. */
    @Autowired
    private AppDocumentationAttachmentMapper appDocumentationAttachmentMapper;

    /** Mapper used to build the attachments of the documents from their input and the uploaded file. */
    @Autowired
    private AppDocumentationAttachmentVersionMapper appDocumentationAttachmentVersionMapper;

    /** Mapper used to build the metadata of the documents from their input. */
    @Autowired
    private AppDocumentationMetadataMapper appDocumentationMetadataMapper;

    /** Repository port used to persist and query the metadata of the documents. */
    @Autowired
    private AppDocumentationMetadataRepository appDocumentationMetadataRepository;

    /** Repository port used to persist and query documents. */
    @Autowired
    private AppDocumentationAttachmentRepository appDocumentationAttachmentRepository;

    /** Repository port used to persist and query the attachments of the documents. */
    @Autowired
    private AppDocumentationAttachmentVersionRepository appDocumentationAttachmentVersionRepository;

    /** Repository port used to check that the application of a document exists. */
    @Autowired
    private ApplicationRepository applicationRepository;

    /** Repository port used to check that the section of a document exists. */
    @Autowired
    private AppSectionRepository appSectionRepository;

    /** Repository port used to check that the state of a document exists. */
    @Autowired
    private DocumentStatusRepository documentStatusRepository;

    /** Repository port used to find the file format of an uploaded file from its extension. */
    @Autowired
    private MimeTypeRepository mimeTypeRepository;

    /** Repository port used to check the category of a document. */
    @Autowired
    private DocumentCategoryRepository documentCategoryRepository;

    /**
     * Retrieves a paginated list of the documents of an application matching the given filter
     * criteria, each one with the data of its current attachment and no file content. Deleted
     * documents are listed too, told apart by their deletion timestamp, unless the filter excludes them.
     *
     * @param filter   filter criteria, scoped by application
     * @param pageable pagination and sorting information
     * @return a page of documents as response DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AppDocumentationAttachmentSimpleOutputDTO> getAll(AppDocumentationAttachmentCriteria filter, Pageable pageable) {
        log.debug("Facade: Fetching application documents via pagination boundaries");
        Page<AppDocumentationAttachment> page = appDocumentationAttachmentRepository.findAll(filter, pageable);

        Map<Long, AppDocumentationAttachmentVersion> currentAttachments = appDocumentationAttachmentVersionRepository
                .findCurrentByDocumentationAttachmentIds(page.getContent().stream().map(AppDocumentationAttachment::getId).toList());
        page.getContent().forEach(appDocumentationAttachment -> appDocumentationAttachment.setCurrentAttachment(currentAttachments.get(appDocumentationAttachment.getId())));

        return page.map(appDocumentationAttachmentMapper::toSimpleResponse);
    }

    /**
     * Retrieves a document with its attachments and metadata, without any file content. A deleted
     * document is returned too, with its deletion timestamp and the attachments and metadata it
     * had when it was deleted.
     *
     * @param id the identifier of the document
     * @return the document with its attachments, the newest first
     * @throws BusinessRuleException if the document doesn't exist
     */
    @Override
    @Transactional(readOnly = true)
    public AppDocumentationAttachmentOutputDTO getById(Long id) {
        log.debug("Facade: Fetching application document by ID: {}", id);
        AppDocumentationAttachment appDocumentationAttachment = appDocumentationAttachmentRepository.findById(id);
        if (Objects.isNull(appDocumentationAttachment)) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTATION_NOT_FOUND);
        }
        boolean deleted = Objects.nonNull(appDocumentationAttachment.getDeletedAt());
        appDocumentationAttachment.setAttachments(deleted
                ? appDocumentationAttachmentVersionRepository.findDeletedWithDocumentationAttachment(id, appDocumentationAttachment.getDeletedAt())
                : appDocumentationAttachmentVersionRepository.findActiveByDocumentationAttachmentId(id));
        List<Long> attachmentIds = appDocumentationAttachment.getAttachments().stream().map(AppDocumentationAttachmentVersion::getId).toList();
        List<AppDocumentationMetadata> metadata = deleted
                ? appDocumentationMetadataRepository.findDeletedWithDocumentationAttachment(attachmentIds, appDocumentationAttachment.getDeletedAt())
                : appDocumentationMetadataRepository.findActiveByAttachmentIds(attachmentIds);
        appDocumentationAttachment.getAttachments().forEach(attachment -> attachment.setMetadata(metadata.stream()
                .filter(item -> Objects.equals(item.getAppDocumentationAttachmentVersionId(), attachment.getId())).toList()));
        return appDocumentationAttachmentMapper.toResponse(appDocumentationAttachment);
    }

    /**
     * Creates a new document along with its first version, which becomes the current one.
     *
     * @param inputDTO the data of the document and of its first version
     * @param file     the file of the first version
     * @return the created document with its version
     * @throws BusinessRuleException if the application, section, state, or category doesn't exist,
     *                               the category isn't active, or the file is empty, too large, or of a format that isn't allowed
     */
    @Override
    public AppDocumentationAttachmentOutputDTO create(AppDocumentationAttachmentInputDTO inputDTO, MultipartFile file) {
        log.debug("Facade: Creating new application document with title: {}", inputDTO.getTitle());

        Utils.sanitize(inputDTO);

        if (Objects.isNull(applicationRepository.findById(inputDTO.getApplicationId()))) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_FOUND);
        }
        validateData(inputDTO.getSectionId(), inputDTO.getCategoryId(), inputDTO.getStatusId(), null);
        validateMetadata(inputDTO.getMetadata());
        MimeType mimeType = validateFile(file);
        byte[] content = Utils.readContent(file);

        AppDocumentationAttachment created = appDocumentationAttachmentRepository.create(appDocumentationAttachmentMapper.toModelFromInput(inputDTO));
        AppDocumentationAttachmentVersion first = appDocumentationAttachmentVersionRepository.create(
                appDocumentationAttachmentVersionMapper.toModelFromInput(inputDTO, created.getId(), file, mimeType, content), content);
        createMetadata(first.getId(), inputDTO.getMetadata());

        return getById(created.getId());
    }

    /**
     * Adds a new version to an existing document; it becomes the current one and the previous
     * current version stops being so.
     *
     * @param id       the identifier of the document
     * @param inputDTO the data of the new version
     * @param file     the file of the new version
     * @return the document with all its attachments
     * @throws BusinessRuleException if the document doesn't exist or is deleted, the version label
     *                               is already used by an active version of it, or the file is empty, too large, or of a format that isn't allowed
     */
    @Override
    public AppDocumentationAttachmentOutputDTO createAttachment(Long id, AppDocumentationAttachmentVersionInputDTO inputDTO, MultipartFile file) {
        log.debug("Facade: Adding version {} to application document ID: {}", inputDTO.getVersionLabel(), id);

        Utils.sanitize(inputDTO);

        AppDocumentationAttachment appDocumentationAttachment = appDocumentationAttachmentRepository.findById(id);
        if (Objects.isNull(appDocumentationAttachment)) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTATION_NOT_FOUND);
        }
        if (Objects.nonNull(appDocumentationAttachment.getDeletedAt())) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTATION_NOT_ACTIVE);
        }

        if (appDocumentationAttachmentVersionRepository.existsActiveWithLabel(id, inputDTO.getVersionLabel())) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTATION_ATTACHMENT_VERSION_DUPLICATED);
        }
        validateMetadata(inputDTO.getMetadata());
        MimeType mimeType = validateFile(file);
        byte[] content = Utils.readContent(file);

        appDocumentationAttachmentVersionRepository.unmarkCurrent(id);
        AppDocumentationAttachmentVersion created = appDocumentationAttachmentVersionRepository.create(
                appDocumentationAttachmentVersionMapper.toModelFromInput(inputDTO, id, file, mimeType, content), content);
        createMetadata(created.getId(), inputDTO.getMetadata());

        return getById(id);
    }

    /**
     * Updates the main record of an active document. Its attachments and files are never modified.
     *
     * @param id       the identifier of the document
     * @param inputDTO the new data
     * @return the updated document with its attachments
     * @throws BusinessRuleException if the document doesn't exist or is deleted, the section or
     *                               state doesn't exist, or the new category doesn't exist or isn't active
     */
    @Override
    public AppDocumentationAttachmentOutputDTO update(Long id, AppDocumentationAttachmentUpdateInputDTO inputDTO) {
        log.debug("Facade: Updating application document with ID: {}", id);

        Utils.sanitize(inputDTO);

        AppDocumentationAttachment existing = findActiveById(id);
        validateData(inputDTO.getSectionId(), inputDTO.getCategoryId(), inputDTO.getStatusId(), existing.getDocumentCategory().getId());

        appDocumentationAttachmentMapper.updateModelFromInput(inputDTO, existing);
        appDocumentationAttachmentRepository.update(existing, id);

        return getById(id);
    }

    /**
     * Logically deletes a document and all its attachments by setting their deletion timestamp and
     * author.
     *
     * @param id the identifier of the document
     * @throws BusinessRuleException if the document doesn't exist or is already deleted
     */
    @Override
    public void delete(Long id) {
        log.debug("Facade: Logically deleting application document with ID: {}", id);
        AppDocumentationAttachment existing = findActiveById(id);

        LocalDateTime deletedAt = LocalDateTime.now();
        String deletedBy = Utils.resolveCurrentUsername();

        List<Long> attachmentIds = appDocumentationAttachmentVersionRepository.findActiveByDocumentationAttachmentId(id).stream()
                .map(AppDocumentationAttachmentVersion::getId).toList();

        existing.setDeletedAt(deletedAt);
        existing.setDeletedBy(deletedBy);
        appDocumentationAttachmentRepository.delete(existing);
        appDocumentationAttachmentVersionRepository.deleteAllByDocumentationAttachmentId(id, deletedAt, deletedBy);
        appDocumentationMetadataRepository.deleteAllByAttachmentIds(attachmentIds, deletedAt, deletedBy);
    }

    /**
     * Logically deletes one attachment of an active document, with its metadata. If it was the
     * current one, the most recent of the remaining attachments becomes the current one. If it was
     * the only attachment left, the whole document is deleted instead, as a document can't be left
     * without one.
     *
     * @param id           the identifier of the document
     * @param attachmentId the identifier of the attachment
     * @throws BusinessRuleException if the document doesn't exist or is deleted, or the attachment
     *                               doesn't exist, is already deleted, or belongs to another document
     */
    @Override
    public void deleteAttachment(Long id, Long attachmentId) {
        log.debug("Facade: Logically deleting attachment ID: {} of application document ID: {}", attachmentId, id);
        findActiveById(id);
        AppDocumentationAttachmentVersion attachment = appDocumentationAttachmentVersionRepository.findById(attachmentId);
        if (Objects.isNull(attachment) || Objects.nonNull(attachment.getDeletedAt()) || !Objects.equals(attachment.getAppDocumentationAttachmentId(), id)) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTATION_ATTACHMENT_NOT_FOUND);
        }

        List<AppDocumentationAttachmentVersion> active = appDocumentationAttachmentVersionRepository.findActiveByDocumentationAttachmentId(id);
        if (active.size() <= 1) {
            delete(id);
            return;
        }

        LocalDateTime deletedAt = LocalDateTime.now();
        String deletedBy = Utils.resolveCurrentUsername();
        attachment.setDeletedAt(deletedAt);
        attachment.setDeletedBy(deletedBy);
        appDocumentationAttachmentVersionRepository.delete(attachment);
        appDocumentationMetadataRepository.deleteAllByAttachmentIds(List.of(attachmentId), deletedAt, deletedBy);

        if (attachment.isCurrent()) {
            active.stream()
                    .filter(remaining -> !Objects.equals(remaining.getId(), attachmentId))
                    .findFirst()
                    .ifPresent(newest -> appDocumentationAttachmentVersionRepository.markCurrent(newest.getId()));
        }
    }

    /**
     * Reads the file of any active version of an active document and registers the access in the
     * audit history of that version: a download, or a view when it is shown in the browser.
     *
     * @param id           the identifier of the document
     * @param attachmentId the identifier of the version
     * @param inline       whether the file is shown in the browser instead of downloaded
     * @return the file with its name and media type
     * @throws BusinessRuleException if the document doesn't exist or is deleted, or the version
     *                               doesn't exist, is deleted, or belongs to another document
     */
    @Override
    public AppDocumentationAttachmentVersionDownloadDTO download(Long id, Long attachmentId, boolean inline) {
        log.debug("Facade: Reading version ID: {} of application document ID: {}", attachmentId, id);
        AppDocumentationAttachment appDocumentationAttachment = appDocumentationAttachmentRepository.findById(id);
        if (Objects.isNull(appDocumentationAttachment) || Objects.nonNull(appDocumentationAttachment.getDeletedAt())) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTATION_NOT_FOUND);
        }
        AppDocumentationAttachmentVersion currentVersionAttachment = appDocumentationAttachmentVersionRepository.findById(attachmentId);
        if (Objects.isNull(currentVersionAttachment) || Objects.nonNull(currentVersionAttachment.getDeletedAt()) || !Objects.equals(currentVersionAttachment.getAppDocumentationAttachmentId(), id)) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTATION_ATTACHMENT_NOT_FOUND);
        }
        appDocumentationAttachmentVersionRepository.registerDownload(attachmentId, inline);
        return new AppDocumentationAttachmentVersionDownloadDTO(currentVersionAttachment.getFileName(), currentVersionAttachment.getMimeType().getMimeType(), appDocumentationAttachmentVersionRepository.findContentById(attachmentId));
    }

    /**
     * Finds a document that is still active.
     *
     * @param id the identifier of the document
     * @return the document
     * @throws BusinessRuleException if the document doesn't exist or is deleted
     */
    private AppDocumentationAttachment findActiveById(Long id) {
        AppDocumentationAttachment appDocumentationAttachment = appDocumentationAttachmentRepository.findById(id);
        if (Objects.isNull(appDocumentationAttachment)) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTATION_NOT_FOUND);
        }
        if (Objects.nonNull(appDocumentationAttachment.getDeletedAt())) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTATION_NOT_ACTIVE);
        }
        return appDocumentationAttachment;
    }

    /**
     * Checks that the section and the state exist and that the category exists and is active. An
     * unchanged category isn't checked again, so a document keeps working when its category is
     * deleted later.
     *
     * @param sectionId         identifier of the section
     * @param categoryId        identifier of the category or subcategory
     * @param statusId          identifier of the state
     * @param currentCategoryId identifier of the category the document already has, {@code null} when creating
     * @throws BusinessRuleException if any of those conditions fails
     */
    private void validateData(Long sectionId, Long categoryId, Long statusId, Long currentCategoryId) {
        if (Objects.isNull(appSectionRepository.findById(sectionId))) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTATION_SECTION_NOT_FOUND);
        }
        if (Objects.isNull(documentStatusRepository.findById(statusId))) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTATION_STATUS_NOT_FOUND);
        }
        if (!Objects.equals(categoryId, currentCategoryId)) {
            DocumentCategory category = documentCategoryRepository.findById(categoryId);
            if (Objects.isNull(category)) {
                throw new BusinessRuleException(Constants.ERR_DOCUMENTATION_CATEGORY_NOT_FOUND);
            }
            if (Objects.nonNull(category.getDeletedAt())) {
                throw new BusinessRuleException(Constants.ERR_DOCUMENTATION_CATEGORY_NOT_ACTIVE);
            }
        }
    }

    /**
     * Checks that the file isn't empty, doesn't exceed the maximum size, and has an extension of
     * the catalog of allowed formats.
     *
     * @param file the uploaded file
     * @return the file format matching the extension of the file
     * @throws BusinessRuleException if the file is missing or empty, too large, or of a format that isn't allowed
     */
    private MimeType validateFile(MultipartFile file) {
        if (Objects.isNull(file) || file.isEmpty()) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTATION_ATTACHMENT_FILE_EMPTY);
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTATION_ATTACHMENT_FILE_TOO_LARGE);
        }
        String extension = StringUtils.lowerCase(StringUtils.substringAfterLast(Utils.baseName(file.getOriginalFilename()), "."));
        MimeType mimeType = StringUtils.isBlank(extension) ? null : mimeTypeRepository.findByExtension(extension);
        if (Objects.isNull(mimeType)) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTATION_ATTACHMENT_FILE_TYPE_NOT_ALLOWED);
        }
        return mimeType;
    }

    /**
     * Checks that the metadata sent together doesn't repeat any key and trims their texts.
     *
     * @param metadata the metadata sent with an attachment, may be {@code null}
     * @throws BusinessRuleException if two of them have the same key
     */
    private void validateMetadata(List<AppDocumentationMetadataInputDTO> metadata) {
        if (Objects.isNull(metadata)) {
            return;
        }
        Set<String> keys = new HashSet<>();
        for (AppDocumentationMetadataInputDTO item : metadata) {
            Utils.sanitize(item);
            if (!keys.add(item.getKey())) {
                throw new BusinessRuleException(Constants.ERR_DOCUMENTATION_METADATA_DUPLICATED);
            }
        }
    }

    /**
     * Stores the metadata of an attachment.
     *
     * @param attachmentId identifier of the attachment
     * @param metadata     the metadata to store, may be {@code null}
     */
    private void createMetadata(Long attachmentId, List<AppDocumentationMetadataInputDTO> metadata) {
        if (Objects.isNull(metadata)) {
            return;
        }
        metadata.forEach(item -> appDocumentationMetadataRepository
                .create(appDocumentationMetadataMapper.toModelFromInput(item, attachmentId)));
    }
}
