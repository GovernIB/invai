package es.caib.invai.back.persistence.repository.maintenance.security.securityMeasureType;

import es.caib.invai.back.persistence.model.maintenance.security.securityMeasureType.SecurityMeasureTypeAudEntity;
import es.caib.invai.back.persistence.model.maintenance.security.securityMeasureType.SecurityMeasureTypeEntity;
import es.caib.invai.back.service.mapper.maintenance.security.securityMeasureType.SecurityMeasureTypeMapper;
import es.caib.invai.back.service.model.maintenance.security.securityMeasureType.SecurityMeasureType;
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
 * Unit tests for {@link SecurityMeasureTypeRepositoryAdapter}, verifying entity/model delegation to the
 * {@link SecurityMeasureTypeJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class SecurityMeasureTypeRepositoryAdapterTest {

    @Mock
    private SecurityMeasureTypeJPARepository securityMeasureTypeJPARepository;

    @Mock
    private SecurityMeasureTypeAudJPARepository securityMeasureTypeAudJPARepository;

    @Mock
    private SecurityMeasureTypeMapper securityMeasureTypeMapper;

    private SecurityMeasureTypeRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private SecurityMeasureTypeRepositoryAdapter buildAdapter() {
        SecurityMeasureTypeRepositoryAdapter a = new SecurityMeasureTypeRepositoryAdapter();
        ReflectionTestUtils.setField(a, "securityMeasureTypeJPARepository", securityMeasureTypeJPARepository);
        ReflectionTestUtils.setField(a, "securityMeasureTypeAudJPARepository", securityMeasureTypeAudJPARepository);
        ReflectionTestUtils.setField(a, "securityMeasureTypeMapper", securityMeasureTypeMapper);
        return a;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        SecurityMeasureTypeEntity entity = new SecurityMeasureTypeEntity();
        entity.setId(1L);
        SecurityMeasureType model = new SecurityMeasureType();
        when(securityMeasureTypeJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(securityMeasureTypeMapper.toModel(entity)).thenReturn(model);

        SecurityMeasureType result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(securityMeasureTypeJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        SecurityMeasureTypeCriteria criteria = new SecurityMeasureTypeCriteria();
        Pageable pageable = Pageable.unpaged();
        SecurityMeasureTypeEntity entity = new SecurityMeasureTypeEntity();
        SecurityMeasureType model = new SecurityMeasureType();
        Page<SecurityMeasureTypeEntity> entityPage = new PageImpl<>(List.of(entity));
        when(securityMeasureTypeJPARepository.findAll(ArgumentMatchers.<Specification<SecurityMeasureTypeEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(securityMeasureTypeMapper.toModel(entity)).thenReturn(model);

        Page<SecurityMeasureType> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByNameAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(securityMeasureTypeJPARepository.existsByNameAndDeletedAtIsNull("Organitzativa")).thenReturn(true);

        assertTrue(adapter.existsByNameAndDeletedAtIsNull("Organitzativa"));
    }

    @Test
    void existsByNameAndIdNotAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(securityMeasureTypeJPARepository.existsByNameAndIdNotAndDeletedAtIsNull("Organitzativa", 1L)).thenReturn(true);

        assertTrue(adapter.existsByNameAndIdNotAndDeletedAtIsNull("Organitzativa", 1L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        SecurityMeasureType model = new SecurityMeasureType();
        SecurityMeasureTypeEntity toSave = new SecurityMeasureTypeEntity();
        SecurityMeasureTypeEntity saved = new SecurityMeasureTypeEntity();
        saved.setId(5L);
        saved.setName("Organitzativa");
        saved.setNameEs("Organizativa");
        SecurityMeasureType response = new SecurityMeasureType();
        when(securityMeasureTypeMapper.toEntity(model)).thenReturn(toSave);
        when(securityMeasureTypeJPARepository.save(toSave)).thenReturn(saved);
        when(securityMeasureTypeMapper.toModel(saved)).thenReturn(response);

        SecurityMeasureType result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<SecurityMeasureTypeAudEntity> captor = ArgumentCaptor.forClass(SecurityMeasureTypeAudEntity.class);
        verify(securityMeasureTypeAudJPARepository).save(captor.capture());
        SecurityMeasureTypeAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getSecurityMeasureTypeId());
        assertEquals("Organitzativa", aud.getName());
        assertEquals("Organizativa", aud.getNameEs());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        SecurityMeasureType model = new SecurityMeasureType();
        SecurityMeasureTypeEntity toSave = new SecurityMeasureTypeEntity();
        SecurityMeasureTypeEntity saved = new SecurityMeasureTypeEntity();
        saved.setId(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(securityMeasureTypeMapper.toEntity(model)).thenReturn(toSave);
        when(securityMeasureTypeJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<SecurityMeasureTypeAudEntity> captor = ArgumentCaptor.forClass(SecurityMeasureTypeAudEntity.class);
        verify(securityMeasureTypeAudJPARepository).save(captor.capture());
        SecurityMeasureTypeAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        SecurityMeasureType model = new SecurityMeasureType();
        SecurityMeasureTypeEntity toSave = new SecurityMeasureTypeEntity();
        SecurityMeasureTypeEntity saved = new SecurityMeasureTypeEntity();
        saved.setId(7L);
        SecurityMeasureType response = new SecurityMeasureType();
        when(securityMeasureTypeMapper.toEntity(model)).thenReturn(toSave);
        when(securityMeasureTypeJPARepository.save(toSave)).thenReturn(saved);
        when(securityMeasureTypeMapper.toModel(saved)).thenReturn(response);

        SecurityMeasureType result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<SecurityMeasureTypeAudEntity> captor = ArgumentCaptor.forClass(SecurityMeasureTypeAudEntity.class);
        verify(securityMeasureTypeAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        SecurityMeasureType model = new SecurityMeasureType();
        model.setId(8L);
        SecurityMeasureTypeEntity toSave = new SecurityMeasureTypeEntity();
        SecurityMeasureTypeEntity saved = new SecurityMeasureTypeEntity();
        saved.setId(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(securityMeasureTypeMapper.toEntity(model)).thenReturn(toSave);
        when(securityMeasureTypeJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<SecurityMeasureTypeAudEntity> captor = ArgumentCaptor.forClass(SecurityMeasureTypeAudEntity.class);
        verify(securityMeasureTypeAudJPARepository).save(captor.capture());
        SecurityMeasureTypeAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
