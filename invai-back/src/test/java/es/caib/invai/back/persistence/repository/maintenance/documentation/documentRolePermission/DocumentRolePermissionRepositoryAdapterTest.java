package es.caib.invai.back.persistence.repository.maintenance.documentation.documentRolePermission;

import es.caib.invai.back.persistence.model.maintenance.documentation.documentCategory.DocumentCategoryEntity;
import es.caib.invai.back.persistence.model.maintenance.documentation.documentRolePermission.DocumentRolePermissionAudEntity;
import es.caib.invai.back.persistence.model.maintenance.documentation.documentRolePermission.DocumentRolePermissionEntity;
import es.caib.invai.back.persistence.model.maintenance.role.RoleEntity;
import es.caib.invai.back.service.mapper.maintenance.documentation.documentRolePermission.DocumentRolePermissionMapper;
import es.caib.invai.back.service.mapper.maintenance.role.RoleMapper;
import es.caib.invai.back.service.model.maintenance.documentation.documentRolePermission.DocumentRolePermission;
import es.caib.invai.back.service.model.maintenance.role.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DocumentRolePermissionRepositoryAdapter}, verifying entity/model delegation
 * to the {@link DocumentRolePermissionJPARepository} and the historical audit record written on
 * every mutation.
 */
@ExtendWith(MockitoExtension.class)
class DocumentRolePermissionRepositoryAdapterTest {

    @Mock
    private DocumentRolePermissionJPARepository documentRolePermissionJPARepository;

    @Mock
    private DocumentRolePermissionAudJPARepository documentRolePermissionAudJPARepository;

    @Mock
    private DocumentRolePermissionMapper documentRolePermissionMapper;

    @Mock
    private RoleMapper roleMapper;

    private DocumentRolePermissionRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DocumentRolePermissionRepositoryAdapter();
        ReflectionTestUtils.setField(adapter, "documentRolePermissionJPARepository", documentRolePermissionJPARepository);
        ReflectionTestUtils.setField(adapter, "documentRolePermissionAudJPARepository", documentRolePermissionAudJPARepository);
        ReflectionTestUtils.setField(adapter, "documentRolePermissionMapper", documentRolePermissionMapper);
        ReflectionTestUtils.setField(adapter, "roleMapper", roleMapper);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static DocumentRolePermissionEntity entity(Long id, Long roleId) {
        RoleEntity role = new RoleEntity();
        role.setId(roleId);
        DocumentCategoryEntity category = new DocumentCategoryEntity();
        category.setId(20L);
        DocumentRolePermissionEntity entity = new DocumentRolePermissionEntity();
        entity.setId(id);
        entity.setRole(role);
        entity.setCategory(category);
        entity.setCanRead(true);
        entity.setCanUpload(false);
        entity.setCanEdit(true);
        entity.setCanDelete(false);
        return entity;
    }

    private static DocumentRolePermission model(Long id, Long roleId) {
        DocumentRolePermission model = new DocumentRolePermission();
        model.setId(id);
        model.setRoleId(roleId);
        model.setCategoryId(20L);
        return model;
    }

    @Test
    void findByCategoryId_mapsEveryRowOfTheCategory() {
        DocumentRolePermissionEntity entity = entity(1L, 10L);
        DocumentRolePermission model = model(1L, 10L);
        when(documentRolePermissionJPARepository.findByCategoryIdOrderByRoleNameAsc(20L)).thenReturn(List.of(entity));
        when(documentRolePermissionMapper.toModel(entity)).thenReturn(model);

        assertEquals(List.of(model), adapter.findByCategoryId(20L));
    }

    @Test
    void findByCategoryId_withoutRows_returnsAnEmptyList() {
        when(documentRolePermissionJPARepository.findByCategoryIdOrderByRoleNameAsc(20L)).thenReturn(List.of());

        assertTrue(adapter.findByCategoryId(20L).isEmpty());
    }

    @Test
    void findByRoleId_mapsEveryRowOfTheRole() {
        DocumentRolePermissionEntity entity = entity(1L, 10L);
        DocumentRolePermission model = model(1L, 10L);
        when(documentRolePermissionJPARepository.findByRoleId(10L)).thenReturn(List.of(entity));
        when(documentRolePermissionMapper.toModel(entity)).thenReturn(model);

        assertEquals(List.of(model), adapter.findByRoleId(10L));
    }

    @Test
    void findByRoleIdAndCategoryId_found_returnsTheMappedModel() {
        DocumentRolePermissionEntity entity = entity(1L, 10L);
        DocumentRolePermission model = model(1L, 10L);
        when(documentRolePermissionJPARepository.findByRoleIdAndCategoryId(10L, 20L)).thenReturn(Optional.of(entity));
        when(documentRolePermissionMapper.toModel(entity)).thenReturn(model);

        assertSame(model, adapter.findByRoleIdAndCategoryId(10L, 20L));
    }

    @Test
    void findByRoleIdAndCategoryId_notFound_returnsNull() {
        when(documentRolePermissionJPARepository.findByRoleIdAndCategoryId(10L, 20L)).thenReturn(Optional.empty());

        assertNull(adapter.findByRoleIdAndCategoryId(10L, 20L));
    }

    @Test
    void findRoles_mapsEveryRoleThatHasRows() {
        RoleEntity entity = new RoleEntity();
        entity.setId(10L);
        Role model = new Role();
        model.setId(10L);
        when(documentRolePermissionJPARepository.findDistinctRoles()).thenReturn(List.of(entity));
        when(roleMapper.toModel(entity)).thenReturn(model);

        assertEquals(List.of(model), adapter.findRoles());
    }

    @Test
    void existsByRoleName_delegatesToTheJpaRepository() {
        when(documentRolePermissionJPARepository.existsByRoleName("INV_ADMIN")).thenReturn(true);
        when(documentRolePermissionJPARepository.existsByRoleName("INV_OTHER")).thenReturn(false);

        assertTrue(adapter.existsByRoleName("INV_ADMIN"));
        assertFalse(adapter.existsByRoleName("INV_OTHER"));
    }

    @Test
    void findByRoleNames_mapsTheRowsOfThoseRoles() {
        DocumentRolePermissionEntity entity = entity(1L, 10L);
        DocumentRolePermission model = model(1L, 10L);
        when(documentRolePermissionJPARepository.findByRoleNameIn(Set.of("INV_ADMIN"))).thenReturn(List.of(entity));
        when(documentRolePermissionMapper.toModel(entity)).thenReturn(model);

        assertEquals(List.of(model), adapter.findByRoleNames(Set.of("INV_ADMIN")));
    }

    @Test
    void create_savesTheEntityAndAuditsAnInsert() {
        DocumentRolePermission toSave = model(null, 10L);
        DocumentRolePermissionEntity unsaved = entity(null, 10L);
        DocumentRolePermissionEntity saved = entity(1L, 10L);
        saved.setCreatedAt(LocalDateTime.of(2026, 10, 27, 9, 0));
        saved.setCreatedBy("jdoe");
        DocumentRolePermission result = model(1L, 10L);
        when(documentRolePermissionMapper.toEntity(toSave)).thenReturn(unsaved);
        when(documentRolePermissionJPARepository.save(unsaved)).thenReturn(saved);
        when(documentRolePermissionMapper.toModel(saved)).thenReturn(result);

        assertSame(result, adapter.create(toSave));

        ArgumentCaptor<DocumentRolePermissionAudEntity> captor = ArgumentCaptor.forClass(DocumentRolePermissionAudEntity.class);
        verify(documentRolePermissionAudJPARepository).save(captor.capture());
        DocumentRolePermissionAudEntity aud = captor.getValue();
        assertEquals("INSERT", aud.getAudAction());
        assertEquals(1L, aud.getDocRolePermId());
        assertEquals(10L, aud.getRoleId());
        assertEquals(20L, aud.getCategoryId());
        assertTrue(aud.isCanRead());
        assertFalse(aud.isCanUpload());
        assertTrue(aud.isCanEdit());
        assertFalse(aud.isCanDelete());
        assertEquals("jdoe", aud.getCreatedBy());
        assertNull(aud.getDeletedAt());
        assertNotNull(aud.getAuditDate());
    }

    @Test
    void create_withoutAuditFieldsOnTheEntity_fillsThemIn() {
        DocumentRolePermission toSave = model(null, 10L);
        DocumentRolePermissionEntity saved = entity(1L, 10L);
        when(documentRolePermissionMapper.toEntity(toSave)).thenReturn(saved);
        when(documentRolePermissionJPARepository.save(saved)).thenReturn(saved);

        adapter.create(toSave);

        ArgumentCaptor<DocumentRolePermissionAudEntity> captor = ArgumentCaptor.forClass(DocumentRolePermissionAudEntity.class);
        verify(documentRolePermissionAudJPARepository).save(captor.capture());
        assertNotNull(captor.getValue().getCreatedAt());
        assertNotNull(captor.getValue().getCreatedBy());
    }

    @Test
    void update_savesTheEntityWithTheGivenIdAndAuditsAnUpdate() {
        DocumentRolePermission toUpdate = model(1L, 10L);
        DocumentRolePermissionEntity entity = entity(null, 10L);
        when(documentRolePermissionMapper.toEntity(toUpdate)).thenReturn(entity);
        when(documentRolePermissionJPARepository.save(entity)).thenReturn(entity);

        adapter.update(toUpdate, 7L);

        assertEquals(7L, entity.getId());
        ArgumentCaptor<DocumentRolePermissionAudEntity> captor = ArgumentCaptor.forClass(DocumentRolePermissionAudEntity.class);
        verify(documentRolePermissionAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
        assertEquals(7L, captor.getValue().getDocRolePermId());
        assertEquals(20L, captor.getValue().getCategoryId());
    }

    @Test
    void delete_auditsTheDeletionAndThenDeletesTheRowForGood() {
        DocumentRolePermission toDelete = model(3L, 10L);
        DocumentRolePermissionEntity entity = entity(null, 10L);
        when(documentRolePermissionMapper.toEntity(toDelete)).thenReturn(entity);

        adapter.delete(toDelete);

        ArgumentCaptor<DocumentRolePermissionAudEntity> captor = ArgumentCaptor.forClass(DocumentRolePermissionAudEntity.class);
        var order = inOrder(documentRolePermissionAudJPARepository, documentRolePermissionJPARepository);
        order.verify(documentRolePermissionAudJPARepository).save(captor.capture());
        order.verify(documentRolePermissionJPARepository).deleteById(3L);
        assertEquals("DELETE", captor.getValue().getAudAction());
        assertEquals(3L, captor.getValue().getDocRolePermId());
        assertEquals(20L, captor.getValue().getCategoryId());
        assertNotNull(captor.getValue().getDeletedAt());
        assertNotNull(captor.getValue().getDeletedBy());
    }
}
