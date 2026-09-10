package es.caib.invai.back.service.mapper.maintenance.complianceSituation;

import es.caib.invai.back.interna.maintenance.complianceSituation.DTO.ComplianceSituationInputDTO;
import es.caib.invai.back.interna.maintenance.complianceSituation.DTO.ComplianceSituationOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.complianceSituation.ComplianceSituationEntity;
import es.caib.invai.back.service.model.maintenance.complianceSituation.ComplianceSituation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link ComplianceSituationMapperImpl}, exercising every conversion direction
 * declared on {@link ComplianceSituationMapper}.
 */
class ComplianceSituationMapperTest {

    private ComplianceSituationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ComplianceSituationMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        ComplianceSituationEntity entity = new ComplianceSituationEntity();
        entity.setId(1L);
        entity.setName("Conforme");
        entity.setNameEs("Conforme");

        ComplianceSituation model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Conforme", model.getName());
        assertEquals("Conforme", model.getNameEs());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        ComplianceSituation model = new ComplianceSituation();
        model.setId(2L);
        model.setName("Parcialment");
        model.setNameEs("Parcialment");

        ComplianceSituationEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Parcialment", entity.getName());
        assertEquals("Parcialment", entity.getNameEs());
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        ComplianceSituation model = new ComplianceSituation();
        model.setId(3L);
        model.setName("No conforme");
        model.setNameEs("No conforme");

        ComplianceSituationOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("No conforme", response.getName());
        assertEquals("No conforme", response.getNameEs());
    }

    @Test
    void toModelFromInput_ignoresAuditFieldsAndId() {
        ComplianceSituationInputDTO inputDTO = new ComplianceSituationInputDTO("Parcialment", "Parcialment");

        ComplianceSituation model = mapper.toModelFromInput(inputDTO);

        assertEquals("Parcialment", model.getName());
        assertEquals("Parcialment", model.getNameEs());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        ComplianceSituation existing = new ComplianceSituation();
        existing.setId(4L);
        existing.setName("Old Name");
        existing.setNameEs("Old Name ES");
        ComplianceSituationInputDTO inputDTO = new ComplianceSituationInputDTO("New Name", "New Name ES");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals("New Name", existing.getName());
        assertEquals("New Name ES", existing.getNameEs());
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        ComplianceSituationOutputDTO stub = mapper.map(5L);

        assertEquals(5L, stub.getId());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map(null));
    }
}
