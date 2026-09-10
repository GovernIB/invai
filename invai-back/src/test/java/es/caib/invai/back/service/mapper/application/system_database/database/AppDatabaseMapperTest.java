package es.caib.invai.back.service.mapper.application.system_database.database;

import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import es.caib.invai.back.interna.application.system_database.core.DTO.AppInformationSystemDbOutputDTO;
import es.caib.invai.back.interna.application.system_database.database.DTO.AppDatabaseInputDTO;
import es.caib.invai.back.interna.application.system_database.database.DTO.AppDatabaseOutputDTO;
import es.caib.invai.back.interna.maintenance.systems.database.DTO.DatabaseOutputDTO;
import es.caib.invai.back.interna.maintenance.systems.databaseVendor.DTO.DatabaseVendorOutputDTO;
import es.caib.invai.back.interna.maintenance.systems.server.DTO.ServerOutputDTO;
import es.caib.invai.back.persistence.model.application.system_database.database.AppDatabaseEntity;
import es.caib.invai.back.persistence.model.application.system_database.core.AppInformationSystemDbEntity;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.maintenance.general.category.CategoryEntity;
import es.caib.invai.back.persistence.model.maintenance.general.commission.CommissionEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.database.DatabaseEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.databaseVendor.DatabaseVendorEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.environment.EnvironmentEntity;
import es.caib.invai.back.persistence.model.maintenance.general.field.FieldEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.server.ServerEntity;
import es.caib.invai.back.persistence.model.maintenance.general.systemType.SystemTypeEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.serverType.LkupServerTypeEntity;
import es.caib.invai.back.persistence.model.catalog.status.LkupStatusEntity;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapper;
import es.caib.invai.back.service.model.application.system_database.database.AppDatabase;
import es.caib.invai.back.service.model.application.system_database.core.AppInformationSystemDb;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.maintenance.general.category.Category;
import es.caib.invai.back.service.model.maintenance.general.commission.Commission;
import es.caib.invai.back.service.model.maintenance.general.commission.CommissionType;
import es.caib.invai.back.service.model.maintenance.systems.database.Database;
import es.caib.invai.back.service.model.maintenance.systems.databaseVendor.DatabaseVendor;
import es.caib.invai.back.service.model.maintenance.systems.environment.Environment;
import es.caib.invai.back.service.model.maintenance.general.field.Field;
import es.caib.invai.back.service.model.maintenance.systems.server.Server;
import es.caib.invai.back.service.model.maintenance.systems.serverType.ServerType;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import es.caib.invai.back.service.model.maintenance.general.systemType.SystemType;
import org.jspecify.annotations.NonNull;
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
 * Unit tests for the generated {@link AppDatabaseMapperImpl}, exercising every conversion
 * direction declared on {@link AppDatabaseMapper}.
 * <p>
 * {@link AppDatabaseMapper} declares {@code uses = {StatusMapper.class, ApplicationMapper.class}},
 * so the generated implementation carries an {@code @Autowired} {@link ApplicationMapper} field used
 * internally by the nested {@code Application -> ApplicationEntity} conversions. Since no Spring
 * context is bootstrapped here, a fully wired {@link ApplicationMapperImpl} (with its own sub-mappers,
 * including {@link StatusMapperImpl}) is injected manually via {@link ReflectionTestUtils}, following
 * the same pattern used in {@code TechnologyMapperTest}.
 * </p>
 * <p>
 * Because {@link AppDatabaseEntity} carries a deep object graph
 * (AppDatabase -&gt; Database -&gt; Server -&gt; Environment / ServerType, and
 * AppDatabase -&gt; AppInformationSystemDb -&gt; Application -&gt; Category / SystemType / Field /
 * AdmUnit / Commission / Status), the generated mapper implementation contains many private
 * helper methods that only execute when the corresponding nested entity is non-null. The
 * "fully populated" tests below deliberately populate every level of that graph to exercise
 * those helpers, while the "null nested FK" tests confirm the alternate null-handling branches
 * behave safely.
 * </p>
 */
class AppDatabaseMapperTest {

    private AppDatabaseMapper mapper;

    @BeforeEach
    void setUp() {
        ApplicationMapperImpl applicationMapperImpl = new ApplicationMapperImpl();
        ReflectionTestUtils.setField(applicationMapperImpl, "categoryMapper", new CategoryMapperImpl());
        ReflectionTestUtils.setField(applicationMapperImpl, "systemTypeMapper", new SystemTypeMapperImpl());
        ReflectionTestUtils.setField(applicationMapperImpl, "fieldMapper", new FieldMapperImpl());
        ReflectionTestUtils.setField(applicationMapperImpl, "commissionMapper", new CommissionMapperImpl());
        ReflectionTestUtils.setField(applicationMapperImpl, "statusMapper", new StatusMapperImpl());

        AppDatabaseMapperImpl impl = new AppDatabaseMapperImpl();
        ReflectionTestUtils.setField(impl, "applicationMapper", applicationMapperImpl);
        mapper = impl;
    }

    // ------------------------------------------------------------------
    // Entity graph builders
    // ------------------------------------------------------------------

    private AppDatabaseEntity buildDeepEntity() {
        EnvironmentEntity environmentEntity = getEnvironmentEntity();

        ServerEntity serverEntity = getServerEntity(environmentEntity);

        DatabaseVendorEntity databaseVendorEntity = getDatabaseVendorEntity();

        DatabaseEntity databaseEntity = getDatabaseEntity(serverEntity, databaseVendorEntity);

        CategoryEntity categoryEntity = getCategoryEntity();

        SystemTypeEntity systemTypeEntity = getSystemTypeEntity();

        FieldEntity fieldEntity = getFieldEntity();

        ApplicationEntity applicationEntity = getApplicationEntity(categoryEntity, systemTypeEntity, fieldEntity);

        AppInformationSystemDbEntity informationSystemDbEntity = getAppInformationSystemDbEntity(applicationEntity);

        return getAppDatabaseEntity(informationSystemDbEntity, databaseEntity);
    }

    private static @NonNull AppDatabaseEntity getAppDatabaseEntity(AppInformationSystemDbEntity informationSystemDbEntity, DatabaseEntity databaseEntity) {
        AppDatabaseEntity entity = new AppDatabaseEntity();
        entity.setId(1L);
        entity.setInformationSystemDb(informationSystemDbEntity);
        entity.setDatabase(databaseEntity);
        entity.setCreatedAt(LocalDateTime.of(2017, 1, 1, 0, 0));
        entity.setCreatedBy("appdb-creator");
        entity.setUpdatedAt(LocalDateTime.of(2017, 2, 1, 0, 0));
        entity.setUpdatedBy("appdb-updater");
        entity.setDeletedAt(LocalDateTime.of(2017, 3, 1, 0, 0));
        entity.setDeletedBy("appdb-deleter");
        return entity;
    }

    private static @NonNull AppInformationSystemDbEntity getAppInformationSystemDbEntity(ApplicationEntity applicationEntity) {
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
        return informationSystemDbEntity;
    }

    private static @NonNull FieldEntity getFieldEntity() {
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
        return fieldEntity;
    }

    private static @NonNull SystemTypeEntity getSystemTypeEntity() {
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
        return systemTypeEntity;
    }

    private static @NonNull CategoryEntity getCategoryEntity() {
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
        return categoryEntity;
    }

    private static @NonNull DatabaseVendorEntity getDatabaseVendorEntity() {
        DatabaseVendorEntity databaseVendorEntity = new DatabaseVendorEntity();
        databaseVendorEntity.setId(103L);
        databaseVendorEntity.setName("PostgreSQL");
        databaseVendorEntity.setDefaultPort(5432);
        databaseVendorEntity.setCreatedAt(LocalDateTime.of(2020, 1, 3, 0, 0));
        databaseVendorEntity.setCreatedBy("vendor-creator");
        databaseVendorEntity.setUpdatedAt(LocalDateTime.of(2020, 2, 3, 0, 0));
        databaseVendorEntity.setUpdatedBy("vendor-updater");
        databaseVendorEntity.setDeletedAt(LocalDateTime.of(2020, 3, 3, 0, 0));
        databaseVendorEntity.setDeletedBy("vendor-deleter");
        return databaseVendorEntity;
    }

    private static @NonNull ApplicationEntity getApplicationEntity(CategoryEntity categoryEntity, SystemTypeEntity systemTypeEntity, FieldEntity fieldEntity) {
        CommissionEntity commissionEntity = getCommissionEntity();

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
        applicationEntity.setAdmUnitCode("ADM-01");
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
        return applicationEntity;
    }

    private static @NonNull EnvironmentEntity getEnvironmentEntity() {
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
        return environmentEntity;
    }

    private static @NonNull CommissionEntity getCommissionEntity() {
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
        return commissionEntity;
    }

    private static @NonNull DatabaseEntity getDatabaseEntity(ServerEntity serverEntity, DatabaseVendorEntity databaseVendorEntity) {
        DatabaseEntity databaseEntity = new DatabaseEntity();
        databaseEntity.setId(20L);
        databaseEntity.setServer(serverEntity);
        databaseEntity.setService("invai-service");
        databaseEntity.setPort(5432);
        databaseEntity.setDatabaseType(databaseVendorEntity);
        databaseEntity.setDescription("Main invai database");
        databaseEntity.setCreatedAt(LocalDateTime.of(2020, 1, 4, 0, 0));
        databaseEntity.setCreatedBy("db-creator");
        databaseEntity.setUpdatedAt(LocalDateTime.of(2020, 2, 4, 0, 0));
        databaseEntity.setUpdatedBy("db-updater");
        databaseEntity.setDeletedAt(LocalDateTime.of(2020, 3, 4, 0, 0));
        databaseEntity.setDeletedBy("db-deleter");
        return databaseEntity;
    }

    private static @NonNull ServerEntity getServerEntity(EnvironmentEntity environmentEntity) {
        LkupServerTypeEntity serverTypeEntity = new LkupServerTypeEntity();
        serverTypeEntity.setId(101L);
        serverTypeEntity.setCode("DATABASE");
        serverTypeEntity.setName("Base de dades");
        serverTypeEntity.setNameEs("Base de datos");

        ServerEntity serverEntity = new ServerEntity();
        serverEntity.setId(102L);
        serverEntity.setName("db-server-01");
        serverEntity.setEnvironment(environmentEntity);
        serverEntity.setServerType(serverTypeEntity);
        serverEntity.setCreatedAt(LocalDateTime.of(2020, 1, 2, 0, 0));
        serverEntity.setCreatedBy("srv-creator");
        serverEntity.setUpdatedAt(LocalDateTime.of(2020, 2, 2, 0, 0));
        serverEntity.setUpdatedBy("srv-updater");
        serverEntity.setDeletedAt(LocalDateTime.of(2020, 3, 2, 0, 0));
        serverEntity.setDeletedBy("srv-deleter");
        return serverEntity;
    }

    private AppDatabase buildDeepModel() {
        Environment environment = getEnvironment();

        Server server = getServer(environment);

        DatabaseVendor databaseVendor = getDatabaseVendor();

        Database database = getDatabase(server, databaseVendor);

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

        SystemType systemType = getSystemType();

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

        Application application = getApplication(category, systemType, field);

        AppInformationSystemDb informationSystemDb = getAppInformationSystemDb(application);

        AppDatabase model = new AppDatabase();
        model.setId(2L);
        model.setInformationSystemDb(informationSystemDb);
        model.setDatabase(database);
        model.setCreatedAt(LocalDateTime.of(2016, 1, 1, 0, 0));
        model.setCreatedBy("appdb-creator");
        model.setUpdatedAt(LocalDateTime.of(2016, 2, 1, 0, 0));
        model.setUpdatedBy("appdb-updater");
        model.setDeletedAt(LocalDateTime.of(2016, 3, 1, 0, 0));
        model.setDeletedBy("appdb-deleter");

        return model;
    }

    private static @NonNull AppInformationSystemDb getAppInformationSystemDb(Application application) {
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
        return informationSystemDb;
    }

    private static @NonNull SystemType getSystemType() {
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
        return systemType;
    }

    private static @NonNull DatabaseVendor getDatabaseVendor() {
        DatabaseVendor databaseVendor = new DatabaseVendor();
        databaseVendor.setId(303L);
        databaseVendor.setName("Oracle");
        databaseVendor.setDefaultPort(1521);
        databaseVendor.setCreatedAt(LocalDateTime.of(2021, 1, 3, 0, 0));
        databaseVendor.setCreatedBy("vendor-creator");
        databaseVendor.setUpdatedAt(LocalDateTime.of(2021, 2, 3, 0, 0));
        databaseVendor.setUpdatedBy("vendor-updater");
        databaseVendor.setDeletedAt(LocalDateTime.of(2021, 3, 3, 0, 0));
        databaseVendor.setDeletedBy("vendor-deleter");
        return databaseVendor;
    }

    private static @NonNull Environment getEnvironment() {
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
        return environment;
    }

    private static @NonNull Database getDatabase(Server server, DatabaseVendor databaseVendor) {
        Database database = new Database();
        database.setId(21L);
        database.setServer(server);
        database.setService("invai-service-dev");
        database.setPort(1521);
        database.setDatabaseType(databaseVendor);
        database.setDescription("Dev database");
        database.setCreatedAt(LocalDateTime.of(2021, 1, 4, 0, 0));
        database.setCreatedBy("db-creator");
        database.setUpdatedAt(LocalDateTime.of(2021, 2, 4, 0, 0));
        database.setUpdatedBy("db-updater");
        database.setDeletedAt(LocalDateTime.of(2021, 3, 4, 0, 0));
        database.setDeletedBy("db-deleter");
        return database;
    }

    private static @NonNull Server getServer(Environment environment) {
        ServerType serverType = new ServerType();
        serverType.setId(301L);
        serverType.setCode("DATABASE");
        serverType.setName("Base de dades");
        serverType.setNameEs("Base de datos");

        Server server = new Server();
        server.setId(302L);
        server.setName("db-server-dev");
        server.setEnvironment(environment);
        server.setServerType(serverType);
        server.setCreatedAt(LocalDateTime.of(2021, 1, 2, 0, 0));
        server.setCreatedBy("srv-creator");
        server.setUpdatedAt(LocalDateTime.of(2021, 2, 2, 0, 0));
        server.setUpdatedBy("srv-updater");
        server.setDeletedAt(LocalDateTime.of(2021, 3, 2, 0, 0));
        server.setDeletedBy("srv-deleter");
        return server;
    }

    private static @NonNull Application getApplication(Category category, SystemType systemType, Field field) {
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
        application.setAdmUnitCode("ADM-02");
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
        return application;
    }

    // ------------------------------------------------------------------
    // toModel
    // ------------------------------------------------------------------

    @Test
    void toModel_deepEntityGraph_mapsEveryNestedField() {
        AppDatabaseEntity entity = buildDeepEntity();

        AppDatabase model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("appdb-creator", model.getCreatedBy());
        assertEquals("appdb-updater", model.getUpdatedBy());
        assertEquals("appdb-deleter", model.getDeletedBy());
        assertEquals(LocalDateTime.of(2017, 1, 1, 0, 0), model.getCreatedAt());
        assertEquals(LocalDateTime.of(2017, 2, 1, 0, 0), model.getUpdatedAt());
        assertEquals(LocalDateTime.of(2017, 3, 1, 0, 0), model.getDeletedAt());

        Database database = model.getDatabase();
        assertEquals(20L, database.getId());
        assertEquals("invai-service", database.getService());
        assertEquals(5432, database.getPort());
        assertEquals("Main invai database", database.getDescription());
        assertEquals("db-creator", database.getCreatedBy());

        DatabaseVendor databaseVendor = database.getDatabaseType();
        assertEquals(103L, databaseVendor.getId());
        assertEquals("PostgreSQL", databaseVendor.getName());
        assertEquals(5432, databaseVendor.getDefaultPort());
        assertEquals("vendor-creator", databaseVendor.getCreatedBy());

        Server server = database.getServer();
        assertEquals(102L, server.getId());
        assertEquals("db-server-01", server.getName());
        assertEquals("srv-creator", server.getCreatedBy());

        Environment environment = server.getEnvironment();
        assertEquals(100L, environment.getId());
        assertEquals("PRO", environment.getCode());
        assertEquals("Produccio", environment.getName());
        assertEquals("Produccion", environment.getNameEs());
        assertEquals("env-creator", environment.getCreatedBy());

        ServerType serverType = server.getServerType();
        assertEquals(101L, serverType.getId());
        assertEquals("DATABASE", serverType.getCode());
        assertEquals("Base de dades", serverType.getName());
        assertEquals("Base de datos", serverType.getNameEs());

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

        assertEquals("ADM-01", application.getAdmUnitCode());

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
        AppDatabaseEntity entity = new AppDatabaseEntity();
        entity.setId(1L);
        entity.setInformationSystemDb(null);
        entity.setDatabase(null);

        AppDatabase model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertNull(model.getInformationSystemDb());
        assertNull(model.getDatabase());
    }

    @Test
    void toModel_applicationWithNullNestedCatalogReferences_leavesApplicationNestedFieldsNull() {
        AppDatabaseEntity entity = getAppDatabaseEntity();

        AppDatabase model = mapper.toModel(entity);

        Application application = model.getInformationSystemDb().getApplication();
        assertEquals(205L, application.getId());
        assertNull(application.getCategory());
        assertNull(application.getSystemType());
        assertNull(application.getField());
        assertNull(application.getAdmUnitCode());
        assertNull(application.getCsCommission());
        assertNull(application.getStatus());
    }

    private static @NonNull AppDatabaseEntity getAppDatabaseEntity() {
        AppInformationSystemDbEntity informationSystemDbEntity = new AppInformationSystemDbEntity();
        informationSystemDbEntity.setId(10L);
        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(205L);
        // category, systemType, field, admUnit, csCommission, status all left null
        informationSystemDbEntity.setApplication(applicationEntity);

        AppDatabaseEntity entity = new AppDatabaseEntity();
        entity.setId(1L);
        entity.setInformationSystemDb(informationSystemDbEntity);
        entity.setDatabase(null);
        return entity;
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
        AppDatabase model = buildDeepModel();

        AppDatabaseEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("appdb-creator", entity.getCreatedBy());
        assertEquals(LocalDateTime.of(2016, 1, 1, 0, 0), entity.getCreatedAt());

        DatabaseEntity databaseEntity = entity.getDatabase();
        assertEquals(21L, databaseEntity.getId());
        assertEquals("invai-service-dev", databaseEntity.getService());
        assertEquals(1521, databaseEntity.getPort());
        assertEquals("Dev database", databaseEntity.getDescription());

        DatabaseVendorEntity databaseVendorEntity = databaseEntity.getDatabaseType();
        assertEquals(303L, databaseVendorEntity.getId());
        assertEquals("Oracle", databaseVendorEntity.getName());
        assertEquals(1521, databaseVendorEntity.getDefaultPort());

        ServerEntity serverEntity = databaseEntity.getServer();
        assertEquals(302L, serverEntity.getId());
        assertEquals("db-server-dev", serverEntity.getName());

        EnvironmentEntity environmentEntity = serverEntity.getEnvironment();
        assertEquals(300L, environmentEntity.getId());
        assertEquals("DEV", environmentEntity.getCode());
        assertEquals("Desenvolupament", environmentEntity.getName());
        assertEquals("Desarrollo", environmentEntity.getNameEs());

        LkupServerTypeEntity serverTypeEntity = serverEntity.getServerType();
        assertEquals(301L, serverTypeEntity.getId());
        assertEquals("DATABASE", serverTypeEntity.getCode());

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

        assertEquals("ADM-02", applicationEntity.getAdmUnitCode());

        CommissionEntity commissionEntity = applicationEntity.getCsCommission();
        assertEquals(411L, commissionEntity.getId());
        assertEquals("EXP-2024-02", commissionEntity.getExpedientNumber());
        assertEquals(CommissionType.SUPERIOR, commissionEntity.getCommissionType());
        assertEquals(LocalDate.of(2024, 2, 20), commissionEntity.getApprovalDate());
    }

    @Test
    void toEntity_nullNestedFields_producesNullNestedEntityFields() {
        AppDatabase model = new AppDatabase();
        model.setId(2L);
        model.setInformationSystemDb(null);
        model.setDatabase(null);

        AppDatabaseEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertNull(entity.getInformationSystemDb());
        assertNull(entity.getDatabase());
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
        AppDatabase model = buildDeepModel();

        AppDatabaseOutputDTO response = mapper.toResponse(model);

        assertEquals(2L, response.getId());
        assertEquals(LocalDateTime.of(2016, 3, 1, 0, 0), response.getDeletedAt());

        DatabaseOutputDTO databaseOutputDTO = response.getDatabase();
        assertEquals(21L, databaseOutputDTO.getId());
        assertEquals("invai-service-dev", databaseOutputDTO.getService());
        assertEquals(1521, databaseOutputDTO.getPort());
        assertEquals("Dev database", databaseOutputDTO.getDescription());

        DatabaseVendorOutputDTO databaseVendorOutputDTO = databaseOutputDTO.getDatabaseType();
        assertEquals(303L, databaseVendorOutputDTO.getId());
        assertEquals("Oracle", databaseVendorOutputDTO.getName());
        assertEquals(1521, databaseVendorOutputDTO.getDefaultPort());

        ServerOutputDTO serverOutputDTO = databaseOutputDTO.getServer();
        assertEquals(302L, serverOutputDTO.getId());
        assertEquals("db-server-dev", serverOutputDTO.getName());
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
        assertNull(applicationOutputDTO.getAdmUnit());
        assertEquals(411L, applicationOutputDTO.getCsCommission().getId());
        assertEquals(LocalDate.of(2031, 12, 31), applicationOutputDTO.getExpirationDate());
    }

    @Test
    void toResponse_nullNestedFields_leavesResponseNestedFieldsNull() {
        AppDatabase model = new AppDatabase();
        model.setId(3L);
        model.setInformationSystemDb(null);
        model.setDatabase(null);

        AppDatabaseOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertNull(response.getInformationSystemDb());
        assertNull(response.getDatabase());
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
        AppDatabaseInputDTO inputDTO = new AppDatabaseInputDTO(50L, 60L);

        AppDatabase model = mapper.toModelFromInput(inputDTO);

        assertEquals(50L, model.getInformationSystemDb().getId());
        assertEquals(60L, model.getDatabase().getId());
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
        Database existingDatabase = new Database();
        existingDatabase.setId(23L);
        AppDatabase existing = new AppDatabase();
        existing.setId(4L);
        existing.setInformationSystemDb(existingInformationSystemDb);
        existing.setDatabase(existingDatabase);
        AppDatabaseInputDTO inputDTO = new AppDatabaseInputDTO(70L, 80L);

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals(70L, existing.getInformationSystemDb().getId());
        assertEquals(80L, existing.getDatabase().getId());
    }

    @Test
    void updateModelFromInput_withNullNestedTargets_initializesThem() {
        AppDatabase existing = new AppDatabase();
        existing.setId(5L);
        existing.setInformationSystemDb(null);
        existing.setDatabase(null);
        AppDatabaseInputDTO inputDTO = new AppDatabaseInputDTO(90L, 91L);

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(5L, existing.getId());
        assertEquals(90L, existing.getInformationSystemDb().getId());
        assertEquals(91L, existing.getDatabase().getId());
    }

    @Test
    void updateModelFromInput_nullInput_doesNothing() {
        AppInformationSystemDb existingInformationSystemDb = new AppInformationSystemDb();
        existingInformationSystemDb.setId(13L);
        AppDatabase existing = new AppDatabase();
        existing.setId(6L);
        existing.setInformationSystemDb(existingInformationSystemDb);

        mapper.updateModelFromInput(null, existing);

        assertEquals(6L, existing.getId());
        assertEquals(13L, existing.getInformationSystemDb().getId());
    }
}
