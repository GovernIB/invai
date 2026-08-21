package es.caib.invai.back.service.mapper.maintenance.systems.databaseVendor;

import es.caib.invai.back.interna.maintenance.systems.databaseVendor.DTO.DatabaseVendorInputDTO;
import es.caib.invai.back.interna.maintenance.systems.databaseVendor.DTO.DatabaseVendorOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.systems.databaseVendor.DatabaseVendorEntity;
import es.caib.invai.back.service.model.maintenance.systems.databaseVendor.DatabaseVendor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for the generated {@link DatabaseVendorMapperImpl}, exercising every conversion
 * direction declared on {@link DatabaseVendorMapper}.
 */
class DatabaseVendorMapperTest {

    private DatabaseVendorMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DatabaseVendorMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        DatabaseVendorEntity entity = new DatabaseVendorEntity();
        entity.setId(1L);
        entity.setName("Oracle");
        entity.setDefaultPort(1521);

        DatabaseVendor model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Oracle", model.getName());
        assertEquals(1521, model.getDefaultPort());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        DatabaseVendor model = new DatabaseVendor();
        model.setId(2L);
        model.setName("PostgreSQL");
        model.setDefaultPort(5432);

        DatabaseVendorEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("PostgreSQL", entity.getName());
        assertEquals(5432, entity.getDefaultPort());
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        DatabaseVendor model = new DatabaseVendor();
        model.setId(3L);
        model.setName("MySQL");
        model.setDefaultPort(3306);

        DatabaseVendorOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("MySQL", response.getName());
        assertEquals(3306, response.getDefaultPort());
    }

    @Test
    void toModelFromInput_ignoresAuditFieldsAndId() {
        DatabaseVendorInputDTO inputDTO = new DatabaseVendorInputDTO();
        inputDTO.setName("SQL Server");
        inputDTO.setDefaultPort(1433);

        DatabaseVendor model = mapper.toModelFromInput(inputDTO);

        assertEquals("SQL Server", model.getName());
        assertEquals(1433, model.getDefaultPort());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        DatabaseVendor existing = new DatabaseVendor();
        existing.setId(4L);
        existing.setName("Old Name");
        existing.setDefaultPort(1111);
        DatabaseVendorInputDTO inputDTO = new DatabaseVendorInputDTO();
        inputDTO.setName("New Name");
        inputDTO.setDefaultPort(2222);

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals("New Name", existing.getName());
        assertEquals(2222, existing.getDefaultPort());
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        DatabaseVendorOutputDTO stub = mapper.map(5L);

        assertEquals(5L, stub.getId());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map((Long) null));
    }

    @Test
    void map_passThroughDomainModel_returnsSameInstance() {
        DatabaseVendor model = new DatabaseVendor();

        assertSame(model, mapper.map(model));
    }
}
