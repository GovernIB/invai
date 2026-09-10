package es.caib.invai.back.service.mapper.maintenance.general.systemType;

import es.caib.invai.back.interna.maintenance.general.systemType.DTO.SystemTypeInputDTO;
import es.caib.invai.back.interna.maintenance.general.systemType.DTO.SystemTypeOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.general.systemType.SystemTypeEntity;
import es.caib.invai.back.service.model.maintenance.general.systemType.SystemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link SystemTypeMapperImpl}, exercising every conversion direction
 * declared on {@link SystemTypeMapper}.
 */
class SystemTypeMapperTest {

    private SystemTypeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SystemTypeMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        SystemTypeEntity entity = new SystemTypeEntity();
        entity.setId(1L);
        entity.setName("Microservei REST");
        entity.setNameEs("Microservicio REST");

        SystemType model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Microservei REST", model.getName());
        assertEquals("Microservicio REST", model.getNameEs());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        SystemType model = new SystemType();
        model.setId(2L);
        model.setName("Aplicacio Web");
        model.setNameEs("Aplicacion Web");

        SystemTypeEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Aplicacio Web", entity.getName());
        assertEquals("Aplicacion Web", entity.getNameEs());
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        SystemType model = new SystemType();
        model.setId(3L);
        model.setName("Batch");
        model.setNameEs("Lote");

        SystemTypeOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("Batch", response.getName());
        assertEquals("Lote", response.getNameEs());
    }

    @Test
    void toModelFromInput_ignoresAuditFieldsAndId() {
        SystemTypeInputDTO inputDTO = new SystemTypeInputDTO();
        inputDTO.setName("Analytics");
        inputDTO.setNameEs("Analitica");

        SystemType model = mapper.toModelFromInput(inputDTO);

        assertEquals("Analytics", model.getName());
        assertEquals("Analitica", model.getNameEs());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        SystemType existing = new SystemType();
        existing.setId(4L);
        existing.setName("Old Name");
        existing.setNameEs("Nombre Antiguo");
        SystemTypeInputDTO inputDTO = new SystemTypeInputDTO();
        inputDTO.setName("New Name");
        inputDTO.setNameEs("Nombre Nuevo");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals("New Name", existing.getName());
        assertEquals("Nombre Nuevo", existing.getNameEs());
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        SystemTypeOutputDTO stub = mapper.map(5L);

        assertEquals(5L, stub.getId());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map(null));
    }
}
