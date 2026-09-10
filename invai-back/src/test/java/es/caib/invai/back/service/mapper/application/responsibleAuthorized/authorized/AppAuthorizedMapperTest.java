package es.caib.invai.back.service.mapper.application.responsibleAuthorized.authorized;

import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonOutputDTO;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.authorized.AppAuthorizedEntity;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.core.AppResponsibleAuthorizedEntity;
import es.caib.invai.back.persistence.model.catalog.status.LkupStatusEntity;
import es.caib.invai.back.persistence.model.maintenance.responsible.company.CompanyEntity;
import es.caib.invai.back.persistence.model.maintenance.responsible.person.PersonEntity;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.core.AppResponsibleAuthorizedMapperImpl;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapperImpl;
import es.caib.invai.back.service.mapper.catalog.status.StatusMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.category.CategoryMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.systemType.SystemTypeMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.field.FieldMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.commission.CommissionMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.responsible.company.CompanyMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.responsible.person.PersonMapperImpl;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import es.caib.invai.back.service.model.maintenance.responsible.company.Company;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the generated {@link AppAuthorizedMapperImpl}, exercising every conversion
 * direction declared on {@link AppAuthorizedMapper}.
 * <p>
 * {@link AppAuthorizedMapper} declares {@code uses = {PersonMapper.class, AppResponsibleAuthorizedMapper.class}},
 * so the generated implementation carries {@code @Autowired} fields for those collaborators. Since no
 * Spring context is bootstrapped here, real {@code *MapperImpl} instances are injected manually via
 * {@link ReflectionTestUtils}, following the same pattern used in {@code AppResponsibleMapperTest}.
 * </p>
 */
class AppAuthorizedMapperTest {

    private AppAuthorizedMapper mapper;

    @BeforeEach
    void setUp() {
        ApplicationMapperImpl applicationMapperImpl = new ApplicationMapperImpl();
        ReflectionTestUtils.setField(applicationMapperImpl, "categoryMapper", new CategoryMapperImpl());
        ReflectionTestUtils.setField(applicationMapperImpl, "systemTypeMapper", new SystemTypeMapperImpl());
        ReflectionTestUtils.setField(applicationMapperImpl, "fieldMapper", new FieldMapperImpl());
        ReflectionTestUtils.setField(applicationMapperImpl, "commissionMapper", new CommissionMapperImpl());
        ReflectionTestUtils.setField(applicationMapperImpl, "statusMapper", new StatusMapperImpl());

        AppResponsibleAuthorizedMapperImpl appResponsibleAuthorizedMapperImpl = new AppResponsibleAuthorizedMapperImpl();
        ReflectionTestUtils.setField(appResponsibleAuthorizedMapperImpl, "applicationMapper", applicationMapperImpl);

        PersonMapperImpl personMapperImpl = new PersonMapperImpl();
        ReflectionTestUtils.setField(personMapperImpl, "companyMapper", new CompanyMapperImpl());

        AppAuthorizedMapperImpl impl = new AppAuthorizedMapperImpl();
        ReflectionTestUtils.setField(impl, "personMapper", personMapperImpl);
        ReflectionTestUtils.setField(impl, "appResponsibleAuthorizedMapper", appResponsibleAuthorizedMapperImpl);
        mapper = impl;
    }

    // ------------------------------------------------------------------
    // Entity/model graph builders
    // ------------------------------------------------------------------

    private AppAuthorizedEntity buildDeepEntity() {
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

        return getAppAuthorizedEntity(personEntity);
    }

    private static @NonNull AppAuthorizedEntity getAppAuthorizedEntity(PersonEntity personEntity) {
        AppResponsibleAuthorizedEntity anchorEntity = getAppResponsibleAuthorizedEntity();

        AppAuthorizedEntity entity = new AppAuthorizedEntity();
        entity.setId(1L);
        entity.setAppResponsibleAuthorized(anchorEntity);
        entity.setPerson(personEntity);
        entity.setObservation("Substitueix a Maria");
        entity.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        entity.setCreatedBy("creator");
        entity.setUpdatedAt(LocalDateTime.of(2025, 2, 1, 0, 0));
        entity.setUpdatedBy("updater");
        entity.setDeletedAt(LocalDateTime.of(2025, 3, 1, 0, 0));
        entity.setDeletedBy("deleter");
        return entity;
    }

    private static @NonNull AppResponsibleAuthorizedEntity getAppResponsibleAuthorizedEntity() {
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
        return anchorEntity;
    }

    private AppAuthorized buildDeepModel() {
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

        return AppAuthorized.builder()
                .id(2L)
                .appResponsibleAuthorized(anchor)
                .person(person)
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
        AppAuthorizedEntity entity = buildDeepEntity();

        AppAuthorized model = mapper.toModel(entity);

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

        AppResponsibleAuthorized anchor = model.getAppResponsibleAuthorized();
        assertEquals(40L, anchor.getId());
        assertEquals(30L, anchor.getApplication().getId());
        assertEquals("APP01", anchor.getApplication().getCode());
        assertEquals(StatusEnum.ACTIVE, anchor.getApplication().getStatus());
    }

    @Test
    void toModel_nullNestedForeignKeys_producesNullNestedModelFields() {
        AppAuthorizedEntity entity = new AppAuthorizedEntity();
        entity.setId(1L);
        entity.setAppResponsibleAuthorized(null);
        entity.setPerson(null);

        AppAuthorized model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertNull(model.getAppResponsibleAuthorized());
        assertNull(model.getPerson());
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
        AppAuthorized model = buildDeepModel();

        AppAuthorizedEntity entity = mapper.toEntity(model);

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

        AppResponsibleAuthorizedEntity anchorEntity = entity.getAppResponsibleAuthorized();
        assertEquals(41L, anchorEntity.getId());
        assertEquals(31L, anchorEntity.getApplication().getId());
        assertEquals("APP02", anchorEntity.getApplication().getCode());
        assertEquals(StatusEnum.INACTIVE.getId(), anchorEntity.getApplication().getStatus().getId());
    }

    @Test
    void toEntity_nullNestedFields_producesNullNestedEntityFields() {
        AppAuthorized model = AppAuthorized.builder().id(2L).build();

        AppAuthorizedEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertNull(entity.getAppResponsibleAuthorized());
        assertNull(entity.getPerson());
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
        AppAuthorized model = buildDeepModel();

        AppAuthorizedOutputDTO response = mapper.toResponse(model);

        assertEquals(2L, response.getId());
        assertEquals(41L, response.getAppResponsibleAuthorizedId());
        assertEquals("Nova incorporacio", response.getObservation());
        assertEquals(LocalDateTime.of(2024, 3, 1, 0, 0), response.getDeletedAt());
        assertNull(response.getAuthorizationTypes());

        PersonOutputDTO personOutputDTO = response.getPerson();
        assertEquals(11L, personOutputDTO.getId());
        assertEquals("Maria", personOutputDTO.getFirstName());
        assertEquals("Puig", personOutputDTO.getLastName());
        assertEquals(51L, personOutputDTO.getCompany().getId());
    }

    @Test
    void toResponse_nullAppResponsibleAuthorized_leavesAppResponsibleAuthorizedIdNull() {
        AppAuthorized model = AppAuthorized.builder().id(3L).appResponsibleAuthorized(null).build();

        AppAuthorizedOutputDTO response = mapper.toResponse(model);

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
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(40L, 10L, null, null, null, null, java.util.List.of(20L, 21L), null, false);

        AppAuthorized model = mapper.toModelFromInput(inputDTO);

        assertEquals(40L, model.getAppResponsibleAuthorized().getId());
        assertEquals(10L, model.getPerson().getId());
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
    void updateModelFromInput_updatesObservationWithoutTouchingIdAnchorOrPerson() {
        AppAuthorized existing = AppAuthorized.builder()
                .id(5L)
                .appResponsibleAuthorized(AppResponsibleAuthorized.builder().id(40L).build())
                .person(new Person())
                .observation("Old observation")
                .build();
        existing.getPerson().setId(10L);
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(41L, 11L, null, null, null, null, java.util.List.of(22L), "New observation", false);

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(5L, existing.getId());
        assertEquals(40L, existing.getAppResponsibleAuthorized().getId());
        assertEquals(10L, existing.getPerson().getId());
        assertEquals("New observation", existing.getObservation());
    }

    @Test
    void updateModelFromInput_withNullNestedTargets_leavesThemUntouched() {
        AppAuthorized existing = AppAuthorized.builder().id(6L).build();
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(42L, 12L, null, null, null, null, java.util.List.of(23L), null, false);

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(6L, existing.getId());
        assertNull(existing.getAppResponsibleAuthorized());
        assertNull(existing.getPerson());
    }

    @Test
    void updateModelFromInput_nullInput_doesNothing() {
        AppAuthorized existing = AppAuthorized.builder()
                .id(7L)
                .appResponsibleAuthorized(AppResponsibleAuthorized.builder().id(40L).build())
                .build();

        mapper.updateModelFromInput(null, existing);

        assertEquals(7L, existing.getId());
        assertEquals(40L, existing.getAppResponsibleAuthorized().getId());
    }
}
