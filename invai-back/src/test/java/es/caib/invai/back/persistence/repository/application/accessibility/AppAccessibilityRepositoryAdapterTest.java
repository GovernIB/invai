package es.caib.invai.back.persistence.repository.application.accessibility;

import es.caib.invai.back.persistence.model.application.accessibility.AppAccessibilityAudEntity;
import es.caib.invai.back.persistence.model.application.accessibility.AppAccessibilityEntity;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.maintenance.complianceSituation.ComplianceSituationEntity;
import es.caib.invai.back.persistence.model.maintenance.classificationSegment.ClassificationSegmentEntity;
import es.caib.invai.back.service.mapper.application.accessibility.AppAccessibilityMapper;
import es.caib.invai.back.service.model.application.accessibility.AppAccessibility;
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
 * Unit tests for {@link AppAccessibilityRepositoryAdapter}, verifying entity/model delegation to
 * the {@link AppAccessibilityJPARepository} and the historical audit record written on every
 * mutation.
 */
@ExtendWith(MockitoExtension.class)
class AppAccessibilityRepositoryAdapterTest {

    @Mock
    private AppAccessibilityJPARepository appAccessibilityJPARepository;

    @Mock
    private AppAccessibilityAudJPARepository appAccessibilityAudJPARepository;

    @Mock
    private AppAccessibilityMapper appAccessibilityMapper;

    private AppAccessibilityRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AppAccessibilityRepositoryAdapter buildAdapter() {
        AppAccessibilityRepositoryAdapter a = new AppAccessibilityRepositoryAdapter();
        ReflectionTestUtils.setField(a, "appAccessibilityJPARepository", appAccessibilityJPARepository);
        ReflectionTestUtils.setField(a, "appAccessibilityAudJPARepository", appAccessibilityAudJPARepository);
        ReflectionTestUtils.setField(a, "appAccessibilityMapper", appAccessibilityMapper);
        return a;
    }

    private AppAccessibilityEntity entityWithApplication(Long id) {
        AppAccessibilityEntity entity = new AppAccessibilityEntity();
        entity.setId(id);
        ApplicationEntity application = new ApplicationEntity();
        application.setId(10L);
        entity.setApplication(application);
        return entity;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        AppAccessibilityEntity entity = entityWithApplication(1L);
        AppAccessibility model = new AppAccessibility();
        when(appAccessibilityJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(appAccessibilityMapper.toModel(entity)).thenReturn(model);

        AppAccessibility result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(appAccessibilityJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findByApplicationId_found_delegatesToJPARepositoryAndMapsModel() {
        adapter = buildAdapter();
        AppAccessibilityEntity entity = entityWithApplication(1L);
        AppAccessibility model = new AppAccessibility();
        when(appAccessibilityJPARepository.findByApplicationId(10L)).thenReturn(Optional.of(entity));
        when(appAccessibilityMapper.toModel(entity)).thenReturn(model);

        AppAccessibility result = adapter.findByApplicationId(10L);

        assertSame(model, result);
    }

    @Test
    void findByApplicationId_notFound_returnsNull() {
        adapter = buildAdapter();
        when(appAccessibilityJPARepository.findByApplicationId(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findByApplicationId(99L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        AppAccessibility model = new AppAccessibility();
        AppAccessibilityEntity toSave = new AppAccessibilityEntity();
        AppAccessibilityEntity saved = entityWithApplication(5L);
        ComplianceSituationEntity complianceEntity = new ComplianceSituationEntity();
        complianceEntity.setId(40L);
        saved.setCompliance(complianceEntity);
        ClassificationSegmentEntity classificationSegmentEntity = new ClassificationSegmentEntity();
        classificationSegmentEntity.setId(41L);
        saved.setClassificationSegment(classificationSegmentEntity);
        saved.setPublicUrl("https://invai.example.test");
        saved.setMobileApplication(Boolean.TRUE);
        saved.setMobileApplicationName("APP INVAI Android");
        saved.setNonAccessibleContent("PDF antic");
        saved.setObservations("Pendent");
        saved.setExpireDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        AppAccessibility response = new AppAccessibility();
        when(appAccessibilityMapper.toEntity(model)).thenReturn(toSave);
        when(appAccessibilityJPARepository.save(toSave)).thenReturn(saved);
        when(appAccessibilityMapper.toModel(saved)).thenReturn(response);

        AppAccessibility result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<AppAccessibilityAudEntity> captor = ArgumentCaptor.forClass(AppAccessibilityAudEntity.class);
        verify(appAccessibilityAudJPARepository).save(captor.capture());
        AppAccessibilityAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getAppAccessibilityId());
        assertEquals(10L, aud.getApplicationId());
        assertEquals(40L, aud.getComplianceId());
        assertEquals(41L, aud.getClassificationSegmentId());
        assertEquals("https://invai.example.test", aud.getPublicUrl());
        assertEquals(Boolean.TRUE, aud.getMobileApplication());
        assertEquals("APP INVAI Android", aud.getMobileApplicationName());
        assertEquals("PDF antic", aud.getNonAccessibleContent());
        assertEquals("Pendent", aud.getObservations());
        assertEquals(saved.getExpireDate(), aud.getExpireDate());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        AppAccessibility model = new AppAccessibility();
        AppAccessibilityEntity toSave = new AppAccessibilityEntity();
        AppAccessibilityEntity saved = entityWithApplication(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(appAccessibilityMapper.toEntity(model)).thenReturn(toSave);
        when(appAccessibilityJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<AppAccessibilityAudEntity> captor = ArgumentCaptor.forClass(AppAccessibilityAudEntity.class);
        verify(appAccessibilityAudJPARepository).save(captor.capture());
        AppAccessibilityAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        AppAccessibility model = new AppAccessibility();
        AppAccessibilityEntity toSave = new AppAccessibilityEntity();
        AppAccessibilityEntity saved = entityWithApplication(7L);
        AppAccessibility response = new AppAccessibility();
        when(appAccessibilityMapper.toEntity(model)).thenReturn(toSave);
        when(appAccessibilityJPARepository.save(toSave)).thenReturn(saved);
        when(appAccessibilityMapper.toModel(saved)).thenReturn(response);

        AppAccessibility result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<AppAccessibilityAudEntity> captor = ArgumentCaptor.forClass(AppAccessibilityAudEntity.class);
        verify(appAccessibilityAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        AppAccessibility model = new AppAccessibility();
        model.setId(8L);
        AppAccessibilityEntity toSave = new AppAccessibilityEntity();
        AppAccessibilityEntity saved = entityWithApplication(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(appAccessibilityMapper.toEntity(model)).thenReturn(toSave);
        when(appAccessibilityJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<AppAccessibilityAudEntity> captor = ArgumentCaptor.forClass(AppAccessibilityAudEntity.class);
        verify(appAccessibilityAudJPARepository).save(captor.capture());
        AppAccessibilityAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
