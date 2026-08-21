package es.caib.invai.back.service.mapper.maintenance.responsible.authorizationType;

import es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO.AuthorizationTypeInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO.AuthorizationTypeOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.responsible.authorizationType.AuthorizationTypeEntity;
import es.caib.invai.back.service.model.maintenance.responsible.authorizationType.AuthorizationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link AuthorizationTypeMapperImpl}, exercising every conversion direction
 * declared on {@link AuthorizationTypeMapper}.
 */
class AuthorizationTypeMapperTest {

    private AuthorizationTypeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AuthorizationTypeMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        AuthorizationTypeEntity entity = new AuthorizationTypeEntity();
        entity.setId(1L);
        entity.setName("Firmar peticiones");
        entity.setNameEs("Firmar peticiones");

        AuthorizationType model = mapper.toModel(entity);

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
        AuthorizationType model = new AuthorizationType();
        model.setId(2L);
        model.setName("Acceso a los logs");
        model.setNameEs("Acceso a los logs");

        AuthorizationTypeEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Acceso a los logs", entity.getName());
        assertEquals("Acceso a los logs", entity.getNameEs());
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        AuthorizationType model = new AuthorizationType();
        model.setId(3L);
        model.setName("Despliegue de aplicaciones");
        model.setNameEs("Despliegue de aplicaciones");

        AuthorizationTypeOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("Despliegue de aplicaciones", response.getName());
        assertEquals("Despliegue de aplicaciones", response.getNameEs());
    }

    @Test
    void toModelFromInput_ignoresAuditFieldsAndId() {
        AuthorizationTypeInputDTO inputDTO = new AuthorizationTypeInputDTO("Acceso a los logs", "Acceso a los logs");

        AuthorizationType model = mapper.toModelFromInput(inputDTO);

        assertEquals("Acceso a los logs", model.getName());
        assertEquals("Acceso a los logs", model.getNameEs());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        AuthorizationType existing = new AuthorizationType();
        existing.setId(4L);
        existing.setName("Old Name");
        existing.setNameEs("Old Name ES");
        AuthorizationTypeInputDTO inputDTO = new AuthorizationTypeInputDTO("New Name", "New Name ES");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals("New Name", existing.getName());
        assertEquals("New Name ES", existing.getNameEs());
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        AuthorizationTypeOutputDTO stub = mapper.map(5L);

        assertEquals(5L, stub.getId());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map((Long) null));
    }
}
