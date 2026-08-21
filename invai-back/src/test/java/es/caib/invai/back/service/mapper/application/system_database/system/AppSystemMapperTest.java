package es.caib.invai.back.service.mapper.application.system_database.system;

import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import es.caib.invai.back.interna.application.system_database.core.DTO.AppInformationSystemDbOutputDTO;
import es.caib.invai.back.interna.application.system_database.system.DTO.AppSystemInputDTO;
import es.caib.invai.back.interna.application.system_database.system.DTO.AppSystemOutputDTO;
import es.caib.invai.back.interna.maintenance.systems.server.DTO.ServerOutputDTO;
import es.caib.invai.back.interna.maintenance.systems.system.DTO.SystemOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.admUnit.AdmUnitEntity;
import es.caib.invai.back.persistence.model.application.system_database.core.AppInformationSystemDbEntity;
import es.caib.invai.back.persistence.model.application.system_database.system.AppSystemEntity;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.maintenance.general.category.CategoryEntity;
import es.caib.invai.back.persistence.model.maintenance.general.commission.CommissionEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.environment.EnvironmentEntity;
import es.caib.invai.back.persistence.model.maintenance.general.field.FieldEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.server.ServerEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.system.SystemEntity;
import es.caib.invai.back.persistence.model.maintenance.general.systemType.SystemTypeEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.serverType.LkupServerTypeEntity;
import es.caib.invai.back.persistence.model.catalog.status.LkupStatusEntity;
import es.caib.invai.back.service.model.maintenance.admUnit.AdmUnit;
import es.caib.invai.back.service.model.application.system_database.core.AppInformationSystemDb;
import es.caib.invai.back.service.model.application.system_database.system.AppSystem;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.maintenance.general.category.Category;
import es.caib.invai.back.service.model.maintenance.general.commission.Commission;
import es.caib.invai.back.service.model.maintenance.general.commission.CommissionType;
import es.caib.invai.back.service.model.maintenance.systems.environment.Environment;
import es.caib.invai.back.service.model.maintenance.general.field.Field;
import es.caib.invai.back.service.model.maintenance.systems.server.Server;
import es.caib.invai.back.service.model.maintenance.systems.serverType.ServerType;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import es.caib.invai.back.service.model.maintenance.systems.system.System;
import es.caib.invai.back.service.model.maintenance.general.systemType.SystemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import es.caib.invai.back.service.mapper.catalog.status.StatusMapperImpl;

/**
 * Unit tests for the generated {@link AppSystemMapperImpl}, exercising every conversion
 * direction declared on {@link AppSystemMapper}.
 * <p>
 * {@link AppSystemMapper} declares {@code uses = {StatusMapper.class}}, so the generated
 * implementation carries an {@code @Autowired} {@link StatusMapper} field used internally by the
 * nested {@code Application -> ApplicationEntity} conversions. Since no Spring context is
 * bootstrapped here, a real {@link StatusMapperImpl} is injected manually via
 * {@link ReflectionTestUtils}, following the same pattern used in {@code TechnologyMapperTest}.
 * </p>
 * <p>
 * Because {@link AppSystemEntity} carries a deep object graph
 * (AppSystem -&gt; System -&gt; Server -&gt; Environment / ServerType, and
 * AppSystem -&gt; AppInformationSystemDb -&gt; Application -&gt; Category / SystemType / Field /
 * AdmUnit / Commission / Status), the generated mapper implementation contains many private
 * helper methods that only execute when the corresponding nested entity is non-null. The
 * "fully populated" tests below deliberately populate every level of that graph to exercise
 * those helpers, while the "null nested FK" tests confirm the alternate null-handling branches
 * behave safely.
 * </p>
 */
class AppSystemMapperTest {

    private AppSystemMapper mapper;

    @BeforeEach
    void setUp() {
        AppSystemMapperImpl impl = new AppSystemMapperImpl();
        ReflectionTestUtils.setField(impl, "statusMapper", new StatusMapperImpl());
        mapper = impl;
    }

    // ------------------------------------------------------------------
    // Entity graph builders
    // ------------------------------------------------------------------

    private AppSystemEntity buildDeepEntity() {
        EnvironmentEntity environmentEntity = new EnvironmentEntity();
        environmentEntity.setId(100L);
        environmentEntity.setCode("PRO");
        environmentEntity.setName("Produccio");
        environmentEntity.setNameEs("Produccion");
        environmentEntity.setCreatedAt(LocalDateTime.of(2020, 1, 1, 0, 0));
        environmentEntity.setCreatedBy("env-creator");
        environmentEntity.setUpdatedAt(LocalDateTime.of(2020, 2, 1, 0, 0));
        environmentEntity.setUpdatedBy("env-updater");
        environmentEntity.setDeletedAt(LocalDateTime.of(2020, 3, 1, 0, 0));
        environmentEntity.setDeletedBy("env-deleter");

        LkupServerTypeEntity serverTypeEntity = new LkupServerTypeEntity();
        serverTypeEntity.setId(101L);
        serverTypeEntity.setCode("APPLICATION");
        serverTypeEntity.setName("Aplicacio");
        serverTypeEntity.setNameEs("Aplicacion");

        ServerEntity serverEntity = new ServerEntity();
        serverEntity.setId(102L);
        serverEntity.setName("app-server-01");
        serverEntity.setEnvironment(environmentEntity);
        serverEntity.setServerType(serverTypeEntity);
        serverEntity.setCreatedAt(LocalDateTime.of(2020, 1, 2, 0, 0));
        serverEntity.setCreatedBy("srv-creator");
        serverEntity.setUpdatedAt(LocalDateTime.of(2020, 2, 2, 0, 0));
        serverEntity.setUpdatedBy("srv-updater");
        serverEntity.setDeletedAt(LocalDateTime.of(2020, 3, 2, 0, 0));
        serverEntity.setDeletedBy("srv-deleter");

        SystemEntity systemEntity = new SystemEntity();
        systemEntity.setId(20L);
        systemEntity.setServer(serverEntity);
        systemEntity.setInstance("invai-instance");
        systemEntity.setPort(8080);
        systemEntity.setVersion("1.2.3");
        systemEntity.setDescription("Main invai system");
        systemEntity.setCreatedAt(LocalDateTime.of(2020, 1, 4, 0, 0));
        systemEntity.setCreatedBy("sys-creator");
        systemEntity.setUpdatedAt(LocalDateTime.of(2020, 2, 4, 0, 0));
        systemEntity.setUpdatedBy("sys-updater");
        systemEntity.setDeletedAt(LocalDateTime.of(2020, 3, 4, 0, 0));
        systemEntity.setDeletedBy("sys-deleter");

        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setId(200L);
        categoryEntity.setName("Categoria");
        categoryEntity.setNameEs("Categoria ES");
        categoryEntity.setCreatedAt(LocalDateTime.of(2019, 1, 1, 0, 0));
        categoryEntity.setCreatedBy("cat-creator");
        categoryEntity.setUpdatedAt(LocalDateTime.of(2019, 2, 1, 0, 0));
        categoryEntity.setUpdatedBy("cat-updater");
        categoryEntity.setDeletedAt(LocalDateTime.of(2019, 3, 1, 0, 0));
        categoryEntity.setDeletedBy("cat-deleter");

        SystemTypeEntity systemTypeEntity = new SystemTypeEntity();
        systemTypeEntity.setId(201L);
        systemTypeEntity.setName("Web App");
        systemTypeEntity.setNameEs("Aplicacion Web");
        systemTypeEntity.setCreatedAt(LocalDateTime.of(2019, 1, 2, 0, 0));
        systemTypeEntity.setCreatedBy("st-creator");
        systemTypeEntity.setUpdatedAt(LocalDateTime.of(2019, 2, 2, 0, 0));
        systemTypeEntity.setUpdatedBy("st-updater");
        systemTypeEntity.setDeletedAt(LocalDateTime.of(2019, 3, 2, 0, 0));
        systemTypeEntity.setDeletedBy("st-deleter");

        FieldEntity fieldEntity = new FieldEntity();
        fieldEntity.setId(202L);
        fieldEntity.setName("Hisenda");
        fieldEntity.setNameEs("Hacienda");
        fieldEntity.setCreatedAt(LocalDateTime.of(2019, 1, 3, 0, 0));
        fieldEntity.setCreatedBy("field-creator");
        fieldEntity.setUpdatedAt(LocalDateTime.of(2019, 2, 3, 0, 0));
        fieldEntity.setUpdatedBy("field-updater");
        fieldEntity.setDeletedAt(LocalDateTime.of(2019, 3, 3, 0, 0));
        fieldEntity.setDeletedBy("field-deleter");

        AdmUnitEntity admUnitEntity = new AdmUnitEntity();
        admUnitEntity.setId(203L);
        admUnitEntity.setCode("ADM-01");
        admUnitEntity.setName("Unitat");
        admUnitEntity.setNameEs("Unidad");
        admUnitEntity.setCreatedAt(LocalDateTime.of(2019, 1, 4, 0, 0));
        admUnitEntity.setCreatedBy("adm-creator");
        admUnitEntity.setUpdatedAt(LocalDateTime.of(2019, 2, 4, 0, 0));
        admUnitEntity.setUpdatedBy("adm-updater");
        admUnitEntity.setDeletedAt(LocalDateTime.of(2019, 3, 4, 0, 0));
        admUnitEntity.setDeletedBy("adm-deleter");

        CommissionEntity commissionEntity = new CommissionEntity();
        commissionEntity.setId(204L);
        commissionEntity.setName("Comissio");
        commissionEntity.setNameEs("Comision");
        commissionEntity.setExpedientNumber("EXP-2024-01");
        commissionEntity.setCommissionType(CommissionType.TECNICA);
        commissionEntity.setApprovalDate(LocalDate.of(2024, 1, 15));
        commissionEntity.setCreatedAt(LocalDateTime.of(2019, 1, 5, 0, 0));
        commissionEntity.setCreatedBy("com-creator");
        commissionEntity.setUpdatedAt(LocalDateTime.of(2019, 2, 5, 0, 0));
        commissionEntity.setUpdatedBy("com-updater");
        commissionEntity.setDeletedAt(LocalDateTime.of(2019, 3, 5, 0, 0));
        commissionEntity.setDeletedBy("com-deleter");

        LkupStatusEntity statusEntity = new LkupStatusEntity();
        statusEntity.setId(StatusEnum.ACTIVE.getId());
        statusEntity.setName("Actiu");
        statusEntity.setNameEs("Activo");

        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(205L);
        applicationEntity.setCode("APP01");
        applicationEntity.setPrefix("AP1");
        applicationEntity.setName("Application One");
        applicationEntity.setCategory(categoryEntity);
        applicationEntity.setSystemType(systemTypeEntity);
        applicationEntity.setField(fieldEntity);
        applicationEntity.setAdmUnit(admUnitEntity);
        applicationEntity.setCsCommission(commissionEntity);
        applicationEntity.setStatus(statusEntity);
        applicationEntity.setDescription("Application description");
        applicationEntity.setExpirationDate(LocalDateTime.of(2030, 12, 31, 0, 0));
        applicationEntity.setCreatedAt(LocalDateTime.of(2019, 1, 6, 0, 0));
        applicationEntity.setCreatedBy("app-creator");
        applicationEntity.setUpdatedAt(LocalDateTime.of(2019, 2, 6, 0, 0));
        applicationEntity.setUpdatedBy("app-updater");
        applicationEntity.setDeletedAt(LocalDateTime.of(2019, 3, 6, 0, 0));
        applicationEntity.setDeletedBy("app-deleter");

        AppInformationSystemDbEntity informationSystemDbEntity = new AppInformationSystemDbEntity();
        informationSystemDbEntity.setId(10L);
        informationSystemDbEntity.setApplication(applicationEntity);
        informationSystemDbEntity.setObservation("Some observation");
        informationSystemDbEntity.setCreatedAt(LocalDateTime.of(2018, 1, 1, 0, 0));
        informationSystemDbEntity.setCreatedBy("isdb-creator");
        informationSystemDbEntity.setUpdatedAt(LocalDateTime.of(2018, 2, 1, 0, 0));
        informationSystemDbEntity.setUpdatedBy("isdb-updater");
        informationSystemDbEntity.setDeletedAt(LocalDateTime.of(2018, 3, 1, 0, 0));
        informationSystemDbEntity.setDeletedBy("isdb-deleter");

        AppSystemEntity entity = new AppSystemEntity();
        entity.setId(1L);
        entity.setInformationSystemDb(informationSystemDbEntity);
        entity.setSystem(systemEntity);
        entity.setCreatedAt(LocalDateTime.of(2017, 1, 1, 0, 0));
        entity.setCreatedBy("appsys-creator");
        entity.setUpdatedAt(LocalDateTime.of(2017, 2, 1, 0, 0));
        entity.setUpdatedBy("appsys-updater");
        entity.setDeletedAt(LocalDateTime.of(2017, 3, 1, 0, 0));
        entity.setDeletedBy("appsys-deleter");

        return entity;
    }

    private AppSystem buildDeepModel() {
        Environment environment = new Environment();
        environment.setId(300L);
        environment.setCode("DEV");
        environment.setName("Desenvolupament");
        environment.setNameEs("Desarrollo");
        environment.setCreatedAt(LocalDateTime.of(2021, 1, 1, 0, 0));
        environment.setCreatedBy("env-creator");
        environment.setUpdatedAt(LocalDateTime.of(2021, 2, 1, 0, 0));
        environment.setUpdatedBy("env-updater");
        environment.setDeletedAt(LocalDateTime.of(2021, 3, 1, 0, 0));
        environment.setDeletedBy("env-deleter");

        ServerType serverType = new ServerType();
        serverType.setId(301L);
        serverType.setCode("APPLICATION");
        serverType.setName("Aplicacio");
        serverType.setNameEs("Aplicacion");

        Server server = new Server();
        server.setId(302L);
        server.setName("app-server-dev");
        server.setEnvironment(environment);
        server.setServerType(serverType);
        server.setCreatedAt(LocalDateTime.of(2021, 1, 2, 0, 0));
        server.setCreatedBy("srv-creator");
        server.setUpdatedAt(LocalDateTime.of(2021, 2, 2, 0, 0));
        server.setUpdatedBy("srv-updater");
        server.setDeletedAt(LocalDateTime.of(2021, 3, 2, 0, 0));
        server.setDeletedBy("srv-deleter");

        System system = new System();
        system.setId(21L);
        system.setServer(server);
        system.setInstance("invai-instance-dev");
        system.setPort(9090);
        system.setVersion("2.0.0");
        system.setDescription("Dev system");
        system.setCreatedAt(LocalDateTime.of(2021, 1, 4, 0, 0));
        system.setCreatedBy("sys-creator");
        system.setUpdatedAt(LocalDateTime.of(2021, 2, 4, 0, 0));
        system.setUpdatedBy("sys-updater");
        system.setDeletedAt(LocalDateTime.of(2021, 3, 4, 0, 0));
        system.setDeletedBy("sys-deleter");

        Category category = new Category();
        category.setId(400L);
        category.setName("Categoria2");
        category.setNameEs("Categoria2 ES");
        category.setCreatedAt(LocalDateTime.of(2022, 1, 1, 0, 0));
        category.setCreatedBy("cat-creator");
        category.setUpdatedAt(LocalDateTime.of(2022, 2, 1, 0, 0));
        category.setUpdatedBy("cat-updater");
        category.setDeletedAt(LocalDateTime.of(2022, 3, 1, 0, 0));
        category.setDeletedBy("cat-deleter");

        SystemType systemType = new SystemType();
        systemType.setId(401L);
        systemType.setName("Microservice");
        systemType.setNameEs("Microservicio");
        systemType.setCreatedAt(LocalDateTime.of(2022, 1, 2, 0, 0));
        systemType.setCreatedBy("st-creator");
        systemType.setUpdatedAt(LocalDateTime.of(2022, 2, 2, 0, 0));
        systemType.setUpdatedBy("st-updater");
        systemType.setDeletedAt(LocalDateTime.of(2022, 3, 2, 0, 0));
        systemType.setDeletedBy("st-deleter");

        Field field = new Field();
        field.setId(402L);
        field.setName("Educacio");
        field.setNameEs("Educacion");
        field.setCreatedAt(LocalDateTime.of(2022, 1, 3, 0, 0));
        field.setCreatedBy("field-creator");
        field.setUpdatedAt(LocalDateTime.of(2022, 2, 3, 0, 0));
        field.setUpdatedBy("field-updater");
        field.setDeletedAt(LocalDateTime.of(2022, 3, 3, 0, 0));
        field.setDeletedBy("field-deleter");

        AdmUnit admUnit = new AdmUnit(410L, "ADM-02", "Unitat2", "Unidad2",
                LocalDateTime.of(2022, 1, 4, 0, 0), "adm-creator",
                LocalDateTime.of(2022, 2, 4, 0, 0), "adm-updater",
                LocalDateTime.of(2022, 3, 4, 0, 0), "adm-deleter");

        Commission commission = new Commission(411L, "Comissio2", "Comision2", "EXP-2024-02",
                CommissionType.SUPERIOR, LocalDate.of(2024, 2, 20),
                LocalDateTime.of(2022, 1, 5, 0, 0), "com-creator",
                LocalDateTime.of(2022, 2, 5, 0, 0), "com-updater",
                LocalDateTime.of(2022, 3, 5, 0, 0), "com-deleter");

        Application application = new Application();
        application.setId(420L);
        application.setCode("APP02");
        application.setPrefix("AP2");
        application.setName("Application Two");
        application.setCategory(category);
        application.setSystemType(systemType);
        application.setField(field);
        application.setAdmUnit(admUnit);
        application.setCsCommission(commission);
        application.setStatus(StatusEnum.INACTIVE);
        application.setDescription("Application two description");
        application.setExpirationDate(LocalDateTime.of(2031, 12, 31, 0, 0));
        application.setCreatedAt(LocalDateTime.of(2022, 1, 6, 0, 0));
        application.setCreatedBy("app-creator");
        application.setUpdatedAt(LocalDateTime.of(2022, 2, 6, 0, 0));
        application.setUpdatedBy("app-updater");
        application.setDeletedAt(LocalDateTime.of(2022, 3, 6, 0, 0));
        application.setDeletedBy("app-deleter");

        AppInformationSystemDb informationSystemDb = new AppInformationSystemDb();
        informationSystemDb.setId(11L);
        informationSystemDb.setApplication(application);
        informationSystemDb.setObservation("Model observation");
        informationSystemDb.setCreatedAt(LocalDateTime.of(2023, 1, 1, 0, 0));
        informationSystemDb.setCreatedBy("isdb-creator");
        informationSystemDb.setUpdatedAt(LocalDateTime.of(2023, 2, 1, 0, 0));
        informationSystemDb.setUpdatedBy("isdb-updater");
        informationSystemDb.setDeletedAt(LocalDateTime.of(2023, 3, 1, 0, 0));
        informationSystemDb.setDeletedBy("isdb-deleter");

        AppSystem model = new AppSystem();
        model.setId(2L);
        model.setInformationSystemDb(informationSystemDb);
        model.setSystem(system);
        model.setCreatedAt(LocalDateTime.of(2016, 1, 1, 0, 0));
        model.setCreatedBy("appsys-creator");
        model.setUpdatedAt(LocalDateTime.of(2016, 2, 1, 0, 0));
        model.setUpdatedBy("appsys-updater");
        model.setDeletedAt(LocalDateTime.of(2016, 3, 1, 0, 0));
        model.setDeletedBy("appsys-deleter");

        return model;
    }

    // ------------------------------------------------------------------
    // toModel
    // ------------------------------------------------------------------

    @Test
    void toModel_deepEntityGraph_mapsEveryNestedField() {
        AppSystemEntity entity = buildDeepEntity();

        AppSystem model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("appsys-creator", model.getCreatedBy());
        assertEquals("appsys-updater", model.getUpdatedBy());
        assertEquals("appsys-deleter", model.getDeletedBy());
        assertEquals(LocalDateTime.of(2017, 1, 1, 0, 0), model.getCreatedAt());
        assertEquals(LocalDateTime.of(2017, 2, 1, 0, 0), model.getUpdatedAt());
        assertEquals(LocalDateTime.of(2017, 3, 1, 0, 0), model.getDeletedAt());

        System system = model.getSystem();
        assertEquals(20L, system.getId());
        assertEquals("invai-instance", system.getInstance());
        assertEquals(8080, system.getPort());
        assertEquals("1.2.3", system.getVersion());
        assertEquals("Main invai system", system.getDescription());
        assertEquals("sys-creator", system.getCreatedBy());

        Server server = system.getServer();
        assertEquals(102L, server.getId());
        assertEquals("app-server-01", server.getName());
        assertEquals("srv-creator", server.getCreatedBy());

        Environment environment = server.getEnvironment();
        assertEquals(100L, environment.getId());
        assertEquals("PRO", environment.getCode());
        assertEquals("Produccio", environment.getName());
        assertEquals("Produccion", environment.getNameEs());
        assertEquals("env-creator", environment.getCreatedBy());

        ServerType serverType = server.getServerType();
        assertEquals(101L, serverType.getId());
        assertEquals("APPLICATION", serverType.getCode());
        assertEquals("Aplicacio", serverType.getName());
        assertEquals("Aplicacion", serverType.getNameEs());

        AppInformationSystemDb informationSystemDb = model.getInformationSystemDb();
        assertEquals(10L, informationSystemDb.getId());
        assertEquals("Some observation", informationSystemDb.getObservation());
        assertEquals("isdb-creator", informationSystemDb.getCreatedBy());

        Application application = informationSystemDb.getApplication();
        assertEquals(205L, application.getId());
        assertEquals("APP01", application.getCode());
        assertEquals("AP1", application.getPrefix());
        assertEquals("Application One", application.getName());
        assertEquals("Application description", application.getDescription());
        assertEquals(LocalDateTime.of(2030, 12, 31, 0, 0), application.getExpirationDate());
        assertEquals(StatusEnum.ACTIVE, application.getStatus());
        assertEquals("app-creator", application.getCreatedBy());

        Category category = application.getCategory();
        assertEquals(200L, category.getId());
        assertEquals("Categoria", category.getName());
        assertEquals("Categoria ES", category.getNameEs());

        SystemType systemType = application.getSystemType();
        assertEquals(201L, systemType.getId());
        assertEquals("Web App", systemType.getName());
        assertEquals("Aplicacion Web", systemType.getNameEs());

        Field field = application.getField();
        assertEquals(202L, field.getId());
        assertEquals("Hisenda", field.getName());
        assertEquals("Hacienda", field.getNameEs());

        AdmUnit admUnit = application.getAdmUnit();
        assertEquals(203L, admUnit.getId());
        assertEquals("ADM-01", admUnit.getCode());
        assertEquals("Unitat", admUnit.getName());
        assertEquals("Unidad", admUnit.getNameEs());

        Commission commission = application.getCsCommission();
        assertEquals(204L, commission.getId());
        assertEquals("Comissio", commission.getName());
        assertEquals("Comision", commission.getNameEs());
        assertEquals("EXP-2024-01", commission.getExpedientNumber());
        assertEquals(CommissionType.TECNICA, commission.getCommissionType());
        assertEquals(LocalDate.of(2024, 1, 15), commission.getApprovalDate());
    }

    @Test
    void toModel_nullNestedForeignKeys_producesNullNestedModelFields() {
        AppSystemEntity entity = new AppSystemEntity();
        entity.setId(1L);
        entity.setInformationSystemDb(null);
        entity.setSystem(null);

        AppSystem model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertNull(model.getInformationSystemDb());
        assertNull(model.getSystem());
    }

    @Test
    void toModel_applicationWithNullNestedCatalogReferences_leavesApplicationNestedFieldsNull() {
        AppInformationSystemDbEntity informationSystemDbEntity = new AppInformationSystemDbEntity();
        informationSystemDbEntity.setId(10L);
        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(205L);
        // category, systemType, field, admUnit, csCommission, status all left null
        informationSystemDbEntity.setApplication(applicationEntity);

        AppSystemEntity entity = new AppSystemEntity();
        entity.setId(1L);
        entity.setInformationSystemDb(informationSystemDbEntity);
        entity.setSystem(null);

        AppSystem model = mapper.toModel(entity);

        Application application = model.getInformationSystemDb().getApplication();
        assertEquals(205L, application.getId());
        assertNull(application.getCategory());
        assertNull(application.getSystemType());
        assertNull(application.getField());
        assertNull(application.getAdmUnit());
        assertNull(application.getCsCommission());
        assertNull(application.getStatus());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    // ------------------------------------------------------------------
    // toEntity
    // ------------------------------------------------------------------

    @Test
    void toEntity_deepModelGraph_mapsEveryNestedField() {
        AppSystem model = buildDeepModel();

        AppSystemEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("appsys-creator", entity.getCreatedBy());
        assertEquals(LocalDateTime.of(2016, 1, 1, 0, 0), entity.getCreatedAt());

        SystemEntity systemEntity = entity.getSystem();
        assertEquals(21L, systemEntity.getId());
        assertEquals("invai-instance-dev", systemEntity.getInstance());
        assertEquals(9090, systemEntity.getPort());
        assertEquals("2.0.0", systemEntity.getVersion());
        assertEquals("Dev system", systemEntity.getDescription());

        ServerEntity serverEntity = systemEntity.getServer();
        assertEquals(302L, serverEntity.getId());
        assertEquals("app-server-dev", serverEntity.getName());

        EnvironmentEntity environmentEntity = serverEntity.getEnvironment();
        assertEquals(300L, environmentEntity.getId());
        assertEquals("DEV", environmentEntity.getCode());
        assertEquals("Desenvolupament", environmentEntity.getName());
        assertEquals("Desarrollo", environmentEntity.getNameEs());

        LkupServerTypeEntity serverTypeEntity = serverEntity.getServerType();
        assertEquals(301L, serverTypeEntity.getId());
        assertEquals("APPLICATION", serverTypeEntity.getCode());

        AppInformationSystemDbEntity informationSystemDbEntity = entity.getInformationSystemDb();
        assertEquals(11L, informationSystemDbEntity.getId());
        assertEquals("Model observation", informationSystemDbEntity.getObservation());

        ApplicationEntity applicationEntity = informationSystemDbEntity.getApplication();
        assertEquals(420L, applicationEntity.getId());
        assertEquals("APP02", applicationEntity.getCode());
        assertEquals("AP2", applicationEntity.getPrefix());
        assertEquals("Application Two", applicationEntity.getName());
        assertEquals("Application two description", applicationEntity.getDescription());
        assertEquals(LocalDateTime.of(2031, 12, 31, 0, 0), applicationEntity.getExpirationDate());

        LkupStatusEntity statusEntity = applicationEntity.getStatus();
        assertEquals(StatusEnum.INACTIVE.getId(), statusEntity.getId());

        CategoryEntity categoryEntity = applicationEntity.getCategory();
        assertEquals(400L, categoryEntity.getId());
        assertEquals("Categoria2", categoryEntity.getName());

        SystemTypeEntity systemTypeEntity = applicationEntity.getSystemType();
        assertEquals(401L, systemTypeEntity.getId());
        assertEquals("Microservice", systemTypeEntity.getName());

        FieldEntity fieldEntity = applicationEntity.getField();
        assertEquals(402L, fieldEntity.getId());
        assertEquals("Educacio", fieldEntity.getName());

        AdmUnitEntity admUnitEntity = applicationEntity.getAdmUnit();
        assertEquals(410L, admUnitEntity.getId());
        assertEquals("ADM-02", admUnitEntity.getCode());

        CommissionEntity commissionEntity = applicationEntity.getCsCommission();
        assertEquals(411L, commissionEntity.getId());
        assertEquals("EXP-2024-02", commissionEntity.getExpedientNumber());
        assertEquals(CommissionType.SUPERIOR, commissionEntity.getCommissionType());
        assertEquals(LocalDate.of(2024, 2, 20), commissionEntity.getApprovalDate());
    }

    @Test
    void toEntity_nullNestedFields_producesNullNestedEntityFields() {
        AppSystem model = new AppSystem();
        model.setId(2L);
        model.setInformationSystemDb(null);
        model.setSystem(null);

        AppSystemEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertNull(entity.getInformationSystemDb());
        assertNull(entity.getSystem());
    }

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    // ------------------------------------------------------------------
    // toResponse
    // ------------------------------------------------------------------

    @Test
    void toResponse_deepModel_flattensEveryNestedFieldIntoOutputDTO() {
        AppSystem model = buildDeepModel();

        AppSystemOutputDTO response = mapper.toResponse(model);

        assertEquals(2L, response.getId());
        assertEquals(LocalDateTime.of(2016, 3, 1, 0, 0), response.getDeletedAt());

        SystemOutputDTO systemOutputDTO = response.getSystem();
        assertEquals(21L, systemOutputDTO.getId());
        assertEquals("invai-instance-dev", systemOutputDTO.getInstance());
        assertEquals(9090, systemOutputDTO.getPort());
        assertEquals("2.0.0", systemOutputDTO.getVersion());
        assertEquals("Dev system", systemOutputDTO.getDescription());

        ServerOutputDTO serverOutputDTO = systemOutputDTO.getServer();
        assertEquals(302L, serverOutputDTO.getId());
        assertEquals("app-server-dev", serverOutputDTO.getName());
        assertEquals(301L, serverOutputDTO.getServerType().getId());

        assertEquals(300L, serverOutputDTO.getEnvironment().getId());
        assertEquals("DEV", serverOutputDTO.getEnvironment().getCode());

        AppInformationSystemDbOutputDTO informationSystemDbOutputDTO = response.getInformationSystemDb();
        assertEquals(11L, informationSystemDbOutputDTO.getId());
        assertEquals("Model observation", informationSystemDbOutputDTO.getObservation());

        ApplicationOutputDTO applicationOutputDTO = informationSystemDbOutputDTO.getApplication();
        assertEquals(420L, applicationOutputDTO.getId());
        assertEquals("APP02", applicationOutputDTO.getCode());
        assertEquals("Application Two", applicationOutputDTO.getName());
        assertEquals(StatusEnum.INACTIVE, applicationOutputDTO.getStatus());
        assertEquals(400L, applicationOutputDTO.getCategory().getId());
        assertEquals(401L, applicationOutputDTO.getSystemType().getId());
        assertEquals(402L, applicationOutputDTO.getField().getId());
        assertEquals(410L, applicationOutputDTO.getAdmUnit().getId());
        assertEquals(411L, applicationOutputDTO.getCsCommission().getId());
        assertEquals(LocalDate.of(2031, 12, 31), applicationOutputDTO.getExpirationDate());
    }

    @Test
    void toResponse_nullNestedFields_leavesResponseNestedFieldsNull() {
        AppSystem model = new AppSystem();
        model.setId(3L);
        model.setInformationSystemDb(null);
        model.setSystem(null);

        AppSystemOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertNull(response.getInformationSystemDb());
        assertNull(response.getSystem());
    }

    @Test
    void toResponse_null_returnsNull() {
        assertNull(mapper.toResponse(null));
    }

    // ------------------------------------------------------------------
    // toModelFromInput / updateModelFromInput
    // ------------------------------------------------------------------

    @Test
    void toModelFromInput_resolvesNestedIdsAndIgnoresAuditFields() {
        AppSystemInputDTO inputDTO = new AppSystemInputDTO(50L, 60L);

        AppSystem model = mapper.toModelFromInput(inputDTO);

        assertEquals(50L, model.getInformationSystemDb().getId());
        assertEquals(60L, model.getSystem().getId());
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
        AppInformationSystemDb existingInformationSystemDb = new AppInformationSystemDb();
        existingInformationSystemDb.setId(13L);
        System existingSystem = new System();
        existingSystem.setId(23L);
        AppSystem existing = new AppSystem();
        existing.setId(4L);
        existing.setInformationSystemDb(existingInformationSystemDb);
        existing.setSystem(existingSystem);
        AppSystemInputDTO inputDTO = new AppSystemInputDTO(70L, 80L);

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals(70L, existing.getInformationSystemDb().getId());
        assertEquals(80L, existing.getSystem().getId());
    }

    @Test
    void updateModelFromInput_withNullNestedTargets_initializesThem() {
        AppSystem existing = new AppSystem();
        existing.setId(5L);
        existing.setInformationSystemDb(null);
        existing.setSystem(null);
        AppSystemInputDTO inputDTO = new AppSystemInputDTO(90L, 91L);

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(5L, existing.getId());
        assertEquals(90L, existing.getInformationSystemDb().getId());
        assertEquals(91L, existing.getSystem().getId());
    }

    @Test
    void updateModelFromInput_nullInput_doesNothing() {
        AppInformationSystemDb existingInformationSystemDb = new AppInformationSystemDb();
        existingInformationSystemDb.setId(13L);
        AppSystem existing = new AppSystem();
        existing.setId(6L);
        existing.setInformationSystemDb(existingInformationSystemDb);

        mapper.updateModelFromInput(null, existing);

        assertEquals(6L, existing.getId());
        assertEquals(13L, existing.getInformationSystemDb().getId());
    }
}
