package es.caib.invai.back.persistence.repository.maintenance.general.category;

import es.caib.invai.back.persistence.model.maintenance.general.category.CategoryAudEntity;
import es.caib.invai.back.persistence.model.maintenance.general.category.CategoryEntity;
import es.caib.invai.back.service.mapper.maintenance.general.category.CategoryMapper;
import es.caib.invai.back.service.model.maintenance.general.category.Category;
import org.junit.jupiter.api.AfterEach;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentMatchers;

/**
 * Unit tests for {@link CategoryRepositoryAdapter}, verifying entity/model delegation to the
 * {@link CategoryJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class CategoryRepositoryAdapterTest {

    @Mock
    private CategoryJPARepository categoryJPARepository;

    @Mock
    private CategoryAudJPARepository categoryAudJPARepository;

    @Mock
    private CategoryMapper categoryMapper;

    private CategoryRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private CategoryRepositoryAdapter buildAdapter() {
        CategoryRepositoryAdapter a = new CategoryRepositoryAdapter();
        ReflectionTestUtils.setField(a, "categoryJPARepository", categoryJPARepository);
        ReflectionTestUtils.setField(a, "categoryAudJPARepository", categoryAudJPARepository);
        ReflectionTestUtils.setField(a, "categoryMapper", categoryMapper);
        return a;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        CategoryEntity entity = new CategoryEntity();
        entity.setId(1L);
        Category model = new Category();
        when(categoryJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(categoryMapper.toModel(entity)).thenReturn(model);

        Category result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(categoryJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        CategoryCriteria criteria = new CategoryCriteria();
        Pageable pageable = Pageable.unpaged();
        CategoryEntity entity = new CategoryEntity();
        Category model = new Category();
        Page<CategoryEntity> entityPage = new PageImpl<>(List.of(entity));
        when(categoryJPARepository.findAll(ArgumentMatchers.<Specification<CategoryEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(categoryMapper.toModel(entity)).thenReturn(model);

        Page<Category> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByNameAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(categoryJPARepository.existsByNameAndDeletedAtIsNull("Hardware")).thenReturn(true);

        assertEquals(true, adapter.existsByNameAndDeletedAtIsNull("Hardware"));
    }

    @Test
    void existsByNameAndIdNotAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(categoryJPARepository.existsByNameAndIdNotAndDeletedAtIsNull("Hardware", 1L)).thenReturn(true);

        assertEquals(true, adapter.existsByNameAndIdNotAndDeletedAtIsNull("Hardware", 1L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        Category model = new Category();
        CategoryEntity toSave = new CategoryEntity();
        CategoryEntity saved = new CategoryEntity();
        saved.setId(5L);
        saved.setName("Hardware");
        saved.setNameEs("Hardware ES");
        Category response = new Category();
        when(categoryMapper.toEntity(model)).thenReturn(toSave);
        when(categoryJPARepository.save(toSave)).thenReturn(saved);
        when(categoryMapper.toModel(saved)).thenReturn(response);

        Category result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<CategoryAudEntity> captor = ArgumentCaptor.forClass(CategoryAudEntity.class);
        verify(categoryAudJPARepository).save(captor.capture());
        CategoryAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getCategoryId());
        assertEquals("Hardware", aud.getName());
        assertEquals("Hardware ES", aud.getNameEs());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        Category model = new Category();
        CategoryEntity toSave = new CategoryEntity();
        CategoryEntity saved = new CategoryEntity();
        saved.setId(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(categoryMapper.toEntity(model)).thenReturn(toSave);
        when(categoryJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<CategoryAudEntity> captor = ArgumentCaptor.forClass(CategoryAudEntity.class);
        verify(categoryAudJPARepository).save(captor.capture());
        CategoryAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        Category model = new Category();
        CategoryEntity toSave = new CategoryEntity();
        CategoryEntity saved = new CategoryEntity();
        saved.setId(7L);
        Category response = new Category();
        when(categoryMapper.toEntity(model)).thenReturn(toSave);
        when(categoryJPARepository.save(toSave)).thenReturn(saved);
        when(categoryMapper.toModel(saved)).thenReturn(response);

        Category result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<CategoryAudEntity> captor = ArgumentCaptor.forClass(CategoryAudEntity.class);
        verify(categoryAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        Category model = new Category();
        model.setId(8L);
        CategoryEntity toSave = new CategoryEntity();
        CategoryEntity saved = new CategoryEntity();
        saved.setId(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(categoryMapper.toEntity(model)).thenReturn(toSave);
        when(categoryJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<CategoryAudEntity> captor = ArgumentCaptor.forClass(CategoryAudEntity.class);
        verify(categoryAudJPARepository).save(captor.capture());
        CategoryAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
