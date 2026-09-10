package es.caib.invai.back.service.mapper.maintenance.general.field;

import es.caib.invai.back.interna.maintenance.general.field.DTO.FieldInputDTO;
import es.caib.invai.back.interna.maintenance.general.field.DTO.FieldOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.general.field.FieldEntity;
import es.caib.invai.back.service.model.maintenance.general.field.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link FieldMapperImpl}, exercising every conversion direction
 * declared on {@link FieldMapper}.
 */
class FieldMapperTest {

    private FieldMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new FieldMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        FieldEntity entity = new FieldEntity();
        entity.setId(1L);
        entity.setName("Cybersecurity");
        entity.setNameEs("Ciberseguridad");

        Field model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Cybersecurity", model.getName());
        assertEquals("Ciberseguridad", model.getNameEs());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        Field model = new Field();
        model.setId(2L);
        model.setName("Data Science");
        model.setNameEs("Ciencia de Datos");

        FieldEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Data Science", entity.getName());
        assertEquals("Ciencia de Datos", entity.getNameEs());
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        Field model = new Field();
        model.setId(3L);
        model.setName("Networking");
        model.setNameEs("Redes");

        FieldOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("Networking", response.getName());
        assertEquals("Redes", response.getNameEs());
    }

    @Test
    void toModelFromInput_ignoresAuditFieldsAndId() {
        FieldInputDTO inputDTO = new FieldInputDTO("Analytics", "Analitica");

        Field model = mapper.toModelFromInput(inputDTO);

        assertEquals("Analytics", model.getName());
        assertEquals("Analitica", model.getNameEs());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        Field existing = new Field();
        existing.setId(4L);
        existing.setName("Old Name");
        existing.setNameEs("Nombre Antiguo");
        FieldInputDTO inputDTO = new FieldInputDTO("New Name", "Nombre Nuevo");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals("New Name", existing.getName());
        assertEquals("Nombre Nuevo", existing.getNameEs());
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        FieldOutputDTO stub = mapper.map(5L);

        assertEquals(5L, stub.getId());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map(null));
    }
}
