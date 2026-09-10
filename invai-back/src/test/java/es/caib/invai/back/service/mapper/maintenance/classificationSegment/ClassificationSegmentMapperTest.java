package es.caib.invai.back.service.mapper.maintenance.classificationSegment;

import es.caib.invai.back.interna.maintenance.classificationSegment.DTO.ClassificationSegmentInputDTO;
import es.caib.invai.back.interna.maintenance.classificationSegment.DTO.ClassificationSegmentOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.classificationSegment.ClassificationSegmentEntity;
import es.caib.invai.back.service.model.maintenance.classificationSegment.ClassificationSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link ClassificationSegmentMapperImpl}, exercising every conversion direction
 * declared on {@link ClassificationSegmentMapper}.
 */
class ClassificationSegmentMapperTest {

    private ClassificationSegmentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ClassificationSegmentMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        ClassificationSegmentEntity entity = new ClassificationSegmentEntity();
        entity.setId(1L);
        entity.setName("Segment I");
        entity.setNameEs("Segment I");

        ClassificationSegment model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Segment I", model.getName());
        assertEquals("Segment I", model.getNameEs());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        ClassificationSegment model = new ClassificationSegment();
        model.setId(2L);
        model.setName("Segment II");
        model.setNameEs("Segment II");

        ClassificationSegmentEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Segment II", entity.getName());
        assertEquals("Segment II", entity.getNameEs());
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        ClassificationSegment model = new ClassificationSegment();
        model.setId(3L);
        model.setName("Segment III");
        model.setNameEs("Segment III");

        ClassificationSegmentOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("Segment III", response.getName());
        assertEquals("Segment III", response.getNameEs());
    }

    @Test
    void toModelFromInput_ignoresAuditFieldsAndId() {
        ClassificationSegmentInputDTO inputDTO = new ClassificationSegmentInputDTO("Segment II", "Segment II");

        ClassificationSegment model = mapper.toModelFromInput(inputDTO);

        assertEquals("Segment II", model.getName());
        assertEquals("Segment II", model.getNameEs());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        ClassificationSegment existing = new ClassificationSegment();
        existing.setId(4L);
        existing.setName("Old Name");
        existing.setNameEs("Old Name ES");
        ClassificationSegmentInputDTO inputDTO = new ClassificationSegmentInputDTO("New Name", "New Name ES");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals("New Name", existing.getName());
        assertEquals("New Name ES", existing.getNameEs());
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        ClassificationSegmentOutputDTO stub = mapper.map(5L);

        assertEquals(5L, stub.getId());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map(null));
    }
}
