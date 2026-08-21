package es.caib.invai.back.persistence.repository.maintenance.systems.system;

import es.caib.invai.back.persistence.model.maintenance.systems.server.ServerEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.system.SystemAudEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.system.SystemEntity;
import es.caib.invai.back.service.mapper.maintenance.systems.system.SystemMapper;
import es.caib.invai.back.service.model.maintenance.systems.system.System;
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
 * Unit tests for {@link SystemRepositoryAdapter}, verifying entity/model delegation to the
 * {@link SystemJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class SystemRepositoryAdapterTest {

    @Mock
    private SystemJPARepository systemJPARepository;

    @Mock
    private SystemAudJPARepository systemAudJPARepository;

    @Mock
    private SystemMapper systemMapper;

    private SystemRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private SystemRepositoryAdapter buildAdapter() {
        SystemRepositoryAdapter a = new SystemRepositoryAdapter();
        ReflectionTestUtils.setField(a, "systemJPARepository", systemJPARepository);
        ReflectionTestUtils.setField(a, "systemAudJPARepository", systemAudJPARepository);
        ReflectionTestUtils.setField(a, "systemMapper", systemMapper);
        return a;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        SystemEntity entity = new SystemEntity();
        entity.setId(1L);
        System model = new System();
        when(systemJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(systemMapper.toModel(entity)).thenReturn(model);

        System result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(systemJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        SystemCriteria criteria = new SystemCriteria();
        Pageable pageable = Pageable.unpaged();
        SystemEntity entity = new SystemEntity();
        System model = new System();
        Page<SystemEntity> entityPage = new PageImpl<>(List.of(entity));
        when(systemJPARepository.findAll(ArgumentMatchers.<Specification<SystemEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(systemMapper.toModel(entity)).thenReturn(model);

        Page<System> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByServerIdAndInstance_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(systemJPARepository.existsByServerIdAndInstance(1L, "INST01")).thenReturn(true);

        assertEquals(true, adapter.existsByServerIdAndInstance(1L, "INST01"));
    }

    @Test
    void existsByServerIdAndInstanceAndId_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(systemJPARepository.existsByServerIdAndInstanceAndId(1L, "INST01", 2L)).thenReturn(true);

        assertEquals(true, adapter.existsByServerIdAndInstanceAndId(1L, "INST01", 2L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        System model = new System();
        SystemEntity toSave = new SystemEntity();
        SystemEntity saved = new SystemEntity();
        saved.setId(5L);
        ServerEntity server = new ServerEntity();
        server.setId(11L);
        saved.setServer(server);
        saved.setInstance("INST01");
        saved.setPort(8080);
        saved.setVersion("1.2.3");
        saved.setDescription("Main instance");
        System response = new System();
        when(systemMapper.toEntity(model)).thenReturn(toSave);
        when(systemJPARepository.save(toSave)).thenReturn(saved);
        when(systemMapper.toModel(saved)).thenReturn(response);

        System result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<SystemAudEntity> captor = ArgumentCaptor.forClass(SystemAudEntity.class);
        verify(systemAudJPARepository).save(captor.capture());
        SystemAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getSystemId());
        assertEquals(11L, aud.getServerId());
        assertEquals("INST01", aud.getInstance());
        assertEquals(8080, aud.getPort());
        assertEquals("1.2.3", aud.getVersion());
        assertEquals("Main instance", aud.getDescription());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        System model = new System();
        SystemEntity toSave = new SystemEntity();
        SystemEntity saved = new SystemEntity();
        saved.setId(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(systemMapper.toEntity(model)).thenReturn(toSave);
        when(systemJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<SystemAudEntity> captor = ArgumentCaptor.forClass(SystemAudEntity.class);
        verify(systemAudJPARepository).save(captor.capture());
        SystemAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void create_entityWithoutServer_writesNullServerId() {
        adapter = buildAdapter();
        System model = new System();
        SystemEntity toSave = new SystemEntity();
        SystemEntity saved = new SystemEntity();
        saved.setId(9L);
        when(systemMapper.toEntity(model)).thenReturn(toSave);
        when(systemJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<SystemAudEntity> captor = ArgumentCaptor.forClass(SystemAudEntity.class);
        verify(systemAudJPARepository).save(captor.capture());
        assertNull(captor.getValue().getServerId());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        System model = new System();
        SystemEntity toSave = new SystemEntity();
        SystemEntity saved = new SystemEntity();
        saved.setId(7L);
        System response = new System();
        when(systemMapper.toEntity(model)).thenReturn(toSave);
        when(systemJPARepository.save(toSave)).thenReturn(saved);
        when(systemMapper.toModel(saved)).thenReturn(response);

        System result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<SystemAudEntity> captor = ArgumentCaptor.forClass(SystemAudEntity.class);
        verify(systemAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        System model = new System();
        model.setId(8L);
        SystemEntity toSave = new SystemEntity();
        SystemEntity saved = new SystemEntity();
        saved.setId(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(systemMapper.toEntity(model)).thenReturn(toSave);
        when(systemJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<SystemAudEntity> captor = ArgumentCaptor.forClass(SystemAudEntity.class);
        verify(systemAudJPARepository).save(captor.capture());
        SystemAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
