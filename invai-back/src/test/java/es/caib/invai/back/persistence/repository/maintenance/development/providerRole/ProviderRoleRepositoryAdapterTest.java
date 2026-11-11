package es.caib.invai.back.persistence.repository.maintenance.development.providerRole;

import es.caib.invai.back.persistence.model.maintenance.development.providerRole.ProviderRoleAudEntity;
import es.caib.invai.back.persistence.model.maintenance.development.providerRole.ProviderRoleEntity;
import es.caib.invai.back.service.mapper.maintenance.development.providerRole.ProviderRoleMapper;
import es.caib.invai.back.service.model.maintenance.development.providerRole.ProviderRole;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentMatchers;

/**
 * Unit tests for {@link ProviderRoleRepositoryAdapter}, verifying entity/model delegation to the
 * {@link ProviderRoleJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class ProviderRoleRepositoryAdapterTest {

    @Mock
    private ProviderRoleJPARepository providerRoleJPARepository;

    @Mock
    private ProviderRoleAudJPARepository providerRoleAudJPARepository;

    @Mock
    private ProviderRoleMapper providerRoleMapper;

    private ProviderRoleRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private ProviderRoleRepositoryAdapter buildAdapter() {
        ProviderRoleRepositoryAdapter a = new ProviderRoleRepositoryAdapter();
        ReflectionTestUtils.setField(a, "providerRoleJPARepository", providerRoleJPARepository);
        ReflectionTestUtils.setField(a, "providerRoleAudJPARepository", providerRoleAudJPARepository);
        ReflectionTestUtils.setField(a, "providerRoleMapper", providerRoleMapper);
        return a;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        ProviderRoleEntity entity = new ProviderRoleEntity();
        entity.setId(1L);
        ProviderRole model = new ProviderRole();
        when(providerRoleJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(providerRoleMapper.toModel(entity)).thenReturn(model);

        ProviderRole result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(providerRoleJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        ProviderRoleCriteria criteria = new ProviderRoleCriteria();
        Pageable pageable = Pageable.unpaged();
        ProviderRoleEntity entity = new ProviderRoleEntity();
        ProviderRole model = new ProviderRole();
        Page<ProviderRoleEntity> entityPage = new PageImpl<>(List.of(entity));
        when(providerRoleJPARepository.findAll(ArgumentMatchers.<Specification<ProviderRoleEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(providerRoleMapper.toModel(entity)).thenReturn(model);

        Page<ProviderRole> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByNameAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(providerRoleJPARepository.existsByNameAndDeletedAtIsNull("Dev")).thenReturn(true);

        assertTrue(adapter.existsByNameAndDeletedAtIsNull("Dev"));
    }

    @Test
    void existsByNameAndIdNotAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(providerRoleJPARepository.existsByNameAndIdNotAndDeletedAtIsNull("Dev", 1L)).thenReturn(true);

        assertTrue(adapter.existsByNameAndIdNotAndDeletedAtIsNull("Dev", 1L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        ProviderRole model = new ProviderRole();
        ProviderRoleEntity toSave = new ProviderRoleEntity();
        ProviderRoleEntity saved = new ProviderRoleEntity();
        saved.setId(5L);
        saved.setName("DevOps Lead");
        saved.setNameEs("Responsable DevOps");
        ProviderRole response = new ProviderRole();
        when(providerRoleMapper.toEntity(model)).thenReturn(toSave);
        when(providerRoleJPARepository.save(toSave)).thenReturn(saved);
        when(providerRoleMapper.toModel(saved)).thenReturn(response);

        ProviderRole result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<ProviderRoleAudEntity> captor = ArgumentCaptor.forClass(ProviderRoleAudEntity.class);
        verify(providerRoleAudJPARepository).save(captor.capture());
        ProviderRoleAudEntity aud = captor.getValue();
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
        ProviderRole model = new ProviderRole();
        ProviderRoleEntity toSave = new ProviderRoleEntity();
        ProviderRoleEntity saved = new ProviderRoleEntity();
        saved.setId(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(providerRoleMapper.toEntity(model)).thenReturn(toSave);
        when(providerRoleJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<ProviderRoleAudEntity> captor = ArgumentCaptor.forClass(ProviderRoleAudEntity.class);
        verify(providerRoleAudJPARepository).save(captor.capture());
        ProviderRoleAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        ProviderRole model = new ProviderRole();
        ProviderRoleEntity toSave = new ProviderRoleEntity();
        ProviderRoleEntity saved = new ProviderRoleEntity();
        saved.setId(7L);
        ProviderRole response = new ProviderRole();
        when(providerRoleMapper.toEntity(model)).thenReturn(toSave);
        when(providerRoleJPARepository.save(toSave)).thenReturn(saved);
        when(providerRoleMapper.toModel(saved)).thenReturn(response);

        ProviderRole result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<ProviderRoleAudEntity> captor = ArgumentCaptor.forClass(ProviderRoleAudEntity.class);
        verify(providerRoleAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        ProviderRole model = new ProviderRole();
        model.setId(8L);
        ProviderRoleEntity toSave = new ProviderRoleEntity();
        ProviderRoleEntity saved = new ProviderRoleEntity();
        saved.setId(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(providerRoleMapper.toEntity(model)).thenReturn(toSave);
        when(providerRoleJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<ProviderRoleAudEntity> captor = ArgumentCaptor.forClass(ProviderRoleAudEntity.class);
        verify(providerRoleAudJPARepository).save(captor.capture());
        ProviderRoleAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
