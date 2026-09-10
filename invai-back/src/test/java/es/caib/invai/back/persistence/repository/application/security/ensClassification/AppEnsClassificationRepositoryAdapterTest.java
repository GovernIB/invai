package es.caib.invai.back.persistence.repository.application.security.ensClassification;

import es.caib.invai.back.persistence.model.application.security.ensClassification.AppEnsClassificationAudEntity;
import es.caib.invai.back.persistence.model.application.security.ensClassification.AppEnsClassificationEntity;
import es.caib.invai.back.persistence.model.application.security.core.AppSecurityEntity;
import es.caib.invai.back.persistence.model.maintenance.security.identityProvider.IdentityProviderEntity;
import es.caib.invai.back.persistence.model.catalog.ensSubject.LkupEnsSubjectEntity;
import es.caib.invai.back.service.mapper.application.security.ensClassification.AppEnsClassificationMapper;
import es.caib.invai.back.service.model.application.security.ensClassification.AppEnsClassification;
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
 * Unit tests for {@link AppEnsClassificationRepositoryAdapter}, verifying entity/model delegation to the
 * {@link AppEnsClassificationJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class AppEnsClassificationRepositoryAdapterTest {

    @Mock
    private AppEnsClassificationJPARepository appEnsClassificationJPARepository;

    @Mock
    private AppEnsClassificationAudJPARepository appEnsClassificationAudJPARepository;

    @Mock
    private AppEnsClassificationMapper appEnsClassificationMapper;

    private AppEnsClassificationRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AppEnsClassificationRepositoryAdapter buildAdapter() {
        AppEnsClassificationRepositoryAdapter a = new AppEnsClassificationRepositoryAdapter();
        ReflectionTestUtils.setField(a, "appEnsClassificationJPARepository", appEnsClassificationJPARepository);
        ReflectionTestUtils.setField(a, "appEnsClassificationAudJPARepository", appEnsClassificationAudJPARepository);
        ReflectionTestUtils.setField(a, "appEnsClassificationMapper", appEnsClassificationMapper);
        return a;
    }

    private AppEnsClassificationEntity entityWithRelations(Long id) {
        AppEnsClassificationEntity entity = new AppEnsClassificationEntity();
        entity.setId(id);
        AppSecurityEntity appSecurity = new AppSecurityEntity();
        appSecurity.setId(10L);
        entity.setAppSecurity(appSecurity);
        IdentityProviderEntity identityProvider = new IdentityProviderEntity();
        identityProvider.setId(20L);
        entity.setIdentityProvider(identityProvider);
        LkupEnsSubjectEntity ensSubject = new LkupEnsSubjectEntity();
        ensSubject.setId(30L);
        entity.setEnsSubject(ensSubject);
        // personalDataProcessing and the 6 security-level FKs are intentionally left null to
        // exercise the adapter's null-safe optional FK reads in saveAuditRecord.
        return entity;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        AppEnsClassificationEntity entity = entityWithRelations(1L);
        AppEnsClassification model = new AppEnsClassification();
        when(appEnsClassificationJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(appEnsClassificationMapper.toModel(entity)).thenReturn(model);

        AppEnsClassification result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(appEnsClassificationJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        AppEnsClassificationCriteria criteria = new AppEnsClassificationCriteria();
        Pageable pageable = Pageable.unpaged();
        AppEnsClassificationEntity entity = entityWithRelations(1L);
        AppEnsClassification model = new AppEnsClassification();
        Page<AppEnsClassificationEntity> entityPage = new PageImpl<>(List.of(entity));
        when(appEnsClassificationJPARepository.findAll(ArgumentMatchers.<Specification<AppEnsClassificationEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(appEnsClassificationMapper.toModel(entity)).thenReturn(model);

        Page<AppEnsClassification> result = adapter.findAll(10L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        AppEnsClassification model = new AppEnsClassification();
        AppEnsClassificationEntity toSave = new AppEnsClassificationEntity();
        AppEnsClassificationEntity saved = entityWithRelations(5L);
        AppEnsClassification response = new AppEnsClassification();
        when(appEnsClassificationMapper.toEntity(model)).thenReturn(toSave);
        when(appEnsClassificationJPARepository.save(toSave)).thenReturn(saved);
        when(appEnsClassificationMapper.toModel(saved)).thenReturn(response);

        AppEnsClassification result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<AppEnsClassificationAudEntity> captor = ArgumentCaptor.forClass(AppEnsClassificationAudEntity.class);
        verify(appEnsClassificationAudJPARepository).save(captor.capture());
        AppEnsClassificationAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getAppEnsClassificationId());
        assertEquals(10L, aud.getAppSecurityId());
        assertEquals(20L, aud.getIdentityProviderId());
        assertEquals(30L, aud.getEnsSubjectId());
        assertNull(aud.getPersonalDataProcessingId());
        assertNull(aud.getConfidentialityId());
        assertNull(aud.getIntegrityId());
        assertNull(aud.getTraceabilityId());
        assertNull(aud.getAvailabilityId());
        assertNull(aud.getAuthenticityId());
        assertNull(aud.getOverallGradeId());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        AppEnsClassification model = new AppEnsClassification();
        AppEnsClassificationEntity toSave = new AppEnsClassificationEntity();
        AppEnsClassificationEntity saved = entityWithRelations(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(appEnsClassificationMapper.toEntity(model)).thenReturn(toSave);
        when(appEnsClassificationJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<AppEnsClassificationAudEntity> captor = ArgumentCaptor.forClass(AppEnsClassificationAudEntity.class);
        verify(appEnsClassificationAudJPARepository).save(captor.capture());
        AppEnsClassificationAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        AppEnsClassification model = new AppEnsClassification();
        AppEnsClassificationEntity toSave = new AppEnsClassificationEntity();
        AppEnsClassificationEntity saved = entityWithRelations(7L);
        AppEnsClassification response = new AppEnsClassification();
        when(appEnsClassificationMapper.toEntity(model)).thenReturn(toSave);
        when(appEnsClassificationJPARepository.save(toSave)).thenReturn(saved);
        when(appEnsClassificationMapper.toModel(saved)).thenReturn(response);

        AppEnsClassification result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<AppEnsClassificationAudEntity> captor = ArgumentCaptor.forClass(AppEnsClassificationAudEntity.class);
        verify(appEnsClassificationAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        AppEnsClassification model = new AppEnsClassification();
        model.setId(8L);
        AppEnsClassificationEntity toSave = new AppEnsClassificationEntity();
        AppEnsClassificationEntity saved = entityWithRelations(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(appEnsClassificationMapper.toEntity(model)).thenReturn(toSave);
        when(appEnsClassificationJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<AppEnsClassificationAudEntity> captor = ArgumentCaptor.forClass(AppEnsClassificationAudEntity.class);
        verify(appEnsClassificationAudJPARepository).save(captor.capture());
        AppEnsClassificationAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
