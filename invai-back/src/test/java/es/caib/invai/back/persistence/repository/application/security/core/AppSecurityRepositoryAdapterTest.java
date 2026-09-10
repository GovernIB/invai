package es.caib.invai.back.persistence.repository.application.security.core;

import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.application.security.core.AppSecurityAudEntity;
import es.caib.invai.back.persistence.model.application.security.core.AppSecurityEntity;
import es.caib.invai.back.service.mapper.application.security.core.AppSecurityMapper;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
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
 * Unit tests for {@link AppSecurityRepositoryAdapter}, verifying entity/model delegation to the
 * {@link AppSecurityJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class AppSecurityRepositoryAdapterTest {

    @Mock
    private AppSecurityJPARepository appSecurityJPARepository;

    @Mock
    private AppSecurityAudJPARepository appSecurityAudJPARepository;

    @Mock
    private AppSecurityMapper appSecurityMapper;

    private AppSecurityRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AppSecurityRepositoryAdapter buildAdapter() {
        AppSecurityRepositoryAdapter a = new AppSecurityRepositoryAdapter();
        ReflectionTestUtils.setField(a, "appSecurityJPARepository", appSecurityJPARepository);
        ReflectionTestUtils.setField(a, "appSecurityAudJPARepository", appSecurityAudJPARepository);
        ReflectionTestUtils.setField(a, "appSecurityMapper", appSecurityMapper);
        return a;
    }

    private AppSecurityEntity entityWithApplication(Long id) {
        AppSecurityEntity entity = new AppSecurityEntity();
        entity.setId(id);
        ApplicationEntity application = new ApplicationEntity();
        application.setId(10L);
        entity.setApplication(application);
        return entity;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        AppSecurityEntity entity = entityWithApplication(1L);
        AppSecurity model = new AppSecurity();
        when(appSecurityJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(appSecurityMapper.toModel(entity)).thenReturn(model);

        AppSecurity result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(appSecurityJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findByApplicationId_found_delegatesToJPARepositoryAndMapsModel() {
        adapter = buildAdapter();
        AppSecurityEntity entity = entityWithApplication(1L);
        AppSecurity model = new AppSecurity();
        when(appSecurityJPARepository.findByApplicationId(10L)).thenReturn(Optional.of(entity));
        when(appSecurityMapper.toModel(entity)).thenReturn(model);

        AppSecurity result = adapter.findByApplicationId(10L);

        assertSame(model, result);
    }

    @Test
    void findByApplicationId_notFound_returnsNull() {
        adapter = buildAdapter();
        when(appSecurityJPARepository.findByApplicationId(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findByApplicationId(99L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        AppSecurity model = new AppSecurity();
        AppSecurityEntity toSave = new AppSecurityEntity();
        AppSecurityEntity saved = entityWithApplication(5L);
        saved.setObservation("Legacy ledger");
        AppSecurity response = new AppSecurity();
        when(appSecurityMapper.toEntity(model)).thenReturn(toSave);
        when(appSecurityJPARepository.save(toSave)).thenReturn(saved);
        when(appSecurityMapper.toModel(saved)).thenReturn(response);

        AppSecurity result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<AppSecurityAudEntity> captor = ArgumentCaptor.forClass(AppSecurityAudEntity.class);
        verify(appSecurityAudJPARepository).save(captor.capture());
        AppSecurityAudEntity aud = captor.getValue();
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
        AppSecurity model = new AppSecurity();
        AppSecurityEntity toSave = new AppSecurityEntity();
        AppSecurityEntity saved = entityWithApplication(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(appSecurityMapper.toEntity(model)).thenReturn(toSave);
        when(appSecurityJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<AppSecurityAudEntity> captor = ArgumentCaptor.forClass(AppSecurityAudEntity.class);
        verify(appSecurityAudJPARepository).save(captor.capture());
        AppSecurityAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        AppSecurity model = new AppSecurity();
        AppSecurityEntity toSave = new AppSecurityEntity();
        AppSecurityEntity saved = entityWithApplication(7L);
        AppSecurity response = new AppSecurity();
        when(appSecurityMapper.toEntity(model)).thenReturn(toSave);
        when(appSecurityJPARepository.save(toSave)).thenReturn(saved);
        when(appSecurityMapper.toModel(saved)).thenReturn(response);

        AppSecurity result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<AppSecurityAudEntity> captor = ArgumentCaptor.forClass(AppSecurityAudEntity.class);
        verify(appSecurityAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        AppSecurity model = new AppSecurity();
        model.setId(8L);
        AppSecurityEntity toSave = new AppSecurityEntity();
        AppSecurityEntity saved = entityWithApplication(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(appSecurityMapper.toEntity(model)).thenReturn(toSave);
        when(appSecurityJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<AppSecurityAudEntity> captor = ArgumentCaptor.forClass(AppSecurityAudEntity.class);
        verify(appSecurityAudJPARepository).save(captor.capture());
        AppSecurityAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
