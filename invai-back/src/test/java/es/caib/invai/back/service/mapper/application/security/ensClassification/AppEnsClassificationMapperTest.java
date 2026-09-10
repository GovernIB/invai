package es.caib.invai.back.service.mapper.application.security.ensClassification;

import es.caib.invai.back.interna.application.security.ensClassification.DTO.AppEnsClassificationInputDTO;
import es.caib.invai.back.interna.application.security.ensClassification.DTO.AppEnsClassificationOutputDTO;
import es.caib.invai.back.interna.application.security.core.DTO.AppSecurityOutputDTO;
import es.caib.invai.back.interna.maintenance.security.identityProvider.DTO.IdentityProviderOutputDTO;
import es.caib.invai.back.interna.catalog.ensSubject.DTO.EnsSubjectOutputDTO;
import es.caib.invai.back.interna.maintenance.security.personalDataProcessing.DTO.PersonalDataProcessingOutputDTO;
import es.caib.invai.back.interna.catalog.securityLevel.DTO.SecurityLevelOutputDTO;
import es.caib.invai.back.persistence.model.application.security.ensClassification.AppEnsClassificationEntity;
import es.caib.invai.back.persistence.model.application.security.core.AppSecurityEntity;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.maintenance.security.identityProvider.IdentityProviderEntity;
import es.caib.invai.back.persistence.model.catalog.ensSubject.LkupEnsSubjectEntity;
import es.caib.invai.back.persistence.model.maintenance.security.personalDataProcessing.PersonalDataProcessingEntity;
import es.caib.invai.back.persistence.model.catalog.securityLevel.LkupSecurityLevelEntity;
import es.caib.invai.back.service.model.application.security.ensClassification.AppEnsClassification;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.maintenance.security.identityProvider.IdentityProvider;
import es.caib.invai.back.service.model.catalog.ensSubject.EnsSubject;
import es.caib.invai.back.service.model.maintenance.security.personalDataProcessing.PersonalDataProcessing;
import es.caib.invai.back.service.model.catalog.securityLevel.SecurityLevel;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import es.caib.invai.back.service.mapper.catalog.status.StatusMapperImpl;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.category.CategoryMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.systemType.SystemTypeMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.field.FieldMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.commission.CommissionMapperImpl;
import es.caib.invai.back.service.mapper.application.security.core.AppSecurityMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.security.identityProvider.IdentityProviderMapperImpl;
import es.caib.invai.back.service.mapper.catalog.ensSubject.EnsSubjectMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.security.personalDataProcessing.PersonalDataProcessingMapperImpl;
import es.caib.invai.back.service.mapper.catalog.securityLevel.SecurityLevelMapperImpl;

/**
 * Unit tests for the generated {@link AppEnsClassificationMapperImpl}, exercising every conversion
 * direction declared on {@link AppEnsClassificationMapper}.
 * <p>
 * {@link AppEnsClassificationMapper} declares {@code uses = {AppSecurityMapper.class,
 * IdentityProviderMapper.class, EnsSubjectMapper.class, PersonalDataProcessingMapper.class,
 * SecurityLevelMapper.class}}, so the generated implementation carries {@code @Autowired} fields
 * for each of those sub-mappers. Notably, all 6 security-level FK fields
 * (confidentiality/integrity/traceability/availability/authenticity/overallGrade) share the exact
 * same {@code securityLevelMapper} field, since MapStruct wires used-mapper types by decapitalized
 * simple class name rather than one field per usage. Since no Spring context is bootstrapped here,
 * every collaborator is wired manually via {@link ReflectionTestUtils}, following the same pattern
 * used in {@code AppDatabaseMapperTest}.
 * </p>
 * <p>
 * Because almost every field on this entity is optional, the "fully populated" tests deliberately
 * give each of the 6 same-typed {@link LkupSecurityLevelEntity}/{@link SecurityLevel} instances a
 * distinct id/name so a bug swapping e.g. confidentiality and integrity would be caught, while the
 * "null nested FK" tests confirm every optional relation safely maps to {@code null}.
 * </p>
 */
class AppEnsClassificationMapperTest {

    private AppEnsClassificationMapper mapper;

    @BeforeEach
    void setUp() {
        ApplicationMapperImpl applicationMapperImpl = new ApplicationMapperImpl();
        ReflectionTestUtils.setField(applicationMapperImpl, "categoryMapper", new CategoryMapperImpl());
        ReflectionTestUtils.setField(applicationMapperImpl, "systemTypeMapper", new SystemTypeMapperImpl());
        ReflectionTestUtils.setField(applicationMapperImpl, "fieldMapper", new FieldMapperImpl());
        ReflectionTestUtils.setField(applicationMapperImpl, "commissionMapper", new CommissionMapperImpl());
        ReflectionTestUtils.setField(applicationMapperImpl, "statusMapper", new StatusMapperImpl());

        AppSecurityMapperImpl appSecurityMapperImpl = new AppSecurityMapperImpl();
        ReflectionTestUtils.setField(appSecurityMapperImpl, "applicationMapper", applicationMapperImpl);

        AppEnsClassificationMapperImpl impl = new AppEnsClassificationMapperImpl();
        ReflectionTestUtils.setField(impl, "appSecurityMapper", appSecurityMapperImpl);
        ReflectionTestUtils.setField(impl, "identityProviderMapper", new IdentityProviderMapperImpl());
        ReflectionTestUtils.setField(impl, "ensSubjectMapper", new EnsSubjectMapperImpl());
        ReflectionTestUtils.setField(impl, "personalDataProcessingMapper", new PersonalDataProcessingMapperImpl());
        ReflectionTestUtils.setField(impl, "securityLevelMapper", new SecurityLevelMapperImpl());
        mapper = impl;
    }

    // ------------------------------------------------------------------
    // Entity graph builders
    // ------------------------------------------------------------------

    private static @NonNull ApplicationEntity getApplicationEntity() {
        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(205L);
        applicationEntity.setCode("APP01");
        applicationEntity.setName("Application One");
        return applicationEntity;
    }

    private static @NonNull AppSecurityEntity getAppSecurityEntity() {
        AppSecurityEntity appSecurityEntity = new AppSecurityEntity();
        appSecurityEntity.setId(10L);
        appSecurityEntity.setApplication(getApplicationEntity());
        appSecurityEntity.setObservation("Security observation");
        return appSecurityEntity;
    }

    private static @NonNull IdentityProviderEntity getIdentityProviderEntity() {
        IdentityProviderEntity entity = new IdentityProviderEntity();
        entity.setId(20L);
        entity.setName("Cl@ve");
        return entity;
    }

    private static @NonNull LkupEnsSubjectEntity getEnsSubjectEntity() {
        LkupEnsSubjectEntity entity = new LkupEnsSubjectEntity();
        entity.setId(30L);
        entity.setName("Si");
        entity.setNameEs("Si");
        return entity;
    }

    private static @NonNull PersonalDataProcessingEntity getPersonalDataProcessingEntity() {
        PersonalDataProcessingEntity entity = new PersonalDataProcessingEntity();
        entity.setId(40L);
        entity.setName("Firmar peticiones");
        entity.setNameEs("Firmar peticiones ES");
        return entity;
    }

    private static @NonNull LkupSecurityLevelEntity securityLevelEntity(Long id, String name, String nameEs) {
        LkupSecurityLevelEntity entity = new LkupSecurityLevelEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setNameEs(nameEs);
        return entity;
    }

    private AppEnsClassificationEntity buildDeepEntity() {
        AppEnsClassificationEntity entity = new AppEnsClassificationEntity();
        entity.setId(1L);
        entity.setAppSecurity(getAppSecurityEntity());
        entity.setIdentityProvider(getIdentityProviderEntity());
        entity.setEnsSubject(getEnsSubjectEntity());
        entity.setPersonalDataProcessing(getPersonalDataProcessingEntity());
        entity.setApprovalDate(LocalDateTime.of(2024, 6, 15, 0, 0));
        entity.setConfidentiality(securityLevelEntity(50L, "Confidentiality-High", "Confidentiality-High-ES"));
        entity.setIntegrity(securityLevelEntity(51L, "Integrity-High", "Integrity-High-ES"));
        entity.setTraceability(securityLevelEntity(52L, "Traceability-High", "Traceability-High-ES"));
        entity.setAvailability(securityLevelEntity(53L, "Availability-High", "Availability-High-ES"));
        entity.setAuthenticity(securityLevelEntity(54L, "Authenticity-High", "Authenticity-High-ES"));
        entity.setOverallGrade(securityLevelEntity(55L, "OverallGrade-High", "OverallGrade-High-ES"));
        entity.setCreatedAt(LocalDateTime.of(2017, 1, 1, 0, 0));
        entity.setCreatedBy("ens-creator");
        entity.setUpdatedAt(LocalDateTime.of(2017, 2, 1, 0, 0));
        entity.setUpdatedBy("ens-updater");
        entity.setDeletedAt(LocalDateTime.of(2017, 3, 1, 0, 0));
        entity.setDeletedBy("ens-deleter");
        return entity;
    }

    // ------------------------------------------------------------------
    // Model graph builders
    // ------------------------------------------------------------------

    private static @NonNull Application getApplication() {
        Application application = new Application();
        application.setId(420L);
        application.setCode("APP02");
        application.setName("Application Two");
        return application;
    }

    private static @NonNull AppSecurity getAppSecurity() {
        AppSecurity appSecurity = new AppSecurity();
        appSecurity.setId(11L);
        appSecurity.setApplication(getApplication());
        appSecurity.setObservation("Model observation");
        return appSecurity;
    }

    private static @NonNull IdentityProvider getIdentityProvider() {
        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setId(21L);
        identityProvider.setName("SAML");
        return identityProvider;
    }

    private static @NonNull EnsSubject getEnsSubject() {
        EnsSubject ensSubject = new EnsSubject();
        ensSubject.setId(31L);
        ensSubject.setName("No");
        ensSubject.setNameEs("No");
        return ensSubject;
    }

    private static @NonNull PersonalDataProcessing getPersonalDataProcessing() {
        PersonalDataProcessing personalDataProcessing = new PersonalDataProcessing();
        personalDataProcessing.setId(41L);
        personalDataProcessing.setName("Enviar notificaciones");
        personalDataProcessing.setNameEs("Enviar notificaciones ES");
        return personalDataProcessing;
    }

    private static @NonNull SecurityLevel securityLevel(Long id, String name, String nameEs) {
        SecurityLevel securityLevel = new SecurityLevel();
        securityLevel.setId(id);
        securityLevel.setName(name);
        securityLevel.setNameEs(nameEs);
        return securityLevel;
    }

    private AppEnsClassification buildDeepModel() {
        AppEnsClassification model = new AppEnsClassification();
        model.setId(2L);
        model.setAppSecurity(getAppSecurity());
        model.setIdentityProvider(getIdentityProvider());
        model.setEnsSubject(getEnsSubject());
        model.setPersonalDataProcessing(getPersonalDataProcessing());
        model.setApprovalDate(LocalDateTime.of(2024, 7, 20, 0, 0));
        model.setConfidentiality(securityLevel(60L, "Confidentiality2", "Confidentiality2-ES"));
        model.setIntegrity(securityLevel(61L, "Integrity2", "Integrity2-ES"));
        model.setTraceability(securityLevel(62L, "Traceability2", "Traceability2-ES"));
        model.setAvailability(securityLevel(63L, "Availability2", "Availability2-ES"));
        model.setAuthenticity(securityLevel(64L, "Authenticity2", "Authenticity2-ES"));
        model.setOverallGrade(securityLevel(65L, "OverallGrade2", "OverallGrade2-ES"));
        model.setCreatedAt(LocalDateTime.of(2016, 1, 1, 0, 0));
        model.setCreatedBy("ens-creator");
        model.setUpdatedAt(LocalDateTime.of(2016, 2, 1, 0, 0));
        model.setUpdatedBy("ens-updater");
        model.setDeletedAt(LocalDateTime.of(2016, 3, 1, 0, 0));
        model.setDeletedBy("ens-deleter");
        return model;
    }

    // ------------------------------------------------------------------
    // toModel
    // ------------------------------------------------------------------

    @Test
    void toModel_deepEntityGraph_mapsEveryFieldWithoutSwappingSameTypedSecurityLevels() {
        AppEnsClassificationEntity entity = buildDeepEntity();

        AppEnsClassification model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals(LocalDateTime.of(2024, 6, 15, 0, 0), model.getApprovalDate());
        assertEquals("ens-creator", model.getCreatedBy());
        assertEquals("ens-updater", model.getUpdatedBy());
        assertEquals("ens-deleter", model.getDeletedBy());

        assertEquals(10L, model.getAppSecurity().getId());
        assertEquals(205L, model.getAppSecurity().getApplication().getId());
        assertEquals("APP01", model.getAppSecurity().getApplication().getCode());

        assertEquals(20L, model.getIdentityProvider().getId());
        assertEquals("Cl@ve", model.getIdentityProvider().getName());

        assertEquals(30L, model.getEnsSubject().getId());
        assertEquals("Si", model.getEnsSubject().getName());

        assertEquals(40L, model.getPersonalDataProcessing().getId());
        assertEquals("Firmar peticiones", model.getPersonalDataProcessing().getName());

        assertEquals(50L, model.getConfidentiality().getId());
        assertEquals("Confidentiality-High", model.getConfidentiality().getName());
        assertEquals(51L, model.getIntegrity().getId());
        assertEquals("Integrity-High", model.getIntegrity().getName());
        assertEquals(52L, model.getTraceability().getId());
        assertEquals("Traceability-High", model.getTraceability().getName());
        assertEquals(53L, model.getAvailability().getId());
        assertEquals("Availability-High", model.getAvailability().getName());
        assertEquals(54L, model.getAuthenticity().getId());
        assertEquals("Authenticity-High", model.getAuthenticity().getName());
        assertEquals(55L, model.getOverallGrade().getId());
        assertEquals("OverallGrade-High", model.getOverallGrade().getName());
    }

    @Test
    void toModel_nullNestedForeignKeys_producesNullNestedModelFields() {
        AppEnsClassificationEntity entity = new AppEnsClassificationEntity();
        entity.setId(1L);
        entity.setAppSecurity(getAppSecurityEntity());
        // all other FKs left null: identityProvider, ensSubject, personalDataProcessing,
        // confidentiality, integrity, traceability, availability, authenticity, overallGrade.

        AppEnsClassification model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals(10L, model.getAppSecurity().getId());
        assertNull(model.getIdentityProvider());
        assertNull(model.getEnsSubject());
        assertNull(model.getPersonalDataProcessing());
        assertNull(model.getConfidentiality());
        assertNull(model.getIntegrity());
        assertNull(model.getTraceability());
        assertNull(model.getAvailability());
        assertNull(model.getAuthenticity());
        assertNull(model.getOverallGrade());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    // ------------------------------------------------------------------
    // toEntity
    // ------------------------------------------------------------------

    @Test
    void toEntity_deepModelGraph_mapsEveryFieldWithoutSwappingSameTypedSecurityLevels() {
        AppEnsClassification model = buildDeepModel();

        AppEnsClassificationEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals(LocalDateTime.of(2024, 7, 20, 0, 0), entity.getApprovalDate());
        assertEquals("ens-creator", entity.getCreatedBy());

        assertEquals(11L, entity.getAppSecurity().getId());
        assertEquals(420L, entity.getAppSecurity().getApplication().getId());

        assertEquals(21L, entity.getIdentityProvider().getId());
        assertEquals(31L, entity.getEnsSubject().getId());
        assertEquals(41L, entity.getPersonalDataProcessing().getId());

        assertEquals(60L, entity.getConfidentiality().getId());
        assertEquals("Confidentiality2", entity.getConfidentiality().getName());
        assertEquals(61L, entity.getIntegrity().getId());
        assertEquals("Integrity2", entity.getIntegrity().getName());
        assertEquals(62L, entity.getTraceability().getId());
        assertEquals("Traceability2", entity.getTraceability().getName());
        assertEquals(63L, entity.getAvailability().getId());
        assertEquals("Availability2", entity.getAvailability().getName());
        assertEquals(64L, entity.getAuthenticity().getId());
        assertEquals("Authenticity2", entity.getAuthenticity().getName());
        assertEquals(65L, entity.getOverallGrade().getId());
        assertEquals("OverallGrade2", entity.getOverallGrade().getName());
    }

    @Test
    void toEntity_nullNestedFields_producesNullNestedEntityFields() {
        AppEnsClassification model = new AppEnsClassification();
        model.setId(2L);
        model.setAppSecurity(getAppSecurity());

        AppEnsClassificationEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertNull(entity.getIdentityProvider());
        assertNull(entity.getEnsSubject());
        assertNull(entity.getPersonalDataProcessing());
        assertNull(entity.getConfidentiality());
        assertNull(entity.getIntegrity());
        assertNull(entity.getTraceability());
        assertNull(entity.getAvailability());
        assertNull(entity.getAuthenticity());
        assertNull(entity.getOverallGrade());
    }

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    // ------------------------------------------------------------------
    // toResponse
    // ------------------------------------------------------------------

    @Test
    void toResponse_deepModel_flattensEveryFieldWithoutSwappingSameTypedSecurityLevels() {
        AppEnsClassification model = buildDeepModel();

        AppEnsClassificationOutputDTO response = mapper.toResponse(model);

        assertEquals(2L, response.getId());
        assertEquals(LocalDateTime.of(2024, 7, 20, 0, 0), response.getApprovalDate());
        assertEquals(LocalDateTime.of(2016, 3, 1, 0, 0), response.getDeletedAt());

        AppSecurityOutputDTO appSecurityOutputDTO = response.getAppSecurity();
        assertEquals(11L, appSecurityOutputDTO.getId());
        assertEquals(420L, appSecurityOutputDTO.getApplication().getId());

        IdentityProviderOutputDTO identityProviderOutputDTO = response.getIdentityProvider();
        assertEquals(21L, identityProviderOutputDTO.getId());
        assertEquals("SAML", identityProviderOutputDTO.getName());

        EnsSubjectOutputDTO ensSubjectOutputDTO = response.getEnsSubject();
        assertEquals(31L, ensSubjectOutputDTO.getId());
        assertEquals("No", ensSubjectOutputDTO.getName());

        PersonalDataProcessingOutputDTO personalDataProcessingOutputDTO = response.getPersonalDataProcessing();
        assertEquals(41L, personalDataProcessingOutputDTO.getId());
        assertEquals("Enviar notificaciones", personalDataProcessingOutputDTO.getName());

        SecurityLevelOutputDTO confidentiality = response.getConfidentiality();
        assertEquals(60L, confidentiality.getId());
        assertEquals("Confidentiality2", confidentiality.getName());
        SecurityLevelOutputDTO integrity = response.getIntegrity();
        assertEquals(61L, integrity.getId());
        assertEquals("Integrity2", integrity.getName());
        SecurityLevelOutputDTO traceability = response.getTraceability();
        assertEquals(62L, traceability.getId());
        assertEquals("Traceability2", traceability.getName());
        SecurityLevelOutputDTO availability = response.getAvailability();
        assertEquals(63L, availability.getId());
        assertEquals("Availability2", availability.getName());
        SecurityLevelOutputDTO authenticity = response.getAuthenticity();
        assertEquals(64L, authenticity.getId());
        assertEquals("Authenticity2", authenticity.getName());
        SecurityLevelOutputDTO overallGrade = response.getOverallGrade();
        assertEquals(65L, overallGrade.getId());
        assertEquals("OverallGrade2", overallGrade.getName());
    }

    @Test
    void toResponse_nullNestedFields_leavesResponseNestedFieldsNull() {
        AppEnsClassification model = new AppEnsClassification();
        model.setId(3L);
        model.setAppSecurity(getAppSecurity());

        AppEnsClassificationOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertNull(response.getIdentityProvider());
        assertNull(response.getEnsSubject());
        assertNull(response.getPersonalDataProcessing());
        assertNull(response.getConfidentiality());
        assertNull(response.getIntegrity());
        assertNull(response.getTraceability());
        assertNull(response.getAvailability());
        assertNull(response.getAuthenticity());
        assertNull(response.getOverallGrade());
    }

    @Test
    void toResponse_null_returnsNull() {
        assertNull(mapper.toResponse(null));
    }

    // ------------------------------------------------------------------
    // toModelFromInput / updateModelFromInput
    // ------------------------------------------------------------------

    private static @NonNull AppEnsClassificationInputDTO fullInputDTO() {
        AppEnsClassificationInputDTO inputDTO = new AppEnsClassificationInputDTO();
        inputDTO.setAppSecurityId(50L);
        inputDTO.setIdentityProviderId(60L);
        inputDTO.setEnsSubjectId(70L);
        inputDTO.setPersonalDataProcessingId(80L);
        inputDTO.setApprovalDate(LocalDateTime.of(2024, 9, 1, 0, 0));
        inputDTO.setConfidentialityId(90L);
        inputDTO.setIntegrityId(91L);
        inputDTO.setTraceabilityId(92L);
        inputDTO.setAvailabilityId(93L);
        inputDTO.setAuthenticityId(94L);
        inputDTO.setOverallGradeId(95L);
        return inputDTO;
    }

    @Test
    void toModelFromInput_resolvesEveryNestedIdWithoutSwappingSameTypedSecurityLevelsAndIgnoresAuditFields() {
        AppEnsClassificationInputDTO inputDTO = fullInputDTO();

        AppEnsClassification model = mapper.toModelFromInput(inputDTO);

        assertEquals(50L, model.getAppSecurity().getId());
        assertEquals(60L, model.getIdentityProvider().getId());
        assertEquals(70L, model.getEnsSubject().getId());
        assertEquals(80L, model.getPersonalDataProcessing().getId());
        assertEquals(LocalDateTime.of(2024, 9, 1, 0, 0), model.getApprovalDate());
        assertEquals(90L, model.getConfidentiality().getId());
        assertEquals(91L, model.getIntegrity().getId());
        assertEquals(92L, model.getTraceability().getId());
        assertEquals(93L, model.getAvailability().getId());
        assertEquals(94L, model.getAuthenticity().getId());
        assertEquals(95L, model.getOverallGrade().getId());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getCreatedBy());
        assertNull(model.getUpdatedAt());
        assertNull(model.getUpdatedBy());
        assertNull(model.getDeletedAt());
        assertNull(model.getDeletedBy());
    }

    @Test
    void toModelFromInput_onlyMandatoryAppSecurityId_leavesOptionalFksNull() {
        // toModelFromInput always builds a brand-new instance with nothing to preserve, so every
        // optional FK whose id is absent from the payload maps to a fully null reference (not an
        // empty stub with a null id) - see AppEnsClassificationMapper#mapIdentityProviderId et al.
        AppEnsClassificationInputDTO inputDTO = new AppEnsClassificationInputDTO();
        inputDTO.setAppSecurityId(50L);

        AppEnsClassification model = mapper.toModelFromInput(inputDTO);

        assertEquals(50L, model.getAppSecurity().getId());
        assertNull(model.getIdentityProvider());
        assertNull(model.getEnsSubject());
        assertNull(model.getPersonalDataProcessing());
        assertNull(model.getConfidentiality());
        assertNull(model.getIntegrity());
        assertNull(model.getTraceability());
        assertNull(model.getAvailability());
        assertNull(model.getAuthenticity());
        assertNull(model.getOverallGrade());
        assertNull(model.getApprovalDate());
    }

    @Test
    void toModelFromInput_null_returnsNull() {
        assertNull(mapper.toModelFromInput(null));
    }

    @Test
    void updateModelFromInput_mergesEveryFieldWithoutTouchingId() {
        AppEnsClassification existing = new AppEnsClassification();
        existing.setId(4L);
        AppEnsClassificationInputDTO inputDTO = fullInputDTO();

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals(50L, existing.getAppSecurity().getId());
        assertEquals(60L, existing.getIdentityProvider().getId());
        assertEquals(70L, existing.getEnsSubject().getId());
        assertEquals(80L, existing.getPersonalDataProcessing().getId());
        assertEquals(90L, existing.getConfidentiality().getId());
        assertEquals(91L, existing.getIntegrity().getId());
        assertEquals(92L, existing.getTraceability().getId());
        assertEquals(93L, existing.getAvailability().getId());
        assertEquals(94L, existing.getAuthenticity().getId());
        assertEquals(95L, existing.getOverallGrade().getId());
    }

    @Test
    void updateModelFromInput_optionalFksOmittedFromInput_preserveExistingValues() {
        AppEnsClassification existing = new AppEnsClassification();
        existing.setId(5L);
        existing.setIdentityProvider(getIdentityProvider());
        AppEnsClassificationInputDTO inputDTO = new AppEnsClassificationInputDTO();
        inputDTO.setAppSecurityId(50L);
        // identityProviderId left null in the input: the IGNORE strategy must leave the
        // existing nested identityProvider untouched rather than nulling it out.

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(5L, existing.getId());
        assertEquals(50L, existing.getAppSecurity().getId());
        assertEquals(21L, existing.getIdentityProvider().getId());
    }

    @Test
    void updateModelFromInput_nullInput_doesNothing() {
        AppEnsClassification existing = new AppEnsClassification();
        existing.setId(6L);
        existing.setAppSecurity(getAppSecurity());

        mapper.updateModelFromInput(null, existing);

        assertEquals(6L, existing.getId());
        assertEquals(11L, existing.getAppSecurity().getId());
    }
}
