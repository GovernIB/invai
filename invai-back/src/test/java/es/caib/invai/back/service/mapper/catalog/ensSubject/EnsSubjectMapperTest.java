package es.caib.invai.back.service.mapper.catalog.ensSubject;

import es.caib.invai.back.persistence.model.catalog.ensSubject.LkupEnsSubjectEntity;
import es.caib.invai.back.service.model.catalog.ensSubject.EnsSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the generated {@link EnsSubjectMapperImpl}, exercising every conversion
 * direction declared on {@link EnsSubjectMapper}, including its default {@code map(Long)}
 * lookup-stub factory method.
 */
class EnsSubjectMapperTest {

    private EnsSubjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new EnsSubjectMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        LkupEnsSubjectEntity entity = new LkupEnsSubjectEntity();
        entity.setId(1L);
        entity.setName("Si");
        entity.setNameEs("Si");

        EnsSubject model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Si", model.getName());
        assertEquals("Si", model.getNameEs());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        EnsSubject model = new EnsSubject();
        model.setId(2L);
        model.setName("No");
        model.setNameEs("No");

        LkupEnsSubjectEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("No", entity.getName());
        assertEquals("No", entity.getNameEs());
    }

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        EnsSubject model = new EnsSubject();
        model.setId(3L);
        model.setName("Pendent d'analisi");
        model.setNameEs("Pendiente de analisis");

        var response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("Pendent d'analisi", response.getName());
        assertEquals("Pendiente de analisis", response.getNameEs());
    }

    @Test
    void toResponse_null_returnsNull() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void toModelList_mapsEveryEntry() {
        LkupEnsSubjectEntity entity = new LkupEnsSubjectEntity();
        entity.setId(4L);
        entity.setName("Si");
        entity.setNameEs("Si");

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
        EnsSubject model = new EnsSubject();
        model.setId(5L);
        model.setName("Si");

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
        EnsSubject stub = mapper.map(6L);

        assertEquals(6L, stub.getId());
        assertNull(stub.getName());
        assertNull(stub.getNameEs());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map(null));
    }
}
