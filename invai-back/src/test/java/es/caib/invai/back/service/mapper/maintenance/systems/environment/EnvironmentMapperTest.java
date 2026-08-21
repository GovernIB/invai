package es.caib.invai.back.service.mapper.maintenance.systems.environment;

import es.caib.invai.back.interna.maintenance.systems.environment.DTO.EnvironmentInputDTO;
import es.caib.invai.back.interna.maintenance.systems.environment.DTO.EnvironmentOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.systems.environment.EnvironmentEntity;
import es.caib.invai.back.service.model.maintenance.systems.environment.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for the generated {@link EnvironmentMapperImpl}, exercising every conversion direction
 * declared on {@link EnvironmentMapper}.
 */
class EnvironmentMapperTest {

    private EnvironmentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new EnvironmentMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        EnvironmentEntity entity = new EnvironmentEntity();
        entity.setId(1L);
        entity.setCode("PRO");
        entity.setName("Produccio");
        entity.setNameEs("Produccion");

        Environment model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("PRO", model.getCode());
        assertEquals("Produccio", model.getName());
        assertEquals("Produccion", model.getNameEs());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        Environment model = new Environment();
        model.setId(2L);
        model.setCode("DEV");
        model.setName("Desenvolupament");
        model.setNameEs("Desarrollo");

        EnvironmentEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("DEV", entity.getCode());
        assertEquals("Desenvolupament", entity.getName());
        assertEquals("Desarrollo", entity.getNameEs());
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        Environment model = new Environment();
        model.setId(3L);
        model.setCode("PRE");
        model.setName("Preproduccio");
        model.setNameEs("Preproduccion");

        EnvironmentOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("PRE", response.getCode());
        assertEquals("Preproduccio", response.getName());
        assertEquals("Preproduccion", response.getNameEs());
    }

    @Test
    void toModelFromInput_ignoresAuditFieldsAndId() {
        EnvironmentInputDTO inputDTO = new EnvironmentInputDTO();
        inputDTO.setCode("TST");
        inputDTO.setName("Proves");
        inputDTO.setNameEs("Pruebas");

        Environment model = mapper.toModelFromInput(inputDTO);

        assertEquals("TST", model.getCode());
        assertEquals("Proves", model.getName());
        assertEquals("Pruebas", model.getNameEs());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        Environment existing = new Environment();
        existing.setId(4L);
        existing.setCode("OLD");
        existing.setName("Old Name");
        existing.setNameEs("Nombre Antiguo");
        EnvironmentInputDTO inputDTO = new EnvironmentInputDTO();
        inputDTO.setCode("NEW");
        inputDTO.setName("New Name");
        inputDTO.setNameEs("Nombre Nuevo");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals("NEW", existing.getCode());
        assertEquals("New Name", existing.getName());
        assertEquals("Nombre Nuevo", existing.getNameEs());
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        EnvironmentOutputDTO stub = mapper.map(5L);

        assertEquals(5L, stub.getId());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map((Long) null));
    }

    @Test
    void map_environmentPassThrough_returnsSameInstance() {
        Environment environment = new Environment();

        assertSame(environment, mapper.map(environment));
    }

    @Test
    void map_nullEnvironment_returnsNull() {
        assertNull(mapper.map((Environment) null));
    }
}
