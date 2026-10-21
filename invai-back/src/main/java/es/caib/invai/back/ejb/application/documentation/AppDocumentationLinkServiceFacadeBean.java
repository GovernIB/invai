package es.caib.invai.back.ejb.application.documentation;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkInputDTO;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkOutputDTO;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkUpdateInputDTO;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.persistence.repository.application.documentation.link.AppDocumentationLinkCriteria;
import es.caib.invai.back.persistence.repository.application.documentation.link.AppLinkRepository;
import es.caib.invai.back.persistence.repository.catalog.appSection.AppSectionRepository;
import es.caib.invai.back.service.facade.application.documentation.link.AppDocumentationLinkService;
import es.caib.invai.back.service.mapper.application.documentation.link.AppLinkMapper;
import es.caib.invai.back.service.model.application.documentation.link.AppLink;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Facade service implementation for the links of applications (the "Enllaços" list of the
 * Documentació tab). A link can't be repeated: an application has at most one active link with the
 * same title, section and address.
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

    /** Repository port used to check that the section of a link exists. */
    @Autowired
    private AppSectionRepository appSectionRepository;

    /**
     * Retrieves a paginated list of the links of an application matching the given filter
     * criteria. Deleted links are listed too, told apart by their deletion timestamp, unless the
     * filter excludes them.
     *
     * @param filter   filter criteria, scoped by application
     * @param pageable pagination and sorting information
     * @return a page of links as response DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AppDocumentationLinkOutputDTO> getAll(AppDocumentationLinkCriteria filter, Pageable pageable) {
        log.debug("Facade: Fetching application links via pagination boundaries");
        return appLinkRepository.findAll(filter, pageable).map(appLinkMapper::toResponse);
    }

    /**
     * Creates a link of an application.
     *
     * @param inputDTO the data of the link
     * @return the created link
     * @throws BusinessRuleException if the application or the section doesn't exist, or the
     *                               application already has an active link with the same title, section and address
     */
    @Override
    public AppDocumentationLinkOutputDTO create(AppDocumentationLinkInputDTO inputDTO) {
        log.debug("Facade: Creating new application link with title: {}", inputDTO.getTitle());

        Utils.sanitize(inputDTO);

        if (Objects.isNull(applicationRepository.findById(inputDTO.getApplicationId()))) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_FOUND);
        }
        validateSection(inputDTO.getSectionId());
        if (appLinkRepository.existsActiveDuplicate(inputDTO.getApplicationId(), inputDTO.getTitle(), inputDTO.getSectionId(), inputDTO.getUrl())) {
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
     * @throws BusinessRuleException if the link doesn't exist or is deleted, the section doesn't
     *                               exist, or another active link of the application already has the same title, section and address
     */
    @Override
    public AppDocumentationLinkOutputDTO update(Long id, AppDocumentationLinkUpdateInputDTO inputDTO) {
        log.debug("Facade: Updating application link with ID: {}", id);

        Utils.sanitize(inputDTO);

        AppLink existing = findActiveById(id);
        validateSection(inputDTO.getSectionId());
        if (appLinkRepository.existsActiveDuplicateExcluding(existing.getApplicationId(), inputDTO.getTitle(), inputDTO.getSectionId(), inputDTO.getUrl(), id)) {
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
     * Checks that the section exists.
     *
     * @param sectionId identifier of the section
     * @throws BusinessRuleException if the section doesn't exist
     */
    private void validateSection(Long sectionId) {
        if (Objects.isNull(appSectionRepository.findById(sectionId))) {
            throw new BusinessRuleException(Constants.ERR_LINK_SECTION_NOT_FOUND);
        }
    }
}
