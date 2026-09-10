package es.caib.invai.back.persistence.repository.application.system_database.system;

import es.caib.invai.back.persistence.model.application.system_database.core.AppInformationSystemDbEntity;
import es.caib.invai.back.persistence.model.application.system_database.system.AppSystemAudEntity;
import es.caib.invai.back.persistence.model.application.system_database.system.AppSystemEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.system.SystemEntity;
import es.caib.invai.back.service.mapper.application.system_database.system.AppSystemMapper;
import es.caib.invai.back.service.model.application.system_database.system.AppSystem;
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
 * Unit tests for {@link AppSystemRepositoryAdapter}, verifying entity/model delegation to the
 * {@link AppSystemJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class AppSystemRepositoryAdapterTest {

    @Mock
    private AppSystemJPARepository appSystemJPARepository;

    @Mock
    private AppSystemAudJPARepository appSystemAudJPARepository;

    @Mock
    private AppSystemMapper appSystemMapper;

    private AppSystemRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AppSystemRepositoryAdapter buildAdapter() {
        AppSystemRepositoryAdapter a = new AppSystemRepositoryAdapter();
        ReflectionTestUtils.setField(a, "appSystemJPARepository", appSystemJPARepository);
        ReflectionTestUtils.setField(a, "appSystemAudJPARepository", appSystemAudJPARepository);
        ReflectionTestUtils.setField(a, "appSystemMapper", appSystemMapper);
        return a;
    }

    private AppSystemEntity entityWithRelations(Long id) {
        AppSystemEntity entity = new AppSystemEntity();
        entity.setId(id);
        AppInformationSystemDbEntity informationSystemDb = new AppInformationSystemDbEntity();
        informationSystemDb.setId(10L);
        entity.setInformationSystemDb(informationSystemDb);
        SystemEntity system = new SystemEntity();
        system.setId(20L);
        entity.setSystem(system);
        return entity;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        AppSystemEntity entity = entityWithRelations(1L);
        AppSystem model = new AppSystem();
        when(appSystemJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(appSystemMapper.toModel(entity)).thenReturn(model);

        AppSystem result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(appSystemJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        AppSystemCriteria criteria = new AppSystemCriteria();
        Pageable pageable = Pageable.unpaged();
        AppSystemEntity entity = entityWithRelations(1L);
        AppSystem model = new AppSystem();
        Page<AppSystemEntity> entityPage = new PageImpl<>(List.of(entity));
        when(appSystemJPARepository.findAll(ArgumentMatchers.<Specification<AppSystemEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(appSystemMapper.toModel(entity)).thenReturn(model);

        Page<AppSystem> result = adapter.findAll(10L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByInformationSystemDbAndSystem_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(appSystemJPARepository.existsByInformationSystemDbIdAndSystemId(10L, 20L)).thenReturn(true);

        assertTrue(adapter.existsByInformationSystemDbAndSystem(10L, 20L));
    }

    @Test
    void existsByInformationSystemDbAndSystemAndIdNot_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(appSystemJPARepository.existsByInformationSystemDbIdAndSystemIdAndIdNot(10L, 20L, 1L)).thenReturn(true);

        assertTrue(adapter.existsByInformationSystemDbAndSystemAndIdNot(10L, 20L, 1L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        AppSystem model = new AppSystem();
        AppSystemEntity toSave = new AppSystemEntity();
        AppSystemEntity saved = entityWithRelations(5L);
        AppSystem response = new AppSystem();
        when(appSystemMapper.toEntity(model)).thenReturn(toSave);
        when(appSystemJPARepository.save(toSave)).thenReturn(saved);
        when(appSystemMapper.toModel(saved)).thenReturn(response);

        AppSystem result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<AppSystemAudEntity> captor = ArgumentCaptor.forClass(AppSystemAudEntity.class);
        verify(appSystemAudJPARepository).save(captor.capture());
        AppSystemAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getAppSystemId());
        assertEquals(10L, aud.getInformationSystemDbId());
        assertEquals(20L, aud.getSystemId());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        AppSystem model = new AppSystem();
        AppSystemEntity toSave = new AppSystemEntity();
        AppSystemEntity saved = entityWithRelations(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(appSystemMapper.toEntity(model)).thenReturn(toSave);
        when(appSystemJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<AppSystemAudEntity> captor = ArgumentCaptor.forClass(AppSystemAudEntity.class);
        verify(appSystemAudJPARepository).save(captor.capture());
        AppSystemAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        AppSystem model = new AppSystem();
        AppSystemEntity toSave = new AppSystemEntity();
        AppSystemEntity saved = entityWithRelations(7L);
        AppSystem response = new AppSystem();
        when(appSystemMapper.toEntity(model)).thenReturn(toSave);
        when(appSystemJPARepository.save(toSave)).thenReturn(saved);
        when(appSystemMapper.toModel(saved)).thenReturn(response);

        AppSystem result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<AppSystemAudEntity> captor = ArgumentCaptor.forClass(AppSystemAudEntity.class);
        verify(appSystemAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        AppSystem model = new AppSystem();
        model.setId(8L);
        AppSystemEntity toSave = new AppSystemEntity();
        AppSystemEntity saved = entityWithRelations(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(appSystemMapper.toEntity(model)).thenReturn(toSave);
        when(appSystemJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<AppSystemAudEntity> captor = ArgumentCaptor.forClass(AppSystemAudEntity.class);
        verify(appSystemAudJPARepository).save(captor.capture());
        AppSystemAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
