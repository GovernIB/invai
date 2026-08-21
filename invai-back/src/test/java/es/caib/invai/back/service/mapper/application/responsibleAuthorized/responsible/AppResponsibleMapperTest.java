package es.caib.invai.back.service.mapper.application.responsibleAuthorized.responsible;

import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonOutputDTO;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.core.AppResponsibleAuthorizedEntity;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.responsible.AppResponsibleEntity;
import es.caib.invai.back.persistence.model.catalog.responsibleType.LkupResponsibleTypeEntity;
import es.caib.invai.back.persistence.model.catalog.status.LkupStatusEntity;
import es.caib.invai.back.persistence.model.maintenance.responsible.company.CompanyEntity;
import es.caib.invai.back.persistence.model.maintenance.responsible.person.PersonEntity;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.core.AppResponsibleAuthorizedMapperImpl;
import es.caib.invai.back.service.mapper.catalog.responsibleType.ResponsibleTypeMapperImpl;
import es.caib.invai.back.service.mapper.catalog.status.StatusMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.responsible.company.CompanyMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.responsible.person.PersonMapperImpl;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import es.caib.invai.back.service.model.catalog.responsibleType.ResponsibleType;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import es.caib.invai.back.service.model.maintenance.responsible.company.Company;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the generated {@link AppResponsibleMapperImpl}, exercising every conversion
 * direction declared on {@link AppResponsibleMapper}.
 * <p>
 * {@link AppResponsibleMapper} declares {@code uses = {AppResponsibleAuthorizedMapper.class,
 * PersonMapper.class, ResponsibleTypeMapper.class}}, so the generated implementation carries
 * {@code @Autowired} fields for those three collaborators. Since no Spring context is bootstrapped
 * here, real {@code *MapperImpl} instances are injected manually via {@link ReflectionTestUtils},
 * following the same pattern used in {@code AppSystemMapperTest}. {@link AppResponsibleAuthorizedMapperImpl}
 * itself carries a {@code statusMapper} field (resolving {@code Application.status}) and
 * {@link PersonMapperImpl} carries a {@code companyMapper} field, both wired the same way.
 * </p>
 */
class AppResponsibleMapperTest {

    private AppResponsibleMapper mapper;

    @BeforeEach
    void setUp() {
        AppResponsibleAuthorizedMapperImpl appResponsibleAuthorizedMapperImpl = new AppResponsibleAuthorizedMapperImpl();
        ReflectionTestUtils.setField(appResponsibleAuthorizedMapperImpl, "statusMapper", new StatusMapperImpl());

        PersonMapperImpl personMapperImpl = new PersonMapperImpl();
        ReflectionTestUtils.setField(personMapperImpl, "companyMapper", new CompanyMapperImpl());

        AppResponsibleMapperImpl impl = new AppResponsibleMapperImpl();
        ReflectionTestUtils.setField(impl, "appResponsibleAuthorizedMapper", appResponsibleAuthorizedMapperImpl);
        ReflectionTestUtils.setField(impl, "personMapper", personMapperImpl);
        ReflectionTestUtils.setField(impl, "responsibleTypeMapper", new ResponsibleTypeMapperImpl());
        mapper = impl;
    }

    // ------------------------------------------------------------------
    // Entity/model graph builders
    // ------------------------------------------------------------------

    private AppResponsibleEntity buildDeepEntity() {
        CompanyEntity companyEntity = new CompanyEntity();
        companyEntity.setId(50L);
        companyEntity.setName("Acme Corp");

        PersonEntity personEntity = new PersonEntity();
        personEntity.setId(10L);
        personEntity.setCompany(companyEntity);
        personEntity.setFirstName("Joan");
        personEntity.setLastName("Fuster");
        personEntity.setEmail("joan.fuster@caib.es");
        personEntity.setPersonalCaib(true);

        LkupResponsibleTypeEntity responsibleTypeEntity = new LkupResponsibleTypeEntity();
        responsibleTypeEntity.setId(20L);
        responsibleTypeEntity.setName("Responsable funcional");
        responsibleTypeEntity.setNameEs("Responsable funcional");

        LkupStatusEntity statusEntity = new LkupStatusEntity();
        statusEntity.setId(StatusEnum.ACTIVE.getId());

        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(30L);
        applicationEntity.setCode("APP01");
        applicationEntity.setPrefix("AP1");
        applicationEntity.setName("Application One");
        applicationEntity.setStatus(statusEntity);

        AppResponsibleAuthorizedEntity anchorEntity = new AppResponsibleAuthorizedEntity();
        anchorEntity.setId(40L);
        anchorEntity.setApplication(applicationEntity);

        AppResponsibleEntity entity = new AppResponsibleEntity();
        entity.setId(1L);
        entity.setAppResponsibleAuthorized(anchorEntity);
        entity.setPerson(personEntity);
        entity.setResponsibleType(responsibleTypeEntity);
        entity.setObservation("Substitueix a Maria");
        entity.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        entity.setCreatedBy("creator");
        entity.setUpdatedAt(LocalDateTime.of(2025, 2, 1, 0, 0));
        entity.setUpdatedBy("updater");
        entity.setDeletedAt(LocalDateTime.of(2025, 3, 1, 0, 0));
        entity.setDeletedBy("deleter");

        return entity;
    }

    private AppResponsible buildDeepModel() {
        Company company = new Company();
        company.setId(51L);
        company.setName("Globex SA");

        Person person = new Person();
        person.setId(11L);
        person.setCompany(company);
        person.setFirstName("Maria");
        person.setLastName("Puig");
        person.setEmail("maria.puig@caib.es");
        person.setPersonalCaib(false);

        ResponsibleType responsibleType = new ResponsibleType();
        responsibleType.setId(21L);
        responsibleType.setName("Responsable tecnic");
        responsibleType.setNameEs("Responsable tecnico");

        Application application = new Application();
        application.setId(31L);
        application.setCode("APP02");
        application.setPrefix("AP2");
        application.setName("Application Two");
        application.setStatus(StatusEnum.INACTIVE);

        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder()
                .id(41L)
                .application(application)
                .build();

        return AppResponsible.builder()
                .id(2L)
                .appResponsibleAuthorized(anchor)
                .person(person)
                .responsibleType(responsibleType)
                .observation("Nova incorporacio")
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .createdBy("creator2")
                .updatedAt(LocalDateTime.of(2024, 2, 1, 0, 0))
                .updatedBy("updater2")
                .deletedAt(LocalDateTime.of(2024, 3, 1, 0, 0))
                .deletedBy("deleter2")
                .build();
    }

    // ------------------------------------------------------------------
    // toModel
    // ------------------------------------------------------------------

    @Test
    void toModel_deepEntityGraph_mapsEveryNestedField() {
        AppResponsibleEntity entity = buildDeepEntity();

        AppResponsible model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Substitueix a Maria", model.getObservation());
        assertEquals("creator", model.getCreatedBy());
        assertEquals("updater", model.getUpdatedBy());
        assertEquals("deleter", model.getDeletedBy());
        assertEquals(LocalDateTime.of(2025, 1, 1, 0, 0), model.getCreatedAt());
        assertEquals(LocalDateTime.of(2025, 3, 1, 0, 0), model.getDeletedAt());

        Person person = model.getPerson();
        assertEquals(10L, person.getId());
        assertEquals("Joan", person.getFirstName());
        assertEquals("Fuster", person.getLastName());
        assertEquals("joan.fuster@caib.es", person.getEmail());
        assertTrue(person.isPersonalCaib());
        assertEquals(50L, person.getCompany().getId());
        assertEquals("Acme Corp", person.getCompany().getName());

        ResponsibleType responsibleType = model.getResponsibleType();
        assertEquals(20L, responsibleType.getId());
        assertEquals("Responsable funcional", responsibleType.getName());

        AppResponsibleAuthorized anchor = model.getAppResponsibleAuthorized();
        assertEquals(40L, anchor.getId());
        assertEquals(30L, anchor.getApplication().getId());
        assertEquals("APP01", anchor.getApplication().getCode());
        assertEquals(StatusEnum.ACTIVE, anchor.getApplication().getStatus());
    }

    @Test
    void toModel_nullNestedForeignKeys_producesNullNestedModelFields() {
        AppResponsibleEntity entity = new AppResponsibleEntity();
        entity.setId(1L);
        entity.setAppResponsibleAuthorized(null);
        entity.setPerson(null);
        entity.setResponsibleType(null);

        AppResponsible model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertNull(model.getAppResponsibleAuthorized());
        assertNull(model.getPerson());
        assertNull(model.getResponsibleType());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    // ------------------------------------------------------------------
    // toEntity
    // ------------------------------------------------------------------

    @Test
    void toEntity_deepModelGraph_mapsEveryNestedField() {
        AppResponsible model = buildDeepModel();

        AppResponsibleEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Nova incorporacio", entity.getObservation());
        assertEquals("creator2", entity.getCreatedBy());
        assertEquals(LocalDateTime.of(2024, 1, 1, 0, 0), entity.getCreatedAt());

        PersonEntity personEntity = entity.getPerson();
        assertEquals(11L, personEntity.getId());
        assertEquals("Maria", personEntity.getFirstName());
        assertEquals("Puig", personEntity.getLastName());
        assertEquals("maria.puig@caib.es", personEntity.getEmail());
        assertEquals(51L, personEntity.getCompany().getId());
        assertEquals("Globex SA", personEntity.getCompany().getName());

        LkupResponsibleTypeEntity responsibleTypeEntity = entity.getResponsibleType();
        assertEquals(21L, responsibleTypeEntity.getId());
        assertEquals("Responsable tecnic", responsibleTypeEntity.getName());

        AppResponsibleAuthorizedEntity anchorEntity = entity.getAppResponsibleAuthorized();
        assertEquals(41L, anchorEntity.getId());
        assertEquals(31L, anchorEntity.getApplication().getId());
        assertEquals("APP02", anchorEntity.getApplication().getCode());
        assertEquals(StatusEnum.INACTIVE.getId(), anchorEntity.getApplication().getStatus().getId());
    }

    @Test
    void toEntity_nullNestedFields_producesNullNestedEntityFields() {
        AppResponsible model = AppResponsible.builder().id(2L).build();

        AppResponsibleEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertNull(entity.getAppResponsibleAuthorized());
        assertNull(entity.getPerson());
        assertNull(entity.getResponsibleType());
    }

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    // ------------------------------------------------------------------
    // toResponse
    // ------------------------------------------------------------------

    @Test
    void toResponse_deepModel_flattensAppResponsibleAuthorizedIdAndMapsPerson() {
        AppResponsible model = buildDeepModel();

        AppResponsibleOutputDTO response = mapper.toResponse(model);

        assertEquals(2L, response.getId());
        assertEquals(41L, response.getAppResponsibleAuthorizedId());
        assertEquals("Nova incorporacio", response.getObservation());
        assertEquals(LocalDateTime.of(2024, 3, 1, 0, 0), response.getDeletedAt());

        PersonOutputDTO personOutputDTO = response.getPerson();
        assertEquals(11L, personOutputDTO.getId());
        assertEquals("Maria", personOutputDTO.getFirstName());
        assertEquals("Puig", personOutputDTO.getLastName());
        assertEquals(51L, personOutputDTO.getCompany().getId());

        // ResponsibleType is embedded directly as the raw domain model (no dedicated Output DTO).
        assertSame(model.getResponsibleType(), response.getResponsibleType());
    }

    @Test
    void toResponse_nullAppResponsibleAuthorized_leavesAppResponsibleAuthorizedIdNull() {
        AppResponsible model = AppResponsible.builder().id(3L).appResponsibleAuthorized(null).build();

        AppResponsibleOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertNull(response.getAppResponsibleAuthorizedId());
    }

    @Test
    void toResponse_null_returnsNull() {
        assertNull(mapper.toResponse(null));
    }

    // ------------------------------------------------------------------
    // toModelFromInput / updateModelFromInput
    // ------------------------------------------------------------------

    @Test
    void toModelFromInput_resolvesNestedIdsAndIgnoresAuditFields() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, "Cap de projecte", null, false);

        AppResponsible model = mapper.toModelFromInput(inputDTO);

        assertEquals(40L, model.getAppResponsibleAuthorized().getId());
        assertEquals(10L, model.getPerson().getId());
        assertEquals(20L, model.getResponsibleType().getId());
        assertEquals("Cap de projecte", model.getJobTitle());
        assertNull(model.getId());
        assertNull(model.getObservation());
        assertNull(model.getCreatedAt());
        assertNull(model.getCreatedBy());
        assertNull(model.getUpdatedAt());
        assertNull(model.getUpdatedBy());
        assertNull(model.getDeletedAt());
        assertNull(model.getDeletedBy());
    }

    @Test
    void toModelFromInput_null_returnsNull() {
        assertNull(mapper.toModelFromInput(null));
    }

    @Test
    void updateModelFromInput_mergesNestedIdsAndObservationWithoutTouchingIdOrResponsibleType() {
        AppResponsible existing = AppResponsible.builder()
                .id(5L)
                .appResponsibleAuthorized(AppResponsibleAuthorized.builder().id(40L).build())
                .person(new Person())
                .responsibleType(new ResponsibleType())
                .observation("Keep me")
                .build();
        existing.getPerson().setId(10L);
        existing.getResponsibleType().setId(20L);
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(41L, 11L, null, null, null, null, 21L, "Analista", "Updated observation", false);

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(5L, existing.getId());
        assertEquals(41L, existing.getAppResponsibleAuthorized().getId());
        assertEquals(11L, existing.getPerson().getId());
        assertEquals(20L, existing.getResponsibleType().getId());
        assertEquals("Analista", existing.getJobTitle());
        assertEquals("Updated observation", existing.getObservation());
    }

    @Test
    void updateModelFromInput_withNullNestedTargets_initializesThemButLeavesResponsibleTypeUntouched() {
        AppResponsible existing = AppResponsible.builder().id(6L).build();
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(42L, 12L, null, null, null, null, 22L, null, null, false);

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(6L, existing.getId());
        assertEquals(42L, existing.getAppResponsibleAuthorized().getId());
        assertEquals(12L, existing.getPerson().getId());
        assertNull(existing.getResponsibleType());
    }

    @Test
    void updateModelFromInput_nullInput_doesNothing() {
        AppResponsible existing = AppResponsible.builder()
                .id(7L)
                .appResponsibleAuthorized(AppResponsibleAuthorized.builder().id(40L).build())
                .build();

        mapper.updateModelFromInput(null, existing);

        assertEquals(7L, existing.getId());
        assertEquals(40L, existing.getAppResponsibleAuthorized().getId());
    }
}
