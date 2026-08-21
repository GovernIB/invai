package es.caib.invai.back.service.mapper.application.core;

import es.caib.invai.back.interna.application.core.DTO.ApplicationInputDTO;
import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.admUnit.AdmUnitEntity;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.maintenance.general.category.CategoryEntity;
import es.caib.invai.back.persistence.model.maintenance.general.commission.CommissionEntity;
import es.caib.invai.back.persistence.model.maintenance.general.field.FieldEntity;
import es.caib.invai.back.persistence.model.maintenance.general.systemType.SystemTypeEntity;
import es.caib.invai.back.persistence.model.catalog.status.LkupStatusEntity;
import es.caib.invai.back.service.model.maintenance.admUnit.AdmUnit;
import es.caib.invai.back.service.model.application.system_database.core.AppInformationSystemDb;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.maintenance.general.category.Category;
import es.caib.invai.back.service.model.maintenance.general.commission.Commission;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import es.caib.invai.back.service.model.maintenance.general.field.Field;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import es.caib.invai.back.service.model.maintenance.general.systemType.SystemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import es.caib.invai.back.service.mapper.maintenance.admUnit.AdmUnitMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.category.CategoryMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.commission.CommissionMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.field.FieldMapperImpl;
import es.caib.invai.back.service.mapper.catalog.status.StatusMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.systemType.SystemTypeMapperImpl;

/**
 * Unit tests for the generated {@link ApplicationMapperImpl}, exercising every conversion direction
 * declared on {@link ApplicationMapper} with its composed leaf mappers wired in via reflection
 * (no Spring context).
 */
class ApplicationMapperTest {

    private ApplicationMapper mapper;

    @BeforeEach
    void setUp() {
        ApplicationMapperImpl impl = new ApplicationMapperImpl();
        ReflectionTestUtils.setField(impl, "categoryMapper", new CategoryMapperImpl());
        ReflectionTestUtils.setField(impl, "systemTypeMapper", new SystemTypeMapperImpl());
        ReflectionTestUtils.setField(impl, "fieldMapper", new FieldMapperImpl());
        ReflectionTestUtils.setField(impl, "admUnitMapper", new AdmUnitMapperImpl());
        ReflectionTestUtils.setField(impl, "commissionMapper", new CommissionMapperImpl());
        ReflectionTestUtils.setField(impl, "statusMapper", new StatusMapperImpl());
        mapper = impl;
    }

    private ApplicationEntity buildFullEntity() {
        CategoryEntity category = new CategoryEntity();
        category.setId(1L);
        category.setName("Cat");

        SystemTypeEntity systemType = new SystemTypeEntity();
        systemType.setId(2L);
        systemType.setName("SysType");

        FieldEntity field = new FieldEntity();
        field.setId(3L);
        field.setName("Field");

        AdmUnitEntity admUnit = new AdmUnitEntity();
        admUnit.setId(4L);
        admUnit.setCode("AU1");
        admUnit.setName("AdmUnit");

        CommissionEntity commission = new CommissionEntity();
        commission.setId(5L);
        commission.setName("Commission");

        LkupStatusEntity status = new LkupStatusEntity();
        status.setId(StatusEnum.ACTIVE.getId());
        status.setName("Active");

        ApplicationEntity entity = new ApplicationEntity();
        entity.setId(100L);
        entity.setCode("APP01");
        entity.setPrefix("AP1");
        entity.setName("Application One");
        entity.setDescription("A test application");
        entity.setCategory(category);
        entity.setSystemType(systemType);
        entity.setField(field);
        entity.setAdmUnit(admUnit);
        entity.setCsCommission(commission);
        entity.setStatus(status);
        entity.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        entity.setCreatedBy("tester");
        return entity;
    }

    @Test
    void toModel_mapsEveryEntityField() {
        ApplicationEntity entity = buildFullEntity();

        Application model = mapper.toModel(entity);

        assertEquals(100L, model.getId());
        assertEquals("APP01", model.getCode());
        assertEquals("AP1", model.getPrefix());
        assertEquals("Application One", model.getName());
        assertEquals("A test application", model.getDescription());
        assertEquals(StatusEnum.ACTIVE, model.getStatus());
        assertNotNull(model.getCategory());
        assertEquals(1L, model.getCategory().getId());
        assertNotNull(model.getSystemType());
        assertEquals(2L, model.getSystemType().getId());
        assertNotNull(model.getField());
        assertEquals(3L, model.getField().getId());
        assertNotNull(model.getAdmUnit());
        assertEquals(4L, model.getAdmUnit().getId());
        assertNotNull(model.getCsCommission());
        assertEquals(5L, model.getCsCommission().getId());
        assertEquals("tester", model.getCreatedBy());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelFieldAndResolvesStatus() {
        Category category = new Category();
        category.setId(10L);
        SystemType systemType = new SystemType();
        systemType.setId(20L);
        Field field = new Field();
        field.setId(30L);
        AdmUnit admUnit = new AdmUnit();
        admUnit.setId(40L);
        Commission commission = new Commission();
        commission.setId(50L);

        Application model = new Application();
        model.setId(200L);
        model.setCode("APP02");
        model.setPrefix("AP2");
        model.setName("Application Two");
        model.setCategory(category);
        model.setSystemType(systemType);
        model.setField(field);
        model.setAdmUnit(admUnit);
        model.setCsCommission(commission);
        model.setStatus(StatusEnum.INACTIVE);

        ApplicationEntity entity = mapper.toEntity(model);

        assertEquals(200L, entity.getId());
        assertEquals("APP02", entity.getCode());
        assertEquals("AP2", entity.getPrefix());
        assertNotNull(entity.getCategory());
        assertEquals(10L, entity.getCategory().getId());
        assertNotNull(entity.getStatus());
        assertEquals(StatusEnum.INACTIVE.getId(), entity.getStatus().getId());
    }

    @Test
    void toResponse_allSourcesPresent_flattensEveryField() {
        Application application = new Application();
        application.setId(1L);
        application.setName("Application One");
        application.setPrefix("AP1");
        application.setCode("APP01");
        application.setDescription("desc");
        application.setStatus(StatusEnum.ACTIVE);
        application.setCreatedAt(LocalDateTime.of(2026, 2, 3, 10, 0));
        application.setCreatedBy("creator");
        application.setUpdatedAt(LocalDateTime.of(2026, 2, 4, 10, 0));
        application.setUpdatedBy("updater");

        AppInformationSystemDb infoDb = new AppInformationSystemDb();
        infoDb.setId(500L);

        AppDevelopment development = new AppDevelopment();
        development.setId(700L);

        AppResponsibleAuthorized appResponsibleAuthorized = new AppResponsibleAuthorized();
        appResponsibleAuthorized.setId(900L);

        ApplicationOutputDTO response = mapper.toResponse(application, infoDb, development, appResponsibleAuthorized);

        assertEquals(1L, response.getId());
        assertEquals("Application One", response.getName());
        assertEquals("AP1", response.getPrefix());
        assertEquals("APP01", response.getCode());
        assertEquals("desc", response.getDescription());
        assertEquals(StatusEnum.ACTIVE, response.getStatus());
        assertEquals("creator", response.getCreatedBy());
        assertEquals("updater", response.getUpdatedBy());
        assertEquals(500L, response.getAppInformationSystemDbId());
        assertEquals(700L, response.getAppDevelopmentId());
        assertEquals(900L, response.getAppResponsibleAuthorizedId());
    }

    @Test
    void toResponse_nullInfoDbAndDevelopment_leavesIdsNull() {
        Application application = new Application();
        application.setId(1L);
        application.setStatus(StatusEnum.ACTIVE);

        ApplicationOutputDTO response = mapper.toResponse(application, null, null, null);

        assertEquals(1L, response.getId());
        assertNull(response.getAppInformationSystemDbId());
        assertNull(response.getAppDevelopmentId());
        assertNull(response.getAppResponsibleAuthorizedId());
    }

    @Test
    void toResponse_allSourcesNull_returnsNull() {
        assertNull(mapper.toResponse(null, null, null, null));
    }

    private ApplicationInputDTO buildInputDTO() {
        ApplicationInputDTO inputDTO = new ApplicationInputDTO();
        inputDTO.setName("Application Three");
        inputDTO.setPrefix("AP3");
        inputDTO.setCode("APP03");
        inputDTO.setCategoryId(1L);
        inputDTO.setSystemTypeId(2L);
        inputDTO.setFieldId(3L);
        inputDTO.setAdmUnitId(4L);
        inputDTO.setCommissionId(5L);
        inputDTO.setDescription("desc");
        inputDTO.setStatusId(StatusEnum.ACTIVE.getId());
        return inputDTO;
    }

    @Test
    void toModelFromInput_mapsFlatReferencesAndIgnoresAuditFields() {
        ApplicationInputDTO inputDTO = buildInputDTO();

        Application model = mapper.toModelFromInput(inputDTO);

        assertEquals("Application Three", model.getName());
        assertEquals("AP3", model.getPrefix());
        assertEquals("APP03", model.getCode());
        assertEquals("desc", model.getDescription());
        assertEquals(StatusEnum.ACTIVE, model.getStatus());
        assertNotNull(model.getCategory());
        assertEquals(1L, model.getCategory().getId());
        assertNotNull(model.getSystemType());
        assertEquals(2L, model.getSystemType().getId());
        assertNotNull(model.getField());
        assertEquals(3L, model.getField().getId());
        assertNotNull(model.getAdmUnit());
        assertEquals(4L, model.getAdmUnit().getId());
        assertNotNull(model.getCsCommission());
        assertEquals(5L, model.getCsCommission().getId());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void toModelFromInput_null_returnsNull() {
        assertNull(mapper.toModelFromInput(null));
    }

    @Test
    void toModelFromInput_unknownStatusId_throwsIllegalArgumentException() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        inputDTO.setStatusId(999L);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> mapper.toModelFromInput(inputDTO));
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        Application existing = new Application();
        existing.setId(400L);
        existing.setName("Old Name");
        existing.setCode("OLD");
        existing.setPrefix("OLD");
        existing.setCategory(new Category());
        existing.setSystemType(new SystemType());
        existing.setField(new Field());
        existing.setAdmUnit(new AdmUnit());
        existing.setCsCommission(new Commission());

        ApplicationInputDTO inputDTO = buildInputDTO();

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(400L, existing.getId());
        assertEquals("Application Three", existing.getName());
        assertEquals("APP03", existing.getCode());
        assertEquals(1L, existing.getCategory().getId());
        assertEquals(2L, existing.getSystemType().getId());
        assertEquals(3L, existing.getField().getId());
        assertEquals(4L, existing.getAdmUnit().getId());
        assertEquals(5L, existing.getCsCommission().getId());
        assertEquals(StatusEnum.ACTIVE, existing.getStatus());
    }

    @Test
    void updateModelFromInput_nullInputDTO_doesNothing() {
        Application existing = new Application();
        existing.setId(400L);
        existing.setName("Unchanged");

        mapper.updateModelFromInput(null, existing);

        assertEquals(400L, existing.getId());
        assertEquals("Unchanged", existing.getName());
    }
}
