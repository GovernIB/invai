package es.caib.invai.back.service.mapper.maintenance.security.identityProvider;

import es.caib.invai.back.interna.maintenance.security.identityProvider.DTO.IdentityProviderInputDTO;
import es.caib.invai.back.interna.maintenance.security.identityProvider.DTO.IdentityProviderOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.security.identityProvider.IdentityProviderEntity;
import es.caib.invai.back.service.model.maintenance.security.identityProvider.IdentityProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link IdentityProviderMapperImpl}, exercising every conversion direction
 * declared on {@link IdentityProviderMapper}.
 */
class IdentityProviderMapperTest {

    private IdentityProviderMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new IdentityProviderMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        IdentityProviderEntity entity = new IdentityProviderEntity();
        entity.setId(1L);
        entity.setName("Cl@ve");

        IdentityProvider model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Cl@ve", model.getName());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        IdentityProvider model = new IdentityProvider();
        model.setId(2L);
        model.setName("Keycloak CAIB");

        IdentityProviderEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Keycloak CAIB", entity.getName());
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        IdentityProvider model = new IdentityProvider();
        model.setId(3L);
        model.setName("Cl@ve Signatura");

        IdentityProviderOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("Cl@ve Signatura", response.getName());
    }

    @Test
    void toModelFromInput_ignoresAuditFieldsAndId() {
        IdentityProviderInputDTO inputDTO = new IdentityProviderInputDTO("Keycloak CAIB");

        IdentityProvider model = mapper.toModelFromInput(inputDTO);

        assertEquals("Keycloak CAIB", model.getName());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        IdentityProvider existing = new IdentityProvider();
        existing.setId(4L);
        existing.setName("Old Name");
        IdentityProviderInputDTO inputDTO = new IdentityProviderInputDTO("New Name");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals("New Name", existing.getName());
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        IdentityProviderOutputDTO stub = mapper.map(5L);

        assertEquals(5L, stub.getId());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map(null));
    }
}
