package es.caib.invai.back.persistence.repository.maintenance.development.role;

import es.caib.invai.back.persistence.model.maintenance.development.role.RoleAudEntity;
import es.caib.invai.back.persistence.model.maintenance.development.role.RoleEntity;
import es.caib.invai.back.service.mapper.maintenance.development.role.RoleMapper;
import es.caib.invai.back.service.model.maintenance.development.role.Role;
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
 * Unit tests for {@link RoleRepositoryAdapter}, verifying entity/model delegation to the
 * {@link RoleJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class RoleRepositoryAdapterTest {

    @Mock
    private RoleJPARepository roleJPARepository;

    @Mock
    private RoleAudJPARepository roleAudJPARepository;

    @Mock
    private RoleMapper roleMapper;

    private RoleRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private RoleRepositoryAdapter buildAdapter() {
        RoleRepositoryAdapter a = new RoleRepositoryAdapter();
        ReflectionTestUtils.setField(a, "roleJPARepository", roleJPARepository);
        ReflectionTestUtils.setField(a, "roleAudJPARepository", roleAudJPARepository);
        ReflectionTestUtils.setField(a, "roleMapper", roleMapper);
        return a;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        RoleEntity entity = new RoleEntity();
        entity.setId(1L);
        Role model = new Role();
        when(roleJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(roleMapper.toModel(entity)).thenReturn(model);

        Role result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(roleJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        RoleCriteria criteria = new RoleCriteria();
        Pageable pageable = Pageable.unpaged();
        RoleEntity entity = new RoleEntity();
        Role model = new Role();
        Page<RoleEntity> entityPage = new PageImpl<>(List.of(entity));
        when(roleJPARepository.findAll(ArgumentMatchers.<Specification<RoleEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(roleMapper.toModel(entity)).thenReturn(model);

        Page<Role> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByNameAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(roleJPARepository.existsByNameAndDeletedAtIsNull("Dev")).thenReturn(true);

        assertEquals(true, adapter.existsByNameAndDeletedAtIsNull("Dev"));
    }

    @Test
    void existsByNameAndIdNotAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(roleJPARepository.existsByNameAndIdNotAndDeletedAtIsNull("Dev", 1L)).thenReturn(true);

        assertEquals(true, adapter.existsByNameAndIdNotAndDeletedAtIsNull("Dev", 1L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        Role model = new Role();
        RoleEntity toSave = new RoleEntity();
        RoleEntity saved = new RoleEntity();
        saved.setId(5L);
        saved.setName("DevOps Lead");
        saved.setNameEs("Responsable DevOps");
        Role response = new Role();
        when(roleMapper.toEntity(model)).thenReturn(toSave);
        when(roleJPARepository.save(toSave)).thenReturn(saved);
        when(roleMapper.toModel(saved)).thenReturn(response);

        Role result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<RoleAudEntity> captor = ArgumentCaptor.forClass(RoleAudEntity.class);
        verify(roleAudJPARepository).save(captor.capture());
        RoleAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getRoleId());
        assertEquals("DevOps Lead", aud.getName());
        assertEquals("Responsable DevOps", aud.getNameEs());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        Role model = new Role();
        RoleEntity toSave = new RoleEntity();
        RoleEntity saved = new RoleEntity();
        saved.setId(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(roleMapper.toEntity(model)).thenReturn(toSave);
        when(roleJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<RoleAudEntity> captor = ArgumentCaptor.forClass(RoleAudEntity.class);
        verify(roleAudJPARepository).save(captor.capture());
        RoleAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        Role model = new Role();
        RoleEntity toSave = new RoleEntity();
        RoleEntity saved = new RoleEntity();
        saved.setId(7L);
        Role response = new Role();
        when(roleMapper.toEntity(model)).thenReturn(toSave);
        when(roleJPARepository.save(toSave)).thenReturn(saved);
        when(roleMapper.toModel(saved)).thenReturn(response);

        Role result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<RoleAudEntity> captor = ArgumentCaptor.forClass(RoleAudEntity.class);
        verify(roleAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        Role model = new Role();
        model.setId(8L);
        RoleEntity toSave = new RoleEntity();
        RoleEntity saved = new RoleEntity();
        saved.setId(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(roleMapper.toEntity(model)).thenReturn(toSave);
        when(roleJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<RoleAudEntity> captor = ArgumentCaptor.forClass(RoleAudEntity.class);
        verify(roleAudJPARepository).save(captor.capture());
        RoleAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
