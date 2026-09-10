package es.caib.invai.back.service.mapper.application.security.risk;

import es.caib.invai.back.interna.application.security.core.DTO.AppSecurityOutputDTO;
import es.caib.invai.back.interna.application.security.risk.DTO.AppSecurityRiskInputDTO;
import es.caib.invai.back.interna.application.security.risk.DTO.AppSecurityRiskOutputDTO;
import es.caib.invai.back.interna.catalog.securityLevel.DTO.SecurityLevelOutputDTO;
import es.caib.invai.back.interna.maintenance.general.field.DTO.FieldOutputDTO;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.application.security.core.AppSecurityEntity;
import es.caib.invai.back.persistence.model.application.security.risk.AppSecurityRiskEntity;
import es.caib.invai.back.persistence.model.catalog.securityLevel.LkupSecurityLevelEntity;
import es.caib.invai.back.persistence.model.maintenance.general.field.FieldEntity;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapperImpl;
import es.caib.invai.back.service.mapper.application.security.core.AppSecurityMapper;
import es.caib.invai.back.service.mapper.application.security.core.AppSecurityMapperImpl;
import es.caib.invai.back.service.mapper.catalog.securityLevel.SecurityLevelMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.category.CategoryMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.commission.CommissionMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.field.FieldMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.systemType.SystemTypeMapperImpl;
import es.caib.invai.back.service.mapper.catalog.status.StatusMapperImpl;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
import es.caib.invai.back.service.model.application.security.risk.AppSecurityRisk;
import es.caib.invai.back.service.model.catalog.securityLevel.SecurityLevel;
import es.caib.invai.back.service.model.maintenance.general.field.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link AppSecurityRiskMapperImpl}, exercising every conversion
 * direction declared on {@link AppSecurityRiskMapper}.
 * <p>
 * {@link AppSecurityRiskMapper} declares {@code uses = {AppSecurityMapper.class, SecurityLevelMapper.class,
 * FieldMapper.class}}, so the generated implementation carries {@code @Autowired} fields for all three.
 * {@link AppSecurityMapper} itself declares {@code uses = {StatusMapper.class, ApplicationMapper.class}},
 * so a fully wired {@link AppSecurityMapperImpl} (with its own nested {@link ApplicationMapperImpl} and
 * that mapper's sub-mappers) is manually injected via {@link ReflectionTestUtils}, following the same
 * pattern used in {@code AppDatabaseMapperTest}. Note that two independent {@link FieldMapperImpl}
 * instances are wired: one nested inside {@link ApplicationMapperImpl} (for {@code Application.field}),
 * and one directly on {@link AppSecurityRiskMapperImpl} (for the risk's own {@code field}) - this is
 * correct and intentional.
 * </p>
 */
class AppSecurityRiskMapperTest {

    private AppSecurityRiskMapper mapper;

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

        AppSecurityRiskMapperImpl impl = new AppSecurityRiskMapperImpl();
        ReflectionTestUtils.setField(impl, "appSecurityMapper", appSecurityMapperImpl);
        ReflectionTestUtils.setField(impl, "securityLevelMapper", new SecurityLevelMapperImpl());
        ReflectionTestUtils.setField(impl, "fieldMapper", new FieldMapperImpl());
        mapper = impl;
    }

    // ------------------------------------------------------------------
    // Entity graph builders
    // ------------------------------------------------------------------

    private ApplicationEntity buildApplicationEntity() {
        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(205L);
        applicationEntity.setCode("APP01");
        applicationEntity.setPrefix("AP1");
        applicationEntity.setName("Application One");
        return applicationEntity;
    }

    private AppSecurityEntity buildAppSecurityEntity() {
        AppSecurityEntity appSecurityEntity = new AppSecurityEntity();
        appSecurityEntity.setId(10L);
        appSecurityEntity.setApplication(buildApplicationEntity());
        appSecurityEntity.setObservation("Security observation");
        appSecurityEntity.setCreatedAt(LocalDateTime.of(2018, 1, 1, 0, 0));
        appSecurityEntity.setCreatedBy("sec-creator");
        appSecurityEntity.setUpdatedAt(LocalDateTime.of(2018, 2, 1, 0, 0));
        appSecurityEntity.setUpdatedBy("sec-updater");
        appSecurityEntity.setDeletedAt(LocalDateTime.of(2018, 3, 1, 0, 0));
        appSecurityEntity.setDeletedBy("sec-deleter");
        return appSecurityEntity;
    }

    private LkupSecurityLevelEntity buildLevelEntity() {
        LkupSecurityLevelEntity levelEntity = new LkupSecurityLevelEntity();
        levelEntity.setId(30L);
        levelEntity.setName("Alt");
        levelEntity.setNameEs("Alto");
        return levelEntity;
    }

    private FieldEntity buildFieldEntity() {
        FieldEntity fieldEntity = new FieldEntity();
        fieldEntity.setId(40L);
        fieldEntity.setName("Hisenda");
        fieldEntity.setNameEs("Hacienda");
        return fieldEntity;
    }

    private AppSecurityRiskEntity buildDeepEntity() {
        AppSecurityRiskEntity entity = new AppSecurityRiskEntity();
        entity.setId(1L);
        entity.setAppSecurity(buildAppSecurityEntity());
        entity.setLevel(buildLevelEntity());
        entity.setDescription("Risk description");
        entity.setField(buildFieldEntity());
        entity.setCreatedAt(LocalDateTime.of(2017, 1, 1, 0, 0));
        entity.setCreatedBy("risk-creator");
        entity.setUpdatedAt(LocalDateTime.of(2017, 2, 1, 0, 0));
        entity.setUpdatedBy("risk-updater");
        entity.setDeletedAt(LocalDateTime.of(2017, 3, 1, 0, 0));
        entity.setDeletedBy("risk-deleter");
        return entity;
    }

    // ------------------------------------------------------------------
    // Model graph builders
    // ------------------------------------------------------------------

    private Application buildApplicationModel() {
        Application application = new Application();
        application.setId(420L);
        application.setCode("APP02");
        application.setPrefix("AP2");
        application.setName("Application Two");
        return application;
    }

    private AppSecurity buildAppSecurityModel() {
        AppSecurity appSecurity = new AppSecurity();
        appSecurity.setId(11L);
        appSecurity.setApplication(buildApplicationModel());
        appSecurity.setObservation("Model observation");
        appSecurity.setCreatedAt(LocalDateTime.of(2023, 1, 1, 0, 0));
        appSecurity.setCreatedBy("sec-creator");
        appSecurity.setUpdatedAt(LocalDateTime.of(2023, 2, 1, 0, 0));
        appSecurity.setUpdatedBy("sec-updater");
        appSecurity.setDeletedAt(LocalDateTime.of(2023, 3, 1, 0, 0));
        appSecurity.setDeletedBy("sec-deleter");
        return appSecurity;
    }

    private SecurityLevel buildLevelModel() {
        SecurityLevel level = new SecurityLevel();
        level.setId(31L);
        level.setName("Mitjà");
        level.setNameEs("Medio");
        return level;
    }

    private Field buildFieldModel() {
        Field field = new Field();
        field.setId(41L);
        field.setName("Educacio");
        field.setNameEs("Educacion");
        return field;
    }

    private AppSecurityRisk buildDeepModel() {
        AppSecurityRisk model = new AppSecurityRisk();
        model.setId(2L);
        model.setAppSecurity(buildAppSecurityModel());
        model.setLevel(buildLevelModel());
        model.setDescription("Model risk description");
        model.setField(buildFieldModel());
        model.setCreatedAt(LocalDateTime.of(2016, 1, 1, 0, 0));
        model.setCreatedBy("risk-creator");
        model.setUpdatedAt(LocalDateTime.of(2016, 2, 1, 0, 0));
        model.setUpdatedBy("risk-updater");
        model.setDeletedAt(LocalDateTime.of(2016, 3, 1, 0, 0));
        model.setDeletedBy("risk-deleter");
        return model;
    }

    // ------------------------------------------------------------------
    // toModel
    // ------------------------------------------------------------------

    @Test
    void toModel_deepEntityGraph_mapsEveryNestedField() {
        AppSecurityRiskEntity entity = buildDeepEntity();

        AppSecurityRisk model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Risk description", model.getDescription());
        assertEquals("risk-creator", model.getCreatedBy());
        assertEquals("risk-deleter", model.getDeletedBy());

        AppSecurity appSecurity = model.getAppSecurity();
        assertEquals(10L, appSecurity.getId());
        assertEquals("Security observation", appSecurity.getObservation());

        Application application = appSecurity.getApplication();
        assertEquals(205L, application.getId());
        assertEquals("APP01", application.getCode());

        SecurityLevel level = model.getLevel();
        assertEquals(30L, level.getId());
        assertEquals("Alt", level.getName());
        assertEquals("Alto", level.getNameEs());

        Field field = model.getField();
        assertEquals(40L, field.getId());
        assertEquals("Hisenda", field.getName());
        assertEquals("Hacienda", field.getNameEs());
    }

    @Test
    void toModel_nullOptionalNestedForeignKeys_producesNullNestedModelFields() {
        AppSecurityRiskEntity entity = new AppSecurityRiskEntity();
        entity.setId(1L);
        entity.setAppSecurity(buildAppSecurityEntity());
        entity.setLevel(null);
        entity.setField(null);

        AppSecurityRisk model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertNull(model.getLevel());
        assertNull(model.getField());
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
        AppSecurityRisk model = buildDeepModel();

        AppSecurityRiskEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Model risk description", entity.getDescription());
        assertEquals("risk-creator", entity.getCreatedBy());

        AppSecurityEntity appSecurityEntity = entity.getAppSecurity();
        assertEquals(11L, appSecurityEntity.getId());
        assertEquals("Model observation", appSecurityEntity.getObservation());

        ApplicationEntity applicationEntity = appSecurityEntity.getApplication();
        assertEquals(420L, applicationEntity.getId());
        assertEquals("APP02", applicationEntity.getCode());

        LkupSecurityLevelEntity levelEntity = entity.getLevel();
        assertEquals(31L, levelEntity.getId());
        assertEquals("Mitjà", levelEntity.getName());

        FieldEntity fieldEntity = entity.getField();
        assertEquals(41L, fieldEntity.getId());
        assertEquals("Educacio", fieldEntity.getName());
    }

    @Test
    void toEntity_nullOptionalNestedFields_producesNullNestedEntityFields() {
        AppSecurityRisk model = new AppSecurityRisk();
        model.setId(2L);
        model.setAppSecurity(buildAppSecurityModel());
        model.setLevel(null);
        model.setField(null);

        AppSecurityRiskEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertNull(entity.getLevel());
        assertNull(entity.getField());
    }

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    // ------------------------------------------------------------------
    // toResponse
    // ------------------------------------------------------------------

    @Test
    void toResponse_deepModel_flattensEveryNestedFieldIntoOutputDTO() {
        AppSecurityRisk model = buildDeepModel();

        AppSecurityRiskOutputDTO response = mapper.toResponse(model);

        assertEquals(2L, response.getId());
        assertEquals("Model risk description", response.getDescription());
        assertEquals(LocalDateTime.of(2016, 3, 1, 0, 0), response.getDeletedAt());

        AppSecurityOutputDTO appSecurityOutputDTO = response.getAppSecurity();
        assertEquals(11L, appSecurityOutputDTO.getId());
        assertEquals("Model observation", appSecurityOutputDTO.getObservation());
        assertEquals(420L, appSecurityOutputDTO.getApplication().getId());

        SecurityLevelOutputDTO levelOutputDTO = response.getLevel();
        assertEquals(31L, levelOutputDTO.getId());
        assertEquals("Mitjà", levelOutputDTO.getName());

        FieldOutputDTO fieldOutputDTO = response.getField();
        assertEquals(41L, fieldOutputDTO.getId());
        assertEquals("Educacio", fieldOutputDTO.getName());
    }

    @Test
    void toResponse_nullOptionalNestedFields_leavesResponseNestedFieldsNull() {
        AppSecurityRisk model = new AppSecurityRisk();
        model.setId(3L);
        model.setAppSecurity(buildAppSecurityModel());
        model.setLevel(null);
        model.setField(null);

        AppSecurityRiskOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertNull(response.getLevel());
        assertNull(response.getField());
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
        AppSecurityRiskInputDTO inputDTO = new AppSecurityRiskInputDTO(50L, 60L, "Some risk", 70L);

        AppSecurityRisk model = mapper.toModelFromInput(inputDTO);

        assertEquals(50L, model.getAppSecurity().getId());
        assertEquals(60L, model.getLevel().getId());
        assertEquals("Some risk", model.getDescription());
        assertEquals(70L, model.getField().getId());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getCreatedBy());
        assertNull(model.getUpdatedAt());
        assertNull(model.getUpdatedBy());
        assertNull(model.getDeletedAt());
        assertNull(model.getDeletedBy());
    }

    @Test
    void toModelFromInput_optionalFksNull_createsNestedObjectsWithNullId() {
        // NullValuePropertyMappingStrategy.IGNORE only skips the setter call on an
        // already-existing @MappingTarget (see updateModelFromInput); when building a brand
        // new model there is no prior state to preserve, so MapStruct still instantiates the
        // nested level/field objects, just with a null id. This mirrors the generated
        // AppSecurityMapperImpl#appSecurityInputDTOToApplication behavior for its own
        // IGNORE-strategy applicationId mapping.
        AppSecurityRiskInputDTO inputDTO = new AppSecurityRiskInputDTO(50L, null, "Some risk", null);

        AppSecurityRisk model = mapper.toModelFromInput(inputDTO);

        assertEquals(50L, model.getAppSecurity().getId());
        assertNull(model.getLevel().getId());
        assertNull(model.getField().getId());
    }

    @Test
    void toModelFromInput_null_returnsNull() {
        assertNull(mapper.toModelFromInput(null));
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        AppSecurity existingAppSecurity = new AppSecurity();
        existingAppSecurity.setId(13L);
        SecurityLevel existingLevel = new SecurityLevel();
        existingLevel.setId(23L);
        Field existingField = new Field();
        existingField.setId(33L);
        AppSecurityRisk existing = new AppSecurityRisk();
        existing.setId(4L);
        existing.setAppSecurity(existingAppSecurity);
        existing.setLevel(existingLevel);
        existing.setField(existingField);
        AppSecurityRiskInputDTO inputDTO = new AppSecurityRiskInputDTO(70L, 80L, "Updated risk", 90L);

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals(70L, existing.getAppSecurity().getId());
        assertEquals(80L, existing.getLevel().getId());
        assertEquals("Updated risk", existing.getDescription());
        assertEquals(90L, existing.getField().getId());
    }

    @Test
    void updateModelFromInput_withNullNestedTargets_initializesThem() {
        AppSecurityRisk existing = new AppSecurityRisk();
        existing.setId(5L);
        existing.setAppSecurity(null);
        existing.setLevel(null);
        existing.setField(null);
        AppSecurityRiskInputDTO inputDTO = new AppSecurityRiskInputDTO(91L, 92L, "New risk", 93L);

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(5L, existing.getId());
        assertEquals(91L, existing.getAppSecurity().getId());
        assertEquals(92L, existing.getLevel().getId());
        assertEquals(93L, existing.getField().getId());
    }

    @Test
    void updateModelFromInput_optionalFksNull_leavesExistingNestedTargetsUntouched() {
        SecurityLevel existingLevel = new SecurityLevel();
        existingLevel.setId(23L);
        Field existingField = new Field();
        existingField.setId(33L);
        AppSecurityRisk existing = new AppSecurityRisk();
        existing.setId(6L);
        existing.setLevel(existingLevel);
        existing.setField(existingField);
        AppSecurityRiskInputDTO inputDTO = new AppSecurityRiskInputDTO(70L, null, "Updated risk", null);

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(6L, existing.getId());
        assertEquals(70L, existing.getAppSecurity().getId());
        assertEquals(23L, existing.getLevel().getId());
        assertEquals(33L, existing.getField().getId());
    }

    @Test
    void updateModelFromInput_nullInput_doesNothing() {
        AppSecurity existingAppSecurity = new AppSecurity();
        existingAppSecurity.setId(13L);
        AppSecurityRisk existing = new AppSecurityRisk();
        existing.setId(7L);
        existing.setAppSecurity(existingAppSecurity);

        mapper.updateModelFromInput(null, existing);

        assertEquals(7L, existing.getId());
        assertEquals(13L, existing.getAppSecurity().getId());
    }
}
