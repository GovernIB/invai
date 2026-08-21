package es.caib.invai.back.persistence.repository.application.development.technology;

import es.caib.invai.back.persistence.model.application.development.technology.AppTechnologyAudEntity;
import es.caib.invai.back.persistence.model.application.development.technology.AppTechnologyEntity;
import es.caib.invai.back.persistence.model.application.development.core.AppDevelopmentEntity;
import es.caib.invai.back.persistence.model.maintenance.development.layer.LayerEntity;
import es.caib.invai.back.persistence.model.maintenance.development.technology.TechnologyEntity;
import es.caib.invai.back.service.mapper.application.development.technology.AppTechnologyMapper;
import es.caib.invai.back.service.model.application.development.technology.AppTechnology;
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
 * Unit tests for {@link AppTechnologyRepositoryAdapter}, verifying entity/model delegation to the
 * {@link AppTechnologyJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class AppTechnologyRepositoryAdapterTest {

    @Mock
    private AppTechnologyJPARepository appTechnologyJPARepository;

    @Mock
    private AppTechnologyAudJPARepository appTechnologyAudJPARepository;

    @Mock
    private AppTechnologyMapper appTechnologyMapper;

    private AppTechnologyRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AppTechnologyRepositoryAdapter buildAdapter() {
        AppTechnologyRepositoryAdapter a = new AppTechnologyRepositoryAdapter();
        ReflectionTestUtils.setField(a, "appTechnologyJPARepository", appTechnologyJPARepository);
        ReflectionTestUtils.setField(a, "appTechnologyAudJPARepository", appTechnologyAudJPARepository);
        ReflectionTestUtils.setField(a, "appTechnologyMapper", appTechnologyMapper);
        return a;
    }

    private AppDevelopmentEntity developmentEntity(Long id) {
        AppDevelopmentEntity development = new AppDevelopmentEntity();
        development.setId(id);
        return development;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        AppTechnologyEntity entity = new AppTechnologyEntity();
        entity.setId(1L);
        AppTechnology model = new AppTechnology();
        when(appTechnologyJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(appTechnologyMapper.toModel(entity)).thenReturn(model);

        AppTechnology result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(appTechnologyJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        AppTechnologyCriteria criteria = new AppTechnologyCriteria();
        Pageable pageable = Pageable.unpaged();
        AppTechnologyEntity entity = new AppTechnologyEntity();
        AppTechnology model = new AppTechnology();
        Page<AppTechnologyEntity> entityPage = new PageImpl<>(List.of(entity));
        when(appTechnologyJPARepository.findAll(ArgumentMatchers.<Specification<AppTechnologyEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(appTechnologyMapper.toModel(entity)).thenReturn(model);

        Page<AppTechnology> result = adapter.findAll(10L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByLayerId_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(appTechnologyJPARepository.existsByLayerId(4L)).thenReturn(true);

        assertEquals(true, adapter.existsByLayerId(4L));
    }

    @Test
    void existsByTechnologyId_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(appTechnologyJPARepository.existsByTechnologyId(5L)).thenReturn(true);

        assertEquals(true, adapter.existsByTechnologyId(5L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        AppTechnology model = new AppTechnology();
        AppTechnologyEntity toSave = new AppTechnologyEntity();
        AppTechnologyEntity saved = new AppTechnologyEntity();
        saved.setId(5L);
        saved.setAppDevelopment(developmentEntity(20L));
        LayerEntity layer = new LayerEntity();
        layer.setId(11L);
        saved.setLayer(layer);
        TechnologyEntity technology = new TechnologyEntity();
        technology.setId(12L);
        saved.setTechnology(technology);
        saved.setVersion("1.2.3");
        saved.setArchitecture("x86_64");
        AppTechnology response = new AppTechnology();
        when(appTechnologyMapper.toEntity(model)).thenReturn(toSave);
        when(appTechnologyJPARepository.save(toSave)).thenReturn(saved);
        when(appTechnologyMapper.toModel(saved)).thenReturn(response);

        AppTechnology result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<AppTechnologyAudEntity> captor = ArgumentCaptor.forClass(AppTechnologyAudEntity.class);
        verify(appTechnologyAudJPARepository).save(captor.capture());
        AppTechnologyAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getTechnologyId());
        assertEquals(20L, aud.getAppDevelopmentId());
        assertEquals(11L, aud.getLayerId());
        assertEquals(12L, aud.getTechnologyCatalogId());
        assertEquals("1.2.3", aud.getVersion());
        assertEquals("x86_64", aud.getArchitecture());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getUpdatedAt());
        assertEquals("SYSTEM_USER", aud.getUpdatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_withNullLayerAndTechnology_setsNullReferenceIdsOnAuditRecord() {
        adapter = buildAdapter();
        AppTechnology model = new AppTechnology();
        AppTechnologyEntity toSave = new AppTechnologyEntity();
        AppTechnologyEntity saved = new AppTechnologyEntity();
        saved.setId(6L);
        saved.setAppDevelopment(developmentEntity(21L));
        saved.setLayer(null);
        saved.setTechnology(null);
        when(appTechnologyMapper.toEntity(model)).thenReturn(toSave);
        when(appTechnologyJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<AppTechnologyAudEntity> captor = ArgumentCaptor.forClass(AppTechnologyAudEntity.class);
        verify(appTechnologyAudJPARepository).save(captor.capture());
        AppTechnologyAudEntity aud = captor.getValue();
        assertNull(aud.getLayerId());
        assertNull(aud.getTechnologyCatalogId());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        AppTechnology model = new AppTechnology();
        AppTechnologyEntity toSave = new AppTechnologyEntity();
        AppTechnologyEntity saved = new AppTechnologyEntity();
        saved.setId(7L);
        saved.setAppDevelopment(developmentEntity(22L));
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        LocalDateTime existingUpdatedAt = LocalDateTime.of(2025, 6, 1, 0, 0);
        saved.setUpdatedAt(existingUpdatedAt);
        saved.setUpdatedBy("asmith");
        when(appTechnologyMapper.toEntity(model)).thenReturn(toSave);
        when(appTechnologyJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<AppTechnologyAudEntity> captor = ArgumentCaptor.forClass(AppTechnologyAudEntity.class);
        verify(appTechnologyAudJPARepository).save(captor.capture());
        AppTechnologyAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
        assertEquals(existingUpdatedAt, aud.getUpdatedAt());
        assertEquals("asmith", aud.getUpdatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        AppTechnology model = new AppTechnology();
        AppTechnologyEntity toSave = new AppTechnologyEntity();
        AppTechnologyEntity saved = new AppTechnologyEntity();
        saved.setId(8L);
        saved.setAppDevelopment(developmentEntity(23L));
        AppTechnology response = new AppTechnology();
        when(appTechnologyMapper.toEntity(model)).thenReturn(toSave);
        when(appTechnologyJPARepository.save(toSave)).thenReturn(saved);
        when(appTechnologyMapper.toModel(saved)).thenReturn(response);

        AppTechnology result = adapter.update(model, 8L);

        assertSame(response, result);
        assertEquals(8L, toSave.getId());
        ArgumentCaptor<AppTechnologyAudEntity> captor = ArgumentCaptor.forClass(AppTechnologyAudEntity.class);
        verify(appTechnologyAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        AppTechnology model = new AppTechnology();
        model.setId(9L);
        AppTechnologyEntity toSave = new AppTechnologyEntity();
        AppTechnologyEntity saved = new AppTechnologyEntity();
        saved.setId(9L);
        saved.setAppDevelopment(developmentEntity(24L));
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(appTechnologyMapper.toEntity(model)).thenReturn(toSave);
        when(appTechnologyJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(9L, toSave.getId());
        ArgumentCaptor<AppTechnologyAudEntity> captor = ArgumentCaptor.forClass(AppTechnologyAudEntity.class);
        verify(appTechnologyAudJPARepository).save(captor.capture());
        AppTechnologyAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
