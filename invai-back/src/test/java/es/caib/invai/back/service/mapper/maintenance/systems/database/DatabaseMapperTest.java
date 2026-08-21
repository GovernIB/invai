package es.caib.invai.back.service.mapper.maintenance.systems.database;

import es.caib.invai.back.interna.maintenance.systems.database.DTO.DatabaseInputDTO;
import es.caib.invai.back.interna.maintenance.systems.database.DTO.DatabaseOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.systems.database.DatabaseEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.databaseVendor.DatabaseVendorEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.environment.EnvironmentEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.server.ServerEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.serverType.LkupServerTypeEntity;
import es.caib.invai.back.service.model.maintenance.systems.database.Database;
import es.caib.invai.back.service.model.maintenance.systems.databaseVendor.DatabaseVendor;
import es.caib.invai.back.service.model.maintenance.systems.server.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import es.caib.invai.back.service.mapper.maintenance.systems.databaseVendor.DatabaseVendorMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.systems.environment.EnvironmentMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.systems.server.ServerMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.systems.serverType.ServerTypeMapperImpl;

/**
 * Unit tests for the generated {@link DatabaseMapperImpl}, exercising every conversion direction
 * declared on {@link DatabaseMapper}, including the {@code nullifyDatabaseTypeWhenIdMissing}
 * {@code @AfterMapping} guard.
 */
class DatabaseMapperTest {

    private DatabaseMapper mapper;

    @BeforeEach
    void setUp() {
        DatabaseMapperImpl impl = new DatabaseMapperImpl();

        ServerMapperImpl serverMapperImpl = new ServerMapperImpl();
        ReflectionTestUtils.setField(serverMapperImpl, "environmentMapper", new EnvironmentMapperImpl());
        ReflectionTestUtils.setField(serverMapperImpl, "serverTypeMapper", new ServerTypeMapperImpl());

        ReflectionTestUtils.setField(impl, "serverMapper", serverMapperImpl);
        ReflectionTestUtils.setField(impl, "databaseVendorMapper", new DatabaseVendorMapperImpl());

        mapper = impl;
    }

    @Test
    void toModel_mapsEveryEntityField() {
        EnvironmentEntity environmentEntity = new EnvironmentEntity();
        environmentEntity.setId(1L);
        environmentEntity.setCode("PRO");
        environmentEntity.setName("Producció");

        LkupServerTypeEntity serverTypeEntity = new LkupServerTypeEntity();
        serverTypeEntity.setId(2L);
        serverTypeEntity.setCode("DATABASE");
        serverTypeEntity.setName("Base de dades");

        ServerEntity serverEntity = new ServerEntity();
        serverEntity.setId(10L);
        serverEntity.setName("db-host-01");
        serverEntity.setEnvironment(environmentEntity);
        serverEntity.setServerType(serverTypeEntity);

        DatabaseVendorEntity vendorEntity = new DatabaseVendorEntity();
        vendorEntity.setId(3L);
        vendorEntity.setName("PostgreSQL");
        vendorEntity.setDefaultPort(5432);

        DatabaseEntity entity = new DatabaseEntity();
        entity.setId(100L);
        entity.setServer(serverEntity);
        entity.setService("invai_db");
        entity.setPort(5432);
        entity.setDatabaseType(vendorEntity);
        entity.setDescription("Main database");

        Database model = mapper.toModel(entity);

        assertEquals(100L, model.getId());
        assertEquals("invai_db", model.getService());
        assertEquals(5432, model.getPort());
        assertEquals("Main database", model.getDescription());
        assertNotNull(model.getServer());
        assertEquals(10L, model.getServer().getId());
        assertEquals("db-host-01", model.getServer().getName());
        assertNotNull(model.getDatabaseType());
        assertEquals(3L, model.getDatabaseType().getId());
        assertEquals("PostgreSQL", model.getDatabaseType().getName());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        Server server = new Server();
        server.setId(11L);
        server.setName("db-host-02");

        DatabaseVendor vendor = new DatabaseVendor();
        vendor.setId(4L);
        vendor.setName("MySQL");

        Database model = new Database();
        model.setId(200L);
        model.setServer(server);
        model.setService("other_db");
        model.setPort(3306);
        model.setDatabaseType(vendor);
        model.setDescription("Secondary database");

        DatabaseEntity entity = mapper.toEntity(model);

        assertEquals(200L, entity.getId());
        assertEquals("other_db", entity.getService());
        assertEquals(3306, entity.getPort());
        assertEquals("Secondary database", entity.getDescription());
        assertNotNull(entity.getServer());
        assertEquals(11L, entity.getServer().getId());
        assertNotNull(entity.getDatabaseType());
        assertEquals(4L, entity.getDatabaseType().getId());
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        Server server = new Server();
        server.setId(12L);
        server.setName("db-host-03");

        DatabaseVendor vendor = new DatabaseVendor();
        vendor.setId(5L);
        vendor.setName("Oracle");

        Database model = new Database();
        model.setId(300L);
        model.setServer(server);
        model.setService("reporting_db");
        model.setPort(1521);
        model.setDatabaseType(vendor);
        model.setDescription("Reporting database");

        DatabaseOutputDTO response = mapper.toResponse(model);

        assertEquals(300L, response.getId());
        assertEquals("reporting_db", response.getService());
        assertEquals(1521, response.getPort());
        assertEquals("Reporting database", response.getDescription());
        assertNotNull(response.getServer());
        assertEquals(12L, response.getServer().getId());
        assertNotNull(response.getDatabaseType());
        assertEquals(5L, response.getDatabaseType().getId());
    }

    @Test
    void toResponse_null_returnsNull() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void toModelFromInput_withDatabaseTypeId_setsServerAndDatabaseTypeReferences() {
        DatabaseInputDTO inputDTO = new DatabaseInputDTO();
        inputDTO.setServerId(20L);
        inputDTO.setService("invai_db");
        inputDTO.setPort(5432);
        inputDTO.setDatabaseTypeId(30L);
        inputDTO.setDescription("desc");

        Database model = mapper.toModelFromInput(inputDTO);

        assertEquals("invai_db", model.getService());
        assertEquals(5432, model.getPort());
        assertEquals("desc", model.getDescription());
        assertNotNull(model.getServer());
        assertEquals(20L, model.getServer().getId());
        assertNotNull(model.getDatabaseType());
        assertEquals(30L, model.getDatabaseType().getId());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    /**
     * Documents a real quirk in the generated mapper: the {@code nullifyDatabaseTypeWhenIdMissing}
     * {@code @AfterMapping} guard is only wired onto {@code updateModelFromInput}. Because
     * {@code toModelFromInput} builds its target through the Lombok {@code Database.DatabaseBuilder}
     * (a distinct type from {@code Database}), MapStruct cannot match the {@code @MappingTarget Database}
     * guard signature against the builder variable, so the guard never fires on the create path. The
     * resulting model therefore keeps a non-null {@code DatabaseVendor} stub with a {@code null} id,
     * instead of a fully null {@code databaseType} reference.
     */
    @Test
    void toModelFromInput_nullDatabaseTypeId_leavesNonNullDatabaseVendorStubWithNullId() {
        DatabaseInputDTO inputDTO = new DatabaseInputDTO();
        inputDTO.setServerId(20L);
        inputDTO.setService("invai_db");
        inputDTO.setPort(5432);
        inputDTO.setDatabaseTypeId(null);

        Database model = mapper.toModelFromInput(inputDTO);

        assertNotNull(model.getDatabaseType());
        assertNull(model.getDatabaseType().getId());
    }

    @Test
    void toModelFromInput_null_returnsNull() {
        assertNull(mapper.toModelFromInput(null));
    }

    @Test
    void updateModelFromInput_withDatabaseTypeId_mergesFieldsWithoutTouchingId() {
        Database existing = new Database();
        existing.setId(400L);
        existing.setServer(new Server());
        existing.setDatabaseType(new DatabaseVendor());
        existing.setService("old_db");
        existing.setPort(1000);

        DatabaseInputDTO inputDTO = new DatabaseInputDTO();
        inputDTO.setServerId(50L);
        inputDTO.setService("new_db");
        inputDTO.setPort(2000);
        inputDTO.setDatabaseTypeId(60L);
        inputDTO.setDescription("updated");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(400L, existing.getId());
        assertEquals("new_db", existing.getService());
        assertEquals(2000, existing.getPort());
        assertEquals("updated", existing.getDescription());
        assertEquals(50L, existing.getServer().getId());
        assertNotNull(existing.getDatabaseType());
        assertEquals(60L, existing.getDatabaseType().getId());
    }

    @Test
    void updateModelFromInput_nullDatabaseTypeId_nullifiesDatabaseTypeViaAfterMappingGuard() {
        Database existing = new Database();
        existing.setId(400L);
        existing.setServer(new Server());
        DatabaseVendor existingVendor = new DatabaseVendor();
        existingVendor.setId(99L);
        existing.setDatabaseType(existingVendor);

        DatabaseInputDTO inputDTO = new DatabaseInputDTO();
        inputDTO.setServerId(50L);
        inputDTO.setService("new_db");
        inputDTO.setPort(2000);
        inputDTO.setDatabaseTypeId(null);

        mapper.updateModelFromInput(inputDTO, existing);

        assertNull(existing.getDatabaseType());
    }

    @Test
    void updateModelFromInput_null_doesNothing() {
        Database existing = new Database();
        existing.setId(400L);
        existing.setService("unchanged");

        mapper.updateModelFromInput(null, existing);

        assertEquals(400L, existing.getId());
        assertEquals("unchanged", existing.getService());
    }
}
