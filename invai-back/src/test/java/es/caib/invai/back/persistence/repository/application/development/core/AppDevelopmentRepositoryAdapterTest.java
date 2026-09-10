package es.caib.invai.back.persistence.repository.application.development.core;

import es.caib.invai.back.persistence.model.catalog.modality.LkupModalityEntity;
import es.caib.invai.back.persistence.model.catalog.standardAdaption.LkupStandardAdaptionEntity;
import es.caib.invai.back.service.mapper.application.development.core.AppDevelopmentMapper;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
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
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.environment.EnvironmentEntity;
import es.caib.invai.back.persistence.model.application.development.core.AppDevelopmentAudEntity;
import es.caib.invai.back.persistence.model.application.development.core.AppDevelopmentEntity;

/**
 * Unit tests for {@link AppDevelopmentRepositoryAdapter}, verifying entity/model delegation to the
 * {@link AppDevelopmentJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class AppDevelopmentRepositoryAdapterTest {

    @Mock
    private AppDevelopmentJPARepository appDevelopmentJPARepository;

    @Mock
    private AppDevelopmentAudJPARepository appDevelopmentAudJPARepository;

    @Mock
    private AppDevelopmentMapper appDevelopmentMapper;

    private AppDevelopmentRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AppDevelopmentRepositoryAdapter buildAdapter() {
        AppDevelopmentRepositoryAdapter a = new AppDevelopmentRepositoryAdapter();
        ReflectionTestUtils.setField(a, "appDevelopmentJPARepository", appDevelopmentJPARepository);
        ReflectionTestUtils.setField(a, "appDevelopmentAudJPARepository", appDevelopmentAudJPARepository);
        ReflectionTestUtils.setField(a, "appDevelopmentMapper", appDevelopmentMapper);
        return a;
    }

    private ApplicationEntity applicationEntity(Long id) {
        ApplicationEntity application = new ApplicationEntity();
        application.setId(id);
        return application;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        AppDevelopmentEntity entity = new AppDevelopmentEntity();
        entity.setId(1L);
        AppDevelopment model = new AppDevelopment();
        when(appDevelopmentJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(appDevelopmentMapper.toModel(entity)).thenReturn(model);

        AppDevelopment result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(appDevelopmentJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findByApplicationId_found_delegatesToJPARepositoryAndMapsModel() {
        adapter = buildAdapter();
        AppDevelopmentEntity entity = new AppDevelopmentEntity();
        AppDevelopment model = new AppDevelopment();
        when(appDevelopmentJPARepository.findByApplicationId(10L)).thenReturn(Optional.of(entity));
        when(appDevelopmentMapper.toModel(entity)).thenReturn(model);

        AppDevelopment result = adapter.findByApplicationId(10L);

        assertSame(model, result);
    }

    @Test
    void findByApplicationId_notFound_returnsNull() {
        adapter = buildAdapter();
        when(appDevelopmentJPARepository.findByApplicationId(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findByApplicationId(99L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        AppDevelopment model = new AppDevelopment();
        AppDevelopmentEntity toSave = new AppDevelopmentEntity();
        AppDevelopmentEntity saved = new AppDevelopmentEntity();
        saved.setId(5L);
        saved.setApplication(applicationEntity(20L));
        EnvironmentEntity environment = new EnvironmentEntity();
        environment.setId(30L);
        saved.setEnvironment(environment);
        LkupModalityEntity modality = new LkupModalityEntity();
        modality.setId(40L);
        saved.setModality(modality);
        saved.setCode("DEV-001");
        LkupStandardAdaptionEntity standardAdaption = new LkupStandardAdaptionEntity();
        standardAdaption.setId(50L);
        saved.setStandardAdaption(standardAdaption);
        saved.setRevisionDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        saved.setObservation("Some observation");
        AppDevelopment response = new AppDevelopment();
        when(appDevelopmentMapper.toEntity(model)).thenReturn(toSave);
        when(appDevelopmentJPARepository.save(toSave)).thenReturn(saved);
        when(appDevelopmentMapper.toModel(saved)).thenReturn(response);

        AppDevelopment result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<AppDevelopmentAudEntity> captor = ArgumentCaptor.forClass(AppDevelopmentAudEntity.class);
        verify(appDevelopmentAudJPARepository).save(captor.capture());
        AppDevelopmentAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getDevelopmentId());
        assertEquals(20L, aud.getApplicationId());
        assertEquals(30L, aud.getEnvironmentId());
        assertEquals(40L, aud.getModalityId());
        assertEquals("DEV-001", aud.getCode());
        assertEquals(50L, aud.getStandardAdaptionId());
        assertEquals(saved.getRevisionDate(), aud.getRevisionDate());
        assertEquals("Some observation", aud.getObservation());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNull(aud.getUpdatedAt());
        assertNull(aud.getUpdatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_withNullEnvironmentModalityAndStandardAdaption_setsNullReferenceIdsOnAuditRecord() {
        adapter = buildAdapter();
        AppDevelopment model = new AppDevelopment();
        AppDevelopmentEntity toSave = new AppDevelopmentEntity();
        AppDevelopmentEntity saved = new AppDevelopmentEntity();
        saved.setId(6L);
        saved.setApplication(applicationEntity(21L));
        saved.setEnvironment(null);
        saved.setModality(null);
        saved.setStandardAdaption(null);
        when(appDevelopmentMapper.toEntity(model)).thenReturn(toSave);
        when(appDevelopmentJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<AppDevelopmentAudEntity> captor = ArgumentCaptor.forClass(AppDevelopmentAudEntity.class);
        verify(appDevelopmentAudJPARepository).save(captor.capture());
        AppDevelopmentAudEntity aud = captor.getValue();
        assertNull(aud.getEnvironmentId());
        assertNull(aud.getModalityId());
        assertNull(aud.getStandardAdaptionId());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        AppDevelopment model = new AppDevelopment();
        AppDevelopmentEntity toSave = new AppDevelopmentEntity();
        AppDevelopmentEntity saved = new AppDevelopmentEntity();
        saved.setId(7L);
        saved.setApplication(applicationEntity(22L));
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        LocalDateTime existingUpdatedAt = LocalDateTime.of(2025, 6, 1, 0, 0);
        saved.setUpdatedAt(existingUpdatedAt);
        saved.setUpdatedBy("asmith");
        when(appDevelopmentMapper.toEntity(model)).thenReturn(toSave);
        when(appDevelopmentJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<AppDevelopmentAudEntity> captor = ArgumentCaptor.forClass(AppDevelopmentAudEntity.class);
        verify(appDevelopmentAudJPARepository).save(captor.capture());
        AppDevelopmentAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
        assertEquals(existingUpdatedAt, aud.getUpdatedAt());
        assertEquals("asmith", aud.getUpdatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        AppDevelopment model = new AppDevelopment();
        AppDevelopmentEntity toSave = new AppDevelopmentEntity();
        AppDevelopmentEntity saved = new AppDevelopmentEntity();
        saved.setId(8L);
        saved.setApplication(applicationEntity(23L));
        AppDevelopment response = new AppDevelopment();
        when(appDevelopmentMapper.toEntity(model)).thenReturn(toSave);
        when(appDevelopmentJPARepository.save(toSave)).thenReturn(saved);
        when(appDevelopmentMapper.toModel(saved)).thenReturn(response);

        AppDevelopment result = adapter.update(model, 8L);

        assertSame(response, result);
        assertEquals(8L, toSave.getId());
        ArgumentCaptor<AppDevelopmentAudEntity> captor = ArgumentCaptor.forClass(AppDevelopmentAudEntity.class);
        verify(appDevelopmentAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        AppDevelopment model = new AppDevelopment();
        model.setId(9L);
        AppDevelopmentEntity toSave = new AppDevelopmentEntity();
        AppDevelopmentEntity saved = new AppDevelopmentEntity();
        saved.setId(9L);
        saved.setApplication(applicationEntity(24L));
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(appDevelopmentMapper.toEntity(model)).thenReturn(toSave);
        when(appDevelopmentJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(9L, toSave.getId());
        ArgumentCaptor<AppDevelopmentAudEntity> captor = ArgumentCaptor.forClass(AppDevelopmentAudEntity.class);
        verify(appDevelopmentAudJPARepository).save(captor.capture());
        AppDevelopmentAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
