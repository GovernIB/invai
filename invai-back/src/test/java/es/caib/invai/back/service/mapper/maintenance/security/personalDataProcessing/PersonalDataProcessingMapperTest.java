package es.caib.invai.back.service.mapper.maintenance.security.personalDataProcessing;

import es.caib.invai.back.interna.maintenance.security.personalDataProcessing.DTO.PersonalDataProcessingInputDTO;
import es.caib.invai.back.interna.maintenance.security.personalDataProcessing.DTO.PersonalDataProcessingOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.security.personalDataProcessing.PersonalDataProcessingEntity;
import es.caib.invai.back.service.model.maintenance.security.personalDataProcessing.PersonalDataProcessing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link PersonalDataProcessingMapperImpl}, exercising every conversion direction
 * declared on {@link PersonalDataProcessingMapper}.
 */
class PersonalDataProcessingMapperTest {

    private PersonalDataProcessingMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PersonalDataProcessingMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        PersonalDataProcessingEntity entity = new PersonalDataProcessingEntity();
        entity.setId(1L);
        entity.setName("Firmar peticiones");
        entity.setNameEs("Firmar peticiones");

        PersonalDataProcessing model = mapper.toModel(entity);

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
        PersonalDataProcessing model = new PersonalDataProcessing();
        model.setId(2L);
        model.setName("Acceso a los logs");
        model.setNameEs("Acceso a los logs");

        PersonalDataProcessingEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Acceso a los logs", entity.getName());
        assertEquals("Acceso a los logs", entity.getNameEs());
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        PersonalDataProcessing model = new PersonalDataProcessing();
        model.setId(3L);
        model.setName("Despliegue de aplicaciones");
        model.setNameEs("Despliegue de aplicaciones");

        PersonalDataProcessingOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("Despliegue de aplicaciones", response.getName());
        assertEquals("Despliegue de aplicaciones", response.getNameEs());
    }

    @Test
    void toModelFromInput_ignoresAuditFieldsAndId() {
        PersonalDataProcessingInputDTO inputDTO = new PersonalDataProcessingInputDTO("Acceso a los logs", "Acceso a los logs");

        PersonalDataProcessing model = mapper.toModelFromInput(inputDTO);

        assertEquals("Acceso a los logs", model.getName());
        assertEquals("Acceso a los logs", model.getNameEs());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        PersonalDataProcessing existing = new PersonalDataProcessing();
        existing.setId(4L);
        existing.setName("Old Name");
        existing.setNameEs("Old Name ES");
        PersonalDataProcessingInputDTO inputDTO = new PersonalDataProcessingInputDTO("New Name", "New Name ES");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals("New Name", existing.getName());
        assertEquals("New Name ES", existing.getNameEs());
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        PersonalDataProcessingOutputDTO stub = mapper.map(5L);

        assertEquals(5L, stub.getId());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map(null));
    }
}
