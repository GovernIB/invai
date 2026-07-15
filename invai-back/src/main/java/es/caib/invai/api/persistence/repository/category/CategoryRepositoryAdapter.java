package es.caib.invai.api.persistence.repository.category;

import es.caib.invai.api.service.model.Category;
import es.caib.invai.api.persistence.model.CategoryEntity;
import es.caib.invai.api.persistence.model.CategoryAudEntity;
import es.caib.invai.api.service.mapper.CategoryMapper;
import es.caib.invai.api.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link CategoryRepository}.
 * <p>
 * Orchestrates technical structural operations, routing operations across active database layers
 * ({@link CategoryJPARepository} and {@link CategoryAudJPARepository}) while utilizing
 * mapping layers to enforce decoupling rules.
 * </p>
 *
 * @since 1.0.0
 */
@Repository
@Slf4j
public class CategoryRepositoryAdapter implements CategoryRepository {

    @Autowired
    private CategoryJPARepository categoryJPARepository;

    @Autowired
    private CategoryAudJPARepository categoryAudJPARepository;

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * Resolves a category record by its unique key, filtering out soft-deleted data traces.
     *
     * @param id unique sequence row identifier tracking the asset
     * @return the mapped domain {@link Category} instance, or {@code null} if missing or soft-deleted
     */
    @Override
    public Category findById(Long id) {
        return categoryJPARepository.findById(id)
                .filter(f -> f.getDeletedAt() == null)
                .map(categoryMapper::toModel)
                .orElse(null);
    }

    /**
     * Fetches all active asset taxonomy category classifications from the database and maps them to domain models.
     *
     * @param pageable pagination and sorting parameters
     * @return a page containing the mapped domain objects
     */
    @Override
    public Page<Category> findAll(Pageable pageable) {
        return categoryJPARepository.findAllActive(pageable).map(categoryMapper::toModel);
    }

    /**
     * Delegates uniqueness validation based on category labels to the active JPA layer.
     *
     * @param name target descriptive name text to verify
     * @return {@code true} if a match exists, {@code false} otherwise
     */
    @Override
    public boolean existsByNameAndDeletedAtIsNull(String name) {
        return categoryJPARepository.existsByNameAndDeletedAtIsNull(name);
    }

    /**
     * Delegates update-specific label conflict validation queries to the underlying JPA layer.
     *
     * @param name target descriptive name text to verify
     * @param id   the primary identifier sequence key to exclude
     * @return {@code true} if a duplicate collision occurs, {@code false} otherwise
     */
    @Override
    public boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id) {
        return categoryJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
    }

    /**
     * Maps a transient domain taxonomy object, persists it into relational systems,
     * registers historical snapshots, and returns a business context data schema model.
     *
     * @param category the data schema model profile values containing registration variables
     * @return persistent domain profile containing data markers
     */
    @Override
    public Category create(Category category) {
        log.info("Repository: Persisting new category entity into database");
        CategoryEntity entity = categoryMapper.toEntity(category);
        entity = categoryJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");

        return categoryMapper.toModel(entity);
    }

    /**
     * Maps updated category configurations onto relational records, commits changes to active data rows,
     * appends audit trails, and returns fresh outcomes.
     *
     * @param category property modifications schema containing target update variables data
     * @param id       persistent identity identifier index tracking state records
     * @return updated model domain properties specifications variables
     */
    @Override
    public Category update(Category category, Long id) {
        log.info("Repository: Merging changes into existing category record for ID: {}", id);
        CategoryEntity entity = categoryMapper.toEntity(category);
        entity.setId(id);
        entity = categoryJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");

        return categoryMapper.toModel(entity);
    }

    /**
     * Performs a soft logical deactivation by rewriting active category entities,
     * flagging timeline delete records, and saving tracking logs.
     *
     * @param category target domain representation configuration modeling details to remove
     */
    @Override
    public void delete(Category category) {
        log.info("Repository: Soft deleting category entity with ID: {}", category.getId());
        CategoryEntity entity = categoryMapper.toEntity(category);
        entity.setId(category.getId());
        entity = categoryJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Compiles point-in-time snapshot mirror values from active category entries,
     * resolves identity user info claims tokens, and saves historic compliance tracks.
     *
     * @param entity the currently tracked persistent data state row entity map representation
     * @param action systemic string token descriptor identifying database mutation contexts (e.g., INSERT, UPDATE, DELETE)
     */
    private void saveAuditRecord(CategoryEntity entity, String action) {
        CategoryAudEntity aud = new CategoryAudEntity();

        aud.setCategoryId(entity.getId());
        aud.setName(entity.getName());
        aud.setNameEs(entity.getNameEs());

        aud.setCreatedAt(entity.getCreatedAt());
        aud.setCreatedBy(entity.getCreatedBy());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());

        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());

        OidcUserInfo currentUser = (OidcUserInfo) SecurityUtils.getCurrentUser();
        if (currentUser != null && currentUser.getClaims().get("preferred_username") != null) {
            aud.setAuditUser((String) currentUser.getClaims().get("preferred_username"));
        } else {
            aud.setAuditUser("SYSTEM");
        }

        categoryAudJPARepository.save(aud);
    }
}