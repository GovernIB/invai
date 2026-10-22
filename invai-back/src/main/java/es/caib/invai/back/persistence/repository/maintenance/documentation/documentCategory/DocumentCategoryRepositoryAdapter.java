package es.caib.invai.back.persistence.repository.maintenance.documentation.documentCategory;

import es.caib.invai.back.persistence.model.maintenance.documentation.documentCategory.DocumentCategoryAudEntity;
import es.caib.invai.back.persistence.model.maintenance.documentation.documentCategory.DocumentCategoryEntity;
import es.caib.invai.back.service.mapper.maintenance.documentation.documentCategory.DocumentCategoryMapper;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import es.caib.invai.back.service.model.maintenance.documentation.documentCategory.DocumentCategory;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Objects;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link DocumentCategoryRepository}.
 *
 * @since 1.0.6
 */
@Repository
@Slf4j
public class DocumentCategoryRepositoryAdapter implements DocumentCategoryRepository {

    /** Spring Data JPA repository used to access live {@link DocumentCategoryEntity} records. */
    @Autowired
    private DocumentCategoryJPARepository documentCategoryJPARepository;

    /** Spring Data JPA repository used to persist {@link DocumentCategoryAudEntity} audit snapshots. */
    @Autowired
    private DocumentCategoryAudJPARepository documentCategoryAudJPARepository;

    /** Mapper used to convert between documentation category domain models and JPA entities. */
    @Autowired
    private DocumentCategoryMapper documentCategoryMapper;

    @Override
    public DocumentCategory findById(Long id) {
        DocumentCategory documentCategory = documentCategoryJPARepository.findById(id)
                .map(documentCategoryMapper::toModel)
                .orElse(null);
        if (Objects.nonNull(documentCategory) && documentCategory.isCategory()) {
            attachSubcategories(List.of(documentCategory), null);
        }
        return documentCategory;
    }

    @Override
    public Page<DocumentCategory> findAll(DocumentCategoryCriteria filter, Pageable pageable) {
        Specification<DocumentCategoryEntity> spec = DocumentCategorySpecification.filterByCriteria(filter);
        Page<DocumentCategory> page = documentCategoryJPARepository.findAll(spec, pageable).map(documentCategoryMapper::toModel);
        attachSubcategories(page.getContent(), filter);
        return page;
    }

    @Override
    public boolean existsActiveWithName(String name, Long parentId, Long excludeId) {
        if (Objects.isNull(parentId)) {
            return Objects.isNull(excludeId)
                    ? documentCategoryJPARepository.existsByNameAndParentIsNullAndDeletedAtIsNull(name)
                    : documentCategoryJPARepository.existsByNameAndParentIsNullAndIdNotAndDeletedAtIsNull(name, excludeId);
        }
        return Objects.isNull(excludeId)
                ? documentCategoryJPARepository.existsByNameAndParentIdAndDeletedAtIsNull(name, parentId)
                : documentCategoryJPARepository.existsByNameAndParentIdAndIdNotAndDeletedAtIsNull(name, parentId, excludeId);
    }

    @Override
    public boolean existsActiveWithNameEs(String nameEs, Long parentId, Long excludeId) {
        if (Objects.isNull(parentId)) {
            return Objects.isNull(excludeId)
                    ? documentCategoryJPARepository.existsByNameEsAndParentIsNullAndDeletedAtIsNull(nameEs)
                    : documentCategoryJPARepository.existsByNameEsAndParentIsNullAndIdNotAndDeletedAtIsNull(nameEs, excludeId);
        }
        return Objects.isNull(excludeId)
                ? documentCategoryJPARepository.existsByNameEsAndParentIdAndDeletedAtIsNull(nameEs, parentId)
                : documentCategoryJPARepository.existsByNameEsAndParentIdAndIdNotAndDeletedAtIsNull(nameEs, parentId, excludeId);
    }

    @Override
    public boolean hasActiveSubcategories(Long categoryId) {
        return documentCategoryJPARepository.existsByParentIdAndDeletedAtIsNull(categoryId);
    }

    @Override
    public DocumentCategory create(DocumentCategory documentCategory) {
        log.debug("Repository: Persisting new documentation category entity into database");
        DocumentCategoryEntity entity = documentCategoryMapper.toEntity(documentCategory);
        entity = documentCategoryJPARepository.save(entity);
        saveAuditRecord(entity, "INSERT");
        return documentCategoryMapper.toModel(entity);
    }

    @Override
    public DocumentCategory update(DocumentCategory documentCategory, Long id) {
        log.debug("Repository: Merging changes into existing documentation category record for ID: {}", id);
        DocumentCategoryEntity entity = documentCategoryMapper.toEntity(documentCategory);
        entity.setId(id);
        entity = documentCategoryJPARepository.save(entity);
        saveAuditRecord(entity, "UPDATE");
        return documentCategoryMapper.toModel(entity);
    }

    @Override
    public void delete(DocumentCategory documentCategory) {
        log.debug("Repository: Soft deleting documentation category entity with ID: {}", documentCategory.getId());
        DocumentCategoryEntity entity = documentCategoryMapper.toEntity(documentCategory);
        entity.setId(documentCategory.getId());
        entity = documentCategoryJPARepository.save(entity);
        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Loads, with a single query, the subcategories (sorted by name) of the given categories and
     * sets on each category the ones that fulfil the filter: the status applies to every
     * subcategory, and the text keeps a subcategory only if it matches it, unless the category itself
     * matches, in which case all its subcategories are kept.
     *
     * @param categories categories to fill in the subcategories of
     * @param filter     filter the subcategories must fulfil, {@code null} to keep all of them
     */
    private void attachSubcategories(List<DocumentCategory> categories, DocumentCategoryCriteria filter) {
        if (categories.isEmpty()) {
            return;
        }
        List<Long> categoryIds = categories.stream().map(DocumentCategory::getId).toList();
        Map<Long, List<DocumentCategory>> byParent = documentCategoryJPARepository.findAllByParentIdInOrderByNameAsc(categoryIds).stream()
                .map(documentCategoryMapper::toModel)
                .collect(Collectors.groupingBy(DocumentCategory::getParentId));
        categories.forEach(category -> category.setSubcategories(byParent.getOrDefault(category.getId(), List.of()).stream()
                .filter(subcategory -> fulfilsFilter(category, subcategory, filter))
                .toList()));
    }

    /**
     * Tells whether a subcategory has to be shown under its category for the given filter.
     *
     * @param category    the category the subcategory belongs to
     * @param subcategory the subcategory to check
     * @param filter      the filter to fulfil, may be {@code null}
     * @return {@code true} if the subcategory fulfils the status and every text of the filter
     */
    private static boolean fulfilsFilter(DocumentCategory category, DocumentCategory subcategory, DocumentCategoryCriteria filter) {
        if (Objects.isNull(filter)) {
            return true;
        }
        if (Objects.nonNull(filter.getStatusId())) {
            boolean active = StatusEnum.ACTIVE.getId().equals(filter.getStatusId());
            if (active != Objects.isNull(subcategory.getDeletedAt())) {
                return false;
            }
        }
        return Stream.of(filter.getName(), filter.getSearch())
                .filter(StringUtils::isNotBlank)
                .allMatch(text -> containsName(category, text) || containsName(subcategory, text));
    }

    /**
     * Tells whether the Catalan or Spanish name of a category or subcategory contains the text,
     * ignoring case.
     *
     * @param category the category or subcategory to check
     * @param text     the text to look for
     * @return {@code true} if either name contains the text
     */
    private static boolean containsName(DocumentCategory category, String text) {
        return StringUtils.containsIgnoreCase(category.getName(), text) || StringUtils.containsIgnoreCase(category.getNameEs(), text);
    }

    /**
     * Builds and persists an audit snapshot capturing the current state of the given entity.
     *
     * @param entity the documentation category entity state to snapshot
     * @param action the type of mutation being audited (e.g. {@code INSERT}, {@code UPDATE}, {@code DELETE})
     */
    private void saveAuditRecord(DocumentCategoryEntity entity, String action) {
        DocumentCategoryAudEntity aud = new DocumentCategoryAudEntity();
        aud.setDocumentCategoryId(entity.getId());
        aud.setName(entity.getName());
        aud.setNameEs(entity.getNameEs());
        aud.setParentId(Objects.nonNull(entity.getParent()) ? entity.getParent().getId() : null);
        aud.setCreatedAt(Objects.nonNull(entity.getCreatedAt()) ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(Objects.nonNull(entity.getCreatedBy()) ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());
        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());
        documentCategoryAudJPARepository.save(aud);
    }
}
