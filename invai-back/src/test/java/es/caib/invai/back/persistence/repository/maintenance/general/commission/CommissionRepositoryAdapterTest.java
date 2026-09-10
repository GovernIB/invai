package es.caib.invai.back.persistence.repository.maintenance.general.commission;

import es.caib.invai.back.persistence.model.maintenance.general.commission.CommissionAudEntity;
import es.caib.invai.back.persistence.model.maintenance.general.commission.CommissionEntity;
import es.caib.invai.back.service.mapper.maintenance.general.commission.CommissionMapper;
import es.caib.invai.back.service.model.maintenance.general.commission.Commission;
import es.caib.invai.back.service.model.maintenance.general.commission.CommissionType;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentMatchers;

/**
 * Unit tests for {@link CommissionRepositoryAdapter}, verifying entity/model delegation to the
 * {@link CommissionJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class CommissionRepositoryAdapterTest {

    @Mock
    private CommissionJPARepository commissionJPARepository;

    @Mock
    private CommissionAudJPARepository commissionAudJPARepository;

    @Mock
    private CommissionMapper commissionMapper;

    private CommissionRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private CommissionRepositoryAdapter buildAdapter() {
        CommissionRepositoryAdapter a = new CommissionRepositoryAdapter();
        ReflectionTestUtils.setField(a, "commissionJPARepository", commissionJPARepository);
        ReflectionTestUtils.setField(a, "commissionAudJPARepository", commissionAudJPARepository);
        ReflectionTestUtils.setField(a, "commissionMapper", commissionMapper);
        return a;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        CommissionEntity entity = new CommissionEntity();
        entity.setId(1L);
        Commission model = new Commission();
        when(commissionJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(commissionMapper.toModel(entity)).thenReturn(model);

        Commission result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(commissionJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        CommissionCriteria criteria = new CommissionCriteria();
        Pageable pageable = Pageable.unpaged();
        CommissionEntity entity = new CommissionEntity();
        Commission model = new Commission();
        Page<CommissionEntity> entityPage = new PageImpl<>(List.of(entity));
        when(commissionJPARepository.findAll(ArgumentMatchers.<Specification<CommissionEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(commissionMapper.toModel(entity)).thenReturn(model);

        Page<Commission> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByNameAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(commissionJPARepository.existsByNameAndDeletedAtIsNull("Governance")).thenReturn(true);

        assertTrue(adapter.existsByNameAndDeletedAtIsNull("Governance"));
    }

    @Test
    void existsByNameAndIdNotAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(commissionJPARepository.existsByNameAndIdNotAndDeletedAtIsNull("Governance", 1L)).thenReturn(true);

        assertTrue(adapter.existsByNameAndIdNotAndDeletedAtIsNull("Governance", 1L));
    }

    @Test
    void existsByNameEsAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(commissionJPARepository.existsByNameEsAndDeletedAtIsNull("Governance ES")).thenReturn(true);

        assertTrue(adapter.existsByNameEsAndDeletedAtIsNull("Governance ES"));
    }

    @Test
    void existsByNameEsAndIdNotAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(commissionJPARepository.existsByNameEsAndIdNotAndDeletedAtIsNull("Governance ES", 1L)).thenReturn(true);

        assertTrue(adapter.existsByNameEsAndIdNotAndDeletedAtIsNull("Governance ES", 1L));
    }

    @Test
    void existsByExpedientNumberAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(commissionJPARepository.existsByExpedientNumberAndDeletedAtIsNull("EXP-001")).thenReturn(true);

        assertTrue(adapter.existsByExpedientNumberAndDeletedAtIsNull("EXP-001"));
    }

    @Test
    void existsByExpedientNumberAndIdNotAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(commissionJPARepository.existsByExpedientNumberAndIdNotAndDeletedAtIsNull("EXP-001", 1L)).thenReturn(true);

        assertTrue(adapter.existsByExpedientNumberAndIdNotAndDeletedAtIsNull("EXP-001", 1L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        Commission model = new Commission();
        CommissionEntity toSave = new CommissionEntity();
        CommissionEntity saved = new CommissionEntity();
        saved.setId(5L);
        saved.setName("Technical Board");
        saved.setNameEs("Comite Tecnico");
        saved.setExpedientNumber("EXP-2025-01");
        saved.setApprovalDate(LocalDate.of(2025, 6, 1));
        saved.setCommissionType(CommissionType.TECNICA);
        Commission response = new Commission();
        when(commissionMapper.toEntity(model)).thenReturn(toSave);
        when(commissionJPARepository.save(toSave)).thenReturn(saved);
        when(commissionMapper.toModel(saved)).thenReturn(response);

        Commission result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<CommissionAudEntity> captor = ArgumentCaptor.forClass(CommissionAudEntity.class);
        verify(commissionAudJPARepository).save(captor.capture());
        CommissionAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getCommissionId());
        assertEquals("Technical Board", aud.getName());
        assertEquals("Comite Tecnico", aud.getNameEs());
        assertEquals("EXP-2025-01", aud.getExpedientNumber());
        assertEquals(LocalDate.of(2025, 6, 1), aud.getApprovalDate());
        assertEquals("TECNICA", aud.getCommissionType());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNull(aud.getUpdatedAt());
        assertNull(aud.getUpdatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithNullCommissionType_writesNullOnAuditRecord() {
        adapter = buildAdapter();
        Commission model = new Commission();
        CommissionEntity toSave = new CommissionEntity();
        CommissionEntity saved = new CommissionEntity();
        saved.setId(9L);
        saved.setCommissionType(null);
        when(commissionMapper.toEntity(model)).thenReturn(toSave);
        when(commissionJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<CommissionAudEntity> captor = ArgumentCaptor.forClass(CommissionAudEntity.class);
        verify(commissionAudJPARepository).save(captor.capture());
        assertNull(captor.getValue().getCommissionType());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        Commission model = new Commission();
        CommissionEntity toSave = new CommissionEntity();
        CommissionEntity saved = new CommissionEntity();
        saved.setId(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime existingUpdatedAt = LocalDateTime.of(2025, 2, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        saved.setUpdatedAt(existingUpdatedAt);
        saved.setUpdatedBy("jdoe2");
        when(commissionMapper.toEntity(model)).thenReturn(toSave);
        when(commissionJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<CommissionAudEntity> captor = ArgumentCaptor.forClass(CommissionAudEntity.class);
        verify(commissionAudJPARepository).save(captor.capture());
        CommissionAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
        assertEquals(existingUpdatedAt, aud.getUpdatedAt());
        assertEquals("jdoe2", aud.getUpdatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        Commission model = new Commission();
        CommissionEntity toSave = new CommissionEntity();
        CommissionEntity saved = new CommissionEntity();
        saved.setId(7L);
        Commission response = new Commission();
        when(commissionMapper.toEntity(model)).thenReturn(toSave);
        when(commissionJPARepository.save(toSave)).thenReturn(saved);
        when(commissionMapper.toModel(saved)).thenReturn(response);

        Commission result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<CommissionAudEntity> captor = ArgumentCaptor.forClass(CommissionAudEntity.class);
        verify(commissionAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        Commission model = new Commission();
        model.setId(8L);
        CommissionEntity toSave = new CommissionEntity();
        CommissionEntity saved = new CommissionEntity();
        saved.setId(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(commissionMapper.toEntity(model)).thenReturn(toSave);
        when(commissionJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<CommissionAudEntity> captor = ArgumentCaptor.forClass(CommissionAudEntity.class);
        verify(commissionAudJPARepository).save(captor.capture());
        CommissionAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
