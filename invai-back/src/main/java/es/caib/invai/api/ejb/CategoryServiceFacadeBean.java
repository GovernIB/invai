package es.caib.invai.api.ejb;

import es.caib.invai.api.interna.category.DTO.CategoryInputDTO;
import es.caib.invai.api.interna.category.DTO.CategoryOutputDTO;
import es.caib.invai.api.utils.SecurityUtils;
import es.caib.invai.api.service.mapper.CategoryMapper;
import es.caib.invai.api.persistence.repository.category.CategoryRepository;
import es.caib.invai.api.persistence.repository.application.ApplicationRepository;
import es.caib.invai.api.service.facade.CategoryService;
import es.caib.invai.api.service.model.Category;
import es.caib.invai.api.exception.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Facade service implementation for administrative taxonomy Categories.
 * Supports basic CRUD mechanics and soft deletes.
 *
 * @since 1.0.0
 */
@Service
@Slf4j
@Transactional
public class CategoryServiceFacadeBean implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    /**
     * Resolves a Category object configuration based on its primary key.
     *
     * @param id target primary identifier
     * @return mapped element properties as {@link CategoryOutputDTO}
     * @throws BusinessRuleException if target key entries are not present within records
     */
    @Override
    @Transactional(readOnly = true)
    public CategoryOutputDTO getById(Long id) {
        log.info("Facade: Fetching category by id: {}", id);
        Category category = categoryRepository.findById(id);
        if (category == null) {
            throw new BusinessRuleException("exception.category.notfound");
        }
        return categoryMapper.toResponse(category);
    }

    /**
     * Evaluates a paginated sequence collection across available instances.
     *
     * @param pageable sorting configuration boundaries
     * @return partitioned items mapping to response shapes
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CategoryOutputDTO> getAll(Pageable pageable) {
        log.info("Facade: Fetching paged categories");
        return categoryRepository.findAll(pageable).map(categoryMapper::toResponse);
    }

    /**
     * Persists new category taxonomy instances after confirming label descriptor uniqueness rules.
     *
     * @param inputDTO parameters framing item details
     * @return operational object snapshot data structures
     * @throws BusinessRuleException if name metrics already map to active elements
     */
    @Override
    public CategoryOutputDTO create(CategoryInputDTO inputDTO) {
        log.info("Facade: Creating category with name: {}", inputDTO.getName());

        if (categoryRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException("exception.category.duplicated");
        }

        Category model = categoryMapper.toModelFromInput(inputDTO);
        Category savedModel = categoryRepository.create(model);
        return categoryMapper.toResponse(savedModel);
    }

    /**
     * Alters properties on active configuration datasets.
     *
     * @param id       identity values index tracking structural items
     * @param inputDTO modified context objects mapping payloads
     * @return structural outputs reflecting new state properties
     * @throws BusinessRuleException if the element is not found or name changes overwrite independent entries
     */
    @Override
    public CategoryOutputDTO update(Long id, CategoryInputDTO inputDTO) {
        log.info("Facade: Updating category ID: {}", id);

        Category existing = categoryRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException("exception.category.notfound");
        }

        if (categoryRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException("exception.category.duplicated");
        }

        categoryMapper.updateModelFromInput(inputDTO, existing);
        Category updatedModel = categoryRepository.update(existing, id);
        return categoryMapper.toResponse(updatedModel);
    }

    /**
     * Finalizes execution pathways logic scopes routing parameter assets towards data deletion states.
     * Validates domain constraints by checking cross-context dependencies against the application port.
     *
     * @param id row metadata index to update
     * @throws BusinessRuleException if matching data criteria is absent or target configuration profile
     * remains actively bound to active application assets
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Executing logical delete for category ID: {}", id);
        Category existing = categoryRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException("exception.category.notfound");
        }

        if (applicationRepository.existsByCategoryId(id)) {
            throw new BusinessRuleException("{exception.category.delete.hasdependencies}");
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy("SYSTEM");

        OidcUserInfo currentUser = (OidcUserInfo) SecurityUtils.getCurrentUser();
        if (currentUser != null && currentUser.getClaims().get("preferred_username") != null) {
            existing.setDeletedBy((String) currentUser.getClaims().get("preferred_username"));
        }

        categoryRepository.delete(existing);
    }
}