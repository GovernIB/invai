package es.caib.invai.back.persistence.repository.maintenance.documentation.documentCategory;

import es.caib.invai.back.persistence.model.maintenance.documentation.documentCategory.DocumentCategoryAudEntity;
import es.caib.invai.back.persistence.model.maintenance.documentation.documentCategory.DocumentCategoryEntity;
import es.caib.invai.back.service.mapper.maintenance.documentation.documentCategory.DocumentCategoryMapper;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import es.caib.invai.back.service.model.maintenance.documentation.documentCategory.DocumentCategory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DocumentCategoryRepositoryAdapter}, verifying entity/model delegation to
 * the {@link DocumentCategoryJPARepository}, the loading of the subcategories of a category, and
 * the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class DocumentCategoryRepositoryAdapterTest {

    @Mock
    private DocumentCategoryJPARepository documentCategoryJPARepository;

    @Mock
    private DocumentCategoryAudJPARepository documentCategoryAudJPARepository;

    @Mock
    private DocumentCategoryMapper documentCategoryMapper;

    private DocumentCategoryRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DocumentCategoryRepositoryAdapter();
        ReflectionTestUtils.setField(adapter, "documentCategoryJPARepository", documentCategoryJPARepository);
        ReflectionTestUtils.setField(adapter, "documentCategoryAudJPARepository", documentCategoryAudJPARepository);
        ReflectionTestUtils.setField(adapter, "documentCategoryMapper", documentCategoryMapper);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static DocumentCategoryEntity entity(Long id, DocumentCategoryEntity parent) {
        DocumentCategoryEntity entity = new DocumentCategoryEntity();
        entity.setId(id);
        entity.setName("name-" + id);
        entity.setNameEs("nombre-" + id);
        entity.setParent(parent);
        return entity;
    }

    private static DocumentCategory model(Long id, Long parentId) {
        DocumentCategory model = new DocumentCategory();
        model.setId(id);
        model.setParentId(parentId);
        return model;
    }

    // ------------------------------------------------------------------
    // findById / findAll
    // ------------------------------------------------------------------

    @Test
    void findById_notFound_returnsNull() {
        when(documentCategoryJPARepository.findById(9L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(9L));
    }

    @Test
    void findById_category_comesWithItsSubcategories() {
        DocumentCategoryEntity categoryEntity = entity(1L, null);
        DocumentCategoryEntity childEntity = entity(2L, categoryEntity);
        DocumentCategory category = model(1L, null);
        DocumentCategory child = model(2L, 1L);
        when(documentCategoryJPARepository.findById(1L)).thenReturn(Optional.of(categoryEntity));
        when(documentCategoryMapper.toModel(categoryEntity)).thenReturn(category);
        when(documentCategoryJPARepository.findAllByParentIdInOrderByNameAsc(List.of(1L))).thenReturn(List.of(childEntity));
        when(documentCategoryMapper.toModel(childEntity)).thenReturn(child);

        DocumentCategory result = adapter.findById(1L);

        assertSame(category, result);
        assertEquals(List.of(child), result.getSubcategories());
    }

    @Test
    void findById_subcategory_doesNotLookForSubcategories() {
        DocumentCategoryEntity childEntity = entity(2L, entity(1L, null));
        DocumentCategory child = model(2L, 1L);
        when(documentCategoryJPARepository.findById(2L)).thenReturn(Optional.of(childEntity));
        when(documentCategoryMapper.toModel(childEntity)).thenReturn(child);

        DocumentCategory result = adapter.findById(2L);

        assertSame(child, result);
        assertTrue(result.getSubcategories().isEmpty());
        verify(documentCategoryJPARepository, never()).findAllByParentIdInOrderByNameAsc(anyCollection());
    }

    @Test
    void findAll_attachesToEachCategoryItsOwnSubcategoriesWithASingleQuery() {
        DocumentCategoryEntity firstEntity = entity(1L, null);
        DocumentCategoryEntity secondEntity = entity(5L, null);
        DocumentCategoryEntity childOfFirst = entity(2L, firstEntity);
        DocumentCategoryEntity anotherChildOfFirst = entity(3L, firstEntity);
        DocumentCategory first = model(1L, null);
        DocumentCategory second = model(5L, null);
        DocumentCategory child = model(2L, 1L);
        DocumentCategory anotherChild = model(3L, 1L);
        Pageable pageable = Pageable.unpaged();
        when(documentCategoryJPARepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(firstEntity, secondEntity)));
        when(documentCategoryMapper.toModel(firstEntity)).thenReturn(first);
        when(documentCategoryMapper.toModel(secondEntity)).thenReturn(second);
        when(documentCategoryJPARepository.findAllByParentIdInOrderByNameAsc(List.of(1L, 5L)))
                .thenReturn(List.of(childOfFirst, anotherChildOfFirst));
        when(documentCategoryMapper.toModel(childOfFirst)).thenReturn(child);
        when(documentCategoryMapper.toModel(anotherChildOfFirst)).thenReturn(anotherChild);

        Page<DocumentCategory> result = adapter.findAll(new DocumentCategoryCriteria(), pageable);

        assertEquals(List.of(child, anotherChild), first.getSubcategories());
        assertTrue(second.getSubcategories().isEmpty());
        assertEquals(2, result.getTotalElements());
        verify(documentCategoryJPARepository).findAllByParentIdInOrderByNameAsc(List.of(1L, 5L));
    }

    @Test
    void findAll_noCategories_doesNotLookForSubcategories() {
        when(documentCategoryJPARepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        Page<DocumentCategory> result = adapter.findAll(null, Pageable.unpaged());

        assertTrue(result.isEmpty());
        verify(documentCategoryJPARepository, never()).findAllByParentIdInOrderByNameAsc(anyCollection());
    }

    // ------------------------------------------------------------------
    // Filtering of the subcategories
    // ------------------------------------------------------------------

    private static DocumentCategory named(Long id, Long parentId, String name, String nameEs, boolean deleted) {
        DocumentCategory model = model(id, parentId);
        model.setName(name);
        model.setNameEs(nameEs);
        model.setDeletedAt(deleted ? LocalDateTime.of(2026, 10, 22, 10, 0) : null);
        return model;
    }

    /** Lists the page of the single category {@code category} whose subcategories are {@code children}. */
    private List<DocumentCategory> subcategoriesFound(DocumentCategoryCriteria criteria, DocumentCategory category, DocumentCategory... children) {
        DocumentCategoryEntity categoryEntity = entity(category.getId(), null);
        when(documentCategoryJPARepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(categoryEntity)));
        when(documentCategoryMapper.toModel(categoryEntity)).thenReturn(category);
        List<DocumentCategoryEntity> childEntities = new ArrayList<>();
        for (DocumentCategory child : children) {
            DocumentCategoryEntity childEntity = entity(child.getId(), categoryEntity);
            childEntities.add(childEntity);
            when(documentCategoryMapper.toModel(childEntity)).thenReturn(child);
        }
        when(documentCategoryJPARepository.findAllByParentIdInOrderByNameAsc(List.of(category.getId()))).thenReturn(childEntities);

        return adapter.findAll(criteria, Pageable.unpaged()).getContent().get(0).getSubcategories();
    }

    @Test
    void findAll_statusActive_showsOnlyTheActiveSubcategories() {
        DocumentCategory active = named(2L, 1L, "Actiu", "Activo", false);
        DocumentCategory deleted = named(3L, 1L, "Esborrat", "Borrado", true);

        List<DocumentCategory> result = subcategoriesFound(new DocumentCategoryCriteria(null, StatusEnum.ACTIVE.getId(), null),
                named(1L, null, "Manuals", "Manuales", false), active, deleted);

        assertEquals(List.of(active), result);
    }

    @Test
    void findAll_statusInactive_showsOnlyTheDeletedSubcategories() {
        DocumentCategory active = named(2L, 1L, "Actiu", "Activo", false);
        DocumentCategory deleted = named(3L, 1L, "Esborrat", "Borrado", true);

        List<DocumentCategory> result = subcategoriesFound(new DocumentCategoryCriteria(null, StatusEnum.INACTIVE.getId(), null),
                named(1L, null, "Manuals", "Manuales", true), active, deleted);

        assertEquals(List.of(deleted), result);
    }

    @Test
    void findAll_textMatchingOnlySomeSubcategories_showsOnlyThose() {
        DocumentCategory matching = named(2L, 1L, "Guia PDF", "Guia PDF", false);
        DocumentCategory other = named(3L, 1L, "Esquema", "Esquema", false);

        List<DocumentCategory> result = subcategoriesFound(new DocumentCategoryCriteria("pdf", null, null),
                named(1L, null, "Manuals", "Manuales", false), matching, other);

        assertEquals(List.of(matching), result);
    }

    @Test
    void findAll_textMatchingTheSpanishNameOfASubcategory_ignoresCase() {
        DocumentCategory matching = named(2L, 1L, "Guia", "Guía de USUARIO", false);
        DocumentCategory other = named(3L, 1L, "Esquema", "Esquema", false);

        List<DocumentCategory> result = subcategoriesFound(new DocumentCategoryCriteria(null, null, "usuario"),
                named(1L, null, "Manuals", "Manuales", false), matching, other);

        assertEquals(List.of(matching), result);
    }

    @Test
    void findAll_textMatchingTheCategoryItself_keepsAllItsSubcategories() {
        DocumentCategory first = named(2L, 1L, "Guia", "Guia", false);
        DocumentCategory second = named(3L, 1L, "Esquema", "Esquema", false);

        List<DocumentCategory> result = subcategoriesFound(new DocumentCategoryCriteria("manual", null, null),
                named(1L, null, "Manuals", "Manuales", false), first, second);

        assertEquals(List.of(first, second), result);
    }

    @Test
    void findAll_nameAndSearchTogether_bothHaveToMatch() {
        DocumentCategory both = named(2L, 1L, "Guia PDF antigua", "Guia PDF antigua", false);
        DocumentCategory onlyOne = named(3L, 1L, "Esquema PDF", "Esquema PDF", false);

        List<DocumentCategory> result = subcategoriesFound(new DocumentCategoryCriteria("pdf", null, "antigua"),
                named(1L, null, "Manuals", "Manuales", false), both, onlyOne);

        assertEquals(List.of(both), result);
    }

    // ------------------------------------------------------------------
    // Existence checks
    // ------------------------------------------------------------------

    @Test
    void existsActiveWithName_categoryLevel_usesTheNoParentQueries() {
        when(documentCategoryJPARepository.existsByNameAndParentIsNullAndDeletedAtIsNull("A")).thenReturn(true);
        when(documentCategoryJPARepository.existsByNameAndParentIsNullAndIdNotAndDeletedAtIsNull("B", 7L)).thenReturn(false);

        assertTrue(adapter.existsActiveWithName("A", null, null));
        assertFalse(adapter.existsActiveWithName("B", null, 7L));
    }

    @Test
    void existsActiveWithName_subcategoryLevel_usesTheParentScopedQueries() {
        when(documentCategoryJPARepository.existsByNameAndParentIdAndDeletedAtIsNull("A", 1L)).thenReturn(true);
        when(documentCategoryJPARepository.existsByNameAndParentIdAndIdNotAndDeletedAtIsNull("B", 1L, 7L)).thenReturn(false);

        assertTrue(adapter.existsActiveWithName("A", 1L, null));
        assertFalse(adapter.existsActiveWithName("B", 1L, 7L));
    }

    @Test
    void existsActiveWithNameEs_categoryLevel_usesTheNoParentQueries() {
        when(documentCategoryJPARepository.existsByNameEsAndParentIsNullAndDeletedAtIsNull("A")).thenReturn(true);
        when(documentCategoryJPARepository.existsByNameEsAndParentIsNullAndIdNotAndDeletedAtIsNull("B", 7L)).thenReturn(false);

        assertTrue(adapter.existsActiveWithNameEs("A", null, null));
        assertFalse(adapter.existsActiveWithNameEs("B", null, 7L));
    }

    @Test
    void existsActiveWithNameEs_subcategoryLevel_usesTheParentScopedQueries() {
        when(documentCategoryJPARepository.existsByNameEsAndParentIdAndDeletedAtIsNull("A", 1L)).thenReturn(true);
        when(documentCategoryJPARepository.existsByNameEsAndParentIdAndIdNotAndDeletedAtIsNull("B", 1L, 7L)).thenReturn(false);

        assertTrue(adapter.existsActiveWithNameEs("A", 1L, null));
        assertFalse(adapter.existsActiveWithNameEs("B", 1L, 7L));
    }

    @Test
    void hasActiveSubcategories_delegatesToTheJpaRepository() {
        when(documentCategoryJPARepository.existsByParentIdAndDeletedAtIsNull(1L)).thenReturn(true);

        assertTrue(adapter.hasActiveSubcategories(1L));
    }

    // ------------------------------------------------------------------
    // Mutations and audit
    // ------------------------------------------------------------------

    @Test
    void create_persistsTheEntityAndWritesAnInsertAudit() {
        DocumentCategory toSave = model(null, 1L);
        DocumentCategoryEntity unsaved = entity(null, null);
        DocumentCategoryEntity saved = entity(2L, entity(1L, null));
        saved.setCreatedAt(LocalDateTime.of(2026, 10, 8, 9, 0));
        saved.setCreatedBy("jdoe");
        DocumentCategory result = model(2L, 1L);
        when(documentCategoryMapper.toEntity(toSave)).thenReturn(unsaved);
        when(documentCategoryJPARepository.save(unsaved)).thenReturn(saved);
        when(documentCategoryMapper.toModel(saved)).thenReturn(result);

        assertSame(result, adapter.create(toSave));

        ArgumentCaptor<DocumentCategoryAudEntity> captor = ArgumentCaptor.forClass(DocumentCategoryAudEntity.class);
        verify(documentCategoryAudJPARepository).save(captor.capture());
        DocumentCategoryAudEntity aud = captor.getValue();
        assertEquals("INSERT", aud.getAudAction());
        assertEquals(2L, aud.getDocumentCategoryId());
        assertEquals("name-2", aud.getName());
        assertEquals("nombre-2", aud.getNameEs());
        assertEquals(1L, aud.getParentId());
        assertEquals("jdoe", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
    }

    @Test
    void create_category_auditsANullParent() {
        DocumentCategory toSave = model(null, null);
        DocumentCategoryEntity saved = entity(1L, null);
        when(documentCategoryMapper.toEntity(toSave)).thenReturn(saved);
        when(documentCategoryJPARepository.save(saved)).thenReturn(saved);
        when(documentCategoryMapper.toModel(saved)).thenReturn(model(1L, null));

        adapter.create(toSave);

        ArgumentCaptor<DocumentCategoryAudEntity> captor = ArgumentCaptor.forClass(DocumentCategoryAudEntity.class);
        verify(documentCategoryAudJPARepository).save(captor.capture());
        assertNull(captor.getValue().getParentId());
        assertNotNull(captor.getValue().getCreatedAt());
    }

    @Test
    void update_forcesTheGivenIdAndWritesAnUpdateAudit() {
        DocumentCategory toUpdate = model(2L, 1L);
        DocumentCategoryEntity mapped = entity(null, entity(1L, null));
        mapped.setUpdatedAt(LocalDateTime.of(2026, 10, 8, 10, 0));
        mapped.setUpdatedBy("jdoe");
        when(documentCategoryMapper.toEntity(toUpdate)).thenReturn(mapped);
        when(documentCategoryJPARepository.save(mapped)).thenReturn(mapped);
        when(documentCategoryMapper.toModel(mapped)).thenReturn(toUpdate);

        adapter.update(toUpdate, 2L);

        assertEquals(2L, mapped.getId());
        ArgumentCaptor<DocumentCategoryAudEntity> captor = ArgumentCaptor.forClass(DocumentCategoryAudEntity.class);
        verify(documentCategoryAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
        assertEquals(2L, captor.getValue().getDocumentCategoryId());
        assertEquals("jdoe", captor.getValue().getUpdatedBy());
    }

    @Test
    void delete_savesTheSoftDeletionAndWritesADeleteAudit() {
        DocumentCategory toDelete = model(2L, 1L);
        DocumentCategoryEntity mapped = entity(null, entity(1L, null));
        mapped.setDeletedAt(LocalDateTime.of(2026, 10, 8, 11, 0));
        mapped.setDeletedBy("jdoe");
        when(documentCategoryMapper.toEntity(toDelete)).thenReturn(mapped);
        when(documentCategoryJPARepository.save(mapped)).thenReturn(mapped);

        adapter.delete(toDelete);

        assertEquals(2L, mapped.getId());
        ArgumentCaptor<DocumentCategoryAudEntity> captor = ArgumentCaptor.forClass(DocumentCategoryAudEntity.class);
        verify(documentCategoryAudJPARepository).save(captor.capture());
        assertEquals("DELETE", captor.getValue().getAudAction());
        assertEquals("jdoe", captor.getValue().getDeletedBy());
        assertNotNull(captor.getValue().getDeletedAt());
    }
}
