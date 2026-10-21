package es.caib.invai.back.ejb.maintenance.documentation.documentCategory;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.documentation.category.DTO.DocumentCategoryInputDTO;
import es.caib.invai.back.interna.maintenance.documentation.category.DTO.DocumentCategoryOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.documentation.documentCategory.DocumentCategoryCriteria;
import es.caib.invai.back.persistence.repository.maintenance.documentation.documentCategory.DocumentCategoryRepository;
import es.caib.invai.back.service.facade.maintenance.documentation.documentCategory.DocumentCategoryService;
import es.caib.invai.back.service.mapper.maintenance.documentation.documentCategory.DocumentCategoryMapper;
import es.caib.invai.back.service.model.maintenance.documentation.documentCategory.DocumentCategory;
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
 * Facade service implementation for documentation categories and subcategories, which share the
 * same use cases: a record without a parent is a category, one with a parent is a subcategory.
 *
 * @since 1.0.6
 */
@Service
@Slf4j
@Transactional
public class DocumentCategoryServiceFacadeBean implements DocumentCategoryService {

    /**
     * Mapper used to convert between documentation category domain models, entities, and DTOs.
     */
    @Autowired
    private DocumentCategoryMapper documentCategoryMapper;

    /**
     * Repository port used to persist and query documentation category domain models.
     */
    @Autowired
    private DocumentCategoryRepository documentCategoryRepository;

    /**
     * Retrieves a category or subcategory by its identifier.
     *
     * @param id the identifier
     * @return the matching record as a response DTO
     * @throws BusinessRuleException if no record exists with the given ID
     */
    @Override
    @Transactional(readOnly = true)
    public DocumentCategoryOutputDTO getById(Long id) {
        log.debug("Facade: Fetching documentation category by ID: {}", id);
        DocumentCategory documentCategory = documentCategoryRepository.findById(id);

        if (Objects.isNull(documentCategory)) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTCATEGORY_NOT_FOUND);
        }

        return documentCategoryMapper.toResponse(documentCategory);
    }

    /**
     * Retrieves a paginated list of the categories matching the given filter criteria, each one
     * with all its subcategories.
     *
     * @param filter   search and status filter criteria
     * @param pageable pagination and sorting information
     * @return a page of matching categories as response DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public Page<DocumentCategoryOutputDTO> getAll(DocumentCategoryCriteria filter, Pageable pageable) {
        log.debug("Facade: Fetching documentation categories via pagination boundaries");
        return documentCategoryRepository.findAll(filter, pageable).map(documentCategoryMapper::toResponse);
    }

    /**
     * Creates a new category (no {@code parentId}) or subcategory (the {@code parentId} of an
     * active category) after sanitizing the input and checking for name duplicates.
     *
     * @param inputDTO the data to create
     * @return the created record as a response DTO
     * @throws BusinessRuleException if the parent doesn't exist, isn't active or is itself a
     *                               subcategory, or an active record with the same name (Catalan or Spanish) already exists in the same level
     */
    @Override
    public DocumentCategoryOutputDTO create(DocumentCategoryInputDTO inputDTO) {
        log.debug("Facade: Creating new documentation category record with name: {}", inputDTO.getName());

        Utils.sanitize(inputDTO);

        if (Objects.nonNull(inputDTO.getParentId())) {
            validateParent(inputDTO.getParentId());
        }

        validateNames(inputDTO.getName(), inputDTO.getNameEs(), inputDTO.getParentId(), null);

        DocumentCategory model = documentCategoryMapper.toModelFromInput(inputDTO);
        return documentCategoryMapper.toResponse(documentCategoryRepository.create(model));
    }

    /**
     * Updates an existing, active category or subcategory with the given input data. A category
     * stays a category and a subcategory stays a subcategory, though it can move to another
     * active category.
     *
     * @param id       the identifier of the record to update
     * @param inputDTO the new data
     * @return the updated record as a response DTO
     * @throws BusinessRuleException if the record doesn't exist or is deleted, would change from
     *                               category to subcategory or the other way round, has an invalid new parent, or the new name
     *                               is already used by another active record in the same level
     */
    @Override
    public DocumentCategoryOutputDTO update(Long id, DocumentCategoryInputDTO inputDTO) {
        log.debug("Facade: Updating documentation category with ID: {}", id);

        DocumentCategory existing = documentCategoryRepository.findById(id);
        if (Objects.isNull(existing)) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTCATEGORY_NOT_FOUND);
        }
        if (Objects.nonNull(existing.getDeletedAt())) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTCATEGORY_NOT_ACTIVE);
        }

        Utils.sanitize(inputDTO);

        if (Objects.isNull(existing.getParentId()) != Objects.isNull(inputDTO.getParentId())) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTCATEGORY_TYPE_CHANGE_NOT_ALLOWED);
        }
        if (Objects.nonNull(existing.getParentId()) && !Objects.equals(existing.getParentId(), inputDTO.getParentId())) {
            validateParent(inputDTO.getParentId());
        }

        validateNames(inputDTO.getName(), inputDTO.getNameEs(), inputDTO.getParentId(), id);

        documentCategoryMapper.updateModelFromInput(inputDTO, existing);
        return documentCategoryMapper.toResponse(documentCategoryRepository.update(existing, id));
    }

    /**
     * Logically deletes a category or subcategory by setting its deletion timestamp and author.
     *
     * @param id the identifier of the record to delete
     * @throws BusinessRuleException if the record doesn't exist, is already deleted, or is a
     *                               category that still has active subcategories
     */
    @Override
    public void delete(Long id) {
        log.debug("Facade: Logically deleting documentation category with ID: {}", id);
        DocumentCategory existing = documentCategoryRepository.findById(id);

        if (Objects.isNull(existing)) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTCATEGORY_NOT_FOUND);
        }
        if (Objects.nonNull(existing.getDeletedAt())) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTCATEGORY_NOT_ACTIVE);
        }
        if (existing.isCategory() && documentCategoryRepository.hasActiveSubcategories(id)) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTCATEGORY_HAS_ACTIVE_SUBCATEGORIES);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        documentCategoryRepository.delete(existing);
    }

    /**
     * Reactivates a previously deleted category or subcategory by clearing its deletion timestamp
     * and author.
     *
     * @param id the identifier of the record to reactivate
     * @return the reactivated record as a response DTO
     * @throws BusinessRuleException if the record doesn't exist or isn't deleted, is a
     *                               subcategory whose category isn't active, or an active record with the same name already
     *                               exists in the same level
     */
    @Override
    public DocumentCategoryOutputDTO reactivate(Long id) {
        log.debug("Facade: Reactivating documentation category with ID: {}", id);
        DocumentCategory existing = documentCategoryRepository.findById(id);

        if (Objects.isNull(existing)) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTCATEGORY_NOT_FOUND);
        }
        if (Objects.isNull(existing.getDeletedAt())) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTCATEGORY_ACTIVE);
        }
        if (!existing.isCategory()) {
            DocumentCategory parent = documentCategoryRepository.findById(existing.getParentId());
            if (Objects.isNull(parent) || Objects.nonNull(parent.getDeletedAt())) {
                throw new BusinessRuleException(Constants.ERR_DOCUMENTCATEGORY_PARENT_NOT_ACTIVE);
            }
        }
        validateNames(existing.getName(), existing.getNameEs(), existing.getParentId(), id);

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        return documentCategoryMapper.toResponse(documentCategoryRepository.update(existing, id));
    }

    /**
     * Checks that neither the Catalan nor the Spanish name is already used by another active record
     * of the same level: among the categories, or among the subcategories of the same category.
     *
     * @param name      the Catalan name to check
     * @param nameEs    the Spanish name to check
     * @param parentId  identifier of the category the names are scoped to, {@code null} for the categories themselves
     * @param excludeId identifier of the record being updated or reactivated, {@code null} when creating
     * @throws BusinessRuleException if either name is already used
     */
    private void validateNames(String name, String nameEs, Long parentId, Long excludeId) {
        if (documentCategoryRepository.existsActiveWithName(name, parentId, excludeId)) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTCATEGORY_DUPLICATED);
        }
        if (documentCategoryRepository.existsActiveWithNameEs(nameEs, parentId, excludeId)) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTCATEGORY_NAME_ES_DUPLICATED);
        }
    }

    /**
     * Checks that a record can be the parent of a subcategory: it exists, is active and is itself
     * a category (the hierarchy has two levels at most).
     *
     * @param parentId identifier of the candidate parent
     * @throws BusinessRuleException if any of those conditions fails
     */
    private void validateParent(Long parentId) {
        DocumentCategory parent = documentCategoryRepository.findById(parentId);
        if (Objects.isNull(parent)) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTCATEGORY_PARENT_NOT_FOUND);
        }
        if (Objects.nonNull(parent.getDeletedAt())) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTCATEGORY_PARENT_NOT_ACTIVE);
        }
        if (!parent.isCategory()) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTCATEGORY_PARENT_NOT_CATEGORY);
        }
    }
}
