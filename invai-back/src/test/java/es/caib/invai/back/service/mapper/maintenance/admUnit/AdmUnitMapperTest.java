package es.caib.invai.back.service.mapper.maintenance.admUnit;

import es.caib.invai.back.interna.maintenance.admUnit.DTO.AdmUnitInputDTO;
import es.caib.invai.back.interna.maintenance.admUnit.DTO.AdmUnitOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.admUnit.AdmUnitEntity;
import es.caib.invai.back.service.model.maintenance.admUnit.AdmUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link AdmUnitMapperImpl}, exercising every conversion direction
 * declared on {@link AdmUnitMapper}.
 */
class AdmUnitMapperTest {

    private AdmUnitMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AdmUnitMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        AdmUnitEntity entity = new AdmUnitEntity();
        entity.setId(1L);
        entity.setCode("PRO");
        entity.setName("Production Unit");
        entity.setNameEs("Unidad de Produccion");

        AdmUnit model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("PRO", model.getCode());
        assertEquals("Production Unit", model.getName());
        assertEquals("Unidad de Produccion", model.getNameEs());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        AdmUnit model = new AdmUnit();
        model.setId(2L);
        model.setCode("DEV");
        model.setName("Development Unit");
        model.setNameEs("Unidad de Desarrollo");

        AdmUnitEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("DEV", entity.getCode());
        assertEquals("Development Unit", entity.getName());
        assertEquals("Unidad de Desarrollo", entity.getNameEs());
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        AdmUnit model = new AdmUnit();
        model.setId(3L);
        model.setCode("QA");
        model.setName("Quality Assurance Unit");
        model.setNameEs("Unidad de Calidad");

        AdmUnitOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("QA", response.getCode());
        assertEquals("Quality Assurance Unit", response.getName());
        assertEquals("Unidad de Calidad", response.getNameEs());
    }

    @Test
    void toModelFromInput_ignoresAuditFieldsAndId() {
        AdmUnitInputDTO inputDTO = new AdmUnitInputDTO("ANL", "Analytics Unit", "Unidad de Analitica");

        AdmUnit model = mapper.toModelFromInput(inputDTO);

        assertEquals("ANL", model.getCode());
        assertEquals("Analytics Unit", model.getName());
        assertEquals("Unidad de Analitica", model.getNameEs());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        AdmUnit existing = new AdmUnit();
        existing.setId(4L);
        existing.setCode("OLD");
        existing.setName("Old Name");
        existing.setNameEs("Nombre Antiguo");
        AdmUnitInputDTO inputDTO = new AdmUnitInputDTO("NEW", "New Name", "Nombre Nuevo");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals("NEW", existing.getCode());
        assertEquals("New Name", existing.getName());
        assertEquals("Nombre Nuevo", existing.getNameEs());
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        AdmUnitOutputDTO stub = mapper.map(5L);

        assertEquals(5L, stub.getId());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map((Long) null));
    }
}
