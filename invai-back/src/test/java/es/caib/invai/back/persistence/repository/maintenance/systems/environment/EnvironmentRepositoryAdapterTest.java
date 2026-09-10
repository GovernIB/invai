package es.caib.invai.back.persistence.repository.maintenance.systems.environment;

import es.caib.invai.back.persistence.model.maintenance.systems.environment.EnvironmentAudEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.environment.EnvironmentEntity;
import es.caib.invai.back.service.mapper.maintenance.systems.environment.EnvironmentMapper;
import es.caib.invai.back.service.model.maintenance.systems.environment.Environment;
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
 * Unit tests for {@link EnvironmentRepositoryAdapter}, verifying entity/model delegation to the
 * {@link EnvironmentJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class EnvironmentRepositoryAdapterTest {

    @Mock
    private EnvironmentJPARepository environmentJPARepository;

    @Mock
    private EnvironmentAudJPARepository environmentAudJPARepository;

    @Mock
    private EnvironmentMapper environmentMapper;

    private EnvironmentRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private EnvironmentRepositoryAdapter buildAdapter() {
        EnvironmentRepositoryAdapter a = new EnvironmentRepositoryAdapter();
        ReflectionTestUtils.setField(a, "environmentJPARepository", environmentJPARepository);
        ReflectionTestUtils.setField(a, "environmentAudJPARepository", environmentAudJPARepository);
        ReflectionTestUtils.setField(a, "environmentMapper", environmentMapper);
        return a;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        EnvironmentEntity entity = new EnvironmentEntity();
        entity.setId(1L);
        Environment model = new Environment();
        when(environmentJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(environmentMapper.toModel(entity)).thenReturn(model);

        Environment result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(environmentJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        EnvironmentCriteria criteria = new EnvironmentCriteria();
        Pageable pageable = Pageable.unpaged();
        EnvironmentEntity entity = new EnvironmentEntity();
        Environment model = new Environment();
        Page<EnvironmentEntity> entityPage = new PageImpl<>(List.of(entity));
        when(environmentJPARepository.findAll(ArgumentMatchers.<Specification<EnvironmentEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(environmentMapper.toModel(entity)).thenReturn(model);

        Page<Environment> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByCode_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(environmentJPARepository.existsByCodeAndDeletedAtIsNull("PRO")).thenReturn(true);

        assertTrue(adapter.existsByCode("PRO"));
    }

    @Test
    void existsByCodeAndIdNot_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(environmentJPARepository.existsByCodeAndIdNotAndDeletedAtIsNull("PRO", 1L)).thenReturn(true);

        assertTrue(adapter.existsByCodeAndIdNot("PRO", 1L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        Environment model = new Environment();
        EnvironmentEntity toSave = new EnvironmentEntity();
        EnvironmentEntity saved = new EnvironmentEntity();
        saved.setId(5L);
        saved.setCode("PRO");
        saved.setName("Production");
        saved.setNameEs("Produccion");
        Environment response = new Environment();
        when(environmentMapper.toEntity(model)).thenReturn(toSave);
        when(environmentJPARepository.save(toSave)).thenReturn(saved);
        when(environmentMapper.toModel(saved)).thenReturn(response);

        Environment result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<EnvironmentAudEntity> captor = ArgumentCaptor.forClass(EnvironmentAudEntity.class);
        verify(environmentAudJPARepository).save(captor.capture());
        EnvironmentAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getEnvironmentId());
        assertEquals("PRO", aud.getCode());
        assertEquals("Production", aud.getName());
        assertEquals("Produccion", aud.getNameEs());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNull(aud.getUpdatedAt());
        assertNull(aud.getUpdatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        Environment model = new Environment();
        EnvironmentEntity toSave = new EnvironmentEntity();
        EnvironmentEntity saved = new EnvironmentEntity();
        saved.setId(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime existingUpdatedAt = LocalDateTime.of(2025, 2, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        saved.setUpdatedAt(existingUpdatedAt);
        saved.setUpdatedBy("jdoe2");
        when(environmentMapper.toEntity(model)).thenReturn(toSave);
        when(environmentJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<EnvironmentAudEntity> captor = ArgumentCaptor.forClass(EnvironmentAudEntity.class);
        verify(environmentAudJPARepository).save(captor.capture());
        EnvironmentAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
        assertEquals(existingUpdatedAt, aud.getUpdatedAt());
        assertEquals("jdoe2", aud.getUpdatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        Environment model = new Environment();
        EnvironmentEntity toSave = new EnvironmentEntity();
        EnvironmentEntity saved = new EnvironmentEntity();
        saved.setId(7L);
        Environment response = new Environment();
        when(environmentMapper.toEntity(model)).thenReturn(toSave);
        when(environmentJPARepository.save(toSave)).thenReturn(saved);
        when(environmentMapper.toModel(saved)).thenReturn(response);

        Environment result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<EnvironmentAudEntity> captor = ArgumentCaptor.forClass(EnvironmentAudEntity.class);
        verify(environmentAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        Environment model = new Environment();
        model.setId(8L);
        EnvironmentEntity toSave = new EnvironmentEntity();
        EnvironmentEntity saved = new EnvironmentEntity();
        saved.setId(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(environmentMapper.toEntity(model)).thenReturn(toSave);
        when(environmentJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<EnvironmentAudEntity> captor = ArgumentCaptor.forClass(EnvironmentAudEntity.class);
        verify(environmentAudJPARepository).save(captor.capture());
        EnvironmentAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
