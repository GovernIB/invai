package es.caib.invai.back.service.mapper.maintenance.responsible.company;

import es.caib.invai.back.interna.maintenance.responsible.company.DTO.CompanyInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.company.DTO.CompanyOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.responsible.company.CompanyEntity;
import es.caib.invai.back.service.model.maintenance.responsible.company.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link CompanyMapperImpl}, exercising every conversion direction
 * declared on {@link CompanyMapper}.
 */
class CompanyMapperTest {

    private CompanyMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CompanyMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        CompanyEntity entity = new CompanyEntity();
        entity.setId(1L);
        entity.setName("Plexus Tech");

        Company model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Plexus Tech", model.getName());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        Company model = new Company();
        model.setId(2L);
        model.setName("Acme Corp");

        CompanyEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Acme Corp", entity.getName());
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        Company model = new Company();
        model.setId(3L);
        model.setName("Globex");

        CompanyOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("Globex", response.getName());
    }

    @Test
    void toModelFromInput_ignoresAuditFieldsAndId() {
        CompanyInputDTO inputDTO = new CompanyInputDTO("Initech");

        Company model = mapper.toModelFromInput(inputDTO);

        assertEquals("Initech", model.getName());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        Company existing = new Company();
        existing.setId(4L);
        existing.setName("Old Name");
        CompanyInputDTO inputDTO = new CompanyInputDTO("New Name");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals("New Name", existing.getName());
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        CompanyOutputDTO stub = mapper.map(5L);

        assertEquals(5L, stub.getId());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map(null));
    }
}
