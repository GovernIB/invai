package es.caib.invai.back.service.mapper.maintenance.development.role;

import es.caib.invai.back.interna.maintenance.development.role.DTO.RoleInputDTO;
import es.caib.invai.back.interna.maintenance.development.role.DTO.RoleOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.development.role.RoleEntity;
import es.caib.invai.back.service.model.maintenance.development.role.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link RoleMapperImpl}, exercising every conversion direction
 * declared on {@link RoleMapper}.
 */
class RoleMapperTest {

    private RoleMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RoleMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        RoleEntity entity = new RoleEntity();
        entity.setId(1L);
        entity.setName("DevOps Lead");
        entity.setNameEs("Responsable DevOps");

        Role model = mapper.toModel(entity);

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
        Role model = new Role();
        model.setId(2L);
        model.setName("QA Lead");
        model.setNameEs("Responsable QA");

        RoleEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("QA Lead", entity.getName());
        assertEquals("Responsable QA", entity.getNameEs());
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        Role model = new Role();
        model.setId(3L);
        model.setName("Reviewer");
        model.setNameEs("Revisor");

        RoleOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("Reviewer", response.getName());
        assertEquals("Revisor", response.getNameEs());
    }

    @Test
    void toModelFromInput_ignoresAuditFieldsAndId() {
        RoleInputDTO inputDTO = new RoleInputDTO("Analyst", "Analista");

        Role model = mapper.toModelFromInput(inputDTO);

        assertEquals("Analyst", model.getName());
        assertEquals("Analista", model.getNameEs());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        Role existing = new Role();
        existing.setId(4L);
        existing.setName("Old Name");
        existing.setNameEs("Nombre Antiguo");
        RoleInputDTO inputDTO = new RoleInputDTO("New Name", "Nombre Nuevo");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals("New Name", existing.getName());
        assertEquals("Nombre Nuevo", existing.getNameEs());
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        RoleOutputDTO stub = mapper.map(5L);

        assertEquals(5L, stub.getId());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map(null));
    }
}
