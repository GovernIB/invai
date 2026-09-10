package es.caib.invai.back.persistence.repository.maintenance.classificationSegment;

import es.caib.invai.back.persistence.model.maintenance.classificationSegment.ClassificationSegmentAudEntity;
import es.caib.invai.back.persistence.model.maintenance.classificationSegment.ClassificationSegmentEntity;
import es.caib.invai.back.service.mapper.maintenance.classificationSegment.ClassificationSegmentMapper;
import es.caib.invai.back.service.model.maintenance.classificationSegment.ClassificationSegment;
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
 * Unit tests for {@link ClassificationSegmentRepositoryAdapter}, verifying entity/model delegation to the
 * {@link ClassificationSegmentJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class ClassificationSegmentRepositoryAdapterTest {

    @Mock
    private ClassificationSegmentJPARepository classificationSegmentJPARepository;

    @Mock
    private ClassificationSegmentAudJPARepository classificationSegmentAudJPARepository;

    @Mock
    private ClassificationSegmentMapper classificationSegmentMapper;

    private ClassificationSegmentRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private ClassificationSegmentRepositoryAdapter buildAdapter() {
        ClassificationSegmentRepositoryAdapter a = new ClassificationSegmentRepositoryAdapter();
        ReflectionTestUtils.setField(a, "classificationSegmentJPARepository", classificationSegmentJPARepository);
        ReflectionTestUtils.setField(a, "classificationSegmentAudJPARepository", classificationSegmentAudJPARepository);
        ReflectionTestUtils.setField(a, "classificationSegmentMapper", classificationSegmentMapper);
        return a;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        ClassificationSegmentEntity entity = new ClassificationSegmentEntity();
        entity.setId(1L);
        ClassificationSegment model = new ClassificationSegment();
        when(classificationSegmentJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(classificationSegmentMapper.toModel(entity)).thenReturn(model);

        ClassificationSegment result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(classificationSegmentJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        ClassificationSegmentCriteria criteria = new ClassificationSegmentCriteria();
        Pageable pageable = Pageable.unpaged();
        ClassificationSegmentEntity entity = new ClassificationSegmentEntity();
        ClassificationSegment model = new ClassificationSegment();
        Page<ClassificationSegmentEntity> entityPage = new PageImpl<>(List.of(entity));
        when(classificationSegmentJPARepository.findAll(ArgumentMatchers.<Specification<ClassificationSegmentEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(classificationSegmentMapper.toModel(entity)).thenReturn(model);

        Page<ClassificationSegment> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByNameAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(classificationSegmentJPARepository.existsByNameAndDeletedAtIsNull("Segment I")).thenReturn(true);

        assertTrue(adapter.existsByNameAndDeletedAtIsNull("Segment I"));
    }

    @Test
    void existsByNameAndIdNotAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(classificationSegmentJPARepository.existsByNameAndIdNotAndDeletedAtIsNull("Segment I", 1L)).thenReturn(true);

        assertTrue(adapter.existsByNameAndIdNotAndDeletedAtIsNull("Segment I", 1L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        ClassificationSegment model = new ClassificationSegment();
        ClassificationSegmentEntity toSave = new ClassificationSegmentEntity();
        ClassificationSegmentEntity saved = new ClassificationSegmentEntity();
        saved.setId(5L);
        saved.setName("Segment I");
        saved.setNameEs("Segment I");
        ClassificationSegment response = new ClassificationSegment();
        when(classificationSegmentMapper.toEntity(model)).thenReturn(toSave);
        when(classificationSegmentJPARepository.save(toSave)).thenReturn(saved);
        when(classificationSegmentMapper.toModel(saved)).thenReturn(response);

        ClassificationSegment result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<ClassificationSegmentAudEntity> captor = ArgumentCaptor.forClass(ClassificationSegmentAudEntity.class);
        verify(classificationSegmentAudJPARepository).save(captor.capture());
        ClassificationSegmentAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getClassificationSegmentId());
        assertEquals("Segment I", aud.getName());
        assertEquals("Segment I", aud.getNameEs());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        ClassificationSegment model = new ClassificationSegment();
        ClassificationSegmentEntity toSave = new ClassificationSegmentEntity();
        ClassificationSegmentEntity saved = new ClassificationSegmentEntity();
        saved.setId(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(classificationSegmentMapper.toEntity(model)).thenReturn(toSave);
        when(classificationSegmentJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<ClassificationSegmentAudEntity> captor = ArgumentCaptor.forClass(ClassificationSegmentAudEntity.class);
        verify(classificationSegmentAudJPARepository).save(captor.capture());
        ClassificationSegmentAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        ClassificationSegment model = new ClassificationSegment();
        ClassificationSegmentEntity toSave = new ClassificationSegmentEntity();
        ClassificationSegmentEntity saved = new ClassificationSegmentEntity();
        saved.setId(7L);
        ClassificationSegment response = new ClassificationSegment();
        when(classificationSegmentMapper.toEntity(model)).thenReturn(toSave);
        when(classificationSegmentJPARepository.save(toSave)).thenReturn(saved);
        when(classificationSegmentMapper.toModel(saved)).thenReturn(response);

        ClassificationSegment result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<ClassificationSegmentAudEntity> captor = ArgumentCaptor.forClass(ClassificationSegmentAudEntity.class);
        verify(classificationSegmentAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        ClassificationSegment model = new ClassificationSegment();
        model.setId(8L);
        ClassificationSegmentEntity toSave = new ClassificationSegmentEntity();
        ClassificationSegmentEntity saved = new ClassificationSegmentEntity();
        saved.setId(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(classificationSegmentMapper.toEntity(model)).thenReturn(toSave);
        when(classificationSegmentJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<ClassificationSegmentAudEntity> captor = ArgumentCaptor.forClass(ClassificationSegmentAudEntity.class);
        verify(classificationSegmentAudJPARepository).save(captor.capture());
        ClassificationSegmentAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
