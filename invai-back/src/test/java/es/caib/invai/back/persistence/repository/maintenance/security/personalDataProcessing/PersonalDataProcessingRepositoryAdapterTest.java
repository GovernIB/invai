package es.caib.invai.back.persistence.repository.maintenance.security.personalDataProcessing;

import es.caib.invai.back.persistence.model.maintenance.security.personalDataProcessing.PersonalDataProcessingAudEntity;
import es.caib.invai.back.persistence.model.maintenance.security.personalDataProcessing.PersonalDataProcessingEntity;
import es.caib.invai.back.service.mapper.maintenance.security.personalDataProcessing.PersonalDataProcessingMapper;
import es.caib.invai.back.service.model.maintenance.security.personalDataProcessing.PersonalDataProcessing;
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
 * Unit tests for {@link PersonalDataProcessingRepositoryAdapter}, verifying entity/model delegation to the
 * {@link PersonalDataProcessingJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class PersonalDataProcessingRepositoryAdapterTest {

    @Mock
    private PersonalDataProcessingJPARepository personalDataProcessingJPARepository;

    @Mock
    private PersonalDataProcessingAudJPARepository personalDataProcessingAudJPARepository;

    @Mock
    private PersonalDataProcessingMapper personalDataProcessingMapper;

    private PersonalDataProcessingRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private PersonalDataProcessingRepositoryAdapter buildAdapter() {
        PersonalDataProcessingRepositoryAdapter a = new PersonalDataProcessingRepositoryAdapter();
        ReflectionTestUtils.setField(a, "personalDataProcessingJPARepository", personalDataProcessingJPARepository);
        ReflectionTestUtils.setField(a, "personalDataProcessingAudJPARepository", personalDataProcessingAudJPARepository);
        ReflectionTestUtils.setField(a, "personalDataProcessingMapper", personalDataProcessingMapper);
        return a;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        PersonalDataProcessingEntity entity = new PersonalDataProcessingEntity();
        entity.setId(1L);
        PersonalDataProcessing model = new PersonalDataProcessing();
        when(personalDataProcessingJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(personalDataProcessingMapper.toModel(entity)).thenReturn(model);

        PersonalDataProcessing result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(personalDataProcessingJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        PersonalDataProcessingCriteria criteria = new PersonalDataProcessingCriteria();
        Pageable pageable = Pageable.unpaged();
        PersonalDataProcessingEntity entity = new PersonalDataProcessingEntity();
        PersonalDataProcessing model = new PersonalDataProcessing();
        Page<PersonalDataProcessingEntity> entityPage = new PageImpl<>(List.of(entity));
        when(personalDataProcessingJPARepository.findAll(ArgumentMatchers.<Specification<PersonalDataProcessingEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(personalDataProcessingMapper.toModel(entity)).thenReturn(model);

        Page<PersonalDataProcessing> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByNameAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(personalDataProcessingJPARepository.existsByNameAndDeletedAtIsNull("Firmar peticiones")).thenReturn(true);

        assertTrue(adapter.existsByNameAndDeletedAtIsNull("Firmar peticiones"));
    }

    @Test
    void existsByNameAndIdNotAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(personalDataProcessingJPARepository.existsByNameAndIdNotAndDeletedAtIsNull("Firmar peticiones", 1L)).thenReturn(true);

        assertTrue(adapter.existsByNameAndIdNotAndDeletedAtIsNull("Firmar peticiones", 1L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        PersonalDataProcessing model = new PersonalDataProcessing();
        PersonalDataProcessingEntity toSave = new PersonalDataProcessingEntity();
        PersonalDataProcessingEntity saved = new PersonalDataProcessingEntity();
        saved.setId(5L);
        saved.setName("Firmar peticiones");
        saved.setNameEs("Firmar peticiones");
        PersonalDataProcessing response = new PersonalDataProcessing();
        when(personalDataProcessingMapper.toEntity(model)).thenReturn(toSave);
        when(personalDataProcessingJPARepository.save(toSave)).thenReturn(saved);
        when(personalDataProcessingMapper.toModel(saved)).thenReturn(response);

        PersonalDataProcessing result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<PersonalDataProcessingAudEntity> captor = ArgumentCaptor.forClass(PersonalDataProcessingAudEntity.class);
        verify(personalDataProcessingAudJPARepository).save(captor.capture());
        PersonalDataProcessingAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getPersonalDataProcessingId());
        assertEquals("Firmar peticiones", aud.getName());
        assertEquals("Firmar peticiones", aud.getNameEs());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        PersonalDataProcessing model = new PersonalDataProcessing();
        PersonalDataProcessingEntity toSave = new PersonalDataProcessingEntity();
        PersonalDataProcessingEntity saved = new PersonalDataProcessingEntity();
        saved.setId(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(personalDataProcessingMapper.toEntity(model)).thenReturn(toSave);
        when(personalDataProcessingJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<PersonalDataProcessingAudEntity> captor = ArgumentCaptor.forClass(PersonalDataProcessingAudEntity.class);
        verify(personalDataProcessingAudJPARepository).save(captor.capture());
        PersonalDataProcessingAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        PersonalDataProcessing model = new PersonalDataProcessing();
        PersonalDataProcessingEntity toSave = new PersonalDataProcessingEntity();
        PersonalDataProcessingEntity saved = new PersonalDataProcessingEntity();
        saved.setId(7L);
        PersonalDataProcessing response = new PersonalDataProcessing();
        when(personalDataProcessingMapper.toEntity(model)).thenReturn(toSave);
        when(personalDataProcessingJPARepository.save(toSave)).thenReturn(saved);
        when(personalDataProcessingMapper.toModel(saved)).thenReturn(response);

        PersonalDataProcessing result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<PersonalDataProcessingAudEntity> captor = ArgumentCaptor.forClass(PersonalDataProcessingAudEntity.class);
        verify(personalDataProcessingAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        PersonalDataProcessing model = new PersonalDataProcessing();
        model.setId(8L);
        PersonalDataProcessingEntity toSave = new PersonalDataProcessingEntity();
        PersonalDataProcessingEntity saved = new PersonalDataProcessingEntity();
        saved.setId(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(personalDataProcessingMapper.toEntity(model)).thenReturn(toSave);
        when(personalDataProcessingJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<PersonalDataProcessingAudEntity> captor = ArgumentCaptor.forClass(PersonalDataProcessingAudEntity.class);
        verify(personalDataProcessingAudJPARepository).save(captor.capture());
        PersonalDataProcessingAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
