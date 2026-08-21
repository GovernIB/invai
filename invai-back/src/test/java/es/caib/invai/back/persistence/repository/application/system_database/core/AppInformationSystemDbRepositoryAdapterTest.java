package es.caib.invai.back.persistence.repository.application.system_database.core;

import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.application.system_database.core.AppInformationSystemDbAudEntity;
import es.caib.invai.back.persistence.model.application.system_database.core.AppInformationSystemDbEntity;
import es.caib.invai.back.service.mapper.application.system_database.core.AppInformationSystemDbMapper;
import es.caib.invai.back.service.model.application.system_database.core.AppInformationSystemDb;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppInformationSystemDbRepositoryAdapter}, verifying entity/model delegation to the
 * {@link AppInformationSystemDbJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class AppInformationSystemDbRepositoryAdapterTest {

    @Mock
    private AppInformationSystemDbJPARepository appInformationSystemDbJPARepository;

    @Mock
    private AppInformationSystemDbAudJPARepository appInformationSystemDbAudJPARepository;

    @Mock
    private AppInformationSystemDbMapper appInformationSystemDbMapper;

    private AppInformationSystemDbRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AppInformationSystemDbRepositoryAdapter buildAdapter() {
        AppInformationSystemDbRepositoryAdapter a = new AppInformationSystemDbRepositoryAdapter();
        ReflectionTestUtils.setField(a, "appInformationSystemDbJPARepository", appInformationSystemDbJPARepository);
        ReflectionTestUtils.setField(a, "appInformationSystemDbAudJPARepository", appInformationSystemDbAudJPARepository);
        ReflectionTestUtils.setField(a, "appInformationSystemDbMapper", appInformationSystemDbMapper);
        return a;
    }

    private AppInformationSystemDbEntity entityWithApplication(Long id, Long applicationId) {
        AppInformationSystemDbEntity entity = new AppInformationSystemDbEntity();
        entity.setId(id);
        ApplicationEntity application = new ApplicationEntity();
        application.setId(applicationId);
        entity.setApplication(application);
        return entity;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        AppInformationSystemDbEntity entity = entityWithApplication(1L, 10L);
        AppInformationSystemDb model = new AppInformationSystemDb();
        when(appInformationSystemDbJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(appInformationSystemDbMapper.toModel(entity)).thenReturn(model);

        AppInformationSystemDb result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(appInformationSystemDbJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findByApplicationId_found_delegatesToJPARepositoryAndMapsModel() {
        adapter = buildAdapter();
        AppInformationSystemDbEntity entity = entityWithApplication(1L, 10L);
        AppInformationSystemDb model = new AppInformationSystemDb();
        when(appInformationSystemDbJPARepository.findByApplicationId(10L)).thenReturn(Optional.of(entity));
        when(appInformationSystemDbMapper.toModel(entity)).thenReturn(model);

        AppInformationSystemDb result = adapter.findByApplicationId(10L);

        assertSame(model, result);
    }

    @Test
    void findByApplicationId_notFound_returnsNull() {
        adapter = buildAdapter();
        when(appInformationSystemDbJPARepository.findByApplicationId(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findByApplicationId(99L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        AppInformationSystemDb model = new AppInformationSystemDb();
        AppInformationSystemDbEntity toSave = new AppInformationSystemDbEntity();
        AppInformationSystemDbEntity saved = entityWithApplication(5L, 10L);
        saved.setObservation("Legacy ledger");
        AppInformationSystemDb response = new AppInformationSystemDb();
        when(appInformationSystemDbMapper.toEntity(model)).thenReturn(toSave);
        when(appInformationSystemDbJPARepository.save(toSave)).thenReturn(saved);
        when(appInformationSystemDbMapper.toModel(saved)).thenReturn(response);

        AppInformationSystemDb result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<AppInformationSystemDbAudEntity> captor = ArgumentCaptor.forClass(AppInformationSystemDbAudEntity.class);
        verify(appInformationSystemDbAudJPARepository).save(captor.capture());
        AppInformationSystemDbAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getId());
        assertEquals(10L, aud.getApplicationId());
        assertEquals("Legacy ledger", aud.getObservation());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        AppInformationSystemDb model = new AppInformationSystemDb();
        AppInformationSystemDbEntity toSave = new AppInformationSystemDbEntity();
        AppInformationSystemDbEntity saved = entityWithApplication(6L, 10L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(appInformationSystemDbMapper.toEntity(model)).thenReturn(toSave);
        when(appInformationSystemDbJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<AppInformationSystemDbAudEntity> captor = ArgumentCaptor.forClass(AppInformationSystemDbAudEntity.class);
        verify(appInformationSystemDbAudJPARepository).save(captor.capture());
        AppInformationSystemDbAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        AppInformationSystemDb model = new AppInformationSystemDb();
        AppInformationSystemDbEntity toSave = new AppInformationSystemDbEntity();
        AppInformationSystemDbEntity saved = entityWithApplication(7L, 10L);
        AppInformationSystemDb response = new AppInformationSystemDb();
        when(appInformationSystemDbMapper.toEntity(model)).thenReturn(toSave);
        when(appInformationSystemDbJPARepository.save(toSave)).thenReturn(saved);
        when(appInformationSystemDbMapper.toModel(saved)).thenReturn(response);

        AppInformationSystemDb result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<AppInformationSystemDbAudEntity> captor = ArgumentCaptor.forClass(AppInformationSystemDbAudEntity.class);
        verify(appInformationSystemDbAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        AppInformationSystemDb model = new AppInformationSystemDb();
        model.setId(8L);
        AppInformationSystemDbEntity toSave = new AppInformationSystemDbEntity();
        AppInformationSystemDbEntity saved = entityWithApplication(8L, 10L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(appInformationSystemDbMapper.toEntity(model)).thenReturn(toSave);
        when(appInformationSystemDbJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<AppInformationSystemDbAudEntity> captor = ArgumentCaptor.forClass(AppInformationSystemDbAudEntity.class);
        verify(appInformationSystemDbAudJPARepository).save(captor.capture());
        AppInformationSystemDbAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
