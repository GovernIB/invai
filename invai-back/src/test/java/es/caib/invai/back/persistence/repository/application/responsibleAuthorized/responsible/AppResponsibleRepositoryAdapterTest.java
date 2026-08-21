package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.core.AppResponsibleAuthorizedEntity;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.responsible.AppResponsibleAudEntity;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.responsible.AppResponsibleEntity;
import es.caib.invai.back.persistence.model.catalog.responsibleType.LkupResponsibleTypeEntity;
import es.caib.invai.back.persistence.model.maintenance.responsible.person.PersonEntity;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.responsible.AppResponsibleMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppResponsibleRepositoryAdapter}, verifying entity/model delegation to the
 * {@link AppResponsibleJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class AppResponsibleRepositoryAdapterTest {

    @Mock
    private AppResponsibleJPARepository appResponsibleJPARepository;

    @Mock
    private AppResponsibleAudJPARepository appResponsibleAudJPARepository;

    @Mock
    private AppResponsibleMapper appResponsibleMapper;

    private AppResponsibleRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AppResponsibleRepositoryAdapter buildAdapter() {
        AppResponsibleRepositoryAdapter a = new AppResponsibleRepositoryAdapter();
        ReflectionTestUtils.setField(a, "appResponsibleJPARepository", appResponsibleJPARepository);
        ReflectionTestUtils.setField(a, "appResponsibleAudJPARepository", appResponsibleAudJPARepository);
        ReflectionTestUtils.setField(a, "appResponsibleMapper", appResponsibleMapper);
        return a;
    }

    private AppResponsibleAuthorizedEntity anchorEntity(Long id) {
        AppResponsibleAuthorizedEntity anchor = new AppResponsibleAuthorizedEntity();
        anchor.setId(id);
        return anchor;
    }

    private PersonEntity personEntity(Long id) {
        PersonEntity person = new PersonEntity();
        person.setId(id);
        return person;
    }

    private LkupResponsibleTypeEntity responsibleTypeEntity(Long id) {
        LkupResponsibleTypeEntity responsibleType = new LkupResponsibleTypeEntity();
        responsibleType.setId(id);
        return responsibleType;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        AppResponsibleEntity entity = new AppResponsibleEntity();
        entity.setId(1L);
        AppResponsible model = new AppResponsible();
        when(appResponsibleJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(appResponsibleMapper.toModel(entity)).thenReturn(model);

        AppResponsible result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(appResponsibleJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        AppResponsibleCriteria criteria = new AppResponsibleCriteria();
        Pageable pageable = Pageable.unpaged();
        AppResponsibleEntity entity = new AppResponsibleEntity();
        AppResponsible model = new AppResponsible();
        Page<AppResponsibleEntity> entityPage = new PageImpl<>(List.of(entity));
        when(appResponsibleJPARepository.findAll(ArgumentMatchers.<Specification<AppResponsibleEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(appResponsibleMapper.toModel(entity)).thenReturn(model);

        Page<AppResponsible> result = adapter.findAll(10L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void findActiveByAppResponsibleAuthorizedAndResponsibleType_found_returnsMappedModel() {
        adapter = buildAdapter();
        AppResponsibleEntity entity = new AppResponsibleEntity();
        entity.setId(1L);
        AppResponsible model = new AppResponsible();
        when(appResponsibleJPARepository.findByAppResponsibleAuthorizedIdAndResponsibleTypeIdAndDeletedAtIsNull(10L, 20L))
                .thenReturn(Optional.of(entity));
        when(appResponsibleMapper.toModel(entity)).thenReturn(model);

        assertSame(model, adapter.findActiveByAppResponsibleAuthorizedAndResponsibleType(10L, 20L));
    }

    @Test
    void findActiveByAppResponsibleAuthorizedAndResponsibleType_notFound_returnsNull() {
        adapter = buildAdapter();
        when(appResponsibleJPARepository.findByAppResponsibleAuthorizedIdAndResponsibleTypeIdAndDeletedAtIsNull(10L, 20L))
                .thenReturn(Optional.empty());

        assertNull(adapter.findActiveByAppResponsibleAuthorizedAndResponsibleType(10L, 20L));
    }

    @Test
    void existsByAppResponsibleAuthorizedAndResponsibleTypeAndIdNot_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(appResponsibleJPARepository.existsByAppResponsibleAuthorizedIdAndResponsibleTypeIdAndDeletedAtIsNullAndIdNot(10L, 20L, 1L)).thenReturn(true);

        assertEquals(true, adapter.existsByAppResponsibleAuthorizedAndResponsibleTypeAndIdNot(10L, 20L, 1L));
    }

    @Test
    void findAllActiveByPersonId_mapsEveryReturnedEntity() {
        adapter = buildAdapter();
        AppResponsibleEntity entity1 = new AppResponsibleEntity();
        AppResponsibleEntity entity2 = new AppResponsibleEntity();
        AppResponsible model1 = new AppResponsible();
        AppResponsible model2 = new AppResponsible();
        when(appResponsibleJPARepository.findAllByPersonIdAndDeletedAtIsNull(10L)).thenReturn(List.of(entity1, entity2));
        when(appResponsibleMapper.toModel(entity1)).thenReturn(model1);
        when(appResponsibleMapper.toModel(entity2)).thenReturn(model2);

        List<AppResponsible> result = adapter.findAllActiveByPersonId(10L);

        assertEquals(List.of(model1, model2), result);
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        AppResponsible model = new AppResponsible();
        AppResponsibleEntity toSave = new AppResponsibleEntity();
        AppResponsibleEntity saved = new AppResponsibleEntity();
        saved.setId(5L);
        saved.setAppResponsibleAuthorized(anchorEntity(40L));
        saved.setPerson(personEntity(10L));
        saved.setResponsibleType(responsibleTypeEntity(20L));
        saved.setObservation("Nova incorporacio");
        AppResponsible response = new AppResponsible();
        when(appResponsibleMapper.toEntity(model)).thenReturn(toSave);
        when(appResponsibleJPARepository.save(toSave)).thenReturn(saved);
        when(appResponsibleMapper.toModel(saved)).thenReturn(response);

        AppResponsible result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<AppResponsibleAudEntity> captor = ArgumentCaptor.forClass(AppResponsibleAudEntity.class);
        verify(appResponsibleAudJPARepository).save(captor.capture());
        AppResponsibleAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getAppResponsibleId());
        assertEquals(40L, aud.getAppResponsibleAuthorizedId());
        assertEquals(10L, aud.getPersonId());
        assertEquals(20L, aud.getResponsibleTypeId());
        assertEquals("Nova incorporacio", aud.getObservation());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getUpdatedAt());
        assertEquals("SYSTEM_USER", aud.getUpdatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        AppResponsible model = new AppResponsible();
        AppResponsibleEntity toSave = new AppResponsibleEntity();
        AppResponsibleEntity saved = new AppResponsibleEntity();
        saved.setId(6L);
        saved.setAppResponsibleAuthorized(anchorEntity(41L));
        saved.setPerson(personEntity(11L));
        saved.setResponsibleType(responsibleTypeEntity(21L));
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        LocalDateTime existingUpdatedAt = LocalDateTime.of(2025, 6, 1, 0, 0);
        saved.setUpdatedAt(existingUpdatedAt);
        saved.setUpdatedBy("asmith");
        when(appResponsibleMapper.toEntity(model)).thenReturn(toSave);
        when(appResponsibleJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<AppResponsibleAudEntity> captor = ArgumentCaptor.forClass(AppResponsibleAudEntity.class);
        verify(appResponsibleAudJPARepository).save(captor.capture());
        AppResponsibleAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
        assertEquals(existingUpdatedAt, aud.getUpdatedAt());
        assertEquals("asmith", aud.getUpdatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        AppResponsible model = new AppResponsible();
        AppResponsibleEntity toSave = new AppResponsibleEntity();
        AppResponsibleEntity saved = new AppResponsibleEntity();
        saved.setId(8L);
        saved.setAppResponsibleAuthorized(anchorEntity(42L));
        saved.setPerson(personEntity(12L));
        saved.setResponsibleType(responsibleTypeEntity(22L));
        AppResponsible response = new AppResponsible();
        when(appResponsibleMapper.toEntity(model)).thenReturn(toSave);
        when(appResponsibleJPARepository.save(toSave)).thenReturn(saved);
        when(appResponsibleMapper.toModel(saved)).thenReturn(response);

        AppResponsible result = adapter.update(model, 8L);

        assertSame(response, result);
        assertEquals(8L, toSave.getId());
        ArgumentCaptor<AppResponsibleAudEntity> captor = ArgumentCaptor.forClass(AppResponsibleAudEntity.class);
        verify(appResponsibleAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        AppResponsible model = new AppResponsible();
        model.setId(9L);
        AppResponsibleEntity toSave = new AppResponsibleEntity();
        AppResponsibleEntity saved = new AppResponsibleEntity();
        saved.setId(9L);
        saved.setAppResponsibleAuthorized(anchorEntity(43L));
        saved.setPerson(personEntity(13L));
        saved.setResponsibleType(responsibleTypeEntity(23L));
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        saved.setObservation("Deixa l'empresa");
        when(appResponsibleMapper.toEntity(model)).thenReturn(toSave);
        when(appResponsibleJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(9L, toSave.getId());
        ArgumentCaptor<AppResponsibleAudEntity> captor = ArgumentCaptor.forClass(AppResponsibleAudEntity.class);
        verify(appResponsibleAudJPARepository).save(captor.capture());
        AppResponsibleAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
        assertEquals("Deixa l'empresa", aud.getObservation());
    }
}
