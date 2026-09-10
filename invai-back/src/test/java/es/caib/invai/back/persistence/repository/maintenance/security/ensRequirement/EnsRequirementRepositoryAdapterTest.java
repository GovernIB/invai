package es.caib.invai.back.persistence.repository.maintenance.security.ensRequirement;

import es.caib.invai.back.persistence.model.maintenance.security.ensRequirement.EnsRequirementAudEntity;
import es.caib.invai.back.persistence.model.maintenance.security.ensRequirement.EnsRequirementEntity;
import es.caib.invai.back.service.mapper.maintenance.security.ensRequirement.EnsRequirementMapper;
import es.caib.invai.back.service.model.maintenance.security.ensRequirement.EnsRequirement;
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
 * Unit tests for {@link EnsRequirementRepositoryAdapter}, verifying entity/model delegation to the
 * {@link EnsRequirementJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class EnsRequirementRepositoryAdapterTest {

    @Mock
    private EnsRequirementJPARepository ensRequirementJPARepository;

    @Mock
    private EnsRequirementAudJPARepository ensRequirementAudJPARepository;

    @Mock
    private EnsRequirementMapper ensRequirementMapper;

    private EnsRequirementRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private EnsRequirementRepositoryAdapter buildAdapter() {
        EnsRequirementRepositoryAdapter a = new EnsRequirementRepositoryAdapter();
        ReflectionTestUtils.setField(a, "ensRequirementJPARepository", ensRequirementJPARepository);
        ReflectionTestUtils.setField(a, "ensRequirementAudJPARepository", ensRequirementAudJPARepository);
        ReflectionTestUtils.setField(a, "ensRequirementMapper", ensRequirementMapper);
        return a;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        EnsRequirementEntity entity = new EnsRequirementEntity();
        entity.setId(1L);
        EnsRequirement model = new EnsRequirement();
        when(ensRequirementJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(ensRequirementMapper.toModel(entity)).thenReturn(model);

        EnsRequirement result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(ensRequirementJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        EnsRequirementCriteria criteria = new EnsRequirementCriteria();
        Pageable pageable = Pageable.unpaged();
        EnsRequirementEntity entity = new EnsRequirementEntity();
        EnsRequirement model = new EnsRequirement();
        Page<EnsRequirementEntity> entityPage = new PageImpl<>(List.of(entity));
        when(ensRequirementJPARepository.findAll(ArgumentMatchers.<Specification<EnsRequirementEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(ensRequirementMapper.toModel(entity)).thenReturn(model);

        Page<EnsRequirement> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByNameAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(ensRequirementJPARepository.existsByNameAndDeletedAtIsNull("Control d'accessos")).thenReturn(true);

        assertTrue(adapter.existsByNameAndDeletedAtIsNull("Control d'accessos"));
    }

    @Test
    void existsByNameAndIdNotAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(ensRequirementJPARepository.existsByNameAndIdNotAndDeletedAtIsNull("Control d'accessos", 1L)).thenReturn(true);

        assertTrue(adapter.existsByNameAndIdNotAndDeletedAtIsNull("Control d'accessos", 1L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        EnsRequirement model = new EnsRequirement();
        EnsRequirementEntity toSave = new EnsRequirementEntity();
        EnsRequirementEntity saved = new EnsRequirementEntity();
        saved.setId(5L);
        saved.setName("Control d'accessos");
        saved.setNameEs("Control de accesos");
        EnsRequirement response = new EnsRequirement();
        when(ensRequirementMapper.toEntity(model)).thenReturn(toSave);
        when(ensRequirementJPARepository.save(toSave)).thenReturn(saved);
        when(ensRequirementMapper.toModel(saved)).thenReturn(response);

        EnsRequirement result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<EnsRequirementAudEntity> captor = ArgumentCaptor.forClass(EnsRequirementAudEntity.class);
        verify(ensRequirementAudJPARepository).save(captor.capture());
        EnsRequirementAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getEnsRequirementId());
        assertEquals("Control d'accessos", aud.getName());
        assertEquals("Control de accesos", aud.getNameEs());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        EnsRequirement model = new EnsRequirement();
        EnsRequirementEntity toSave = new EnsRequirementEntity();
        EnsRequirementEntity saved = new EnsRequirementEntity();
        saved.setId(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(ensRequirementMapper.toEntity(model)).thenReturn(toSave);
        when(ensRequirementJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<EnsRequirementAudEntity> captor = ArgumentCaptor.forClass(EnsRequirementAudEntity.class);
        verify(ensRequirementAudJPARepository).save(captor.capture());
        EnsRequirementAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        EnsRequirement model = new EnsRequirement();
        EnsRequirementEntity toSave = new EnsRequirementEntity();
        EnsRequirementEntity saved = new EnsRequirementEntity();
        saved.setId(7L);
        EnsRequirement response = new EnsRequirement();
        when(ensRequirementMapper.toEntity(model)).thenReturn(toSave);
        when(ensRequirementJPARepository.save(toSave)).thenReturn(saved);
        when(ensRequirementMapper.toModel(saved)).thenReturn(response);

        EnsRequirement result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<EnsRequirementAudEntity> captor = ArgumentCaptor.forClass(EnsRequirementAudEntity.class);
        verify(ensRequirementAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        EnsRequirement model = new EnsRequirement();
        model.setId(8L);
        EnsRequirementEntity toSave = new EnsRequirementEntity();
        EnsRequirementEntity saved = new EnsRequirementEntity();
        saved.setId(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(ensRequirementMapper.toEntity(model)).thenReturn(toSave);
        when(ensRequirementJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<EnsRequirementAudEntity> captor = ArgumentCaptor.forClass(EnsRequirementAudEntity.class);
        verify(ensRequirementAudJPARepository).save(captor.capture());
        EnsRequirementAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
