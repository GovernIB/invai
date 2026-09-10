package es.caib.invai.back.service.mapper.maintenance.security.webContext;

import es.caib.invai.back.interna.maintenance.security.webContext.DTO.WebContextInputDTO;
import es.caib.invai.back.interna.maintenance.security.webContext.DTO.WebContextOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.security.webContext.WebContextEntity;
import es.caib.invai.back.service.model.maintenance.security.webContext.WebContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link WebContextMapperImpl}, exercising every conversion direction
 * declared on {@link WebContextMapper}.
 */
class WebContextMapperTest {

    private WebContextMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new WebContextMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        WebContextEntity entity = new WebContextEntity();
        entity.setId(1L);
        entity.setName("Firmar peticiones");
        entity.setNameEs("Firmar peticiones");

        WebContext model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Firmar peticiones", model.getName());
        assertEquals("Firmar peticiones", model.getNameEs());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        WebContext model = new WebContext();
        model.setId(2L);
        model.setName("Acceso a los logs");
        model.setNameEs("Acceso a los logs");

        WebContextEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Acceso a los logs", entity.getName());
        assertEquals("Acceso a los logs", entity.getNameEs());
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        WebContext model = new WebContext();
        model.setId(3L);
        model.setName("Despliegue de aplicaciones");
        model.setNameEs("Despliegue de aplicaciones");

        WebContextOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("Despliegue de aplicaciones", response.getName());
        assertEquals("Despliegue de aplicaciones", response.getNameEs());
    }

    @Test
    void toModelFromInput_ignoresAuditFieldsAndId() {
        WebContextInputDTO inputDTO = new WebContextInputDTO("Acceso a los logs", "Acceso a los logs");

        WebContext model = mapper.toModelFromInput(inputDTO);

        assertEquals("Acceso a los logs", model.getName());
        assertEquals("Acceso a los logs", model.getNameEs());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        WebContext existing = new WebContext();
        existing.setId(4L);
        existing.setName("Old Name");
        existing.setNameEs("Old Name ES");
        WebContextInputDTO inputDTO = new WebContextInputDTO("New Name", "New Name ES");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals("New Name", existing.getName());
        assertEquals("New Name ES", existing.getNameEs());
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        WebContextOutputDTO stub = mapper.map(5L);

        assertEquals(5L, stub.getId());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map(null));
    }
}
