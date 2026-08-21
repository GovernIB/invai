package es.caib.invai.back.service.mapper.maintenance.systems.serverType;

import es.caib.invai.back.interna.maintenance.systems.serverType.DTO.ServerTypeOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.systems.serverType.LkupServerTypeEntity;
import es.caib.invai.back.service.model.maintenance.systems.serverType.ServerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link ServerTypeMapperImpl}, exercising every conversion
 * direction declared on {@link ServerTypeMapper}.
 */
class ServerTypeMapperTest {

    private ServerTypeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ServerTypeMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        LkupServerTypeEntity entity = new LkupServerTypeEntity();
        entity.setId(1L);
        entity.setCode("DATABASE");
        entity.setName("Base de dades");
        entity.setNameEs("Base de datos");

        ServerType model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("DATABASE", model.getCode());
        assertEquals("Base de dades", model.getName());
        assertEquals("Base de datos", model.getNameEs());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        ServerType model = new ServerType();
        model.setId(2L);
        model.setCode("APPLICATION");
        model.setName("Aplicació");
        model.setNameEs("Aplicación");

        LkupServerTypeEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("APPLICATION", entity.getCode());
        assertEquals("Aplicació", entity.getName());
        assertEquals("Aplicación", entity.getNameEs());
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        ServerType model = new ServerType();
        model.setId(3L);
        model.setCode("WEB");
        model.setName("Web");
        model.setNameEs("Web");

        ServerTypeOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("WEB", response.getCode());
        assertEquals("Web", response.getName());
        assertEquals("Web", response.getNameEs());
    }

    @Test
    void toResponse_null_returnsNull() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void toModelList_mapsEveryEntry() {
        LkupServerTypeEntity entity = new LkupServerTypeEntity();
        entity.setId(4L);
        entity.setCode("DATABASE");
        entity.setName("Base de dades");
        entity.setNameEs("Base de datos");

        List<ServerType> models = mapper.toModelList(List.of(entity));

        assertEquals(1, models.size());
        assertEquals(4L, models.get(0).getId());
    }

    @Test
    void toModelList_null_returnsNull() {
        assertNull(mapper.toModelList(null));
    }

    @Test
    void toResponseList_mapsEveryEntry() {
        ServerType model = new ServerType();
        model.setId(5L);
        model.setCode("DATABASE");

        List<ServerTypeOutputDTO> responses = mapper.toResponseList(List.of(model));

        assertEquals(1, responses.size());
        assertEquals(5L, responses.get(0).getId());
    }

    @Test
    void toResponseList_null_returnsNull() {
        assertNull(mapper.toResponseList(null));
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        ServerType stub = mapper.map(6L);

        assertEquals(6L, stub.getId());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map((Long) null));
    }
}
