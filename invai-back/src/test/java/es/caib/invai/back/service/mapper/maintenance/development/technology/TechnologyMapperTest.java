package es.caib.invai.back.service.mapper.maintenance.development.technology;

import es.caib.invai.back.interna.maintenance.development.technology.DTO.TechnologyInputDTO;
import es.caib.invai.back.interna.maintenance.development.technology.DTO.TechnologyOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.development.layer.LayerEntity;
import es.caib.invai.back.persistence.model.maintenance.development.technology.TechnologyEntity;
import es.caib.invai.back.service.model.maintenance.development.layer.Layer;
import es.caib.invai.back.service.model.maintenance.development.technology.Technology;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import es.caib.invai.back.service.mapper.maintenance.development.layer.LayerMapperImpl;

/**
 * Unit tests for the generated {@link TechnologyMapperImpl}, exercising every conversion
 * direction declared on {@link TechnologyMapper}.
 * <p>
 * {@link TechnologyMapper} declares {@code uses = {LayerMapper.class}}, so the generated
 * implementation carries an {@code @Autowired} {@link LayerMapper} field. Since no Spring
 * context is bootstrapped here, a real {@link LayerMapperImpl} is injected manually via
 * {@link ReflectionTestUtils} to keep the nested Layer conversions functional.
 * </p>
 */
class TechnologyMapperTest {

    private TechnologyMapper mapper;

    @BeforeEach
    void setUp() {
        TechnologyMapperImpl impl = new TechnologyMapperImpl();
        ReflectionTestUtils.setField(impl, "layerMapper", new LayerMapperImpl());
        mapper = impl;
    }

    @Test
    void toModel_mapsEveryEntityFieldIncludingNestedLayer() {
        LayerEntity layerEntity = new LayerEntity();
        layerEntity.setId(10L);
        layerEntity.setName("Backend");

        TechnologyEntity entity = new TechnologyEntity();
        entity.setId(1L);
        entity.setName("Spring Boot");
        entity.setLayer(layerEntity);

        Technology model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Spring Boot", model.getName());
        assertEquals(10L, model.getLayer().getId());
        assertEquals("Backend", model.getLayer().getName());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelFieldIncludingNestedLayer() {
        Layer layer = new Layer();
        layer.setId(20L);
        layer.setName("Frontend");

        Technology model = new Technology();
        model.setId(2L);
        model.setName("Angular");
        model.setLayer(layer);

        TechnologyEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Angular", entity.getName());
        assertEquals(20L, entity.getLayer().getId());
        assertEquals("Frontend", entity.getLayer().getName());
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTOIncludingNestedLayer() {
        Layer layer = new Layer();
        layer.setId(30L);
        layer.setName("Data");

        Technology model = new Technology();
        model.setId(3L);
        model.setName("PostgreSQL Driver");
        model.setLayer(layer);

        TechnologyOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("PostgreSQL Driver", response.getName());
        assertEquals(30L, response.getLayer().getId());
        assertEquals("Data", response.getLayer().getName());
    }

    @Test
    void toModelFromInput_ignoresIdAndAuditFieldsButMapsLayerIdReference() {
        TechnologyInputDTO inputDTO = new TechnologyInputDTO("React", 40L);

        Technology model = mapper.toModelFromInput(inputDTO);

        assertEquals("React", model.getName());
        assertEquals(40L, model.getLayer().getId());
        assertNull(model.getLayer().getName());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void updateModelFromInput_mergesFieldsAndLayerIdWithoutTouchingId() {
        Layer existingLayer = new Layer();
        existingLayer.setId(50L);
        existingLayer.setName("Untouched Layer Name");

        Technology existing = new Technology();
        existing.setId(4L);
        existing.setName("Old Name");
        existing.setLayer(existingLayer);

        TechnologyInputDTO inputDTO = new TechnologyInputDTO("New Name", 60L);

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals("New Name", existing.getName());
        assertEquals(60L, existing.getLayer().getId());
        assertEquals("Untouched Layer Name", existing.getLayer().getName());
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        TechnologyOutputDTO stub = mapper.map(5L);

        assertEquals(5L, stub.getId());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map((Long) null));
    }
}
