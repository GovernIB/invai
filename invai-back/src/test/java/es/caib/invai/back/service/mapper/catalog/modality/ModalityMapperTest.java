package es.caib.invai.back.service.mapper.catalog.modality;

import es.caib.invai.back.persistence.model.catalog.modality.LkupModalityEntity;
import es.caib.invai.back.service.mapper.catalog.modality.ModalityMapper;
import es.caib.invai.back.service.model.catalog.modality.Modality;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link ModalityMapperImpl}, exercising every conversion
 * direction declared on {@link ModalityMapper}, including its default {@code map(Long)}
 * lookup-stub factory method.
 */
class ModalityMapperTest {

    private ModalityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ModalityMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        LkupModalityEntity entity = new LkupModalityEntity();
        entity.setId(1L);
        entity.setName("Internal development");
        entity.setNameEs("Desarrollo interno");

        Modality model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Internal development", model.getName());
        assertEquals("Desarrollo interno", model.getNameEs());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        Modality model = new Modality();
        model.setId(2L);
        model.setName("Outsourced development");
        model.setNameEs("Desarrollo externalizado");

        LkupModalityEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Outsourced development", entity.getName());
        assertEquals("Desarrollo externalizado", entity.getNameEs());
    }

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        Modality stub = mapper.map(3L);

        assertEquals(3L, stub.getId());
        assertNull(stub.getName());
        assertNull(stub.getNameEs());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map(null));
    }
}
