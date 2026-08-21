package es.caib.invai.back.service.mapper.maintenance.responsible.person;

import es.caib.invai.back.interna.maintenance.responsible.company.DTO.CompanyOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.responsible.company.CompanyEntity;
import es.caib.invai.back.persistence.model.maintenance.responsible.person.PersonEntity;
import es.caib.invai.back.service.model.maintenance.responsible.company.Company;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;
import es.caib.invai.back.service.mapper.maintenance.responsible.company.CompanyMapper;

/**
 * Unit tests for the generated {@link PersonMapperImpl}, exercising every conversion direction
 * declared on {@link PersonMapper}, including its {@link CompanyMapper} nested collaborator.
 */
@ExtendWith(MockitoExtension.class)
class PersonMapperTest {

    @Mock
    private CompanyMapper companyMapper;

    private PersonMapper mapper;

    @BeforeEach
    void setUp() {
        PersonMapperImpl impl = new PersonMapperImpl();
        ReflectionTestUtils.setField(impl, "companyMapper", companyMapper);
        mapper = impl;
    }

    @Test
    void toModel_mapsEveryEntityField() {
        PersonEntity entity = new PersonEntity();
        entity.setId(1L);
        entity.setFirstName("Joan");
        entity.setLastName("Puig");
        entity.setEmail("joan.puig@example.com");
        CompanyEntity companyEntity = new CompanyEntity();
        entity.setCompany(companyEntity);
        entity.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        entity.setCreatedBy("admin");
        entity.setUpdatedAt(LocalDateTime.of(2026, 2, 1, 0, 0));
        entity.setUpdatedBy("admin2");
        entity.setDeletedAt(LocalDateTime.of(2026, 3, 1, 0, 0));
        entity.setDeletedBy("admin3");

        Company companyModel = new Company();
        when(companyMapper.toModel(companyEntity)).thenReturn(companyModel);

        Person model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Joan", model.getFirstName());
        assertEquals("Puig", model.getLastName());
        assertEquals("joan.puig@example.com", model.getEmail());
        assertSame(companyModel, model.getCompany());
        assertEquals(entity.getCreatedAt(), model.getCreatedAt());
        assertEquals(entity.getCreatedBy(), model.getCreatedBy());
        assertEquals(entity.getUpdatedAt(), model.getUpdatedAt());
        assertEquals(entity.getUpdatedBy(), model.getUpdatedBy());
        assertEquals(entity.getDeletedAt(), model.getDeletedAt());
        assertEquals(entity.getDeletedBy(), model.getDeletedBy());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        Person model = new Person();
        model.setId(2L);
        model.setFirstName("Anna");
        model.setLastName("Serra");
        model.setEmail("anna.serra@example.com");
        Company companyModel = new Company();
        model.setCompany(companyModel);

        CompanyEntity companyEntity = new CompanyEntity();
        when(companyMapper.toEntity(companyModel)).thenReturn(companyEntity);

        PersonEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Anna", entity.getFirstName());
        assertEquals("Serra", entity.getLastName());
        assertEquals("anna.serra@example.com", entity.getEmail());
        assertSame(companyEntity, entity.getCompany());
    }

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        Person model = new Person();
        model.setId(3L);
        model.setFirstName("Pere");
        model.setLastName("Vila");
        model.setEmail("pere.vila@example.com");
        Company companyModel = new Company();
        companyModel.setId(9L);
        model.setCompany(companyModel);
        model.setDeletedAt(LocalDateTime.of(2026, 4, 1, 0, 0));

        CompanyOutputDTO companyDto = new CompanyOutputDTO();
        when(companyMapper.toResponse(companyModel)).thenReturn(companyDto);

        PersonOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("Pere", response.getFirstName());
        assertEquals("Vila", response.getLastName());
        assertEquals("pere.vila@example.com", response.getEmail());
        assertSame(companyDto, response.getCompany());
        assertEquals(model.getDeletedAt(), response.getDeletedAt());
    }

    @Test
    void toResponse_null_returnsNull() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void toModelFromInput_setsNestedIdAndIgnoresAuditFields() {
        PersonInputDTO inputDTO = new PersonInputDTO();
        inputDTO.setCompanyId(5L);
        inputDTO.setFirstName("New");
        inputDTO.setLastName("Person");
        inputDTO.setEmail("new.person@example.com");

        Person model = mapper.toModelFromInput(inputDTO);

        assertEquals("New", model.getFirstName());
        assertEquals("Person", model.getLastName());
        assertEquals("new.person@example.com", model.getEmail());
        assertEquals(5L, model.getCompany().getId());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void toModelFromInput_null_returnsNull() {
        assertNull(mapper.toModelFromInput(null));
    }

    @Test
    void updateModelFromInput_createsNestedCompanyWhenMissing() {
        Person existing = new Person();
        existing.setId(4L);
        PersonInputDTO inputDTO = new PersonInputDTO();
        inputDTO.setCompanyId(10L);
        inputDTO.setFirstName("Updated");
        inputDTO.setLastName("Person");
        inputDTO.setEmail("updated.person@example.com");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals("Updated", existing.getFirstName());
        assertEquals("Person", existing.getLastName());
        assertEquals("updated.person@example.com", existing.getEmail());
        assertNotNull(existing.getCompany());
        assertEquals(10L, existing.getCompany().getId());
    }

    @Test
    void updateModelFromInput_preservesExistingNestedCompanyFieldsOtherThanId() {
        Person existing = new Person();
        Company company = new Company();
        company.setId(1L);
        company.setName("Existing Corp");
        existing.setCompany(company);

        PersonInputDTO inputDTO = new PersonInputDTO();
        inputDTO.setCompanyId(99L);
        inputDTO.setFirstName("X");
        inputDTO.setLastName("Y");
        inputDTO.setEmail("x@example.com");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(99L, existing.getCompany().getId());
        assertEquals("Existing Corp", existing.getCompany().getName());
    }
}
