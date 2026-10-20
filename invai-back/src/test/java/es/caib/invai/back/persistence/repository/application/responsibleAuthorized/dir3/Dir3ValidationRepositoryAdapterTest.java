package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.dir3;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.dir3.Dir3ValidationAudEntity;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.dir3.Dir3ValidationEntity;
import es.caib.invai.back.persistence.model.catalog.dir3Status.LkupDir3StatusEntity;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.dir3.Dir3ValidationMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.dir3.Dir3Validation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link Dir3ValidationRepositoryAdapter}, verifying entity/model delegation to the
 * {@link Dir3ValidationJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class Dir3ValidationRepositoryAdapterTest {

    @Mock
    private Dir3ValidationJPARepository dir3ValidationJPARepository;

    @Mock
    private Dir3ValidationAudJPARepository dir3ValidationAudJPARepository;

    @Mock
    private Dir3ValidationMapper dir3ValidationMapper;

    private Dir3ValidationRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private Dir3ValidationRepositoryAdapter buildAdapter() {
        Dir3ValidationRepositoryAdapter a = new Dir3ValidationRepositoryAdapter();
        ReflectionTestUtils.setField(a, "dir3ValidationJPARepository", dir3ValidationJPARepository);
        ReflectionTestUtils.setField(a, "dir3ValidationAudJPARepository", dir3ValidationAudJPARepository);
        ReflectionTestUtils.setField(a, "dir3ValidationMapper", dir3ValidationMapper);
        return a;
    }

    private LkupDir3StatusEntity statusEntity(Long id) {
        LkupDir3StatusEntity status = new LkupDir3StatusEntity();
        status.setId(id);
        return status;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        Dir3ValidationEntity entity = new Dir3ValidationEntity();
        entity.setId(1L);
        Dir3Validation model = new Dir3Validation();
        when(dir3ValidationJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(dir3ValidationMapper.toModel(entity)).thenReturn(model);

        assertSame(model, adapter.findById(1L));
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(dir3ValidationJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        Dir3Validation model = Dir3Validation.builder().build();
        Dir3ValidationEntity toSave = new Dir3ValidationEntity();
        Dir3ValidationEntity saved = new Dir3ValidationEntity();
        saved.setId(5L);
        saved.setDir3Status(statusEntity(2L));
        Dir3Validation response = new Dir3Validation();
        when(dir3ValidationMapper.toEntity(model)).thenReturn(toSave);
        when(dir3ValidationJPARepository.save(toSave)).thenReturn(saved);
        when(dir3ValidationMapper.toModel(saved)).thenReturn(response);

        Dir3Validation result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<Dir3ValidationAudEntity> captor = ArgumentCaptor.forClass(Dir3ValidationAudEntity.class);
        verify(dir3ValidationAudJPARepository).save(captor.capture());
        Dir3ValidationAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getDir3ValidationId());
        assertEquals(2L, aud.getDir3StatusId());
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
        Dir3Validation model = Dir3Validation.builder().build();
        Dir3ValidationEntity toSave = new Dir3ValidationEntity();
        Dir3ValidationEntity saved = new Dir3ValidationEntity();
        saved.setId(6L);
        saved.setDir3Status(statusEntity(1L));
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(dir3ValidationMapper.toEntity(model)).thenReturn(toSave);
        when(dir3ValidationJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<Dir3ValidationAudEntity> captor = ArgumentCaptor.forClass(Dir3ValidationAudEntity.class);
        verify(dir3ValidationAudJPARepository).save(captor.capture());
        Dir3ValidationAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        Dir3Validation model = Dir3Validation.builder().build();
        Dir3ValidationEntity toSave = new Dir3ValidationEntity();
        Dir3ValidationEntity saved = new Dir3ValidationEntity();
        saved.setId(8L);
        saved.setDir3Status(statusEntity(3L));
        saved.setManualValidatedAt(LocalDateTime.of(2025, 3, 1, 10, 0));
        saved.setManualValidatedBy("jdoe");
        saved.setReason("DIR3 corregit manualment per l'administrador");
        Dir3Validation response = new Dir3Validation();
        when(dir3ValidationMapper.toEntity(model)).thenReturn(toSave);
        when(dir3ValidationJPARepository.save(toSave)).thenReturn(saved);
        when(dir3ValidationMapper.toModel(saved)).thenReturn(response);

        Dir3Validation result = adapter.update(model, 8L);

        assertSame(response, result);
        assertEquals(8L, toSave.getId());
        ArgumentCaptor<Dir3ValidationAudEntity> captor = ArgumentCaptor.forClass(Dir3ValidationAudEntity.class);
        verify(dir3ValidationAudJPARepository).save(captor.capture());
        Dir3ValidationAudEntity aud = captor.getValue();
        assertEquals("UPDATE", aud.getAudAction());
        assertEquals(3L, aud.getDir3StatusId());
        assertEquals(LocalDateTime.of(2025, 3, 1, 10, 0), aud.getManualValidatedAt());
        assertEquals("jdoe", aud.getManualValidatedBy());
        assertEquals("DIR3 corregit manualment per l'administrador", aud.getReason());
    }
}
