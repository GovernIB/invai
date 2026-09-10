package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.core;

import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.core.AppResponsibleAuthorizedAudEntity;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.core.AppResponsibleAuthorizedEntity;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.core.AppResponsibleAuthorizedMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
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
 * Unit tests for {@link AppResponsibleAuthorizedRepositoryAdapter}, verifying entity/model delegation to the
 * {@link AppResponsibleAuthorizedJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class AppResponsibleAuthorizedRepositoryAdapterTest {

    @Mock
    private AppResponsibleAuthorizedJPARepository appResponsibleAuthorizedJPARepository;

    @Mock
    private AppResponsibleAuthorizedAudJPARepository appResponsibleAuthorizedAudJPARepository;

    @Mock
    private AppResponsibleAuthorizedMapper appResponsibleAuthorizedMapper;

    private AppResponsibleAuthorizedRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AppResponsibleAuthorizedRepositoryAdapter buildAdapter() {
        AppResponsibleAuthorizedRepositoryAdapter a = new AppResponsibleAuthorizedRepositoryAdapter();
        ReflectionTestUtils.setField(a, "appResponsibleAuthorizedJPARepository", appResponsibleAuthorizedJPARepository);
        ReflectionTestUtils.setField(a, "appResponsibleAuthorizedAudJPARepository", appResponsibleAuthorizedAudJPARepository);
        ReflectionTestUtils.setField(a, "appResponsibleAuthorizedMapper", appResponsibleAuthorizedMapper);
        return a;
    }

    private AppResponsibleAuthorizedEntity entityWithApplication(Long id) {
        AppResponsibleAuthorizedEntity entity = new AppResponsibleAuthorizedEntity();
        entity.setId(id);
        ApplicationEntity application = new ApplicationEntity();
        application.setId(10L);
        entity.setApplication(application);
        return entity;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        AppResponsibleAuthorizedEntity entity = entityWithApplication(1L);
        AppResponsibleAuthorized model = new AppResponsibleAuthorized();
        when(appResponsibleAuthorizedJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(appResponsibleAuthorizedMapper.toModel(entity)).thenReturn(model);

        AppResponsibleAuthorized result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(appResponsibleAuthorizedJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findByApplicationId_found_delegatesToJPARepositoryAndMapsModel() {
        adapter = buildAdapter();
        AppResponsibleAuthorizedEntity entity = entityWithApplication(1L);
        AppResponsibleAuthorized model = new AppResponsibleAuthorized();
        when(appResponsibleAuthorizedJPARepository.findByApplicationId(10L)).thenReturn(Optional.of(entity));
        when(appResponsibleAuthorizedMapper.toModel(entity)).thenReturn(model);

        AppResponsibleAuthorized result = adapter.findByApplicationId(10L);

        assertSame(model, result);
    }

    @Test
    void findByApplicationId_notFound_returnsNull() {
        adapter = buildAdapter();
        when(appResponsibleAuthorizedJPARepository.findByApplicationId(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findByApplicationId(99L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        AppResponsibleAuthorized model = new AppResponsibleAuthorized();
        AppResponsibleAuthorizedEntity toSave = new AppResponsibleAuthorizedEntity();
        AppResponsibleAuthorizedEntity saved = entityWithApplication(5L);
        AppResponsibleAuthorized response = new AppResponsibleAuthorized();
        when(appResponsibleAuthorizedMapper.toEntity(model)).thenReturn(toSave);
        when(appResponsibleAuthorizedJPARepository.save(toSave)).thenReturn(saved);
        when(appResponsibleAuthorizedMapper.toModel(saved)).thenReturn(response);

        AppResponsibleAuthorized result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<AppResponsibleAuthorizedAudEntity> captor = ArgumentCaptor.forClass(AppResponsibleAuthorizedAudEntity.class);
        verify(appResponsibleAuthorizedAudJPARepository).save(captor.capture());
        AppResponsibleAuthorizedAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getAppResponsibleAuthorizedId());
        assertEquals(10L, aud.getApplicationId());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        AppResponsibleAuthorized model = new AppResponsibleAuthorized();
        AppResponsibleAuthorizedEntity toSave = new AppResponsibleAuthorizedEntity();
        AppResponsibleAuthorizedEntity saved = entityWithApplication(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(appResponsibleAuthorizedMapper.toEntity(model)).thenReturn(toSave);
        when(appResponsibleAuthorizedJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<AppResponsibleAuthorizedAudEntity> captor = ArgumentCaptor.forClass(AppResponsibleAuthorizedAudEntity.class);
        verify(appResponsibleAuthorizedAudJPARepository).save(captor.capture());
        AppResponsibleAuthorizedAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        AppResponsibleAuthorized model = new AppResponsibleAuthorized();
        AppResponsibleAuthorizedEntity toSave = new AppResponsibleAuthorizedEntity();
        AppResponsibleAuthorizedEntity saved = entityWithApplication(7L);
        AppResponsibleAuthorized response = new AppResponsibleAuthorized();
        when(appResponsibleAuthorizedMapper.toEntity(model)).thenReturn(toSave);
        when(appResponsibleAuthorizedJPARepository.save(toSave)).thenReturn(saved);
        when(appResponsibleAuthorizedMapper.toModel(saved)).thenReturn(response);

        AppResponsibleAuthorized result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<AppResponsibleAuthorizedAudEntity> captor = ArgumentCaptor.forClass(AppResponsibleAuthorizedAudEntity.class);
        verify(appResponsibleAuthorizedAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        AppResponsibleAuthorized model = new AppResponsibleAuthorized();
        model.setId(8L);
        AppResponsibleAuthorizedEntity toSave = new AppResponsibleAuthorizedEntity();
        AppResponsibleAuthorizedEntity saved = entityWithApplication(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(appResponsibleAuthorizedMapper.toEntity(model)).thenReturn(toSave);
        when(appResponsibleAuthorizedJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<AppResponsibleAuthorizedAudEntity> captor = ArgumentCaptor.forClass(AppResponsibleAuthorizedAudEntity.class);
        verify(appResponsibleAuthorizedAudJPARepository).save(captor.capture());
        AppResponsibleAuthorizedAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
