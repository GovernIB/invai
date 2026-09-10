package es.caib.invai.back.persistence.repository.maintenance.development.technology;

import es.caib.invai.back.persistence.model.maintenance.development.layer.LayerEntity;
import es.caib.invai.back.persistence.model.maintenance.development.technology.TechnologyAudEntity;
import es.caib.invai.back.persistence.model.maintenance.development.technology.TechnologyEntity;
import es.caib.invai.back.service.mapper.maintenance.development.technology.TechnologyMapper;
import es.caib.invai.back.service.model.maintenance.development.technology.Technology;
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
 * Unit tests for {@link TechnologyRepositoryAdapter}, verifying entity/model delegation to the
 * {@link TechnologyJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class TechnologyRepositoryAdapterTest {

    @Mock
    private TechnologyJPARepository technologyJPARepository;

    @Mock
    private TechnologyAudJPARepository technologyAudJPARepository;

    @Mock
    private TechnologyMapper technologyMapper;

    private TechnologyRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private TechnologyRepositoryAdapter buildAdapter() {
        TechnologyRepositoryAdapter a = new TechnologyRepositoryAdapter();
        ReflectionTestUtils.setField(a, "technologyJPARepository", technologyJPARepository);
        ReflectionTestUtils.setField(a, "technologyAudJPARepository", technologyAudJPARepository);
        ReflectionTestUtils.setField(a, "technologyMapper", technologyMapper);
        return a;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        TechnologyEntity entity = new TechnologyEntity();
        entity.setId(1L);
        Technology model = new Technology();
        when(technologyJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(technologyMapper.toModel(entity)).thenReturn(model);

        Technology result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(technologyJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        TechnologyCriteria criteria = new TechnologyCriteria();
        Pageable pageable = Pageable.unpaged();
        TechnologyEntity entity = new TechnologyEntity();
        Technology model = new Technology();
        Page<TechnologyEntity> entityPage = new PageImpl<>(List.of(entity));
        when(technologyJPARepository.findAll(ArgumentMatchers.<Specification<TechnologyEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(technologyMapper.toModel(entity)).thenReturn(model);

        Page<Technology> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByNameAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(technologyJPARepository.existsByNameAndDeletedAtIsNull("React")).thenReturn(true);

        assertTrue(adapter.existsByNameAndDeletedAtIsNull("React"));
    }

    @Test
    void existsByNameAndIdNotAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(technologyJPARepository.existsByNameAndIdNotAndDeletedAtIsNull("React", 1L)).thenReturn(true);

        assertTrue(adapter.existsByNameAndIdNotAndDeletedAtIsNull("React", 1L));
    }

    @Test
    void existsByLayerId_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(technologyJPARepository.existsByLayerId(2L)).thenReturn(true);

        assertTrue(adapter.existsByLayerId(2L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecordWithLayerId() {
        adapter = buildAdapter();
        Technology model = new Technology();
        TechnologyEntity toSave = new TechnologyEntity();
        TechnologyEntity saved = new TechnologyEntity();
        saved.setId(5L);
        saved.setName("React");
        LayerEntity layer = new LayerEntity();
        layer.setId(3L);
        saved.setLayer(layer);
        Technology response = new Technology();
        when(technologyMapper.toEntity(model)).thenReturn(toSave);
        when(technologyJPARepository.save(toSave)).thenReturn(saved);
        when(technologyMapper.toModel(saved)).thenReturn(response);

        Technology result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<TechnologyAudEntity> captor = ArgumentCaptor.forClass(TechnologyAudEntity.class);
        verify(technologyAudJPARepository).save(captor.capture());
        TechnologyAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getTechnologyId());
        assertEquals("React", aud.getName());
        assertEquals(3L, aud.getLayerId());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithNullLayer_auditRecordHasNullLayerId() {
        adapter = buildAdapter();
        Technology model = new Technology();
        TechnologyEntity toSave = new TechnologyEntity();
        TechnologyEntity saved = new TechnologyEntity();
        saved.setId(6L);
        saved.setLayer(null);
        when(technologyMapper.toEntity(model)).thenReturn(toSave);
        when(technologyJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<TechnologyAudEntity> captor = ArgumentCaptor.forClass(TechnologyAudEntity.class);
        verify(technologyAudJPARepository).save(captor.capture());
        assertNull(captor.getValue().getLayerId());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        Technology model = new Technology();
        TechnologyEntity toSave = new TechnologyEntity();
        TechnologyEntity saved = new TechnologyEntity();
        saved.setId(7L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(technologyMapper.toEntity(model)).thenReturn(toSave);
        when(technologyJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<TechnologyAudEntity> captor = ArgumentCaptor.forClass(TechnologyAudEntity.class);
        verify(technologyAudJPARepository).save(captor.capture());
        TechnologyAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        Technology model = new Technology();
        TechnologyEntity toSave = new TechnologyEntity();
        TechnologyEntity saved = new TechnologyEntity();
        saved.setId(8L);
        Technology response = new Technology();
        when(technologyMapper.toEntity(model)).thenReturn(toSave);
        when(technologyJPARepository.save(toSave)).thenReturn(saved);
        when(technologyMapper.toModel(saved)).thenReturn(response);

        Technology result = adapter.update(model, 8L);

        assertSame(response, result);
        assertEquals(8L, toSave.getId());
        ArgumentCaptor<TechnologyAudEntity> captor = ArgumentCaptor.forClass(TechnologyAudEntity.class);
        verify(technologyAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        Technology model = new Technology();
        model.setId(9L);
        TechnologyEntity toSave = new TechnologyEntity();
        TechnologyEntity saved = new TechnologyEntity();
        saved.setId(9L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(technologyMapper.toEntity(model)).thenReturn(toSave);
        when(technologyJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(9L, toSave.getId());
        ArgumentCaptor<TechnologyAudEntity> captor = ArgumentCaptor.forClass(TechnologyAudEntity.class);
        verify(technologyAudJPARepository).save(captor.capture());
        TechnologyAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
