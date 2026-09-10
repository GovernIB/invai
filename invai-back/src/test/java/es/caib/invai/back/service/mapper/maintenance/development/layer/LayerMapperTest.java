package es.caib.invai.back.service.mapper.maintenance.development.layer;

import es.caib.invai.back.interna.maintenance.development.layer.DTO.LayerInputDTO;
import es.caib.invai.back.interna.maintenance.development.layer.DTO.LayerOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.development.layer.LayerEntity;
import es.caib.invai.back.service.model.maintenance.development.layer.Layer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link LayerMapperImpl}, exercising every conversion direction
 * declared on {@link LayerMapper}.
 */
class LayerMapperTest {

    private LayerMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new LayerMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        LayerEntity entity = new LayerEntity();
        entity.setId(1L);
        entity.setName("Presentation");

        Layer model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Presentation", model.getName());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        Layer model = new Layer();
        model.setId(2L);
        model.setName("Business");

        LayerEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Business", entity.getName());
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        Layer model = new Layer();
        model.setId(3L);
        model.setName("Data");

        LayerOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("Data", response.getName());
    }

    @Test
    void toModelFromInput_ignoresAuditFieldsAndId() {
        LayerInputDTO inputDTO = new LayerInputDTO("Infrastructure");

        Layer model = mapper.toModelFromInput(inputDTO);

        assertEquals("Infrastructure", model.getName());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        Layer existing = new Layer();
        existing.setId(4L);
        existing.setName("Old Name");
        LayerInputDTO inputDTO = new LayerInputDTO("New Name");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals("New Name", existing.getName());
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        LayerOutputDTO stub = mapper.map(5L);

        assertEquals(5L, stub.getId());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map(null));
    }
}
