package es.caib.invai.back.service.mapper.maintenance.development.providerRole;

import es.caib.invai.back.interna.maintenance.development.providerRole.DTO.ProviderRoleInputDTO;
import es.caib.invai.back.interna.maintenance.development.providerRole.DTO.ProviderRoleOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.development.providerRole.ProviderRoleEntity;
import es.caib.invai.back.service.model.maintenance.development.providerRole.ProviderRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link ProviderRoleMapperImpl}, exercising every conversion direction
 * declared on {@link ProviderRoleMapper}.
 */
class ProviderRoleMapperTest {

    private ProviderRoleMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProviderRoleMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        ProviderRoleEntity entity = new ProviderRoleEntity();
        entity.setId(1L);
        entity.setName("DevOps Lead");
        entity.setNameEs("Responsable DevOps");

        ProviderRole model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("DevOps Lead", model.getName());
        assertEquals("Responsable DevOps", model.getNameEs());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        ProviderRole model = new ProviderRole();
        model.setId(2L);
        model.setName("QA Lead");
        model.setNameEs("Responsable QA");

        ProviderRoleEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("QA Lead", entity.getName());
        assertEquals("Responsable QA", entity.getNameEs());
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        ProviderRole model = new ProviderRole();
        model.setId(3L);
        model.setName("Reviewer");
        model.setNameEs("Revisor");

        ProviderRoleOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("Reviewer", response.getName());
        assertEquals("Revisor", response.getNameEs());
    }

    @Test
    void toModelFromInput_ignoresAuditFieldsAndId() {
        ProviderRoleInputDTO inputDTO = new ProviderRoleInputDTO("Analyst", "Analista");

        ProviderRole model = mapper.toModelFromInput(inputDTO);

        assertEquals("Analyst", model.getName());
        assertEquals("Analista", model.getNameEs());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        ProviderRole existing = new ProviderRole();
        existing.setId(4L);
        existing.setName("Old Name");
        existing.setNameEs("Nombre Antiguo");
        ProviderRoleInputDTO inputDTO = new ProviderRoleInputDTO("New Name", "Nombre Nuevo");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals("New Name", existing.getName());
        assertEquals("Nombre Nuevo", existing.getNameEs());
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        ProviderRoleOutputDTO stub = mapper.map(5L);

        assertEquals(5L, stub.getId());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map(null));
    }
}
