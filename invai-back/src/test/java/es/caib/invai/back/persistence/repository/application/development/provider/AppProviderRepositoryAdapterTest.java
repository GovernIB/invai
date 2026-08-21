package es.caib.invai.back.persistence.repository.application.development.provider;

import es.caib.invai.back.persistence.model.*;
import es.caib.invai.back.service.mapper.application.development.provider.AppProviderMapper;
import es.caib.invai.back.service.model.application.development.provider.AppProvider;
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
import es.caib.invai.back.persistence.model.application.development.core.AppDevelopmentEntity;
import es.caib.invai.back.persistence.model.maintenance.development.role.RoleEntity;
import es.caib.invai.back.persistence.model.application.development.provider.AppProviderAudEntity;
import es.caib.invai.back.persistence.model.application.development.provider.AppProviderEntity;
import org.mockito.ArgumentMatchers;

/**
 * Unit tests for {@link AppProviderRepositoryAdapter}, verifying entity/model delegation to the
 * {@link AppProviderJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class AppProviderRepositoryAdapterTest {

    @Mock
    private AppProviderJPARepository appProviderJPARepository;

    @Mock
    private AppProviderAudJPARepository appProviderAudJPARepository;

    @Mock
    private AppProviderMapper appProviderMapper;

    private AppProviderRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AppProviderRepositoryAdapter buildAdapter() {
        AppProviderRepositoryAdapter a = new AppProviderRepositoryAdapter();
        ReflectionTestUtils.setField(a, "appProviderJPARepository", appProviderJPARepository);
        ReflectionTestUtils.setField(a, "appProviderAudJPARepository", appProviderAudJPARepository);
        ReflectionTestUtils.setField(a, "appProviderMapper", appProviderMapper);
        return a;
    }

    private AppDevelopmentEntity developmentEntity(Long id) {
        AppDevelopmentEntity development = new AppDevelopmentEntity();
        development.setId(id);
        return development;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        AppProviderEntity entity = new AppProviderEntity();
        entity.setId(1L);
        AppProvider model = new AppProvider();
        when(appProviderJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(appProviderMapper.toModel(entity)).thenReturn(model);

        AppProvider result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(appProviderJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        AppProviderCriteria criteria = new AppProviderCriteria();
        Pageable pageable = Pageable.unpaged();
        AppProviderEntity entity = new AppProviderEntity();
        AppProvider model = new AppProvider();
        Page<AppProviderEntity> entityPage = new PageImpl<>(List.of(entity));
        when(appProviderJPARepository.findAll(ArgumentMatchers.<Specification<AppProviderEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(appProviderMapper.toModel(entity)).thenReturn(model);

        Page<AppProvider> result = adapter.findAll(10L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByRoleId_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(appProviderJPARepository.existsByRoleId(3L)).thenReturn(true);

        assertTrue(adapter.existsByRoleId(3L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        AppProvider model = new AppProvider();
        AppProviderEntity toSave = new AppProviderEntity();
        AppProviderEntity saved = new AppProviderEntity();
        saved.setId(5L);
        saved.setAppDevelopment(developmentEntity(20L));
        saved.setCompanyName("Acme Corp");
        RoleEntity role = new RoleEntity();
        role.setId(7L);
        saved.setRole(role);
        saved.setStartDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        saved.setExpireDate(LocalDateTime.of(2027, 1, 1, 0, 0));
        AppProvider response = new AppProvider();
        when(appProviderMapper.toEntity(model)).thenReturn(toSave);
        when(appProviderJPARepository.save(toSave)).thenReturn(saved);
        when(appProviderMapper.toModel(saved)).thenReturn(response);

        AppProvider result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<AppProviderAudEntity> captor = ArgumentCaptor.forClass(AppProviderAudEntity.class);
        verify(appProviderAudJPARepository).save(captor.capture());
        AppProviderAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getProviderId());
        assertEquals(20L, aud.getAppDevelopmentId());
        assertEquals("Acme Corp", aud.getCompanyName());
        assertEquals(7L, aud.getRoleId());
        assertEquals(saved.getStartDate(), aud.getStartDate());
        assertEquals(saved.getExpireDate(), aud.getExpireDate());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getUpdatedAt());
        assertEquals("SYSTEM_USER", aud.getUpdatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_withNullRole_setsNullRoleIdOnAuditRecord() {
        adapter = buildAdapter();
        AppProvider model = new AppProvider();
        AppProviderEntity toSave = new AppProviderEntity();
        AppProviderEntity saved = new AppProviderEntity();
        saved.setId(6L);
        saved.setAppDevelopment(developmentEntity(21L));
        saved.setRole(null);
        when(appProviderMapper.toEntity(model)).thenReturn(toSave);
        when(appProviderJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<AppProviderAudEntity> captor = ArgumentCaptor.forClass(AppProviderAudEntity.class);
        verify(appProviderAudJPARepository).save(captor.capture());
        assertNull(captor.getValue().getRoleId());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        AppProvider model = new AppProvider();
        AppProviderEntity toSave = new AppProviderEntity();
        AppProviderEntity saved = new AppProviderEntity();
        saved.setId(7L);
        saved.setAppDevelopment(developmentEntity(22L));
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        LocalDateTime existingUpdatedAt = LocalDateTime.of(2025, 6, 1, 0, 0);
        saved.setUpdatedAt(existingUpdatedAt);
        saved.setUpdatedBy("asmith");
        when(appProviderMapper.toEntity(model)).thenReturn(toSave);
        when(appProviderJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<AppProviderAudEntity> captor = ArgumentCaptor.forClass(AppProviderAudEntity.class);
        verify(appProviderAudJPARepository).save(captor.capture());
        AppProviderAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
        assertEquals(existingUpdatedAt, aud.getUpdatedAt());
        assertEquals("asmith", aud.getUpdatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        AppProvider model = new AppProvider();
        AppProviderEntity toSave = new AppProviderEntity();
        AppProviderEntity saved = new AppProviderEntity();
        saved.setId(8L);
        saved.setAppDevelopment(developmentEntity(23L));
        AppProvider response = new AppProvider();
        when(appProviderMapper.toEntity(model)).thenReturn(toSave);
        when(appProviderJPARepository.save(toSave)).thenReturn(saved);
        when(appProviderMapper.toModel(saved)).thenReturn(response);

        AppProvider result = adapter.update(model, 8L);

        assertSame(response, result);
        assertEquals(8L, toSave.getId());
        ArgumentCaptor<AppProviderAudEntity> captor = ArgumentCaptor.forClass(AppProviderAudEntity.class);
        verify(appProviderAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        AppProvider model = new AppProvider();
        model.setId(9L);
        AppProviderEntity toSave = new AppProviderEntity();
        AppProviderEntity saved = new AppProviderEntity();
        saved.setId(9L);
        saved.setAppDevelopment(developmentEntity(24L));
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(appProviderMapper.toEntity(model)).thenReturn(toSave);
        when(appProviderJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(9L, toSave.getId());
        ArgumentCaptor<AppProviderAudEntity> captor = ArgumentCaptor.forClass(AppProviderAudEntity.class);
        verify(appProviderAudJPARepository).save(captor.capture());
        AppProviderAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
