package es.caib.invai.back.persistence.repository.maintenance.general.field;

import es.caib.invai.back.persistence.model.maintenance.general.field.FieldAudEntity;
import es.caib.invai.back.persistence.model.maintenance.general.field.FieldEntity;
import es.caib.invai.back.service.mapper.maintenance.general.field.FieldMapper;
import es.caib.invai.back.service.model.maintenance.general.field.Field;
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
 * Unit tests for {@link FieldRepositoryAdapter}, verifying entity/model delegation to the
 * {@link FieldJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class FieldRepositoryAdapterTest {

    @Mock
    private FieldJPARepository fieldJPARepository;

    @Mock
    private FieldAudJPARepository fieldAudJPARepository;

    @Mock
    private FieldMapper fieldMapper;

    private FieldRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private FieldRepositoryAdapter buildAdapter() {
        FieldRepositoryAdapter a = new FieldRepositoryAdapter();
        ReflectionTestUtils.setField(a, "fieldJPARepository", fieldJPARepository);
        ReflectionTestUtils.setField(a, "fieldAudJPARepository", fieldAudJPARepository);
        ReflectionTestUtils.setField(a, "fieldMapper", fieldMapper);
        return a;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        FieldEntity entity = new FieldEntity();
        entity.setId(1L);
        Field model = new Field();
        when(fieldJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(fieldMapper.toModel(entity)).thenReturn(model);

        Field result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(fieldJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        FieldCriteria criteria = new FieldCriteria();
        Pageable pageable = Pageable.unpaged();
        FieldEntity entity = new FieldEntity();
        Field model = new Field();
        Page<FieldEntity> entityPage = new PageImpl<>(List.of(entity));
        when(fieldJPARepository.findAll(ArgumentMatchers.<Specification<FieldEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(fieldMapper.toModel(entity)).thenReturn(model);

        Page<Field> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByNameAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(fieldJPARepository.existsByNameAndDeletedAtIsNull("Finance")).thenReturn(true);

        assertTrue(adapter.existsByNameAndDeletedAtIsNull("Finance"));
    }

    @Test
    void existsByNameAndIdNotAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(fieldJPARepository.existsByNameAndIdNotAndDeletedAtIsNull("Finance", 1L)).thenReturn(true);

        assertTrue(adapter.existsByNameAndIdNotAndDeletedAtIsNull("Finance", 1L));
    }

    @Test
    void existsByNameEsAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(fieldJPARepository.existsByNameEsAndDeletedAtIsNull("Finance ES")).thenReturn(true);

        assertTrue(adapter.existsByNameEsAndDeletedAtIsNull("Finance ES"));
    }

    @Test
    void existsByNameEsAndIdNotAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(fieldJPARepository.existsByNameEsAndIdNotAndDeletedAtIsNull("Finance ES", 1L)).thenReturn(true);

        assertTrue(adapter.existsByNameEsAndIdNotAndDeletedAtIsNull("Finance ES", 1L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        Field model = new Field();
        FieldEntity toSave = new FieldEntity();
        FieldEntity saved = new FieldEntity();
        saved.setId(5L);
        saved.setName("Finance");
        saved.setNameEs("Finanzas");
        Field response = new Field();
        when(fieldMapper.toEntity(model)).thenReturn(toSave);
        when(fieldJPARepository.save(toSave)).thenReturn(saved);
        when(fieldMapper.toModel(saved)).thenReturn(response);

        Field result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<FieldAudEntity> captor = ArgumentCaptor.forClass(FieldAudEntity.class);
        verify(fieldAudJPARepository).save(captor.capture());
        FieldAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getFieldId());
        assertEquals("Finance", aud.getName());
        assertEquals("Finanzas", aud.getNameEs());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        Field model = new Field();
        FieldEntity toSave = new FieldEntity();
        FieldEntity saved = new FieldEntity();
        saved.setId(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(fieldMapper.toEntity(model)).thenReturn(toSave);
        when(fieldJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<FieldAudEntity> captor = ArgumentCaptor.forClass(FieldAudEntity.class);
        verify(fieldAudJPARepository).save(captor.capture());
        FieldAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        Field model = new Field();
        FieldEntity toSave = new FieldEntity();
        FieldEntity saved = new FieldEntity();
        saved.setId(7L);
        Field response = new Field();
        when(fieldMapper.toEntity(model)).thenReturn(toSave);
        when(fieldJPARepository.save(toSave)).thenReturn(saved);
        when(fieldMapper.toModel(saved)).thenReturn(response);

        Field result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<FieldAudEntity> captor = ArgumentCaptor.forClass(FieldAudEntity.class);
        verify(fieldAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        Field model = new Field();
        model.setId(8L);
        FieldEntity toSave = new FieldEntity();
        FieldEntity saved = new FieldEntity();
        saved.setId(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(fieldMapper.toEntity(model)).thenReturn(toSave);
        when(fieldJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<FieldAudEntity> captor = ArgumentCaptor.forClass(FieldAudEntity.class);
        verify(fieldAudJPARepository).save(captor.capture());
        FieldAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
