package es.caib.invai.back.service.mapper.catalog.responsibleType;

import es.caib.invai.back.persistence.model.catalog.responsibleType.LkupResponsibleTypeEntity;
import es.caib.invai.back.service.model.catalog.responsibleType.ResponsibleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the generated {@link ResponsibleTypeMapperImpl}, exercising every conversion
 * direction declared on {@link ResponsibleTypeMapper}, including its default {@code map(Long)}
 * lookup-stub factory method.
 */
class ResponsibleTypeMapperTest {

    private ResponsibleTypeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ResponsibleTypeMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        LkupResponsibleTypeEntity entity = new LkupResponsibleTypeEntity();
        entity.setId(1L);
        entity.setName("Responsable funcional");
        entity.setNameEs("Responsable funcional");
        entity.setRequiresPersonalCaib(true);

        ResponsibleType model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Responsable funcional", model.getName());
        assertEquals("Responsable funcional", model.getNameEs());
        assertTrue(model.isRequiresPersonalCaib());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        ResponsibleType model = new ResponsibleType();
        model.setId(2L);
        model.setName("Responsable tècnic");
        model.setNameEs("Responsable técnico");
        model.setRequiresPersonalCaib(true);

        LkupResponsibleTypeEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Responsable tècnic", entity.getName());
        assertEquals("Responsable técnico", entity.getNameEs());
        assertTrue(entity.isRequiresPersonalCaib());
    }

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        ResponsibleType model = new ResponsibleType();
        model.setId(3L);
        model.setName("Responsable de seguretat");
        model.setNameEs("Responsable de seguridad");
        model.setRequiresPersonalCaib(true);

        var response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("Responsable de seguretat", response.getName());
        assertEquals("Responsable de seguridad", response.getNameEs());
        assertTrue(response.isRequiresPersonalCaib());
    }

    @Test
    void toResponse_null_returnsNull() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void toModelList_mapsEveryEntry() {
        LkupResponsibleTypeEntity entity = new LkupResponsibleTypeEntity();
        entity.setId(4L);
        entity.setName("Responsable funcional");
        entity.setNameEs("Responsable funcional");

        var models = mapper.toModelList(java.util.List.of(entity));

        assertEquals(1, models.size());
        assertEquals(4L, models.get(0).getId());
    }

    @Test
    void toModelList_null_returnsNull() {
        assertNull(mapper.toModelList(null));
    }

    @Test
    void toResponseList_mapsEveryEntry() {
        ResponsibleType model = new ResponsibleType();
        model.setId(5L);
        model.setName("Responsable funcional");

        var responses = mapper.toResponseList(java.util.List.of(model));

        assertEquals(1, responses.size());
        assertEquals(5L, responses.get(0).getId());
    }

    @Test
    void toResponseList_null_returnsNull() {
        assertNull(mapper.toResponseList(null));
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        ResponsibleType stub = mapper.map(6L);

        assertEquals(6L, stub.getId());
        assertNull(stub.getName());
        assertNull(stub.getNameEs());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map(null));
    }
}
