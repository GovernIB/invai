package es.caib.invai.back.persistence.repository.maintenance.systems.databaseVendor;

import es.caib.invai.back.persistence.model.maintenance.systems.databaseVendor.DatabaseVendorAudEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.databaseVendor.DatabaseVendorEntity;
import es.caib.invai.back.service.mapper.maintenance.systems.databaseVendor.DatabaseVendorMapper;
import es.caib.invai.back.service.model.maintenance.systems.databaseVendor.DatabaseVendor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.QueryTimeoutException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentMatchers;

/**
 * Unit tests for {@link DatabaseVendorRepositoryAdapter}, verifying entity/model delegation to the
 * {@link DatabaseVendorJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class DatabaseVendorRepositoryAdapterTest {

    @Mock
    private DatabaseVendorJPARepository databaseVendorJPARepository;

    @Mock
    private DatabaseVendorAudJPARepository databaseVendorAudJPARepository;

    @Mock
    private DatabaseVendorMapper databaseVendorMapper;

    private DatabaseVendorRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private DatabaseVendorRepositoryAdapter buildAdapter() {
        DatabaseVendorRepositoryAdapter a = new DatabaseVendorRepositoryAdapter();
        ReflectionTestUtils.setField(a, "databaseVendorJPARepository", databaseVendorJPARepository);
        ReflectionTestUtils.setField(a, "databaseVendorAudJPARepository", databaseVendorAudJPARepository);
        ReflectionTestUtils.setField(a, "databaseVendorMapper", databaseVendorMapper);
        return a;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        DatabaseVendorEntity entity = new DatabaseVendorEntity();
        entity.setId(1L);
        DatabaseVendor model = new DatabaseVendor();
        when(databaseVendorJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(databaseVendorMapper.toModel(entity)).thenReturn(model);

        DatabaseVendor result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(databaseVendorJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findById_dataAccessException_isRethrown() {
        adapter = buildAdapter();
        when(databaseVendorJPARepository.findById(1L)).thenThrow(new QueryTimeoutException("boom"));

        assertThrows(DataAccessException.class, () -> adapter.findById(1L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        DatabaseVendorCriteria criteria = new DatabaseVendorCriteria();
        Pageable pageable = Pageable.unpaged();
        DatabaseVendorEntity entity = new DatabaseVendorEntity();
        DatabaseVendor model = new DatabaseVendor();
        Page<DatabaseVendorEntity> entityPage = new PageImpl<>(List.of(entity));
        when(databaseVendorJPARepository.findAll(ArgumentMatchers.<Specification<DatabaseVendorEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(databaseVendorMapper.toModel(entity)).thenReturn(model);

        Page<DatabaseVendor> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void findAll_dataAccessException_isRethrown() {
        adapter = buildAdapter();
        DatabaseVendorCriteria criteria = new DatabaseVendorCriteria();
        Pageable pageable = Pageable.unpaged();
        when(databaseVendorJPARepository.findAll(ArgumentMatchers.<Specification<DatabaseVendorEntity>>any(), eq(pageable)))
                .thenThrow(new QueryTimeoutException("boom"));

        assertThrows(DataAccessException.class, () -> adapter.findAll(criteria, pageable));
    }

    @Test
    void existsByName_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(databaseVendorJPARepository.existsByNameAndDeletedAtIsNull("Oracle")).thenReturn(true);

        assertEquals(true, adapter.existsByName("Oracle"));
    }

    @Test
    void existsByName_dataAccessException_isRethrown() {
        adapter = buildAdapter();
        when(databaseVendorJPARepository.existsByNameAndDeletedAtIsNull("Oracle"))
                .thenThrow(new QueryTimeoutException("boom"));

        assertThrows(DataAccessException.class, () -> adapter.existsByName("Oracle"));
    }

    @Test
    void existsByNameAndIdNot_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(databaseVendorJPARepository.existsByNameAndIdNotAndDeletedAtIsNull("Oracle", 1L)).thenReturn(true);

        assertEquals(true, adapter.existsByNameAndIdNot("Oracle", 1L));
    }

    @Test
    void existsByNameAndIdNot_dataAccessException_isRethrown() {
        adapter = buildAdapter();
        when(databaseVendorJPARepository.existsByNameAndIdNotAndDeletedAtIsNull("Oracle", 1L))
                .thenThrow(new QueryTimeoutException("boom"));

        assertThrows(DataAccessException.class, () -> adapter.existsByNameAndIdNot("Oracle", 1L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        DatabaseVendor model = new DatabaseVendor();
        DatabaseVendorEntity toSave = new DatabaseVendorEntity();
        DatabaseVendorEntity saved = new DatabaseVendorEntity();
        saved.setId(5L);
        saved.setName("PostgreSQL");
        saved.setDefaultPort(5432);
        DatabaseVendor response = new DatabaseVendor();
        when(databaseVendorMapper.toEntity(model)).thenReturn(toSave);
        when(databaseVendorJPARepository.save(toSave)).thenReturn(saved);
        when(databaseVendorMapper.toModel(saved)).thenReturn(response);

        DatabaseVendor result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<DatabaseVendorAudEntity> captor = ArgumentCaptor.forClass(DatabaseVendorAudEntity.class);
        verify(databaseVendorAudJPARepository).save(captor.capture());
        DatabaseVendorAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getDatabaseVendorId());
        assertEquals("PostgreSQL", aud.getName());
        assertEquals(5432, aud.getDefaultPort());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        DatabaseVendor model = new DatabaseVendor();
        DatabaseVendorEntity toSave = new DatabaseVendorEntity();
        DatabaseVendorEntity saved = new DatabaseVendorEntity();
        saved.setId(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(databaseVendorMapper.toEntity(model)).thenReturn(toSave);
        when(databaseVendorJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<DatabaseVendorAudEntity> captor = ArgumentCaptor.forClass(DatabaseVendorAudEntity.class);
        verify(databaseVendorAudJPARepository).save(captor.capture());
        DatabaseVendorAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void create_dataAccessException_isRethrown() {
        adapter = buildAdapter();
        DatabaseVendor model = new DatabaseVendor();
        DatabaseVendorEntity toSave = new DatabaseVendorEntity();
        when(databaseVendorMapper.toEntity(model)).thenReturn(toSave);
        when(databaseVendorJPARepository.save(toSave)).thenThrow(new QueryTimeoutException("boom"));

        assertThrows(DataAccessException.class, () -> adapter.create(model));
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        DatabaseVendor model = new DatabaseVendor();
        DatabaseVendorEntity toSave = new DatabaseVendorEntity();
        DatabaseVendorEntity saved = new DatabaseVendorEntity();
        saved.setId(7L);
        DatabaseVendor response = new DatabaseVendor();
        when(databaseVendorMapper.toEntity(model)).thenReturn(toSave);
        when(databaseVendorJPARepository.save(toSave)).thenReturn(saved);
        when(databaseVendorMapper.toModel(saved)).thenReturn(response);

        DatabaseVendor result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<DatabaseVendorAudEntity> captor = ArgumentCaptor.forClass(DatabaseVendorAudEntity.class);
        verify(databaseVendorAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void update_dataAccessException_isRethrown() {
        adapter = buildAdapter();
        DatabaseVendor model = new DatabaseVendor();
        DatabaseVendorEntity toSave = new DatabaseVendorEntity();
        when(databaseVendorMapper.toEntity(model)).thenReturn(toSave);
        when(databaseVendorJPARepository.save(toSave)).thenThrow(new QueryTimeoutException("boom"));

        assertThrows(DataAccessException.class, () -> adapter.update(model, 7L));
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        DatabaseVendor model = new DatabaseVendor();
        model.setId(8L);
        DatabaseVendorEntity toSave = new DatabaseVendorEntity();
        DatabaseVendorEntity saved = new DatabaseVendorEntity();
        saved.setId(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(databaseVendorMapper.toEntity(model)).thenReturn(toSave);
        when(databaseVendorJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<DatabaseVendorAudEntity> captor = ArgumentCaptor.forClass(DatabaseVendorAudEntity.class);
        verify(databaseVendorAudJPARepository).save(captor.capture());
        DatabaseVendorAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }

    @Test
    void delete_dataAccessException_isRethrown() {
        adapter = buildAdapter();
        DatabaseVendor model = new DatabaseVendor();
        model.setId(8L);
        DatabaseVendorEntity toSave = new DatabaseVendorEntity();
        when(databaseVendorMapper.toEntity(model)).thenReturn(toSave);
        when(databaseVendorJPARepository.save(toSave)).thenThrow(new QueryTimeoutException("boom"));

        assertThrows(DataAccessException.class, () -> adapter.delete(model));
    }

    @Test
    void create_auditRepositoryDataAccessException_isRethrown() {
        adapter = buildAdapter();
        DatabaseVendor model = new DatabaseVendor();
        DatabaseVendorEntity toSave = new DatabaseVendorEntity();
        DatabaseVendorEntity saved = new DatabaseVendorEntity();
        saved.setId(9L);
        when(databaseVendorMapper.toEntity(model)).thenReturn(toSave);
        when(databaseVendorJPARepository.save(toSave)).thenReturn(saved);
        when(databaseVendorAudJPARepository.save(any(DatabaseVendorAudEntity.class)))
                .thenThrow(new QueryTimeoutException("boom"));

        assertThrows(DataAccessException.class, () -> adapter.create(model));
    }
}
