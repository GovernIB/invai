package es.caib.invai.back.service.mapper.maintenance.security.securityMeasureType;

import es.caib.invai.back.interna.maintenance.security.securityMeasureType.DTO.SecurityMeasureTypeInputDTO;
import es.caib.invai.back.interna.maintenance.security.securityMeasureType.DTO.SecurityMeasureTypeOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.security.securityMeasureType.SecurityMeasureTypeEntity;
import es.caib.invai.back.service.model.maintenance.security.securityMeasureType.SecurityMeasureType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link SecurityMeasureTypeMapperImpl}, exercising every conversion direction
 * declared on {@link SecurityMeasureTypeMapper}.
 */
class SecurityMeasureTypeMapperTest {

    private SecurityMeasureTypeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SecurityMeasureTypeMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        SecurityMeasureTypeEntity entity = new SecurityMeasureTypeEntity();
        entity.setId(1L);
        entity.setName("Organitzativa");
        entity.setNameEs("Organizativa");

        SecurityMeasureType model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Organitzativa", model.getName());
        assertEquals("Organizativa", model.getNameEs());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        SecurityMeasureType model = new SecurityMeasureType();
        model.setId(2L);
        model.setName("Tecnica");
        model.setNameEs("Tecnica");

        SecurityMeasureTypeEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Tecnica", entity.getName());
        assertEquals("Tecnica", entity.getNameEs());
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        SecurityMeasureType model = new SecurityMeasureType();
        model.setId(3L);
        model.setName("Procedimental");
        model.setNameEs("Procedimental");

        SecurityMeasureTypeOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("Procedimental", response.getName());
        assertEquals("Procedimental", response.getNameEs());
    }

    @Test
    void toModelFromInput_ignoresAuditFieldsAndId() {
        SecurityMeasureTypeInputDTO inputDTO = new SecurityMeasureTypeInputDTO("Tecnica", "Tecnica");

        SecurityMeasureType model = mapper.toModelFromInput(inputDTO);

        assertEquals("Tecnica", model.getName());
        assertEquals("Tecnica", model.getNameEs());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        SecurityMeasureType existing = new SecurityMeasureType();
        existing.setId(4L);
        existing.setName("Old Name");
        existing.setNameEs("Old Name ES");
        SecurityMeasureTypeInputDTO inputDTO = new SecurityMeasureTypeInputDTO("New Name", "New Name ES");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals("New Name", existing.getName());
        assertEquals("New Name ES", existing.getNameEs());
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        SecurityMeasureTypeOutputDTO stub = mapper.map(5L);

        assertEquals(5L, stub.getId());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map(null));
    }
}
