package es.caib.invai.back.persistence.repository.maintenance.complianceSituation;

import es.caib.invai.back.persistence.model.maintenance.complianceSituation.ComplianceSituationAudEntity;
import es.caib.invai.back.persistence.model.maintenance.complianceSituation.ComplianceSituationEntity;
import es.caib.invai.back.service.mapper.maintenance.complianceSituation.ComplianceSituationMapper;
import es.caib.invai.back.service.model.maintenance.complianceSituation.ComplianceSituation;
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
 * Unit tests for {@link ComplianceSituationRepositoryAdapter}, verifying entity/model delegation to the
 * {@link ComplianceSituationJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class ComplianceSituationRepositoryAdapterTest {

    @Mock
    private ComplianceSituationJPARepository complianceSituationJPARepository;

    @Mock
    private ComplianceSituationAudJPARepository complianceSituationAudJPARepository;

    @Mock
    private ComplianceSituationMapper complianceSituationMapper;

    private ComplianceSituationRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private ComplianceSituationRepositoryAdapter buildAdapter() {
        ComplianceSituationRepositoryAdapter a = new ComplianceSituationRepositoryAdapter();
        ReflectionTestUtils.setField(a, "complianceSituationJPARepository", complianceSituationJPARepository);
        ReflectionTestUtils.setField(a, "complianceSituationAudJPARepository", complianceSituationAudJPARepository);
        ReflectionTestUtils.setField(a, "complianceSituationMapper", complianceSituationMapper);
        return a;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        ComplianceSituationEntity entity = new ComplianceSituationEntity();
        entity.setId(1L);
        ComplianceSituation model = new ComplianceSituation();
        when(complianceSituationJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(complianceSituationMapper.toModel(entity)).thenReturn(model);

        ComplianceSituation result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(complianceSituationJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        ComplianceSituationCriteria criteria = new ComplianceSituationCriteria();
        Pageable pageable = Pageable.unpaged();
        ComplianceSituationEntity entity = new ComplianceSituationEntity();
        ComplianceSituation model = new ComplianceSituation();
        Page<ComplianceSituationEntity> entityPage = new PageImpl<>(List.of(entity));
        when(complianceSituationJPARepository.findAll(ArgumentMatchers.<Specification<ComplianceSituationEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(complianceSituationMapper.toModel(entity)).thenReturn(model);

        Page<ComplianceSituation> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByNameAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(complianceSituationJPARepository.existsByNameAndDeletedAtIsNull("Conforme")).thenReturn(true);

        assertTrue(adapter.existsByNameAndDeletedAtIsNull("Conforme"));
    }

    @Test
    void existsByNameAndIdNotAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(complianceSituationJPARepository.existsByNameAndIdNotAndDeletedAtIsNull("Conforme", 1L)).thenReturn(true);

        assertTrue(adapter.existsByNameAndIdNotAndDeletedAtIsNull("Conforme", 1L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        ComplianceSituation model = new ComplianceSituation();
        ComplianceSituationEntity toSave = new ComplianceSituationEntity();
        ComplianceSituationEntity saved = new ComplianceSituationEntity();
        saved.setId(5L);
        saved.setName("Conforme");
        saved.setNameEs("Conforme");
        ComplianceSituation response = new ComplianceSituation();
        when(complianceSituationMapper.toEntity(model)).thenReturn(toSave);
        when(complianceSituationJPARepository.save(toSave)).thenReturn(saved);
        when(complianceSituationMapper.toModel(saved)).thenReturn(response);

        ComplianceSituation result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<ComplianceSituationAudEntity> captor = ArgumentCaptor.forClass(ComplianceSituationAudEntity.class);
        verify(complianceSituationAudJPARepository).save(captor.capture());
        ComplianceSituationAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getComplianceSituationId());
        assertEquals("Conforme", aud.getName());
        assertEquals("Conforme", aud.getNameEs());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        ComplianceSituation model = new ComplianceSituation();
        ComplianceSituationEntity toSave = new ComplianceSituationEntity();
        ComplianceSituationEntity saved = new ComplianceSituationEntity();
        saved.setId(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(complianceSituationMapper.toEntity(model)).thenReturn(toSave);
        when(complianceSituationJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<ComplianceSituationAudEntity> captor = ArgumentCaptor.forClass(ComplianceSituationAudEntity.class);
        verify(complianceSituationAudJPARepository).save(captor.capture());
        ComplianceSituationAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        ComplianceSituation model = new ComplianceSituation();
        ComplianceSituationEntity toSave = new ComplianceSituationEntity();
        ComplianceSituationEntity saved = new ComplianceSituationEntity();
        saved.setId(7L);
        ComplianceSituation response = new ComplianceSituation();
        when(complianceSituationMapper.toEntity(model)).thenReturn(toSave);
        when(complianceSituationJPARepository.save(toSave)).thenReturn(saved);
        when(complianceSituationMapper.toModel(saved)).thenReturn(response);

        ComplianceSituation result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<ComplianceSituationAudEntity> captor = ArgumentCaptor.forClass(ComplianceSituationAudEntity.class);
        verify(complianceSituationAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        ComplianceSituation model = new ComplianceSituation();
        model.setId(8L);
        ComplianceSituationEntity toSave = new ComplianceSituationEntity();
        ComplianceSituationEntity saved = new ComplianceSituationEntity();
        saved.setId(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(complianceSituationMapper.toEntity(model)).thenReturn(toSave);
        when(complianceSituationJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<ComplianceSituationAudEntity> captor = ArgumentCaptor.forClass(ComplianceSituationAudEntity.class);
        verify(complianceSituationAudJPARepository).save(captor.capture());
        ComplianceSituationAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
