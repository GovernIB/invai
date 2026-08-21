package es.caib.invai.back.persistence.repository.application.system_database.database;

import es.caib.invai.back.persistence.model.application.system_database.database.AppDatabaseAudEntity;
import es.caib.invai.back.persistence.model.application.system_database.database.AppDatabaseEntity;
import es.caib.invai.back.persistence.model.application.system_database.core.AppInformationSystemDbEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.database.DatabaseEntity;
import es.caib.invai.back.service.mapper.application.system_database.database.AppDatabaseMapper;
import es.caib.invai.back.service.model.application.system_database.database.AppDatabase;
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
 * Unit tests for {@link AppDatabaseRepositoryAdapter}, verifying entity/model delegation to the
 * {@link AppDatabaseJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class AppDatabaseRepositoryAdapterTest {

    @Mock
    private AppDatabaseJPARepository appDatabaseJPARepository;

    @Mock
    private AppDatabaseAudJPARepository appDatabaseAudJPARepository;

    @Mock
    private AppDatabaseMapper appDatabaseMapper;

    private AppDatabaseRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AppDatabaseRepositoryAdapter buildAdapter() {
        AppDatabaseRepositoryAdapter a = new AppDatabaseRepositoryAdapter();
        ReflectionTestUtils.setField(a, "appDatabaseJPARepository", appDatabaseJPARepository);
        ReflectionTestUtils.setField(a, "appDatabaseAudJPARepository", appDatabaseAudJPARepository);
        ReflectionTestUtils.setField(a, "appDatabaseMapper", appDatabaseMapper);
        return a;
    }

    private AppDatabaseEntity entityWithRelations(Long id, Long informationSystemDbId, Long databaseId) {
        AppDatabaseEntity entity = new AppDatabaseEntity();
        entity.setId(id);
        AppInformationSystemDbEntity informationSystemDb = new AppInformationSystemDbEntity();
        informationSystemDb.setId(informationSystemDbId);
        entity.setInformationSystemDb(informationSystemDb);
        DatabaseEntity database = new DatabaseEntity();
        database.setId(databaseId);
        entity.setDatabase(database);
        return entity;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        AppDatabaseEntity entity = entityWithRelations(1L, 10L, 20L);
        AppDatabase model = new AppDatabase();
        when(appDatabaseJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(appDatabaseMapper.toModel(entity)).thenReturn(model);

        AppDatabase result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(appDatabaseJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        AppDatabaseCriteria criteria = new AppDatabaseCriteria();
        Pageable pageable = Pageable.unpaged();
        AppDatabaseEntity entity = entityWithRelations(1L, 10L, 20L);
        AppDatabase model = new AppDatabase();
        Page<AppDatabaseEntity> entityPage = new PageImpl<>(List.of(entity));
        when(appDatabaseJPARepository.findAll(ArgumentMatchers.<Specification<AppDatabaseEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(appDatabaseMapper.toModel(entity)).thenReturn(model);

        Page<AppDatabase> result = adapter.findAll(10L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByInformationSystemDbIdAndDatabaseId_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(appDatabaseJPARepository.existsByInformationSystemDbIdAndDatabaseId(10L, 20L)).thenReturn(true);

        assertEquals(true, adapter.existsByInformationSystemDbIdAndDatabaseId(10L, 20L));
    }

    @Test
    void existsByUniqueCombinationExcludingId_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(appDatabaseJPARepository.existsByInformationSystemDbIdAndDatabaseIdAndIdNot(10L, 20L, 1L)).thenReturn(true);

        assertEquals(true, adapter.existsByUniqueCombinationExcludingId(10L, 20L, 1L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        AppDatabase model = new AppDatabase();
        AppDatabaseEntity toSave = new AppDatabaseEntity();
        AppDatabaseEntity saved = entityWithRelations(5L, 10L, 20L);
        AppDatabase response = new AppDatabase();
        when(appDatabaseMapper.toEntity(model)).thenReturn(toSave);
        when(appDatabaseJPARepository.save(toSave)).thenReturn(saved);
        when(appDatabaseMapper.toModel(saved)).thenReturn(response);

        AppDatabase result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<AppDatabaseAudEntity> captor = ArgumentCaptor.forClass(AppDatabaseAudEntity.class);
        verify(appDatabaseAudJPARepository).save(captor.capture());
        AppDatabaseAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getAppDatabaseId());
        assertEquals(10L, aud.getInformationSystemDbId());
        assertEquals(20L, aud.getDatabaseId());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        AppDatabase model = new AppDatabase();
        AppDatabaseEntity toSave = new AppDatabaseEntity();
        AppDatabaseEntity saved = entityWithRelations(6L, 10L, 20L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(appDatabaseMapper.toEntity(model)).thenReturn(toSave);
        when(appDatabaseJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<AppDatabaseAudEntity> captor = ArgumentCaptor.forClass(AppDatabaseAudEntity.class);
        verify(appDatabaseAudJPARepository).save(captor.capture());
        AppDatabaseAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        AppDatabase model = new AppDatabase();
        AppDatabaseEntity toSave = new AppDatabaseEntity();
        AppDatabaseEntity saved = entityWithRelations(7L, 10L, 20L);
        AppDatabase response = new AppDatabase();
        when(appDatabaseMapper.toEntity(model)).thenReturn(toSave);
        when(appDatabaseJPARepository.save(toSave)).thenReturn(saved);
        when(appDatabaseMapper.toModel(saved)).thenReturn(response);

        AppDatabase result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<AppDatabaseAudEntity> captor = ArgumentCaptor.forClass(AppDatabaseAudEntity.class);
        verify(appDatabaseAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        AppDatabase model = new AppDatabase();
        model.setId(8L);
        AppDatabaseEntity toSave = new AppDatabaseEntity();
        AppDatabaseEntity saved = entityWithRelations(8L, 10L, 20L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(appDatabaseMapper.toEntity(model)).thenReturn(toSave);
        when(appDatabaseJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<AppDatabaseAudEntity> captor = ArgumentCaptor.forClass(AppDatabaseAudEntity.class);
        verify(appDatabaseAudJPARepository).save(captor.capture());
        AppDatabaseAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
