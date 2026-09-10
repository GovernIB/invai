package es.caib.invai.back.ejb.maintenance.security.webContext;

import es.caib.invai.back.interna.maintenance.security.webContext.DTO.WebContextInputDTO;
import es.caib.invai.back.interna.maintenance.security.webContext.DTO.WebContextOutputDTO;
import es.caib.invai.back.service.mapper.maintenance.security.webContext.WebContextMapper;
import es.caib.invai.back.persistence.repository.maintenance.security.webContext.WebContextCriteria;
import es.caib.invai.back.persistence.repository.maintenance.security.webContext.WebContextRepository;
import es.caib.invai.back.service.facade.maintenance.security.webContext.WebContextService;
import es.caib.invai.back.service.model.maintenance.security.webContext.WebContext;
import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Facade service implementation for administrative WebContexts.
 *
 * @since 1.0.4
 */
@Service
@Slf4j
@Transactional
public class WebContextServiceFacadeBean implements WebContextService {

    /** Mapper used to convert between WebContext domain models, entities and DTOs. */
    @Autowired
    private WebContextMapper webContextMapper;

    /** Repository port used to access WebContext persistence operations. */
    @Autowired
    private WebContextRepository webContextRepository;

    /**
     * Retrieves an web context by its identifier.
     *
     * @param id the web context identifier
     * @return the matching web context as an output DTO
     * @throws BusinessRuleException if no web context exists with the given ID
     */
    @Override
    @Transactional(readOnly = true)
    public WebContextOutputDTO getById(Long id) {
        log.debug("Facade: Fetching web context by ID: {}", id);
        WebContext webContext = webContextRepository.findById(id);

        if (webContext == null) {
            throw new BusinessRuleException(Constants.ERR_WEBCONTEXT_NOT_FOUND);
        }

        return webContextMapper.toResponse(webContext);
    }

    /**
     * Retrieves a paginated list of web contexts matching the given filter criteria.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching web contexts as output DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public Page<WebContextOutputDTO> getAll(WebContextCriteria filter, Pageable pageable) {
        log.debug("Facade: Fetching web contexts via pagination boundaries");
        Page<WebContext> domainPage = webContextRepository.findAll(filter, pageable);
        return domainPage.map(webContextMapper::toResponse);
    }

    /**
     * Creates a new web context after sanitizing the input and checking for name duplicates.
     *
     * @param inputDTO the data for the new web context
     * @return the created web context as an output DTO
     * @throws BusinessRuleException if an active web context with the same name already exists
     */
    @Override
    public WebContextOutputDTO create(WebContextInputDTO inputDTO) {
        log.info("Facade: Creating new web context record with name: {}", inputDTO.getName());

        Utils.sanitize(inputDTO);

        if (webContextRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException(Constants.ERR_WEBCONTEXT_DUPLICATED);
        }

        WebContext model = webContextMapper.toModelFromInput(inputDTO);

        WebContext savedModel = webContextRepository.create(model);
        return webContextMapper.toResponse(savedModel);
    }

    /**
     * Updates an existing web context, validating existence and name uniqueness.
     *
     * @param id the identifier of the web context to update
     * @param inputDTO the new data to apply
     * @return the updated web context as an output DTO
     * @throws BusinessRuleException if the web context does not exist or the name is already used by another record
     */
    @Override
    public WebContextOutputDTO update(Long id, WebContextInputDTO inputDTO) {
        log.info("Facade: Updating web context with ID: {}", id);

        WebContext existing = webContextRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_WEBCONTEXT_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (webContextRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException(Constants.ERR_WEBCONTEXT_DUPLICATED);
        }

        webContextMapper.updateModelFromInput(inputDTO, existing);
        return webContextMapper.toResponse(webContextRepository.update(existing, id));
    }

    /**
     * Logically deletes an web context by stamping its deletion timestamp and author.
     *
     * @param id the identifier of the web context to delete
     * @throws BusinessRuleException if the web context does not exist or is already deleted
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting web context with ID: {}", id);
        WebContext existing = webContextRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_WEBCONTEXT_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_WEBCONTEXT_NOT_ACTIVE);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        webContextRepository.delete(existing);
    }

    /**
     * Reactivates a previously deleted web context by clearing its deletion timestamp and author.
     *
     * @param id the identifier of the web context to reactivate
     * @return the reactivated web context as an output DTO
     * @throws BusinessRuleException if the web context does not exist or is already active
     */
    @Override
    public WebContextOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating web context with ID: {}", id);
        WebContext existing = webContextRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_WEBCONTEXT_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_WEBCONTEXT_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        WebContext updatedModel = webContextRepository.update(existing, id);
        return webContextMapper.toResponse(updatedModel);
    }
}
