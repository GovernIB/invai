package es.caib.invai.back.service.mapper.application.development.core;

import es.caib.invai.back.interna.application.development.core.DTO.DevelopmentInputDTO;
import es.caib.invai.back.interna.application.development.core.DTO.DevelopmentOutputDTO;
import es.caib.invai.back.persistence.model.catalog.modality.LkupModalityEntity;
import es.caib.invai.back.persistence.model.catalog.standardAdaption.LkupStandardAdaptionEntity;
import es.caib.invai.back.persistence.model.catalog.status.LkupStatusEntity;
import es.caib.invai.back.service.model.maintenance.admUnit.AdmUnit;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.maintenance.general.category.Category;
import es.caib.invai.back.service.model.maintenance.general.commission.Commission;
import es.caib.invai.back.service.model.maintenance.general.commission.CommissionType;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import es.caib.invai.back.service.model.maintenance.systems.environment.Environment;
import es.caib.invai.back.service.model.maintenance.general.field.Field;
import es.caib.invai.back.service.model.catalog.modality.Modality;
import es.caib.invai.back.service.model.catalog.standardAdaption.StandardAdaption;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import es.caib.invai.back.service.model.maintenance.general.systemType.SystemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.environment.EnvironmentEntity;
import es.caib.invai.back.persistence.model.maintenance.admUnit.AdmUnitEntity;
import es.caib.invai.back.persistence.model.application.development.core.AppDevelopmentEntity;
import es.caib.invai.back.persistence.model.maintenance.general.category.CategoryEntity;
import es.caib.invai.back.persistence.model.maintenance.general.commission.CommissionEntity;
import es.caib.invai.back.persistence.model.maintenance.general.field.FieldEntity;
import es.caib.invai.back.persistence.model.maintenance.general.systemType.SystemTypeEntity;
import es.caib.invai.back.service.mapper.catalog.modality.ModalityMapperImpl;
import es.caib.invai.back.service.mapper.catalog.standardAdaption.StandardAdaptionMapperImpl;
import es.caib.invai.back.service.mapper.catalog.status.StatusMapperImpl;

/**
 * Unit tests for the generated {@link AppDevelopmentMapperImpl}, exercising every conversion
 * direction declared on {@link AppDevelopmentMapper}.
 *
 * <p>{@link AppDevelopmentMapper} declares {@code uses = {ModalityMapper.class,
 * StandardAdaptionMapper.class, StatusMapper.class}}. Since there is no Spring context in a pure
 * unit test, real {@link ModalityMapperImpl}, {@link StandardAdaptionMapperImpl} and
 * {@link StatusMapperImpl} instances are wired into the generated {@code @Autowired} fields via
 * {@link ReflectionTestUtils}, so the deep {@code AppDevelopment -> Application -> ...} object graph
 * (which the generated {@code DevelopmentMapperImpl} maps through private helper methods declared
 * directly on itself, not through {@code ApplicationMapper}) is exercised with real mapping logic
 * end to end rather than mocked stand-ins.</p>
 */
class AppDevelopmentMapperTest {

    private AppDevelopmentMapper mapper;

    @BeforeEach
    void setUp() {
        AppDevelopmentMapperImpl impl = new AppDevelopmentMapperImpl();
        ReflectionTestUtils.setField(impl, "modalityMapper", new ModalityMapperImpl());
        ReflectionTestUtils.setField(impl, "standardAdaptionMapper", new StandardAdaptionMapperImpl());
        ReflectionTestUtils.setField(impl, "statusMapper", new StatusMapperImpl());
        mapper = impl;
    }

    private static ApplicationEntity buildDeepApplicationEntity() {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setId(100L);
        categoryEntity.setName("Category name");
        categoryEntity.setNameEs("Nombre categoria");
        categoryEntity.setCreatedAt(LocalDateTime.of(2020, 1, 1, 0, 0));
        categoryEntity.setCreatedBy("category-creator");
        categoryEntity.setUpdatedAt(LocalDateTime.of(2020, 2, 1, 0, 0));
        categoryEntity.setUpdatedBy("category-updater");
        categoryEntity.setDeletedAt(LocalDateTime.of(2020, 3, 1, 0, 0));
        categoryEntity.setDeletedBy("category-deleter");

        SystemTypeEntity systemTypeEntity = new SystemTypeEntity();
        systemTypeEntity.setId(101L);
        systemTypeEntity.setName("System type name");
        systemTypeEntity.setNameEs("Nombre tipo sistema");
        systemTypeEntity.setCreatedAt(LocalDateTime.of(2020, 1, 2, 0, 0));
        systemTypeEntity.setCreatedBy("systemtype-creator");
        systemTypeEntity.setUpdatedAt(LocalDateTime.of(2020, 2, 2, 0, 0));
        systemTypeEntity.setUpdatedBy("systemtype-updater");
        systemTypeEntity.setDeletedAt(LocalDateTime.of(2020, 3, 2, 0, 0));
        systemTypeEntity.setDeletedBy("systemtype-deleter");

        FieldEntity fieldEntity = new FieldEntity();
        fieldEntity.setId(102L);
        fieldEntity.setName("Field name");
        fieldEntity.setNameEs("Nombre ambito");
        fieldEntity.setCreatedAt(LocalDateTime.of(2020, 1, 3, 0, 0));
        fieldEntity.setCreatedBy("field-creator");
        fieldEntity.setUpdatedAt(LocalDateTime.of(2020, 2, 3, 0, 0));
        fieldEntity.setUpdatedBy("field-updater");
        fieldEntity.setDeletedAt(LocalDateTime.of(2020, 3, 3, 0, 0));
        fieldEntity.setDeletedBy("field-deleter");

        AdmUnitEntity admUnitEntity = new AdmUnitEntity();
        admUnitEntity.setId(103L);
        admUnitEntity.setCode("ADM-1");
        admUnitEntity.setName("Adm unit name");
        admUnitEntity.setNameEs("Nombre unidad administrativa");
        admUnitEntity.setCreatedAt(LocalDateTime.of(2020, 1, 4, 0, 0));
        admUnitEntity.setCreatedBy("admunit-creator");
        admUnitEntity.setUpdatedAt(LocalDateTime.of(2020, 2, 4, 0, 0));
        admUnitEntity.setUpdatedBy("admunit-updater");
        admUnitEntity.setDeletedAt(LocalDateTime.of(2020, 3, 4, 0, 0));
        admUnitEntity.setDeletedBy("admunit-deleter");

        CommissionEntity commissionEntity = new CommissionEntity();
        commissionEntity.setId(104L);
        commissionEntity.setName("Commission name");
        commissionEntity.setNameEs("Nombre comision");
        commissionEntity.setExpedientNumber("EXP-2024-01");
        commissionEntity.setApprovalDate(LocalDate.of(2024, 4, 1));
        commissionEntity.setCommissionType(CommissionType.SUPERIOR);
        commissionEntity.setCreatedAt(LocalDateTime.of(2020, 1, 5, 0, 0));
        commissionEntity.setCreatedBy("commission-creator");
        commissionEntity.setUpdatedAt(LocalDateTime.of(2020, 2, 5, 0, 0));
        commissionEntity.setUpdatedBy("commission-updater");
        commissionEntity.setDeletedAt(LocalDateTime.of(2020, 3, 5, 0, 0));
        commissionEntity.setDeletedBy("commission-deleter");

        LkupStatusEntity statusEntity = new LkupStatusEntity();
        statusEntity.setId(1L);
        statusEntity.setName("Active");
        statusEntity.setNameEs("Activo");

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
        applicationEntity.setAdmUnit(admUnitEntity);
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
        category.setCreatedAt(LocalDateTime.of(2020, 1, 1, 0, 0));
        category.setCreatedBy("category-creator");
        category.setUpdatedAt(LocalDateTime.of(2020, 2, 1, 0, 0));
        category.setUpdatedBy("category-updater");
        category.setDeletedAt(LocalDateTime.of(2020, 3, 1, 0, 0));
        category.setDeletedBy("category-deleter");

        SystemType systemType = new SystemType();
        systemType.setId(101L);
        systemType.setName("System type name");
        systemType.setNameEs("Nombre tipo sistema");
        systemType.setCreatedAt(LocalDateTime.of(2020, 1, 2, 0, 0));
        systemType.setCreatedBy("systemtype-creator");
        systemType.setUpdatedAt(LocalDateTime.of(2020, 2, 2, 0, 0));
        systemType.setUpdatedBy("systemtype-updater");
        systemType.setDeletedAt(LocalDateTime.of(2020, 3, 2, 0, 0));
        systemType.setDeletedBy("systemtype-deleter");

        Field field = new Field(102L, "Field name", "Nombre ambito",
                LocalDateTime.of(2020, 1, 3, 0, 0), "field-creator",
                LocalDateTime.of(2020, 2, 3, 0, 0), "field-updater",
                LocalDateTime.of(2020, 3, 3, 0, 0), "field-deleter");

        AdmUnit admUnit = new AdmUnit(103L, "ADM-1", "Adm unit name", "Nombre unidad administrativa",
                LocalDateTime.of(2020, 1, 4, 0, 0), "admunit-creator",
                LocalDateTime.of(2020, 2, 4, 0, 0), "admunit-updater",
                LocalDateTime.of(2020, 3, 4, 0, 0), "admunit-deleter");

        Commission commission = new Commission(104L, "Commission name", "Nombre comision", "EXP-2024-01",
                CommissionType.SUPERIOR, LocalDate.of(2024, 4, 1),
                LocalDateTime.of(2020, 1, 5, 0, 0), "commission-creator",
                LocalDateTime.of(2020, 2, 5, 0, 0), "commission-updater",
                LocalDateTime.of(2020, 3, 5, 0, 0), "commission-deleter");

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
        application.setAdmUnit(admUnit);
        application.setCsCommission(commission);
        application.setStatus(StatusEnum.ACTIVE);
        application.setCreatedAt(LocalDateTime.of(2021, 1, 1, 8, 0));
        application.setCreatedBy("application-creator");
        application.setUpdatedAt(LocalDateTime.of(2021, 2, 1, 8, 0));
        application.setUpdatedBy("application-updater");
        application.setDeletedAt(LocalDateTime.of(2021, 3, 1, 8, 0));
        application.setDeletedBy("application-deleter");
        return application;
    }

    private static EnvironmentEntity buildEnvironmentEntity() {
        EnvironmentEntity environmentEntity = new EnvironmentEntity();
        environmentEntity.setId(300L);
        environmentEntity.setCode("PRO");
        environmentEntity.setName("Production");
        environmentEntity.setNameEs("Produccion");
        environmentEntity.setCreatedAt(LocalDateTime.of(2019, 1, 1, 0, 0));
        environmentEntity.setCreatedBy("env-creator");
        environmentEntity.setUpdatedAt(LocalDateTime.of(2019, 2, 1, 0, 0));
        environmentEntity.setUpdatedBy("env-updater");
        environmentEntity.setDeletedAt(LocalDateTime.of(2019, 3, 1, 0, 0));
        environmentEntity.setDeletedBy("env-deleter");
        return environmentEntity;
    }

    private static Environment buildEnvironmentModel() {
        Environment environment = new Environment();
        environment.setId(300L);
        environment.setCode("PRO");
        environment.setName("Production");
        environment.setNameEs("Produccion");
        environment.setCreatedAt(LocalDateTime.of(2019, 1, 1, 0, 0));
        environment.setCreatedBy("env-creator");
        environment.setUpdatedAt(LocalDateTime.of(2019, 2, 1, 0, 0));
        environment.setUpdatedBy("env-updater");
        environment.setDeletedAt(LocalDateTime.of(2019, 3, 1, 0, 0));
        environment.setDeletedBy("env-deleter");
        return environment;
    }

    @Test
    void toModel_deepEntityGraph_mapsEveryNestedField() {
        AppDevelopmentEntity entity = new AppDevelopmentEntity();
        entity.setId(1L);
        entity.setApplication(buildDeepApplicationEntity());
        entity.setEnvironment(buildEnvironmentEntity());

        LkupModalityEntity modalityEntity = new LkupModalityEntity();
        modalityEntity.setId(400L);
        modalityEntity.setName("Internal");
        modalityEntity.setNameEs("Interno");
        entity.setModality(modalityEntity);

        LkupStandardAdaptionEntity standardAdaptionEntity = new LkupStandardAdaptionEntity();
        standardAdaptionEntity.setId(500L);
        standardAdaptionEntity.setName("Fully adapted");
        standardAdaptionEntity.setNameEs("Totalmente adaptado");
        entity.setStandardAdaption(standardAdaptionEntity);

        entity.setCode("https://git.example.com/repo.git");
        entity.setRevisionDate(LocalDateTime.of(2024, 1, 1, 0, 0));
        entity.setObservation("Some observation");
        entity.setCreatedAt(LocalDateTime.of(2023, 1, 1, 0, 0));
        entity.setCreatedBy("dev-creator");
        entity.setUpdatedAt(LocalDateTime.of(2023, 2, 1, 0, 0));
        entity.setUpdatedBy("dev-updater");
        entity.setDeletedAt(LocalDateTime.of(2023, 3, 1, 0, 0));
        entity.setDeletedBy("dev-deleter");

        AppDevelopment model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("https://git.example.com/repo.git", model.getCode());
        assertEquals(entity.getRevisionDate(), model.getRevisionDate());
        assertEquals("Some observation", model.getObservation());
        assertEquals(entity.getCreatedAt(), model.getCreatedAt());
        assertEquals("dev-creator", model.getCreatedBy());
        assertEquals(entity.getUpdatedAt(), model.getUpdatedAt());
        assertEquals("dev-updater", model.getUpdatedBy());
        assertEquals(entity.getDeletedAt(), model.getDeletedAt());
        assertEquals("dev-deleter", model.getDeletedBy());

        // Modality / StandardAdaption (delegated to the composed catalog mappers)
        assertEquals(400L, model.getModality().getId());
        assertEquals("Internal", model.getModality().getName());
        assertEquals("Interno", model.getModality().getNameEs());
        assertEquals(500L, model.getStandardAdaption().getId());
        assertEquals("Fully adapted", model.getStandardAdaption().getName());
        assertEquals("Totalmente adaptado", model.getStandardAdaption().getNameEs());

        // Environment
        Environment environment = model.getEnvironment();
        assertEquals(300L, environment.getId());
        assertEquals("PRO", environment.getCode());
        assertEquals("Production", environment.getName());
        assertEquals("Produccion", environment.getNameEs());
        assertEquals(entity.getEnvironment().getCreatedAt(), environment.getCreatedAt());
        assertEquals(entity.getEnvironment().getDeletedBy(), environment.getDeletedBy());

        // Application and its full nested graph
        Application application = model.getApplication();
        assertEquals(200L, application.getId());
        assertEquals("APP001", application.getCode());
        assertEquals("AP1", application.getPrefix());
        assertEquals("Application name", application.getName());
        assertEquals("Application description", application.getDescription());
        assertEquals(entity.getApplication().getExpirationDate(), application.getExpirationDate());
        assertEquals(StatusEnum.ACTIVE, application.getStatus());
        assertEquals(entity.getApplication().getCreatedAt(), application.getCreatedAt());
        assertEquals("application-creator", application.getCreatedBy());
        assertEquals(entity.getApplication().getDeletedAt(), application.getDeletedAt());
        assertEquals("application-deleter", application.getDeletedBy());

        assertEquals(100L, application.getCategory().getId());
        assertEquals("Category name", application.getCategory().getName());
        assertEquals("Nombre categoria", application.getCategory().getNameEs());
        assertEquals("category-deleter", application.getCategory().getDeletedBy());

        assertEquals(101L, application.getSystemType().getId());
        assertEquals("System type name", application.getSystemType().getName());
        assertEquals("Nombre tipo sistema", application.getSystemType().getNameEs());

        assertEquals(102L, application.getField().getId());
        assertEquals("Field name", application.getField().getName());
        assertEquals("Nombre ambito", application.getField().getNameEs());

        assertEquals(103L, application.getAdmUnit().getId());
        assertEquals("ADM-1", application.getAdmUnit().getCode());
        assertEquals("Adm unit name", application.getAdmUnit().getName());
        assertEquals("Nombre unidad administrativa", application.getAdmUnit().getNameEs());

        assertEquals(104L, application.getCsCommission().getId());
        assertEquals("Commission name", application.getCsCommission().getName());
        assertEquals("Nombre comision", application.getCsCommission().getNameEs());
        assertEquals("EXP-2024-01", application.getCsCommission().getExpedientNumber());
        assertEquals(LocalDate.of(2024, 4, 1), application.getCsCommission().getApprovalDate());
        assertEquals(CommissionType.SUPERIOR, application.getCsCommission().getCommissionType());
    }

    @Test
    void toModel_withNullNestedForeignKeys_leavesNestedModelFieldsNull() {
        AppDevelopmentEntity entity = new AppDevelopmentEntity();
        entity.setId(2L);
        entity.setCode("https://git.example.com/other.git");
        // application, environment, modality, standardAdaption intentionally left null

        AppDevelopment model = mapper.toModel(entity);

        assertEquals(2L, model.getId());
        assertEquals("https://git.example.com/other.git", model.getCode());
        assertNull(model.getApplication());
        assertNull(model.getEnvironment());
        assertNull(model.getModality());
        assertNull(model.getStandardAdaption());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toModel_applicationPresentWithAllNestedCatalogFieldsNull_leavesThemNull() {
        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(201L);
        applicationEntity.setCode("APP002");
        // category / systemType / field / admUnit / csCommission / status intentionally left null

        AppDevelopmentEntity entity = new AppDevelopmentEntity();
        entity.setId(7L);
        entity.setApplication(applicationEntity);

        AppDevelopment model = mapper.toModel(entity);

        Application application = model.getApplication();
        assertEquals(201L, application.getId());
        assertEquals("APP002", application.getCode());
        assertNull(application.getCategory());
        assertNull(application.getSystemType());
        assertNull(application.getField());
        assertNull(application.getAdmUnit());
        assertNull(application.getCsCommission());
        assertNull(application.getStatus());
    }

    @Test
    void toEntity_deepModelGraph_mapsEveryNestedField() {
        Modality modality = new Modality();
        modality.setId(400L);
        modality.setName("Internal");
        modality.setNameEs("Interno");

        StandardAdaption standardAdaption = new StandardAdaption();
        standardAdaption.setId(500L);
        standardAdaption.setName("Fully adapted");
        standardAdaption.setNameEs("Totalmente adaptado");

        AppDevelopment model = AppDevelopment.builder()
                .id(3L)
                .application(buildDeepApplicationModel())
                .environment(buildEnvironmentModel())
                .modality(modality)
                .standardAdaption(standardAdaption)
                .code("https://git.example.com/third.git")
                .revisionDate(LocalDateTime.of(2024, 2, 2, 0, 0))
                .observation("Third observation")
                .createdAt(LocalDateTime.of(2022, 1, 1, 0, 0))
                .createdBy("model-creator")
                .updatedAt(LocalDateTime.of(2022, 2, 1, 0, 0))
                .updatedBy("model-updater")
                .deletedAt(LocalDateTime.of(2022, 3, 1, 0, 0))
                .deletedBy("model-deleter")
                .build();

        AppDevelopmentEntity entity = mapper.toEntity(model);

        assertEquals(3L, entity.getId());
        assertEquals("https://git.example.com/third.git", entity.getCode());
        assertEquals(model.getRevisionDate(), entity.getRevisionDate());
        assertEquals("Third observation", entity.getObservation());
        assertEquals(model.getCreatedAt(), entity.getCreatedAt());
        assertEquals("model-creator", entity.getCreatedBy());
        assertEquals(model.getDeletedAt(), entity.getDeletedAt());
        assertEquals("model-deleter", entity.getDeletedBy());

        assertEquals(400L, entity.getModality().getId());
        assertEquals("Internal", entity.getModality().getName());
        assertEquals(500L, entity.getStandardAdaption().getId());
        assertEquals("Fully adapted", entity.getStandardAdaption().getName());

        assertEquals(300L, entity.getEnvironment().getId());
        assertEquals("PRO", entity.getEnvironment().getCode());
        assertEquals("Production", entity.getEnvironment().getName());

        ApplicationEntity applicationEntity = entity.getApplication();
        assertEquals(200L, applicationEntity.getId());
        assertEquals("APP001", applicationEntity.getCode());
        assertEquals("AP1", applicationEntity.getPrefix());
        assertEquals("Application name", applicationEntity.getName());
        assertEquals("Application description", applicationEntity.getDescription());
        assertEquals(1L, applicationEntity.getStatus().getId());
        assertNull(applicationEntity.getStatus().getName());

        assertEquals(100L, applicationEntity.getCategory().getId());
        assertEquals("Category name", applicationEntity.getCategory().getName());
        assertEquals(101L, applicationEntity.getSystemType().getId());
        assertEquals("System type name", applicationEntity.getSystemType().getName());
        assertEquals(102L, applicationEntity.getField().getId());
        assertEquals("Field name", applicationEntity.getField().getName());
        assertEquals(103L, applicationEntity.getAdmUnit().getId());
        assertEquals("ADM-1", applicationEntity.getAdmUnit().getCode());
        assertEquals(104L, applicationEntity.getCsCommission().getId());
        assertEquals("EXP-2024-01", applicationEntity.getCsCommission().getExpedientNumber());
        assertEquals(CommissionType.SUPERIOR, applicationEntity.getCsCommission().getCommissionType());
    }

    @Test
    void toEntity_withNullNestedFields_leavesNestedEntityFieldsNull() {
        AppDevelopment model = AppDevelopment.builder()
                .id(4L)
                .code("https://git.example.com/fourth.git")
                .build();

        AppDevelopmentEntity entity = mapper.toEntity(model);

        assertEquals(4L, entity.getId());
        assertNull(entity.getApplication());
        assertNull(entity.getEnvironment());
        assertNull(entity.getModality());
        assertNull(entity.getStandardAdaption());
    }

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toEntity_applicationPresentWithAllNestedCatalogFieldsNull_leavesThemNull() {
        Application application = new Application();
        application.setId(202L);
        application.setCode("APP003");
        // category / systemType / field / admUnit / csCommission / status intentionally left null

        AppDevelopment model = AppDevelopment.builder().id(8L).application(application).build();

        AppDevelopmentEntity entity = mapper.toEntity(model);

        ApplicationEntity applicationEntity = entity.getApplication();
        assertEquals(202L, applicationEntity.getId());
        assertEquals("APP003", applicationEntity.getCode());
        assertNull(applicationEntity.getCategory());
        assertNull(applicationEntity.getSystemType());
        assertNull(applicationEntity.getField());
        assertNull(applicationEntity.getAdmUnit());
        assertNull(applicationEntity.getCsCommission());
        assertNull(applicationEntity.getStatus());
    }

    @Test
    void toResponse_deepModel_flattensEveryNestedFieldIntoOutputDTO() {
        Modality modality = new Modality();
        modality.setId(400L);
        modality.setName("Internal");
        StandardAdaption standardAdaption = new StandardAdaption();
        standardAdaption.setId(500L);
        standardAdaption.setName("Fully adapted");

        AppDevelopment model = AppDevelopment.builder()
                .id(5L)
                .application(buildDeepApplicationModel())
                .environment(buildEnvironmentModel())
                .modality(modality)
                .standardAdaption(standardAdaption)
                .code("https://git.example.com/fifth.git")
                .revisionDate(LocalDateTime.of(2024, 3, 3, 0, 0))
                .observation("Fifth observation")
                .deletedAt(LocalDateTime.of(2024, 6, 1, 0, 0))
                .build();

        DevelopmentOutputDTO response = mapper.toResponse(model);

        assertEquals(5L, response.getId());
        assertEquals("https://git.example.com/fifth.git", response.getCode());
        assertEquals(model.getRevisionDate(), response.getRevisionDate());
        assertEquals("Fifth observation", response.getObservation());
        assertEquals(model.getDeletedAt(), response.getDeletedAt());
        // modality / standardAdaption pass straight through untouched (already plain domain models)
        assertEquals(modality, response.getModality());
        assertEquals(standardAdaption, response.getStandardAdaption());

        assertEquals(300L, response.getEnvironment().getId());
        assertEquals("Production", response.getEnvironment().getName());
        assertEquals(model.getEnvironment().getDeletedAt(), response.getEnvironment().getDeletedAt());

        var applicationDTO = response.getApplication();
        assertEquals(200L, applicationDTO.getId());
        assertEquals("APP001", applicationDTO.getCode());
        assertEquals("Application name", applicationDTO.getName());
        assertEquals(StatusEnum.ACTIVE, applicationDTO.getStatus());
        // LocalDateTime -> XMLGregorianCalendar -> LocalDate round trip keeps only the date part
        assertEquals(LocalDate.of(2030, 6, 15), applicationDTO.getExpirationDate());
        assertEquals(LocalDate.of(2021, 1, 1), applicationDTO.getCreatedAt());
        assertEquals("application-creator", applicationDTO.getCreatedBy());
        assertEquals(LocalDate.of(2021, 2, 1), applicationDTO.getUpdatedAt());
        assertEquals("application-updater", applicationDTO.getUpdatedBy());

        assertEquals(100L, applicationDTO.getCategory().getId());
        assertEquals("Category name", applicationDTO.getCategory().getName());
        assertEquals(101L, applicationDTO.getSystemType().getId());
        assertEquals(102L, applicationDTO.getField().getId());
        assertEquals(103L, applicationDTO.getAdmUnit().getId());
        assertEquals(104L, applicationDTO.getCsCommission().getId());
        assertEquals(CommissionType.SUPERIOR, applicationDTO.getCsCommission().getCommissionType());
    }

    @Test
    void toResponse_withNullApplicationAndEnvironment_leavesNestedDtoFieldsNull() {
        AppDevelopment model = AppDevelopment.builder()
                .id(6L)
                .code("https://git.example.com/sixth.git")
                .build();

        DevelopmentOutputDTO response = mapper.toResponse(model);

        assertEquals(6L, response.getId());
        assertNull(response.getApplication());
        assertNull(response.getEnvironment());
        assertNull(response.getModality());
        assertNull(response.getStandardAdaption());
    }

    @Test
    void toResponse_null_returnsNull() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void toResponse_applicationPresentWithAllNestedCatalogFieldsNull_leavesThemNull() {
        Application application = new Application();
        application.setId(203L);
        application.setCode("APP004");
        // category / systemType / field / admUnit / csCommission intentionally left null

        AppDevelopment model = AppDevelopment.builder().id(9L).application(application).build();

        DevelopmentOutputDTO response = mapper.toResponse(model);

        var applicationDTO = response.getApplication();
        assertEquals(203L, applicationDTO.getId());
        assertEquals("APP004", applicationDTO.getCode());
        assertNull(applicationDTO.getCategory());
        assertNull(applicationDTO.getSystemType());
        assertNull(applicationDTO.getField());
        assertNull(applicationDTO.getAdmUnit());
        assertNull(applicationDTO.getCsCommission());
        assertNull(applicationDTO.getExpirationDate());
        assertNull(applicationDTO.getCreatedAt());
        assertNull(applicationDTO.getUpdatedAt());
    }

    @Test
    void toModelFromInput_setsShallowIdReferencesAndDelegatesLookupIds() {
        DevelopmentInputDTO inputDTO = new DevelopmentInputDTO(
                2L, 3L, 4L, "https://repo.git", 5L, LocalDateTime.of(2024, 1, 1, 0, 0), "Observation text");

        AppDevelopment model = mapper.toModelFromInput(inputDTO);

        assertEquals(2L, model.getApplication().getId());
        assertEquals(3L, model.getEnvironment().getId());
        assertEquals(4L, model.getModality().getId());
        assertNull(model.getModality().getName());
        assertEquals(5L, model.getStandardAdaption().getId());
        assertNull(model.getStandardAdaption().getName());
        assertEquals("https://repo.git", model.getCode());
        assertEquals(inputDTO.getRevisionDate(), model.getRevisionDate());
        assertEquals("Observation text", model.getObservation());
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
        AppDevelopment existing = AppDevelopment.builder()
                .id(9L)
                .application(new Application())
                .environment(new Environment())
                .build();
        DevelopmentInputDTO inputDTO = new DevelopmentInputDTO(
                6L, 7L, 8L, "https://updated.git", 10L, LocalDateTime.of(2024, 5, 5, 0, 0), "Updated observation");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(9L, existing.getId());
        assertEquals(6L, existing.getApplication().getId());
        assertEquals(7L, existing.getEnvironment().getId());
        assertEquals(8L, existing.getModality().getId());
        assertEquals(10L, existing.getStandardAdaption().getId());
        assertEquals("https://updated.git", existing.getCode());
        assertEquals(inputDTO.getRevisionDate(), existing.getRevisionDate());
        assertEquals("Updated observation", existing.getObservation());
    }

    @Test
    void updateModelFromInput_whenExistingApplicationAndEnvironmentAreNull_createsThem() {
        AppDevelopment existing = AppDevelopment.builder().id(12L).build();
        DevelopmentInputDTO inputDTO = new DevelopmentInputDTO(
                13L, 14L, 15L, "https://created.git", 16L, LocalDateTime.of(2024, 7, 7, 0, 0), "Created observation");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(12L, existing.getId());
        assertEquals(13L, existing.getApplication().getId());
        assertEquals(14L, existing.getEnvironment().getId());
        assertEquals(15L, existing.getModality().getId());
        assertEquals(16L, existing.getStandardAdaption().getId());
    }

    @Test
    void updateModelFromInput_null_doesNothing() {
        AppDevelopment existing = AppDevelopment.builder().id(11L).code("untouched").build();

        mapper.updateModelFromInput(null, existing);

        assertEquals(11L, existing.getId());
        assertEquals("untouched", existing.getCode());
    }
}
