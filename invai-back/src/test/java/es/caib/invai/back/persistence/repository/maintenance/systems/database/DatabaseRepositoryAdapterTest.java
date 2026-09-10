package es.caib.invai.back.persistence.repository.maintenance.systems.database;

import es.caib.invai.back.persistence.model.maintenance.systems.database.DatabaseAudEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.database.DatabaseEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.databaseVendor.DatabaseVendorEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.server.ServerEntity;
import es.caib.invai.back.service.mapper.maintenance.systems.database.DatabaseMapper;
import es.caib.invai.back.service.model.maintenance.systems.database.Database;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentMatchers;

/**
 * Unit tests for {@link DatabaseRepositoryAdapter}, verifying entity/model delegation to the
 * {@link DatabaseJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class DatabaseRepositoryAdapterTest {

    @Mock
    private DatabaseJPARepository databaseJPARepository;

    @Mock
    private DatabaseAudJPARepository databaseAudJPARepository;

    @Mock
    private DatabaseMapper databaseMapper;

    private DatabaseRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private DatabaseRepositoryAdapter buildAdapter() {
        DatabaseRepositoryAdapter a = new DatabaseRepositoryAdapter();
        ReflectionTestUtils.setField(a, "databaseJPARepository", databaseJPARepository);
        ReflectionTestUtils.setField(a, "databaseAudJPARepository", databaseAudJPARepository);
        ReflectionTestUtils.setField(a, "databaseMapper", databaseMapper);
        return a;
    }

    private ServerEntity buildServer(Long id) {
        ServerEntity server = new ServerEntity();
        server.setId(id);
        return server;
    }

    private DatabaseVendorEntity buildDatabaseType() {
        DatabaseVendorEntity vendor = new DatabaseVendorEntity();
        vendor.setId(3L);
        return vendor;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        DatabaseEntity entity = new DatabaseEntity();
        entity.setId(1L);
        Database model = new Database();
        when(databaseJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(databaseMapper.toModel(entity)).thenReturn(model);

        Database result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(databaseJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        DatabaseCriteria criteria = new DatabaseCriteria();
        Pageable pageable = Pageable.unpaged();
        DatabaseEntity entity = new DatabaseEntity();
        Database model = new Database();
        Page<DatabaseEntity> entityPage = new PageImpl<>(List.of(entity));
        when(databaseJPARepository.findAll(ArgumentMatchers.<Specification<DatabaseEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(databaseMapper.toModel(entity)).thenReturn(model);

        Page<Database> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByServerAndService_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(databaseJPARepository.existsByServerIdAndService(10L, "invai_db")).thenReturn(true);

        assertTrue(adapter.existsByServerAndService(10L, "invai_db"));
    }

    @Test
    void existsByServerAndServiceAndIdNot_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(databaseJPARepository.existsByServerIdAndServiceAndIdNot(10L, "invai_db", 1L)).thenReturn(true);

        assertTrue(adapter.existsByServerAndServiceAndIdNot(10L, "invai_db", 1L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        Database model = new Database();
        DatabaseEntity toSave = new DatabaseEntity();
        DatabaseEntity saved = new DatabaseEntity();
        saved.setId(5L);
        saved.setServer(buildServer(10L));
        saved.setService("invai_db");
        saved.setPort(5432);
        saved.setDatabaseType(buildDatabaseType());
        saved.setDescription("Main database");
        Database response = new Database();
        when(databaseMapper.toEntity(model)).thenReturn(toSave);
        when(databaseJPARepository.save(toSave)).thenReturn(saved);
        when(databaseMapper.toModel(saved)).thenReturn(response);

        Database result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<DatabaseAudEntity> captor = ArgumentCaptor.forClass(DatabaseAudEntity.class);
        verify(databaseAudJPARepository).save(captor.capture());
        DatabaseAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getDatabaseId());
        assertEquals(10L, aud.getServerId());
        assertEquals("invai_db", aud.getService());
        assertEquals(5432, aud.getPort());
        assertEquals(3L, aud.getDatabaseTypeId());
        assertEquals("Main database", aud.getDescription());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNull(aud.getUpdatedAt());
        assertNull(aud.getUpdatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithNullDatabaseType_setsAuditDatabaseTypeIdNull() {
        adapter = buildAdapter();
        Database model = new Database();
        DatabaseEntity toSave = new DatabaseEntity();
        DatabaseEntity saved = new DatabaseEntity();
        saved.setId(6L);
        saved.setServer(buildServer(11L));
        saved.setService("no_vendor_db");
        saved.setDatabaseType(null);
        when(databaseMapper.toEntity(model)).thenReturn(toSave);
        when(databaseJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<DatabaseAudEntity> captor = ArgumentCaptor.forClass(DatabaseAudEntity.class);
        verify(databaseAudJPARepository).save(captor.capture());
        assertNull(captor.getValue().getDatabaseTypeId());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        Database model = new Database();
        DatabaseEntity toSave = new DatabaseEntity();
        DatabaseEntity saved = new DatabaseEntity();
        saved.setId(7L);
        saved.setServer(buildServer(12L));
        saved.setService("invai_db");
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime existingUpdatedAt = LocalDateTime.of(2025, 2, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        saved.setUpdatedAt(existingUpdatedAt);
        saved.setUpdatedBy("asmith");
        when(databaseMapper.toEntity(model)).thenReturn(toSave);
        when(databaseJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<DatabaseAudEntity> captor = ArgumentCaptor.forClass(DatabaseAudEntity.class);
        verify(databaseAudJPARepository).save(captor.capture());
        DatabaseAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
        assertEquals(existingUpdatedAt, aud.getUpdatedAt());
        assertEquals("asmith", aud.getUpdatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        Database model = new Database();
        DatabaseEntity toSave = new DatabaseEntity();
        DatabaseEntity saved = new DatabaseEntity();
        saved.setId(8L);
        saved.setServer(buildServer(13L));
        saved.setService("invai_db");
        Database response = new Database();
        when(databaseMapper.toEntity(model)).thenReturn(toSave);
        when(databaseJPARepository.save(toSave)).thenReturn(saved);
        when(databaseMapper.toModel(saved)).thenReturn(response);

        Database result = adapter.update(model, 8L);

        assertSame(response, result);
        assertEquals(8L, toSave.getId());
        ArgumentCaptor<DatabaseAudEntity> captor = ArgumentCaptor.forClass(DatabaseAudEntity.class);
        verify(databaseAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        Database model = new Database();
        model.setId(9L);
        DatabaseEntity toSave = new DatabaseEntity();
        DatabaseEntity saved = new DatabaseEntity();
        saved.setId(9L);
        saved.setServer(buildServer(14L));
        saved.setService("invai_db");
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(databaseMapper.toEntity(model)).thenReturn(toSave);
        when(databaseJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(9L, toSave.getId());
        ArgumentCaptor<DatabaseAudEntity> captor = ArgumentCaptor.forClass(DatabaseAudEntity.class);
        verify(databaseAudJPARepository).save(captor.capture());
        DatabaseAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
