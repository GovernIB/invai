package es.caib.invai.back.ejb.maintenance.general.category;

import es.caib.invai.back.interna.maintenance.general.category.DTO.CategoryInputDTO;
import es.caib.invai.back.interna.maintenance.general.category.DTO.CategoryOutputDTO;
import es.caib.invai.back.service.mapper.maintenance.general.category.CategoryMapper;
import es.caib.invai.back.persistence.repository.maintenance.general.category.CategoryCriteria;
import es.caib.invai.back.persistence.repository.maintenance.general.category.CategoryRepository;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.service.facade.maintenance.general.category.CategoryService;
import es.caib.invai.back.service.model.maintenance.general.category.Category;
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
 * Facade service implementation for administrative taxonomy Categories.
 * Handles structural length checks, status assignments, and filtering via standard pagination rules.
 *
 * @since 1.0.1
 */
@Service
@Slf4j
@Transactional
public class CategoryServiceFacadeBean implements CategoryService {

    /** Mapper converting between Category domain models, persistence entities, and API DTOs. */
    @Autowired
    private CategoryMapper categoryMapper;

    /** Outbound repository port used to persist and query Category domain models. */
    @Autowired
    private CategoryRepository categoryRepository;

    /** Repository used to check whether categories are referenced by active application records before deletion. */
    @Autowired
    private ApplicationRepository applicationRepository;

    /**
     * Retrieves an active category configuration by its unique database identifier.
     * Evaluates logical deletion properties and structural lifecycle status flags.
     *
     * @param id the unique category metadata record identity pointer
     * @return the mapped {@link CategoryOutputDTO} response presentation payload
     * @throws BusinessRuleException if the identity does not match any active record or has been logically soft-deleted
     */
    @Override
    @Transactional(readOnly = true)
    public CategoryOutputDTO getById(Long id) {
        log.info("Facade: Fetching category by ID: {}", id);
        Category category = categoryRepository.findById(id);

        if (category == null) {
            throw new BusinessRuleException(Constants.ERR_CATEGORY_NOT_FOUND);
        }

        return categoryMapper.toResponse(category);
    }

    /**
     * Gets a paginated distribution framework containing category records matching pagination rules.
     *
     * @param filter   dynamic search criteria used to build the query specification
     * @param pageable sorting parameters and tracking page metadata pagination constraints
     * @return a structured page element populated with converted {@link CategoryOutputDTO} results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CategoryOutputDTO> getAll(CategoryCriteria filter, Pageable pageable) {
        log.info("Facade: Fetching categories via pagination boundaries");
        Page<Category> domainPage = categoryRepository.findAll(filter, pageable);
        return domainPage.map(categoryMapper::toResponse);
    }

    /**
     * Validates structural constraints and registers a new category record within the core.
     * Enforces domain text sanitization and unicity rules regarding the taxonomy name descriptor.
     *
     * @param inputDTO data transfer container holding properties describing the target category record
     * @return the resulting persistent instance transformed into an {@link CategoryOutputDTO} structure
     * @throws BusinessRuleException if text formats fail physical bounds, or if the name descriptor
     * conflicts with an already registered category taxonomy entry
     */
    @Override
    public CategoryOutputDTO create(CategoryInputDTO inputDTO) {
        log.info("Facade: Creating new category record with name: {}", inputDTO.getName());

        Utils.sanitize(inputDTO);

        if (categoryRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException(Constants.ERR_CATEGORY_DUPLICATED);
        }

        Category model = categoryMapper.toModelFromInput(inputDTO);

        Category savedModel = categoryRepository.create(model);
        return categoryMapper.toResponse(savedModel);
    }

    /**
     * Mutates an existing active category entity property by replacing metrics with input payload details.
     * Ensures updates do not overlap unique constraint parameters allocated to sibling records.
     *
     * @param id       the unique database resource key indexing the record targeting modification
     * @param inputDTO data update container outlining property changes intended for persistence merge operations
     * @return the modified domain representation mapped down into an {@link CategoryOutputDTO}
     * @throws BusinessRuleException if the resource key is non-existent, has been marked soft-deleted,
     * or if input data maps identifier fields owned by another category
     */
    @Override
    public CategoryOutputDTO update(Long id, CategoryInputDTO inputDTO) {
        log.info("Facade: Updating category with ID: {}", id);

        Category existing = categoryRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_CATEGORY_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (categoryRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException(Constants.ERR_CATEGORY_DUPLICATED);
        }

        categoryMapper.updateModelFromInput(inputDTO, existing);
        return categoryMapper.toResponse(categoryRepository.update(existing, id));
    }

    /**
     * Executes a logical soft-delete transaction lifecycle phase over a category record.
     * Shifts state configurations to inactive indicators and logs audit metrics profiling execution time and session user.
     *
     * @param id the target identifier mapping the category instance intended for deactivation
     * @throws BusinessRuleException if matching category instance descriptions cannot be found, are already soft-deleted,
     * or remain actively bound to active application assets
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting category with ID: {}", id);
        Category existing = categoryRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_CATEGORY_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_CATEGORY_NOT_ACTIVE);
        }

        if (applicationRepository.existsByCategoryId(id)) {
            throw new BusinessRuleException(Constants.ERR_CATEGORY_DELETE_HAS_DEPENDENCIES);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        categoryRepository.delete(existing);
    }

    /**
     * Reactivates a logically soft-deleted category record back to active state.
     *
     * @param id the target identifier mapping the category instance intended for reactivation
     * @return the reactivated domain representation mapped into an {@link CategoryOutputDTO}
     * @throws BusinessRuleException if matching category cannot be found or is already active
     */
    @Override
    public CategoryOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating category with ID: {}", id);
        Category existing = categoryRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_CATEGORY_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_CATEGORY_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        Category updatedModel = categoryRepository.update(existing, id);
        return categoryMapper.toResponse(updatedModel);
    }
}