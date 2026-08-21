package es.caib.invai.back.persistence.repository.maintenance.general.systemType;

import es.caib.invai.back.persistence.model.maintenance.general.systemType.SystemTypeAudEntity;
import es.caib.invai.back.persistence.model.maintenance.general.systemType.SystemTypeEntity;
import es.caib.invai.back.service.mapper.maintenance.general.systemType.SystemTypeMapper;
import es.caib.invai.back.service.model.maintenance.general.systemType.SystemType;
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
 * Unit tests for {@link SystemTypeRepositoryAdapter}, verifying entity/model delegation to the
 * {@link SystemTypeJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class SystemTypeRepositoryAdapterTest {

    @Mock
    private SystemTypeJPARepository systemTypeJPARepository;

    @Mock
    private SystemTypeAudJPARepository systemTypeAudJPARepository;

    @Mock
    private SystemTypeMapper systemTypeMapper;

    private SystemTypeRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private SystemTypeRepositoryAdapter buildAdapter() {
        SystemTypeRepositoryAdapter a = new SystemTypeRepositoryAdapter();
        ReflectionTestUtils.setField(a, "systemTypeJPARepository", systemTypeJPARepository);
        ReflectionTestUtils.setField(a, "systemTypeAudJPARepository", systemTypeAudJPARepository);
        ReflectionTestUtils.setField(a, "systemTypeMapper", systemTypeMapper);
        return a;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        SystemTypeEntity entity = new SystemTypeEntity();
        entity.setId(1L);
        SystemType model = new SystemType();
        when(systemTypeJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(systemTypeMapper.toModel(entity)).thenReturn(model);

        SystemType result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(systemTypeJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        SystemTypeCriteria criteria = new SystemTypeCriteria();
        Pageable pageable = Pageable.unpaged();
        SystemTypeEntity entity = new SystemTypeEntity();
        SystemType model = new SystemType();
        Page<SystemTypeEntity> entityPage = new PageImpl<>(List.of(entity));
        when(systemTypeJPARepository.findAll(ArgumentMatchers.<Specification<SystemTypeEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(systemTypeMapper.toModel(entity)).thenReturn(model);

        Page<SystemType> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByNameAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(systemTypeJPARepository.existsByNameAndDeletedAtIsNull("REST API")).thenReturn(true);

        assertEquals(true, adapter.existsByNameAndDeletedAtIsNull("REST API"));
    }

    @Test
    void existsByNameAndIdNotAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(systemTypeJPARepository.existsByNameAndIdNotAndDeletedAtIsNull("REST API", 1L)).thenReturn(true);

        assertEquals(true, adapter.existsByNameAndIdNotAndDeletedAtIsNull("REST API", 1L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        SystemType model = new SystemType();
        SystemTypeEntity toSave = new SystemTypeEntity();
        SystemTypeEntity saved = new SystemTypeEntity();
        saved.setId(5L);
        saved.setName("Web App");
        saved.setNameEs("Aplicacion Web");
        SystemType response = new SystemType();
        when(systemTypeMapper.toEntity(model)).thenReturn(toSave);
        when(systemTypeJPARepository.save(toSave)).thenReturn(saved);
        when(systemTypeMapper.toModel(saved)).thenReturn(response);

        SystemType result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<SystemTypeAudEntity> captor = ArgumentCaptor.forClass(SystemTypeAudEntity.class);
        verify(systemTypeAudJPARepository).save(captor.capture());
        SystemTypeAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getSystemTypeId());
        assertEquals("Web App", aud.getName());
        assertEquals("Aplicacion Web", aud.getNameEs());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getUpdatedAt());
        assertEquals("SYSTEM_USER", aud.getUpdatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        SystemType model = new SystemType();
        SystemTypeEntity toSave = new SystemTypeEntity();
        SystemTypeEntity saved = new SystemTypeEntity();
        saved.setId(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime existingUpdatedAt = LocalDateTime.of(2025, 2, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        saved.setUpdatedAt(existingUpdatedAt);
        saved.setUpdatedBy("jdoe2");
        when(systemTypeMapper.toEntity(model)).thenReturn(toSave);
        when(systemTypeJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<SystemTypeAudEntity> captor = ArgumentCaptor.forClass(SystemTypeAudEntity.class);
        verify(systemTypeAudJPARepository).save(captor.capture());
        SystemTypeAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
        assertEquals(existingUpdatedAt, aud.getUpdatedAt());
        assertEquals("jdoe2", aud.getUpdatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        SystemType model = new SystemType();
        SystemTypeEntity toSave = new SystemTypeEntity();
        SystemTypeEntity saved = new SystemTypeEntity();
        saved.setId(7L);
        SystemType response = new SystemType();
        when(systemTypeMapper.toEntity(model)).thenReturn(toSave);
        when(systemTypeJPARepository.save(toSave)).thenReturn(saved);
        when(systemTypeMapper.toModel(saved)).thenReturn(response);

        SystemType result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<SystemTypeAudEntity> captor = ArgumentCaptor.forClass(SystemTypeAudEntity.class);
        verify(systemTypeAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        SystemType model = new SystemType();
        model.setId(8L);
        SystemTypeEntity toSave = new SystemTypeEntity();
        SystemTypeEntity saved = new SystemTypeEntity();
        saved.setId(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(systemTypeMapper.toEntity(model)).thenReturn(toSave);
        when(systemTypeJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<SystemTypeAudEntity> captor = ArgumentCaptor.forClass(SystemTypeAudEntity.class);
        verify(systemTypeAudJPARepository).save(captor.capture());
        SystemTypeAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
