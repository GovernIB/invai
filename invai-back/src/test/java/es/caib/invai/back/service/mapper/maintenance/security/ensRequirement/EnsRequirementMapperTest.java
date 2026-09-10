package es.caib.invai.back.service.mapper.maintenance.security.ensRequirement;

import es.caib.invai.back.interna.maintenance.security.ensRequirement.DTO.EnsRequirementInputDTO;
import es.caib.invai.back.interna.maintenance.security.ensRequirement.DTO.EnsRequirementOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.security.ensRequirement.EnsRequirementEntity;
import es.caib.invai.back.service.model.maintenance.security.ensRequirement.EnsRequirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link EnsRequirementMapperImpl}, exercising every conversion direction
 * declared on {@link EnsRequirementMapper}.
 */
class EnsRequirementMapperTest {

    private EnsRequirementMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new EnsRequirementMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        EnsRequirementEntity entity = new EnsRequirementEntity();
        entity.setId(1L);
        entity.setName("Control d'accessos");
        entity.setNameEs("Control de accesos");

        EnsRequirement model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Control d'accessos", model.getName());
        assertEquals("Control de accesos", model.getNameEs());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        EnsRequirement model = new EnsRequirement();
        model.setId(2L);
        model.setName("Gestio d'incidents");
        model.setNameEs("Gestion de incidentes");

        EnsRequirementEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Gestio d'incidents", entity.getName());
        assertEquals("Gestion de incidentes", entity.getNameEs());
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        EnsRequirement model = new EnsRequirement();
        model.setId(3L);
        model.setName("Continuitat del servei");
        model.setNameEs("Continuidad del servicio");

        EnsRequirementOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("Continuitat del servei", response.getName());
        assertEquals("Continuidad del servicio", response.getNameEs());
    }

    @Test
    void toModelFromInput_ignoresAuditFieldsAndId() {
        EnsRequirementInputDTO inputDTO = new EnsRequirementInputDTO("Gestio d'incidents", "Gestion de incidentes");

        EnsRequirement model = mapper.toModelFromInput(inputDTO);

        assertEquals("Gestio d'incidents", model.getName());
        assertEquals("Gestion de incidentes", model.getNameEs());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        EnsRequirement existing = new EnsRequirement();
        existing.setId(4L);
        existing.setName("Old Name");
        existing.setNameEs("Old Name ES");
        EnsRequirementInputDTO inputDTO = new EnsRequirementInputDTO("New Name", "New Name ES");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals("New Name", existing.getName());
        assertEquals("New Name ES", existing.getNameEs());
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        EnsRequirementOutputDTO stub = mapper.map(5L);

        assertEquals(5L, stub.getId());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map(null));
    }
}
