package es.caib.invai.back.persistence.repository.maintenance.development.layer;

import es.caib.invai.back.persistence.model.maintenance.development.layer.LayerAudEntity;
import es.caib.invai.back.persistence.model.maintenance.development.layer.LayerEntity;
import es.caib.invai.back.service.mapper.maintenance.development.layer.LayerMapper;
import es.caib.invai.back.service.model.maintenance.development.layer.Layer;
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
 * Unit tests for {@link LayerRepositoryAdapter}, verifying entity/model delegation to the
 * {@link LayerJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class LayerRepositoryAdapterTest {

    @Mock
    private LayerJPARepository layerJPARepository;

    @Mock
    private LayerAudJPARepository layerAudJPARepository;

    @Mock
    private LayerMapper layerMapper;

    private LayerRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private LayerRepositoryAdapter buildAdapter() {
        LayerRepositoryAdapter a = new LayerRepositoryAdapter();
        ReflectionTestUtils.setField(a, "layerJPARepository", layerJPARepository);
        ReflectionTestUtils.setField(a, "layerAudJPARepository", layerAudJPARepository);
        ReflectionTestUtils.setField(a, "layerMapper", layerMapper);
        return a;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        LayerEntity entity = new LayerEntity();
        entity.setId(1L);
        Layer model = new Layer();
        when(layerJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(layerMapper.toModel(entity)).thenReturn(model);

        Layer result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(layerJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        LayerCriteria criteria = new LayerCriteria();
        Pageable pageable = Pageable.unpaged();
        LayerEntity entity = new LayerEntity();
        Layer model = new Layer();
        Page<LayerEntity> entityPage = new PageImpl<>(List.of(entity));
        when(layerJPARepository.findAll(ArgumentMatchers.<Specification<LayerEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(layerMapper.toModel(entity)).thenReturn(model);

        Page<Layer> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByNameAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(layerJPARepository.existsByNameAndDeletedAtIsNull("Backend")).thenReturn(true);

        assertEquals(true, adapter.existsByNameAndDeletedAtIsNull("Backend"));
    }

    @Test
    void existsByNameAndIdNotAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(layerJPARepository.existsByNameAndIdNotAndDeletedAtIsNull("Backend", 1L)).thenReturn(true);

        assertEquals(true, adapter.existsByNameAndIdNotAndDeletedAtIsNull("Backend", 1L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        Layer model = new Layer();
        LayerEntity toSave = new LayerEntity();
        LayerEntity saved = new LayerEntity();
        saved.setId(5L);
        saved.setName("Backend");
        Layer response = new Layer();
        when(layerMapper.toEntity(model)).thenReturn(toSave);
        when(layerJPARepository.save(toSave)).thenReturn(saved);
        when(layerMapper.toModel(saved)).thenReturn(response);

        Layer result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<LayerAudEntity> captor = ArgumentCaptor.forClass(LayerAudEntity.class);
        verify(layerAudJPARepository).save(captor.capture());
        LayerAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getLayerId());
        assertEquals("Backend", aud.getName());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        Layer model = new Layer();
        LayerEntity toSave = new LayerEntity();
        LayerEntity saved = new LayerEntity();
        saved.setId(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(layerMapper.toEntity(model)).thenReturn(toSave);
        when(layerJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<LayerAudEntity> captor = ArgumentCaptor.forClass(LayerAudEntity.class);
        verify(layerAudJPARepository).save(captor.capture());
        LayerAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        Layer model = new Layer();
        LayerEntity toSave = new LayerEntity();
        LayerEntity saved = new LayerEntity();
        saved.setId(7L);
        Layer response = new Layer();
        when(layerMapper.toEntity(model)).thenReturn(toSave);
        when(layerJPARepository.save(toSave)).thenReturn(saved);
        when(layerMapper.toModel(saved)).thenReturn(response);

        Layer result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<LayerAudEntity> captor = ArgumentCaptor.forClass(LayerAudEntity.class);
        verify(layerAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        Layer model = new Layer();
        model.setId(8L);
        LayerEntity toSave = new LayerEntity();
        LayerEntity saved = new LayerEntity();
        saved.setId(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(layerMapper.toEntity(model)).thenReturn(toSave);
        when(layerJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<LayerAudEntity> captor = ArgumentCaptor.forClass(LayerAudEntity.class);
        verify(layerAudJPARepository).save(captor.capture());
        LayerAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
