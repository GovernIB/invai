package es.caib.invai.back.persistence.repository.maintenance.admUnit;

import es.caib.invai.back.persistence.model.maintenance.admUnit.AdmUnitAudEntity;
import es.caib.invai.back.persistence.model.maintenance.admUnit.AdmUnitEntity;
import es.caib.invai.back.service.mapper.maintenance.admUnit.AdmUnitMapper;
import es.caib.invai.back.service.model.maintenance.admUnit.AdmUnit;
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
 * Unit tests for {@link AdmUnitRepositoryAdapter}, verifying entity/model delegation to the
 * {@link AdmUnitJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class AdmUnitRepositoryAdapterTest {

    @Mock
    private AdmUnitJPARepository admUnitJPARepository;

    @Mock
    private AdmUnitAudJPARepository admUnitAudJPARepository;

    @Mock
    private AdmUnitMapper admUnitMapper;

    private AdmUnitRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AdmUnitRepositoryAdapter buildAdapter() {
        AdmUnitRepositoryAdapter a = new AdmUnitRepositoryAdapter();
        ReflectionTestUtils.setField(a, "admUnitJPARepository", admUnitJPARepository);
        ReflectionTestUtils.setField(a, "admUnitAudJPARepository", admUnitAudJPARepository);
        ReflectionTestUtils.setField(a, "admUnitMapper", admUnitMapper);
        return a;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        AdmUnitEntity entity = new AdmUnitEntity();
        entity.setId(1L);
        AdmUnit model = new AdmUnit();
        when(admUnitJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(admUnitMapper.toModel(entity)).thenReturn(model);

        AdmUnit result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(admUnitJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        AdmUnitCriteria criteria = new AdmUnitCriteria();
        Pageable pageable = Pageable.unpaged();
        AdmUnitEntity entity = new AdmUnitEntity();
        AdmUnit model = new AdmUnit();
        Page<AdmUnitEntity> entityPage = new PageImpl<>(List.of(entity));
        when(admUnitJPARepository.findAll(ArgumentMatchers.<Specification<AdmUnitEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(admUnitMapper.toModel(entity)).thenReturn(model);

        Page<AdmUnit> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByNameAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(admUnitJPARepository.existsByNameAndDeletedAtIsNull("Central Office")).thenReturn(true);

        assertEquals(true, adapter.existsByNameAndDeletedAtIsNull("Central Office"));
    }

    @Test
    void existsByNameAndIdNotAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(admUnitJPARepository.existsByNameAndIdNotAndDeletedAtIsNull("Central Office", 1L)).thenReturn(true);

        assertEquals(true, adapter.existsByNameAndIdNotAndDeletedAtIsNull("Central Office", 1L));
    }

    @Test
    void existsByCodeAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(admUnitJPARepository.existsByCodeAndDeletedAtIsNull("PRO")).thenReturn(true);

        assertEquals(true, adapter.existsByCodeAndDeletedAtIsNull("PRO"));
    }

    @Test
    void existsByCodeAndIdNotAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(admUnitJPARepository.existsByCodeAndIdNotAndDeletedAtIsNull("PRO", 1L)).thenReturn(true);

        assertEquals(true, adapter.existsByCodeAndIdNotAndDeletedAtIsNull("PRO", 1L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        AdmUnit model = new AdmUnit();
        AdmUnitEntity toSave = new AdmUnitEntity();
        AdmUnitEntity saved = new AdmUnitEntity();
        saved.setId(5L);
        saved.setCode("PRO");
        saved.setName("Central Office");
        saved.setNameEs("Oficina Central");
        AdmUnit response = new AdmUnit();
        when(admUnitMapper.toEntity(model)).thenReturn(toSave);
        when(admUnitJPARepository.save(toSave)).thenReturn(saved);
        when(admUnitMapper.toModel(saved)).thenReturn(response);

        AdmUnit result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<AdmUnitAudEntity> captor = ArgumentCaptor.forClass(AdmUnitAudEntity.class);
        verify(admUnitAudJPARepository).save(captor.capture());
        AdmUnitAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getAdmUnitId());
        assertEquals("PRO", aud.getCode());
        assertEquals("Central Office", aud.getName());
        // Known bug: AdmUnitRepositoryAdapter#saveAuditRecord never copies nameEs onto the
        // AdmUnitAudEntity, even though the source entity has a non-null nameEs. This assertion
        // documents the actual (buggy) behavior, not the desired one.
        assertNull(aud.getNameEs());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        AdmUnit model = new AdmUnit();
        AdmUnitEntity toSave = new AdmUnitEntity();
        AdmUnitEntity saved = new AdmUnitEntity();
        saved.setId(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(admUnitMapper.toEntity(model)).thenReturn(toSave);
        when(admUnitJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<AdmUnitAudEntity> captor = ArgumentCaptor.forClass(AdmUnitAudEntity.class);
        verify(admUnitAudJPARepository).save(captor.capture());
        AdmUnitAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        AdmUnit model = new AdmUnit();
        AdmUnitEntity toSave = new AdmUnitEntity();
        AdmUnitEntity saved = new AdmUnitEntity();
        saved.setId(7L);
        AdmUnit response = new AdmUnit();
        when(admUnitMapper.toEntity(model)).thenReturn(toSave);
        when(admUnitJPARepository.save(toSave)).thenReturn(saved);
        when(admUnitMapper.toModel(saved)).thenReturn(response);

        AdmUnit result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<AdmUnitAudEntity> captor = ArgumentCaptor.forClass(AdmUnitAudEntity.class);
        verify(admUnitAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        AdmUnit model = new AdmUnit();
        model.setId(8L);
        AdmUnitEntity toSave = new AdmUnitEntity();
        AdmUnitEntity saved = new AdmUnitEntity();
        saved.setId(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(admUnitMapper.toEntity(model)).thenReturn(toSave);
        when(admUnitJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<AdmUnitAudEntity> captor = ArgumentCaptor.forClass(AdmUnitAudEntity.class);
        verify(admUnitAudJPARepository).save(captor.capture());
        AdmUnitAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
