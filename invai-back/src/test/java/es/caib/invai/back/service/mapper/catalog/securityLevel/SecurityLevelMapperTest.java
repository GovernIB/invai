package es.caib.invai.back.service.mapper.catalog.securityLevel;

import es.caib.invai.back.persistence.model.catalog.securityLevel.LkupSecurityLevelEntity;
import es.caib.invai.back.service.model.catalog.securityLevel.SecurityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the generated {@link SecurityLevelMapperImpl}, exercising every conversion
 * direction declared on {@link SecurityLevelMapper}, including its default {@code map(Long)}
 * lookup-stub factory method.
 */
class SecurityLevelMapperTest {

    private SecurityLevelMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SecurityLevelMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        LkupSecurityLevelEntity entity = new LkupSecurityLevelEntity();
        entity.setId(1L);
        entity.setName("Alt");
        entity.setNameEs("Alt");

        SecurityLevel model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Alt", model.getName());
        assertEquals("Alt", model.getNameEs());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        SecurityLevel model = new SecurityLevel();
        model.setId(2L);
        model.setName("Mitja");
        model.setNameEs("Medio");

        LkupSecurityLevelEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Mitja", entity.getName());
        assertEquals("Medio", entity.getNameEs());
    }

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        SecurityLevel model = new SecurityLevel();
        model.setId(3L);
        model.setName("Baix");
        model.setNameEs("Bajo");

        var response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("Baix", response.getName());
        assertEquals("Bajo", response.getNameEs());
    }

    @Test
    void toResponse_null_returnsNull() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void toModelList_mapsEveryEntry() {
        LkupSecurityLevelEntity entity = new LkupSecurityLevelEntity();
        entity.setId(4L);
        entity.setName("Alt");
        entity.setNameEs("Alt");

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
        SecurityLevel model = new SecurityLevel();
        model.setId(5L);
        model.setName("Alt");

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
        SecurityLevel stub = mapper.map(6L);

        assertEquals(6L, stub.getId());
        assertNull(stub.getName());
        assertNull(stub.getNameEs());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map(null));
    }
}
