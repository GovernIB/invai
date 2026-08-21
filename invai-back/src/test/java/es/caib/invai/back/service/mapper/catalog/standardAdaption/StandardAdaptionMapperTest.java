package es.caib.invai.back.service.mapper.catalog.standardAdaption;

import es.caib.invai.back.persistence.model.catalog.standardAdaption.LkupStandardAdaptionEntity;
import es.caib.invai.back.service.mapper.catalog.standardAdaption.StandardAdaptionMapper;
import es.caib.invai.back.service.model.catalog.standardAdaption.StandardAdaption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link StandardAdaptionMapperImpl}, exercising every conversion
 * direction declared on {@link StandardAdaptionMapper}, including its default {@code map(Long)}
 * lookup-stub factory method.
 */
class StandardAdaptionMapperTest {

    private StandardAdaptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new StandardAdaptionMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        LkupStandardAdaptionEntity entity = new LkupStandardAdaptionEntity();
        entity.setId(1L);
        entity.setName("Fully adapted");
        entity.setNameEs("Totalmente adaptado");

        StandardAdaption model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Fully adapted", model.getName());
        assertEquals("Totalmente adaptado", model.getNameEs());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        StandardAdaption model = new StandardAdaption();
        model.setId(2L);
        model.setName("Partially adapted");
        model.setNameEs("Parcialmente adaptado");

        LkupStandardAdaptionEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Partially adapted", entity.getName());
        assertEquals("Parcialmente adaptado", entity.getNameEs());
    }

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        StandardAdaption stub = mapper.map(3L);

        assertEquals(3L, stub.getId());
        assertNull(stub.getName());
        assertNull(stub.getNameEs());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map(null));
    }
}
