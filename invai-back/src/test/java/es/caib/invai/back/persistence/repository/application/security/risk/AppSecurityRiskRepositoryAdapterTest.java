package es.caib.invai.back.persistence.repository.application.security.risk;

import es.caib.invai.back.persistence.model.application.security.risk.AppSecurityRiskAudEntity;
import es.caib.invai.back.persistence.model.application.security.risk.AppSecurityRiskEntity;
import es.caib.invai.back.persistence.model.application.security.core.AppSecurityEntity;
import es.caib.invai.back.persistence.model.catalog.securityLevel.LkupSecurityLevelEntity;
import es.caib.invai.back.persistence.model.maintenance.general.field.FieldEntity;
import es.caib.invai.back.service.mapper.application.security.risk.AppSecurityRiskMapper;
import es.caib.invai.back.service.model.application.security.risk.AppSecurityRisk;
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
 * Unit tests for {@link AppSecurityRiskRepositoryAdapter}, verifying entity/model delegation to the
 * {@link AppSecurityRiskJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class AppSecurityRiskRepositoryAdapterTest {

    @Mock
    private AppSecurityRiskJPARepository appSecurityRiskJPARepository;

    @Mock
    private AppSecurityRiskAudJPARepository appSecurityRiskAudJPARepository;

    @Mock
    private AppSecurityRiskMapper appSecurityRiskMapper;

    private AppSecurityRiskRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AppSecurityRiskRepositoryAdapter buildAdapter() {
        AppSecurityRiskRepositoryAdapter a = new AppSecurityRiskRepositoryAdapter();
        ReflectionTestUtils.setField(a, "appSecurityRiskJPARepository", appSecurityRiskJPARepository);
        ReflectionTestUtils.setField(a, "appSecurityRiskAudJPARepository", appSecurityRiskAudJPARepository);
        ReflectionTestUtils.setField(a, "appSecurityRiskMapper", appSecurityRiskMapper);
        return a;
    }

    private AppSecurityRiskEntity entityWithRelations(Long id) {
        AppSecurityRiskEntity entity = new AppSecurityRiskEntity();
        entity.setId(id);
        AppSecurityEntity appSecurity = new AppSecurityEntity();
        appSecurity.setId(10L);
        entity.setAppSecurity(appSecurity);
        LkupSecurityLevelEntity level = new LkupSecurityLevelEntity();
        level.setId(30L);
        entity.setLevel(level);
        FieldEntity field = new FieldEntity();
        field.setId(40L);
        entity.setField(field);
        return entity;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        AppSecurityRiskEntity entity = entityWithRelations(1L);
        AppSecurityRisk model = new AppSecurityRisk();
        when(appSecurityRiskJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(appSecurityRiskMapper.toModel(entity)).thenReturn(model);

        AppSecurityRisk result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(appSecurityRiskJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        AppSecurityRiskCriteria criteria = new AppSecurityRiskCriteria();
        Pageable pageable = Pageable.unpaged();
        AppSecurityRiskEntity entity = entityWithRelations(1L);
        AppSecurityRisk model = new AppSecurityRisk();
        Page<AppSecurityRiskEntity> entityPage = new PageImpl<>(List.of(entity));
        when(appSecurityRiskJPARepository.findAll(ArgumentMatchers.<Specification<AppSecurityRiskEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(appSecurityRiskMapper.toModel(entity)).thenReturn(model);

        Page<AppSecurityRisk> result = adapter.findAll(10L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        AppSecurityRisk model = new AppSecurityRisk();
        AppSecurityRiskEntity toSave = new AppSecurityRiskEntity();
        AppSecurityRiskEntity saved = entityWithRelations(5L);
        AppSecurityRisk response = new AppSecurityRisk();
        when(appSecurityRiskMapper.toEntity(model)).thenReturn(toSave);
        when(appSecurityRiskJPARepository.save(toSave)).thenReturn(saved);
        when(appSecurityRiskMapper.toModel(saved)).thenReturn(response);

        AppSecurityRisk result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<AppSecurityRiskAudEntity> captor = ArgumentCaptor.forClass(AppSecurityRiskAudEntity.class);
        verify(appSecurityRiskAudJPARepository).save(captor.capture());
        AppSecurityRiskAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getAppSecurityRiskId());
        assertEquals(10L, aud.getAppSecurityId());
        assertEquals(30L, aud.getLevelId());
        assertEquals(40L, aud.getFieldId());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithNullOptionalRelations_doesNotNpeAndWritesNullIds() {
        adapter = buildAdapter();
        AppSecurityRisk model = new AppSecurityRisk();
        AppSecurityRiskEntity toSave = new AppSecurityRiskEntity();
        AppSecurityRiskEntity saved = new AppSecurityRiskEntity();
        saved.setId(9L);
        AppSecurityEntity appSecurity = new AppSecurityEntity();
        appSecurity.setId(10L);
        saved.setAppSecurity(appSecurity);
        // level and field intentionally left null, since both are optional relations.
        when(appSecurityRiskMapper.toEntity(model)).thenReturn(toSave);
        when(appSecurityRiskJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<AppSecurityRiskAudEntity> captor = ArgumentCaptor.forClass(AppSecurityRiskAudEntity.class);
        verify(appSecurityRiskAudJPARepository).save(captor.capture());
        AppSecurityRiskAudEntity aud = captor.getValue();
        assertNull(aud.getLevelId());
        assertNull(aud.getFieldId());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        AppSecurityRisk model = new AppSecurityRisk();
        AppSecurityRiskEntity toSave = new AppSecurityRiskEntity();
        AppSecurityRiskEntity saved = entityWithRelations(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(appSecurityRiskMapper.toEntity(model)).thenReturn(toSave);
        when(appSecurityRiskJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<AppSecurityRiskAudEntity> captor = ArgumentCaptor.forClass(AppSecurityRiskAudEntity.class);
        verify(appSecurityRiskAudJPARepository).save(captor.capture());
        AppSecurityRiskAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        AppSecurityRisk model = new AppSecurityRisk();
        AppSecurityRiskEntity toSave = new AppSecurityRiskEntity();
        AppSecurityRiskEntity saved = entityWithRelations(7L);
        AppSecurityRisk response = new AppSecurityRisk();
        when(appSecurityRiskMapper.toEntity(model)).thenReturn(toSave);
        when(appSecurityRiskJPARepository.save(toSave)).thenReturn(saved);
        when(appSecurityRiskMapper.toModel(saved)).thenReturn(response);

        AppSecurityRisk result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<AppSecurityRiskAudEntity> captor = ArgumentCaptor.forClass(AppSecurityRiskAudEntity.class);
        verify(appSecurityRiskAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        AppSecurityRisk model = new AppSecurityRisk();
        model.setId(8L);
        AppSecurityRiskEntity toSave = new AppSecurityRiskEntity();
        AppSecurityRiskEntity saved = entityWithRelations(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(appSecurityRiskMapper.toEntity(model)).thenReturn(toSave);
        when(appSecurityRiskJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<AppSecurityRiskAudEntity> captor = ArgumentCaptor.forClass(AppSecurityRiskAudEntity.class);
        verify(appSecurityRiskAudJPARepository).save(captor.capture());
        AppSecurityRiskAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
