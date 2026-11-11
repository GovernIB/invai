package es.caib.invai.back.ejb.application.documentation;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkInputDTO;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkOutputDTO;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkUpdateInputDTO;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.persistence.repository.application.documentation.link.AppDocumentationLinkCriteria;
import es.caib.invai.back.persistence.repository.application.documentation.link.AppLinkRepository;
import es.caib.invai.back.persistence.repository.maintenance.documentation.documentCategory.DocumentCategoryRepository;
import es.caib.invai.back.service.facade.application.documentation.link.AppDocumentationLinkService;
import es.caib.invai.back.service.facade.maintenance.documentation.documentRolePermission.DocumentRolePermissionService;
import es.caib.invai.back.service.mapper.application.documentation.link.AppLinkMapper;
import es.caib.invai.back.service.model.application.documentation.link.AppLink;
import es.caib.invai.back.service.model.maintenance.documentation.documentCategory.DocumentCategory;
import es.caib.invai.back.service.model.maintenance.documentation.documentRolePermission.DocumentPermission;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Facade service implementation for the links of applications (the "Enllaços" list of the
 * Documentació tab). A link can't be repeated: an application has at most one active link with the
 * same title, category and address.
 *
 * @since 1.0.6
 */
@Service
@Slf4j
@Transactional
public class AppDocumentationLinkServiceFacadeBean implements AppDocumentationLinkService {

    /** Mapper used to convert between link domain models, entities, and DTOs. */
    @Autowired
    private AppLinkMapper appLinkMapper;

    /** Repository port used to persist and query links. */
    @Autowired
    private AppLinkRepository appLinkRepository;

    /** Repository port used to check that the application of a link exists. */
    @Autowired
    private ApplicationRepository applicationRepository;

    /** Repository port used to check that the category of a link exists. */
    @Autowired
    private DocumentCategoryRepository documentCategoryRepository;

    /** Service used to check that the logged user has the permission an action needs. */
    @Autowired
    private DocumentRolePermissionService documentRolePermissionService;

    /**
     * Retrieves a paginated list of the links of an application matching the given filter
     * criteria. Deleted links are listed too, told apart by their deletion timestamp, unless the
     * filter excludes them. Only the links of the categories the logged user can read are listed.
     *
     * @param filter   filter criteria, scoped by application
     * @param pageable pagination and sorting information
     * @return a page of links as response DTOs
     * @throws AccessDeniedException if the filter asks for a
     *                                                                   category the user can't read
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AppDocumentationLinkOutputDTO> getAll(AppDocumentationLinkCriteria filter, Pageable pageable) {
        log.debug("Facade: Fetching application links via pagination boundaries");
        if (Objects.nonNull(filter.getCategoryId())) {
            documentRolePermissionService.requirePermission(DocumentPermission.READ, filter.getCategoryId());
        }
        return appLinkRepository.findAll(filter, documentRolePermissionService.getReadableCategoryIds(), pageable).map(appLinkMapper::toResponse);
    }

    /**
     * Creates a link of an application.
     *
     * @param inputDTO the data of the link
     * @return the created link
     * @throws BusinessRuleException if the application or the category doesn't exist or isn't an
     *                               active category, or the application already has an active link with the same title, category and address
     */
    @Override
    public AppDocumentationLinkOutputDTO create(AppDocumentationLinkInputDTO inputDTO) {
        log.debug("Facade: Creating new application link with title: {}", inputDTO.getTitle());
        documentRolePermissionService.requirePermission(DocumentPermission.UPLOAD, inputDTO.getCategoryId());

        Utils.sanitize(inputDTO);

        if (Objects.isNull(applicationRepository.findById(inputDTO.getApplicationId()))) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_FOUND);
        }
        validateCategory(inputDTO.getCategoryId(), null);
        if (appLinkRepository.existsActiveDuplicate(inputDTO.getApplicationId(), inputDTO.getTitle(), inputDTO.getCategoryId(), inputDTO.getUrl())) {
            throw new BusinessRuleException(Constants.ERR_LINK_DUPLICATED);
        }

        return appLinkMapper.toResponse(appLinkRepository.create(appLinkMapper.toModelFromInput(inputDTO)));
    }

    /**
     * Updates an active link. The application it belongs to never changes.
     *
     * @param id       the identifier of the link
     * @param inputDTO the new data
     * @return the updated link
     * @throws BusinessRuleException if the link doesn't exist or is deleted, the new category
     *                               doesn't exist or isn't an active category, or another active link of the application already has the same title, category and address
     * @throws AccessDeniedException if the user can't edit the
     *                                                                   links of the current category, or of the new one
     */
    @Override
    public AppDocumentationLinkOutputDTO update(Long id, AppDocumentationLinkUpdateInputDTO inputDTO) {
        log.debug("Facade: Updating application link with ID: {}", id);

        Utils.sanitize(inputDTO);

        AppLink existing = findActiveById(id);
        documentRolePermissionService.requirePermission(DocumentPermission.EDIT, existing.getCategory().getId());
        if (!Objects.equals(inputDTO.getCategoryId(), existing.getCategory().getId())) {
            documentRolePermissionService.requirePermission(DocumentPermission.EDIT, inputDTO.getCategoryId());
        }
        validateCategory(inputDTO.getCategoryId(), existing.getCategory().getId());
        if (appLinkRepository.existsActiveDuplicateExcluding(existing.getApplicationId(), inputDTO.getTitle(), inputDTO.getCategoryId(), inputDTO.getUrl(), id)) {
            throw new BusinessRuleException(Constants.ERR_LINK_DUPLICATED);
        }

        appLinkMapper.updateModelFromInput(inputDTO, existing);
        return appLinkMapper.toResponse(appLinkRepository.update(existing, id));
    }

    /**
     * Logically deletes a link by setting its deletion timestamp and author.
     *
     * @param id the identifier of the link
     * @throws BusinessRuleException if the link doesn't exist or is already deleted
     */
    @Override
    public void delete(Long id) {
        log.debug("Facade: Logically deleting application link with ID: {}", id);
        AppLink existing = findActiveById(id);
        documentRolePermissionService.requirePermission(DocumentPermission.DELETE, existing.getCategory().getId());

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());
        appLinkRepository.delete(existing);
    }

    /**
     * Finds a link that is still active.
     *
     * @param id the identifier of the link
     * @return the link
     * @throws BusinessRuleException if the link doesn't exist or is deleted
     */
    private AppLink findActiveById(Long id) {
        AppLink appLink = appLinkRepository.findById(id);
        if (Objects.isNull(appLink)) {
            throw new BusinessRuleException(Constants.ERR_LINK_NOT_FOUND);
        }
        if (Objects.nonNull(appLink.getDeletedAt())) {
            throw new BusinessRuleException(Constants.ERR_LINK_NOT_ACTIVE);
        }
        return appLink;
    }

    /**
     * Checks that the category exists, is a category and not a subcategory, and is active. An
     * unchanged category isn't checked again, so a link keeps working when its category is deleted later.
     *
     * @param categoryId        identifier of the category
     * @param currentCategoryId identifier of the category the link already has, {@code null} when creating
     * @throws BusinessRuleException if any of those conditions fails
     */
    private void validateCategory(Long categoryId, Long currentCategoryId) {
        if (Objects.equals(categoryId, currentCategoryId)) {
            return;
        }
        DocumentCategory category = documentCategoryRepository.findById(categoryId);
        if (Objects.isNull(category)) {
            throw new BusinessRuleException(Constants.ERR_LINK_CATEGORY_NOT_FOUND);
        }
        if (Objects.nonNull(category.getDeletedAt()) || !category.isCategory()) {
            throw new BusinessRuleException(Constants.ERR_LINK_CATEGORY_NOT_ACTIVE);
        }
    }
}
