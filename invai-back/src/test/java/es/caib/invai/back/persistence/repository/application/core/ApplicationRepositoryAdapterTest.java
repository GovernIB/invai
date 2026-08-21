package es.caib.invai.back.persistence.repository.application.core;

import es.caib.invai.back.persistence.model.maintenance.admUnit.AdmUnitEntity;
import es.caib.invai.back.persistence.model.application.core.ApplicationAudEntity;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.maintenance.general.category.CategoryEntity;
import es.caib.invai.back.persistence.model.maintenance.general.commission.CommissionEntity;
import es.caib.invai.back.persistence.model.maintenance.general.field.FieldEntity;
import es.caib.invai.back.persistence.model.maintenance.general.systemType.SystemTypeEntity;
import es.caib.invai.back.persistence.model.catalog.status.LkupStatusEntity;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapper;
import es.caib.invai.back.service.model.application.core.Application;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentMatchers;

/**
 * Unit tests for {@link ApplicationRepositoryAdapter}, verifying entity/model delegation to the
 * {@link ApplicationJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class ApplicationRepositoryAdapterTest {

    @Mock
    private ApplicationJPARepository applicationJPARepository;

    @Mock
    private ApplicationAudJPARepository applicationAudJPARepository;

    @Mock
    private ApplicationMapper applicationMapper;

    private ApplicationRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private ApplicationRepositoryAdapter buildAdapter() {
        ApplicationRepositoryAdapter a = new ApplicationRepositoryAdapter();
        ReflectionTestUtils.setField(a, "applicationJPARepository", applicationJPARepository);
        ReflectionTestUtils.setField(a, "applicationAudJPARepository", applicationAudJPARepository);
        ReflectionTestUtils.setField(a, "applicationMapper", applicationMapper);
        return a;
    }

    private ApplicationEntity buildFullyLinkedEntity(Long id) {
        ApplicationEntity entity = new ApplicationEntity();
        entity.setId(id);
        entity.setCode("APP001");
        entity.setPrefix("AP1");
        entity.setName("Inventory App");
        entity.setDescription("Corporate inventory management system");
        entity.setExpirationDate(LocalDateTime.of(2030, 1, 1, 0, 0));

        CategoryEntity category = new CategoryEntity();
        category.setId(1L);
        entity.setCategory(category);

        SystemTypeEntity systemType = new SystemTypeEntity();
        systemType.setId(2L);
        entity.setSystemType(systemType);

        FieldEntity field = new FieldEntity();
        field.setId(3L);
        entity.setField(field);

        AdmUnitEntity admUnit = new AdmUnitEntity();
        admUnit.setId(4L);
        entity.setAdmUnit(admUnit);

        CommissionEntity commission = new CommissionEntity();
        commission.setId(5L);
        entity.setCsCommission(commission);

        LkupStatusEntity status = new LkupStatusEntity();
        status.setId(6L);
        entity.setStatus(status);

        return entity;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        ApplicationEntity entity = new ApplicationEntity();
        entity.setId(1L);
        Application model = new Application();
        when(applicationJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(applicationMapper.toModel(entity)).thenReturn(model);

        Application result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(applicationJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        ApplicationCriteria criteria = new ApplicationCriteria();
        Pageable pageable = Pageable.unpaged();
        ApplicationEntity entity = new ApplicationEntity();
        Application model = new Application();
        Page<ApplicationEntity> entityPage = new PageImpl<>(List.of(entity));
        when(applicationJPARepository.findAll(ArgumentMatchers.<Specification<ApplicationEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(applicationMapper.toModel(entity)).thenReturn(model);

        Page<Application> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByCode_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(applicationJPARepository.existsByCode("APP001")).thenReturn(true);

        assertTrue(adapter.existsByCode("APP001"));
    }

    @Test
    void existsByPrefix_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(applicationJPARepository.existsByPrefix("AP1")).thenReturn(true);

        assertTrue(adapter.existsByPrefix("AP1"));
    }

    @Test
    void existsByCodeAndIdNot_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(applicationJPARepository.existsByCodeAndIdNot("APP001", 1L)).thenReturn(true);

        assertTrue(adapter.existsByCodeAndIdNot("APP001", 1L));
    }

    @Test
    void existsByPrefixAndIdNot_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(applicationJPARepository.existsByPrefixAndIdNot("AP1", 1L)).thenReturn(true);

        assertTrue(adapter.existsByPrefixAndIdNot("AP1", 1L));
    }

    @Test
    void existsByCategoryId_delegatesToJPARepositoryUsingDeletedAtIsNullVariant() {
        adapter = buildAdapter();
        when(applicationJPARepository.existsByCategoryIdAndDeletedAtIsNull(1L)).thenReturn(true);

        assertTrue(adapter.existsByCategoryId(1L));
    }

    @Test
    void existsBySystemTypeId_delegatesToJPARepositoryUsingDeletedAtIsNullVariant() {
        adapter = buildAdapter();
        when(applicationJPARepository.existsBySystemTypeIdAndDeletedAtIsNull(2L)).thenReturn(true);

        assertTrue(adapter.existsBySystemTypeId(2L));
    }

    @Test
    void existsByFieldId_delegatesToJPARepositoryUsingDeletedAtIsNullVariant() {
        adapter = buildAdapter();
        when(applicationJPARepository.existsByFieldIdAndDeletedAtIsNull(3L)).thenReturn(true);

        assertTrue(adapter.existsByFieldId(3L));
    }

    @Test
    void existsByAdmUnitId_delegatesToJPARepositoryUsingDeletedAtIsNullVariant() {
        adapter = buildAdapter();
        when(applicationJPARepository.existsByAdmUnitIdAndDeletedAtIsNull(4L)).thenReturn(true);

        assertTrue(adapter.existsByAdmUnitId(4L));
    }

    @Test
    void existsByCommissionId_delegatesToJPARepositoryUsingCsCommissionVariant() {
        adapter = buildAdapter();
        when(applicationJPARepository.existsByCsCommissionIdAndDeletedAtIsNull(5L)).thenReturn(true);

        assertTrue(adapter.existsByCommissionId(5L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecordWithAllForeignKeys() {
        adapter = buildAdapter();
        Application model = new Application();
        ApplicationEntity toSave = new ApplicationEntity();
        ApplicationEntity saved = buildFullyLinkedEntity(10L);
        Application response = new Application();
        when(applicationMapper.toEntity(model)).thenReturn(toSave);
        when(applicationJPARepository.save(toSave)).thenReturn(saved);
        when(applicationMapper.toModel(saved)).thenReturn(response);

        Application result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<ApplicationAudEntity> captor = ArgumentCaptor.forClass(ApplicationAudEntity.class);
        verify(applicationAudJPARepository).save(captor.capture());
        ApplicationAudEntity aud = captor.getValue();
        assertEquals(10L, aud.getAppApplicationId());
        assertEquals("APP001", aud.getCode());
        assertEquals("AP1", aud.getPrefix());
        assertEquals("Inventory App", aud.getName());
        assertEquals("Corporate inventory management system", aud.getDescription());
        assertEquals(LocalDateTime.of(2030, 1, 1, 0, 0), aud.getExpirationDate());
        assertEquals(1L, aud.getCategoryId());
        assertEquals(2L, aud.getSystemTypeId());
        assertEquals(3L, aud.getFieldId());
        assertEquals(4L, aud.getAdmUnitId());
        assertEquals(5L, aud.getCommissionId());
        assertEquals(6L, aud.getStatusId());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getUpdatedAt());
        assertEquals("SYSTEM_USER", aud.getUpdatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithoutForeignKeyRelations_leavesAuditForeignKeysNull() {
        adapter = buildAdapter();
        Application model = new Application();
        ApplicationEntity toSave = new ApplicationEntity();
        ApplicationEntity saved = new ApplicationEntity();
        saved.setId(11L);
        saved.setCode("APP002");
        saved.setPrefix("AP2");
        when(applicationMapper.toEntity(model)).thenReturn(toSave);
        when(applicationJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<ApplicationAudEntity> captor = ArgumentCaptor.forClass(ApplicationAudEntity.class);
        verify(applicationAudJPARepository).save(captor.capture());
        ApplicationAudEntity aud = captor.getValue();
        assertNull(aud.getCategoryId());
        assertNull(aud.getSystemTypeId());
        assertNull(aud.getFieldId());
        assertNull(aud.getAdmUnitId());
        assertNull(aud.getCommissionId());
        assertNull(aud.getStatusId());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        Application model = new Application();
        ApplicationEntity toSave = new ApplicationEntity();
        ApplicationEntity saved = new ApplicationEntity();
        saved.setId(12L);
        saved.setCode("APP003");
        saved.setPrefix("AP3");
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime existingUpdatedAt = LocalDateTime.of(2025, 2, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        saved.setUpdatedAt(existingUpdatedAt);
        saved.setUpdatedBy("asmith");
        when(applicationMapper.toEntity(model)).thenReturn(toSave);
        when(applicationJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<ApplicationAudEntity> captor = ArgumentCaptor.forClass(ApplicationAudEntity.class);
        verify(applicationAudJPARepository).save(captor.capture());
        ApplicationAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
        assertEquals(existingUpdatedAt, aud.getUpdatedAt());
        assertEquals("asmith", aud.getUpdatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        Application model = new Application();
        ApplicationEntity toSave = new ApplicationEntity();
        ApplicationEntity saved = new ApplicationEntity();
        saved.setId(13L);
        saved.setCode("APP004");
        saved.setPrefix("AP4");
        Application response = new Application();
        when(applicationMapper.toEntity(model)).thenReturn(toSave);
        when(applicationJPARepository.save(toSave)).thenReturn(saved);
        when(applicationMapper.toModel(saved)).thenReturn(response);

        Application result = adapter.update(model, 13L);

        assertSame(response, result);
        assertEquals(13L, toSave.getId());
        ArgumentCaptor<ApplicationAudEntity> captor = ArgumentCaptor.forClass(ApplicationAudEntity.class);
        verify(applicationAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        Application model = new Application();
        model.setId(14L);
        ApplicationEntity toSave = new ApplicationEntity();
        ApplicationEntity saved = new ApplicationEntity();
        saved.setId(14L);
        saved.setCode("APP005");
        saved.setPrefix("AP5");
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(applicationMapper.toEntity(model)).thenReturn(toSave);
        when(applicationJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(14L, toSave.getId());
        ArgumentCaptor<ApplicationAudEntity> captor = ArgumentCaptor.forClass(ApplicationAudEntity.class);
        verify(applicationAudJPARepository).save(captor.capture());
        ApplicationAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
