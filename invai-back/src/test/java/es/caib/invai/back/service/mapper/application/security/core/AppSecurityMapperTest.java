package es.caib.invai.back.service.mapper.application.security.core;

import es.caib.invai.back.interna.application.security.core.DTO.AppSecurityInputDTO;
import es.caib.invai.back.interna.application.security.core.DTO.AppSecurityOutputDTO;
import es.caib.invai.back.persistence.model.application.security.core.AppSecurityEntity;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.maintenance.general.category.CategoryEntity;
import es.caib.invai.back.persistence.model.maintenance.general.commission.CommissionEntity;
import es.caib.invai.back.persistence.model.maintenance.general.field.FieldEntity;
import es.caib.invai.back.persistence.model.maintenance.general.systemType.SystemTypeEntity;
import es.caib.invai.back.persistence.model.catalog.status.LkupStatusEntity;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.maintenance.general.category.Category;
import es.caib.invai.back.service.model.maintenance.general.commission.Commission;
import es.caib.invai.back.service.model.maintenance.general.commission.CommissionType;
import es.caib.invai.back.service.model.maintenance.general.field.Field;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import es.caib.invai.back.service.model.maintenance.general.systemType.SystemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import es.caib.invai.back.service.mapper.catalog.status.StatusMapperImpl;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.category.CategoryMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.systemType.SystemTypeMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.field.FieldMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.commission.CommissionMapperImpl;

/**
 * Unit tests for the generated {@link AppSecurityMapperImpl}, exercising every
 * conversion direction declared on {@link AppSecurityMapper}.
 *
 * <p>{@link AppSecurityMapper} declares {@code uses = {StatusMapper.class, ApplicationMapper.class}}.
 * A fully wired {@link ApplicationMapperImpl} (with its own sub-mappers, including
 * {@link StatusMapperImpl}) is wired into the generated {@code @Autowired} field via
 * {@link ReflectionTestUtils} so the deep {@code AppSecurity -> Application -> ...}
 * object graph (now delegated to {@code ApplicationMapper} for the nested {@code application}
 * property) is exercised with real mapping logic end to end.</p>
 */
class AppSecurityMapperTest {

    private AppSecurityMapper mapper;

    @BeforeEach
    void setUp() {
        ApplicationMapperImpl applicationMapperImpl = new ApplicationMapperImpl();
        ReflectionTestUtils.setField(applicationMapperImpl, "categoryMapper", new CategoryMapperImpl());
        ReflectionTestUtils.setField(applicationMapperImpl, "systemTypeMapper", new SystemTypeMapperImpl());
        ReflectionTestUtils.setField(applicationMapperImpl, "fieldMapper", new FieldMapperImpl());
        ReflectionTestUtils.setField(applicationMapperImpl, "commissionMapper", new CommissionMapperImpl());
        ReflectionTestUtils.setField(applicationMapperImpl, "statusMapper", new StatusMapperImpl());

        AppSecurityMapperImpl impl = new AppSecurityMapperImpl();
        ReflectionTestUtils.setField(impl, "applicationMapper", applicationMapperImpl);
        mapper = impl;
    }

    private static ApplicationEntity buildDeepApplicationEntity() {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setId(100L);
        categoryEntity.setName("Category name");
        categoryEntity.setNameEs("Nombre categoria");
        categoryEntity.setCreatedAt(LocalDateTime.of(2020, 1, 1, 0, 0));
        categoryEntity.setDeletedBy("category-deleter");

        SystemTypeEntity systemTypeEntity = new SystemTypeEntity();
        systemTypeEntity.setId(101L);
        systemTypeEntity.setName("System type name");
        systemTypeEntity.setNameEs("Nombre tipo sistema");

        FieldEntity fieldEntity = new FieldEntity();
        fieldEntity.setId(102L);
        fieldEntity.setName("Field name");
        fieldEntity.setNameEs("Nombre ambito");

        CommissionEntity commissionEntity = new CommissionEntity();
        commissionEntity.setId(104L);
        commissionEntity.setName("Commission name");
        commissionEntity.setNameEs("Nombre comision");
        commissionEntity.setExpedientNumber("EXP-2024-01");
        commissionEntity.setApprovalDate(LocalDate.of(2024, 4, 1));
        commissionEntity.setCommissionType(CommissionType.TECNICA);

        LkupStatusEntity statusEntity = new LkupStatusEntity();
        statusEntity.setId(2L);
        statusEntity.setName("Inactive");
        statusEntity.setNameEs("Inactivo");

        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(200L);
        applicationEntity.setCode("APP001");
        applicationEntity.setPrefix("AP1");
        applicationEntity.setName("Application name");
        applicationEntity.setDescription("Application description");
        applicationEntity.setExpirationDate(LocalDateTime.of(2030, 6, 15, 10, 30));
        applicationEntity.setCategory(categoryEntity);
        applicationEntity.setSystemType(systemTypeEntity);
        applicationEntity.setField(fieldEntity);
        applicationEntity.setAdmUnitCode("ADM-1");
        applicationEntity.setCsCommission(commissionEntity);
        applicationEntity.setStatus(statusEntity);
        applicationEntity.setCreatedAt(LocalDateTime.of(2021, 1, 1, 8, 0));
        applicationEntity.setCreatedBy("application-creator");
        applicationEntity.setUpdatedAt(LocalDateTime.of(2021, 2, 1, 8, 0));
        applicationEntity.setUpdatedBy("application-updater");
        applicationEntity.setDeletedAt(LocalDateTime.of(2021, 3, 1, 8, 0));
        applicationEntity.setDeletedBy("application-deleter");
        return applicationEntity;
    }

    private static Application buildDeepApplicationModel() {
        Category category = new Category();
        category.setId(100L);
        category.setName("Category name");
        category.setNameEs("Nombre categoria");

        SystemType systemType = new SystemType();
        systemType.setId(101L);
        systemType.setName("System type name");
        systemType.setNameEs("Nombre tipo sistema");

        Field field = new Field(102L, "Field name", "Nombre ambito", null, null, null, null, null, null);

        Commission commission = new Commission(104L, "Commission name", "Nombre comision", "EXP-2024-01",
                CommissionType.TECNICA, LocalDate.of(2024, 4, 1), null, null, null, null, null, null);

        Application application = new Application();
        application.setId(200L);
        application.setCode("APP001");
        application.setPrefix("AP1");
        application.setName("Application name");
        application.setDescription("Application description");
        application.setExpirationDate(LocalDateTime.of(2030, 6, 15, 10, 30));
        application.setCategory(category);
        application.setSystemType(systemType);
        application.setField(field);
        application.setAdmUnitCode("ADM-1");
        application.setCsCommission(commission);
        application.setStatus(StatusEnum.INACTIVE);
        application.setCreatedAt(LocalDateTime.of(2021, 1, 1, 8, 0));
        application.setCreatedBy("application-creator");
        application.setUpdatedAt(LocalDateTime.of(2021, 2, 1, 8, 0));
        application.setUpdatedBy("application-updater");
        application.setDeletedAt(LocalDateTime.of(2021, 3, 1, 8, 0));
        application.setDeletedBy("application-deleter");
        return application;
    }

    @Test
    void toModel_deepEntityGraph_mapsEveryNestedField() {
        AppSecurityEntity entity = new AppSecurityEntity();
        entity.setId(1L);
        entity.setApplication(buildDeepApplicationEntity());
        entity.setObservation("Core grouping");
        entity.setCreatedAt(LocalDateTime.of(2023, 1, 1, 0, 0));
        entity.setCreatedBy("creator");
        entity.setUpdatedAt(LocalDateTime.of(2023, 2, 1, 0, 0));
        entity.setUpdatedBy("updater");
        entity.setDeletedAt(LocalDateTime.of(2023, 3, 1, 0, 0));
        entity.setDeletedBy("deleter");

        AppSecurity model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Core grouping", model.getObservation());
        assertEquals(entity.getCreatedAt(), model.getCreatedAt());
        assertEquals("creator", model.getCreatedBy());
        assertEquals(entity.getUpdatedAt(), model.getUpdatedAt());
        assertEquals("updater", model.getUpdatedBy());
        assertEquals(entity.getDeletedAt(), model.getDeletedAt());
        assertEquals("deleter", model.getDeletedBy());

        Application application = model.getApplication();
        assertEquals(200L, application.getId());
        assertEquals("APP001", application.getCode());
        assertEquals("AP1", application.getPrefix());
        assertEquals("Application name", application.getName());
        assertEquals("Application description", application.getDescription());
        assertEquals(entity.getApplication().getExpirationDate(), application.getExpirationDate());
        assertEquals(StatusEnum.INACTIVE, application.getStatus());
        assertEquals("application-creator", application.getCreatedBy());
        assertEquals("application-deleter", application.getDeletedBy());

        assertEquals(100L, application.getCategory().getId());
        assertEquals("Category name", application.getCategory().getName());
        assertEquals("Nombre categoria", application.getCategory().getNameEs());
        assertEquals("category-deleter", application.getCategory().getDeletedBy());

        assertEquals(101L, application.getSystemType().getId());
        assertEquals("System type name", application.getSystemType().getName());

        assertEquals(102L, application.getField().getId());
        assertEquals("Field name", application.getField().getName());

        assertEquals("ADM-1", application.getAdmUnitCode());

        assertEquals(104L, application.getCsCommission().getId());
        assertEquals("EXP-2024-01", application.getCsCommission().getExpedientNumber());
        assertEquals(LocalDate.of(2024, 4, 1), application.getCsCommission().getApprovalDate());
        assertEquals(CommissionType.TECNICA, application.getCsCommission().getCommissionType());
    }

    @Test
    void toModel_withNullApplication_leavesNestedModelFieldNull() {
        AppSecurityEntity entity = new AppSecurityEntity();
        entity.setId(2L);
        entity.setObservation("No application yet");

        AppSecurity model = mapper.toModel(entity);

        assertEquals(2L, model.getId());
        assertEquals("No application yet", model.getObservation());
        assertNull(model.getApplication());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_deepModelGraph_mapsEveryNestedField() {
        AppSecurity model = new AppSecurity();
        model.setId(3L);
        model.setApplication(buildDeepApplicationModel());
        model.setObservation("Notes");
        model.setCreatedAt(LocalDateTime.of(2022, 1, 1, 0, 0));
        model.setCreatedBy("model-creator");
        model.setDeletedAt(LocalDateTime.of(2022, 3, 1, 0, 0));
        model.setDeletedBy("model-deleter");

        AppSecurityEntity entity = mapper.toEntity(model);

        assertEquals(3L, entity.getId());
        assertEquals("Notes", entity.getObservation());
        assertEquals(model.getCreatedAt(), entity.getCreatedAt());
        assertEquals("model-creator", entity.getCreatedBy());
        assertEquals(model.getDeletedAt(), entity.getDeletedAt());
        assertEquals("model-deleter", entity.getDeletedBy());

        ApplicationEntity applicationEntity = entity.getApplication();
        assertEquals(200L, applicationEntity.getId());
        assertEquals("APP001", applicationEntity.getCode());
        assertEquals("Application name", applicationEntity.getName());
        assertEquals(2L, applicationEntity.getStatus().getId());
        assertNull(applicationEntity.getStatus().getName());

        assertEquals(100L, applicationEntity.getCategory().getId());
        assertEquals("Category name", applicationEntity.getCategory().getName());
        assertEquals(101L, applicationEntity.getSystemType().getId());
        assertEquals(102L, applicationEntity.getField().getId());
        assertEquals("ADM-1", applicationEntity.getAdmUnitCode());
        assertEquals(104L, applicationEntity.getCsCommission().getId());
        assertEquals(CommissionType.TECNICA, applicationEntity.getCsCommission().getCommissionType());
    }

    @Test
    void toEntity_withNullApplication_leavesNestedEntityFieldNull() {
        AppSecurity model = new AppSecurity();
        model.setId(4L);
        model.setObservation("No application yet");

        AppSecurityEntity entity = mapper.toEntity(model);

        assertEquals(4L, entity.getId());
        assertEquals("No application yet", entity.getObservation());
        assertNull(entity.getApplication());
    }

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toResponse_deepModel_flattensEveryNestedFieldIntoOutputDTO() {
        AppSecurity model = new AppSecurity();
        model.setId(5L);
        model.setApplication(buildDeepApplicationModel());
        model.setObservation("Reviewed");
        model.setDeletedAt(LocalDateTime.of(2024, 6, 1, 0, 0));

        AppSecurityOutputDTO response = mapper.toResponse(model);

        assertEquals(5L, response.getId());
        assertEquals("Reviewed", response.getObservation());
        assertEquals(model.getDeletedAt(), response.getDeletedAt());

        var applicationDTO = response.getApplication();
        assertEquals(200L, applicationDTO.getId());
        assertEquals("APP001", applicationDTO.getCode());
        assertEquals("Application name", applicationDTO.getName());
        assertEquals(StatusEnum.INACTIVE, applicationDTO.getStatus());
        // LocalDateTime -> XMLGregorianCalendar -> LocalDate round trip keeps only the date part
        assertEquals(LocalDate.of(2030, 6, 15), applicationDTO.getExpirationDate());
        assertEquals(LocalDate.of(2021, 1, 1), applicationDTO.getCreatedAt());
        assertEquals("application-creator", applicationDTO.getCreatedBy());
        assertEquals(LocalDate.of(2021, 2, 1), applicationDTO.getUpdatedAt());
        assertEquals("application-updater", applicationDTO.getUpdatedBy());

        assertEquals(100L, applicationDTO.getCategory().getId());
        assertEquals(101L, applicationDTO.getSystemType().getId());
        assertEquals(102L, applicationDTO.getField().getId());
        assertNull(applicationDTO.getAdmUnit());
        assertEquals(104L, applicationDTO.getCsCommission().getId());
        assertEquals(CommissionType.TECNICA, applicationDTO.getCsCommission().getCommissionType());
    }

    @Test
    void toResponse_withNullApplication_leavesNestedDtoFieldNull() {
        AppSecurity model = new AppSecurity();
        model.setId(6L);
        model.setObservation("No application yet");

        AppSecurityOutputDTO response = mapper.toResponse(model);

        assertEquals(6L, response.getId());
        assertNull(response.getApplication());
    }

    @Test
    void toResponse_null_returnsNull() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void toModelFromInput_resolvesApplicationIdAndIgnoresAuditFields() {
        AppSecurityInputDTO inputDTO = new AppSecurityInputDTO(50L, "Fresh notes");

        AppSecurity model = mapper.toModelFromInput(inputDTO);

        assertEquals(50L, model.getApplication().getId());
        assertEquals("Fresh notes", model.getObservation());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getCreatedBy());
        assertNull(model.getUpdatedAt());
        assertNull(model.getUpdatedBy());
        assertNull(model.getDeletedAt());
        assertNull(model.getDeletedBy());
    }

    @Test
    void toModelFromInput_null_returnsNull() {
        assertNull(mapper.toModelFromInput(null));
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        Application existingApplication = new Application();
        existingApplication.setId(60L);
        AppSecurity existing = new AppSecurity();
        existing.setId(4L);
        existing.setApplication(existingApplication);
        existing.setObservation("Old notes");
        AppSecurityInputDTO inputDTO = new AppSecurityInputDTO(70L, "New notes");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals(70L, existing.getApplication().getId());
        assertEquals("New notes", existing.getObservation());
    }

    @Test
    void updateModelFromInput_whenExistingApplicationIsNull_createsIt() {
        AppSecurity existing = new AppSecurity();
        existing.setId(8L);
        AppSecurityInputDTO inputDTO = new AppSecurityInputDTO(80L, "Created notes");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(8L, existing.getId());
        assertEquals(80L, existing.getApplication().getId());
        assertEquals("Created notes", existing.getObservation());
    }

    @Test
    void updateModelFromInput_null_doesNothing() {
        AppSecurity existing = new AppSecurity();
        existing.setId(7L);
        existing.setObservation("untouched");

        mapper.updateModelFromInput(null, existing);

        assertEquals(7L, existing.getId());
        assertEquals("untouched", existing.getObservation());
    }
}
