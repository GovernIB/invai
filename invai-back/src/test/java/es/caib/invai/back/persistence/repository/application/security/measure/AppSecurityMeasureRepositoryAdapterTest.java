package es.caib.invai.back.persistence.repository.application.security.measure;

import es.caib.invai.back.persistence.model.application.security.measure.AppSecurityMeasureAudEntity;
import es.caib.invai.back.persistence.model.application.security.measure.AppSecurityMeasureEntity;
import es.caib.invai.back.persistence.model.application.security.core.AppSecurityEntity;
import es.caib.invai.back.persistence.model.maintenance.security.securityMeasureType.SecurityMeasureTypeEntity;
import es.caib.invai.back.persistence.model.maintenance.security.ensRequirement.EnsRequirementEntity;
import es.caib.invai.back.service.mapper.application.security.measure.AppSecurityMeasureMapper;
import es.caib.invai.back.service.model.application.security.measure.AppSecurityMeasure;
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
 * Unit tests for {@link AppSecurityMeasureRepositoryAdapter}, verifying entity/model delegation to the
 * {@link AppSecurityMeasureJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class AppSecurityMeasureRepositoryAdapterTest {

    @Mock
    private AppSecurityMeasureJPARepository appSecurityMeasureJPARepository;

    @Mock
    private AppSecurityMeasureAudJPARepository appSecurityMeasureAudJPARepository;

    @Mock
    private AppSecurityMeasureMapper appSecurityMeasureMapper;

    private AppSecurityMeasureRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AppSecurityMeasureRepositoryAdapter buildAdapter() {
        AppSecurityMeasureRepositoryAdapter a = new AppSecurityMeasureRepositoryAdapter();
        ReflectionTestUtils.setField(a, "appSecurityMeasureJPARepository", appSecurityMeasureJPARepository);
        ReflectionTestUtils.setField(a, "appSecurityMeasureAudJPARepository", appSecurityMeasureAudJPARepository);
        ReflectionTestUtils.setField(a, "appSecurityMeasureMapper", appSecurityMeasureMapper);
        return a;
    }

    private AppSecurityMeasureEntity entityWithRelations(Long id) {
        AppSecurityMeasureEntity entity = new AppSecurityMeasureEntity();
        entity.setId(id);
        AppSecurityEntity appSecurity = new AppSecurityEntity();
        appSecurity.setId(10L);
        entity.setAppSecurity(appSecurity);
        SecurityMeasureTypeEntity type = new SecurityMeasureTypeEntity();
        type.setId(20L);
        entity.setType(type);
        EnsRequirementEntity ensRequirement = new EnsRequirementEntity();
        ensRequirement.setId(30L);
        entity.setEnsRequirement(ensRequirement);
        return entity;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        AppSecurityMeasureEntity entity = entityWithRelations(1L);
        AppSecurityMeasure model = new AppSecurityMeasure();
        when(appSecurityMeasureJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(appSecurityMeasureMapper.toModel(entity)).thenReturn(model);

        AppSecurityMeasure result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(appSecurityMeasureJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        AppSecurityMeasureCriteria criteria = new AppSecurityMeasureCriteria();
        Pageable pageable = Pageable.unpaged();
        AppSecurityMeasureEntity entity = entityWithRelations(1L);
        AppSecurityMeasure model = new AppSecurityMeasure();
        Page<AppSecurityMeasureEntity> entityPage = new PageImpl<>(List.of(entity));
        when(appSecurityMeasureJPARepository.findAll(ArgumentMatchers.<Specification<AppSecurityMeasureEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(appSecurityMeasureMapper.toModel(entity)).thenReturn(model);

        Page<AppSecurityMeasure> result = adapter.findAll(10L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        AppSecurityMeasure model = new AppSecurityMeasure();
        AppSecurityMeasureEntity toSave = new AppSecurityMeasureEntity();
        AppSecurityMeasureEntity saved = entityWithRelations(5L);
        AppSecurityMeasure response = new AppSecurityMeasure();
        when(appSecurityMeasureMapper.toEntity(model)).thenReturn(toSave);
        when(appSecurityMeasureJPARepository.save(toSave)).thenReturn(saved);
        when(appSecurityMeasureMapper.toModel(saved)).thenReturn(response);

        AppSecurityMeasure result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<AppSecurityMeasureAudEntity> captor = ArgumentCaptor.forClass(AppSecurityMeasureAudEntity.class);
        verify(appSecurityMeasureAudJPARepository).save(captor.capture());
        AppSecurityMeasureAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getAppSecurityMeasureId());
        assertEquals(10L, aud.getAppSecurityId());
        assertEquals(20L, aud.getTypeId());
        assertEquals(30L, aud.getEnsRequirementId());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        AppSecurityMeasure model = new AppSecurityMeasure();
        AppSecurityMeasureEntity toSave = new AppSecurityMeasureEntity();
        AppSecurityMeasureEntity saved = entityWithRelations(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(appSecurityMeasureMapper.toEntity(model)).thenReturn(toSave);
        when(appSecurityMeasureJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<AppSecurityMeasureAudEntity> captor = ArgumentCaptor.forClass(AppSecurityMeasureAudEntity.class);
        verify(appSecurityMeasureAudJPARepository).save(captor.capture());
        AppSecurityMeasureAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void create_nullOptionalRelations_writesNullTypeAndEnsRequirementIds() {
        adapter = buildAdapter();
        AppSecurityMeasure model = new AppSecurityMeasure();
        AppSecurityMeasureEntity toSave = new AppSecurityMeasureEntity();
        AppSecurityMeasureEntity saved = new AppSecurityMeasureEntity();
        saved.setId(9L);
        AppSecurityEntity appSecurity = new AppSecurityEntity();
        appSecurity.setId(10L);
        saved.setAppSecurity(appSecurity);
        saved.setType(null);
        saved.setEnsRequirement(null);
        when(appSecurityMeasureMapper.toEntity(model)).thenReturn(toSave);
        when(appSecurityMeasureJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<AppSecurityMeasureAudEntity> captor = ArgumentCaptor.forClass(AppSecurityMeasureAudEntity.class);
        verify(appSecurityMeasureAudJPARepository).save(captor.capture());
        AppSecurityMeasureAudEntity aud = captor.getValue();
        assertNull(aud.getTypeId());
        assertNull(aud.getEnsRequirementId());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        AppSecurityMeasure model = new AppSecurityMeasure();
        AppSecurityMeasureEntity toSave = new AppSecurityMeasureEntity();
        AppSecurityMeasureEntity saved = entityWithRelations(7L);
        AppSecurityMeasure response = new AppSecurityMeasure();
        when(appSecurityMeasureMapper.toEntity(model)).thenReturn(toSave);
        when(appSecurityMeasureJPARepository.save(toSave)).thenReturn(saved);
        when(appSecurityMeasureMapper.toModel(saved)).thenReturn(response);

        AppSecurityMeasure result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<AppSecurityMeasureAudEntity> captor = ArgumentCaptor.forClass(AppSecurityMeasureAudEntity.class);
        verify(appSecurityMeasureAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        AppSecurityMeasure model = new AppSecurityMeasure();
        model.setId(8L);
        AppSecurityMeasureEntity toSave = new AppSecurityMeasureEntity();
        AppSecurityMeasureEntity saved = entityWithRelations(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(appSecurityMeasureMapper.toEntity(model)).thenReturn(toSave);
        when(appSecurityMeasureJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<AppSecurityMeasureAudEntity> captor = ArgumentCaptor.forClass(AppSecurityMeasureAudEntity.class);
        verify(appSecurityMeasureAudJPARepository).save(captor.capture());
        AppSecurityMeasureAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
