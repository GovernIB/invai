package es.caib.invai.back.service.mapper.application.security.webContext;

import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import es.caib.invai.back.interna.application.development.core.DTO.DevelopmentOutputDTO;
import es.caib.invai.back.interna.application.development.webContext.DTO.AppWebContextInputDTO;
import es.caib.invai.back.interna.application.security.core.DTO.AppSecurityOutputDTO;
import es.caib.invai.back.interna.application.development.webContext.DTO.AppWebContextOutputDTO;
import es.caib.invai.back.interna.maintenance.general.field.DTO.FieldOutputDTO;
import es.caib.invai.back.interna.maintenance.security.webContext.DTO.WebContextOutputDTO;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.application.development.core.AppDevelopmentEntity;
import es.caib.invai.back.persistence.model.application.security.core.AppSecurityEntity;
import es.caib.invai.back.persistence.model.application.security.webContext.AppWebContextEntity;
import es.caib.invai.back.persistence.model.maintenance.general.field.FieldEntity;
import es.caib.invai.back.persistence.model.maintenance.security.webContext.WebContextEntity;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapperImpl;
import es.caib.invai.back.service.mapper.application.development.core.AppDevelopmentMapperImpl;
import es.caib.invai.back.service.mapper.application.security.core.AppSecurityMapperImpl;
import es.caib.invai.back.service.mapper.catalog.modality.ModalityMapperImpl;
import es.caib.invai.back.service.mapper.catalog.standardAdaption.StandardAdaptionMapperImpl;
import es.caib.invai.back.service.mapper.catalog.status.StatusMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.category.CategoryMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.commission.CommissionMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.field.FieldMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.systemType.SystemTypeMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.security.webContext.WebContextMapperImpl;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
import es.caib.invai.back.service.model.application.security.webContext.AppWebContext;
import es.caib.invai.back.service.model.maintenance.general.field.Field;
import es.caib.invai.back.service.model.maintenance.security.webContext.WebContext;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the generated {@link AppWebContextMapperImpl}, exercising every conversion
 * direction declared on {@link AppWebContextMapper}.
 * <p>
 * {@link AppWebContextMapper} declares
 * {@code uses = {AppDevelopmentMapper.class, AppSecurityMapper.class, WebContextMapper.class, FieldMapper.class}},
 * so the generated implementation carries {@code @Autowired}
 * {@link es.caib.invai.back.service.mapper.application.development.core.AppDevelopmentMapper},
 * {@link es.caib.invai.back.service.mapper.application.security.core.AppSecurityMapper},
 * {@link es.caib.invai.back.service.mapper.maintenance.security.webContext.WebContextMapper}, and
 * {@link es.caib.invai.back.service.mapper.maintenance.general.field.FieldMapper} fields. Since no
 * Spring context is bootstrapped here, fully wired implementations (including the deep
 * {@link AppSecurityMapperImpl}/{@link AppDevelopmentMapperImpl} -&gt; {@link ApplicationMapperImpl}
 * sub-graphs) are injected manually via {@link ReflectionTestUtils}, following the same pattern used
 * in {@code AppDatabaseMapperTest}.
 * </p>
 * <p>
 * Note that two independent {@link FieldMapperImpl} instances are wired: one nested inside
 * {@link ApplicationMapperImpl} (for {@code Application.field}), and one directly on
 * {@link AppWebContextMapperImpl} (for the entity's own {@code field}). The tests below use
 * distinct identifiers for each to catch any mapping mix-up.
 * </p>
 */
class AppWebContextMapperTest {

    private AppWebContextMapper mapper;

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

        AppDevelopmentMapperImpl appDevelopmentMapperImpl = new AppDevelopmentMapperImpl();
        ReflectionTestUtils.setField(appDevelopmentMapperImpl, "modalityMapper", new ModalityMapperImpl());
        ReflectionTestUtils.setField(appDevelopmentMapperImpl, "standardAdaptionMapper", new StandardAdaptionMapperImpl());
        ReflectionTestUtils.setField(appDevelopmentMapperImpl, "applicationMapper", applicationMapperImpl);

        AppWebContextMapperImpl impl = new AppWebContextMapperImpl();
        ReflectionTestUtils.setField(impl, "appSecurityMapper", appSecurityMapperImpl);
        ReflectionTestUtils.setField(impl, "appDevelopmentMapper", appDevelopmentMapperImpl);
        ReflectionTestUtils.setField(impl, "webContextMapper", new WebContextMapperImpl());
        ReflectionTestUtils.setField(impl, "fieldMapper", new FieldMapperImpl());
        mapper = impl;
    }

    // ------------------------------------------------------------------
    // Entity graph builders
    // ------------------------------------------------------------------

    private static @NonNull ApplicationEntity getApplicationEntity() {
        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(205L);
        applicationEntity.setCode("APP01");
        applicationEntity.setPrefix("AP1");
        applicationEntity.setName("Application One");
        applicationEntity.setCreatedAt(LocalDateTime.of(2019, 1, 6, 0, 0));
        applicationEntity.setCreatedBy("app-creator");
        applicationEntity.setUpdatedAt(LocalDateTime.of(2019, 2, 6, 0, 0));
        applicationEntity.setUpdatedBy("app-updater");
        applicationEntity.setDeletedAt(LocalDateTime.of(2019, 3, 6, 0, 0));
        applicationEntity.setDeletedBy("app-deleter");
        return applicationEntity;
    }

    private static @NonNull AppSecurityEntity getAppSecurityEntity() {
        AppSecurityEntity appSecurityEntity = new AppSecurityEntity();
        appSecurityEntity.setId(100L);
        appSecurityEntity.setApplication(getApplicationEntity());
        appSecurityEntity.setObservation("Security observation");
        appSecurityEntity.setCreatedAt(LocalDateTime.of(2018, 1, 1, 0, 0));
        appSecurityEntity.setCreatedBy("sec-creator");
        appSecurityEntity.setUpdatedAt(LocalDateTime.of(2018, 2, 1, 0, 0));
        appSecurityEntity.setUpdatedBy("sec-updater");
        appSecurityEntity.setDeletedAt(LocalDateTime.of(2018, 3, 1, 0, 0));
        appSecurityEntity.setDeletedBy("sec-deleter");
        return appSecurityEntity;
    }

    private static @NonNull AppDevelopmentEntity getAppDevelopmentEntity() {
        AppDevelopmentEntity appDevelopmentEntity = new AppDevelopmentEntity();
        appDevelopmentEntity.setId(150L);
        appDevelopmentEntity.setApplication(getApplicationEntity());
        appDevelopmentEntity.setCode("https://repo.example/app");
        appDevelopmentEntity.setCreatedAt(LocalDateTime.of(2018, 1, 2, 0, 0));
        appDevelopmentEntity.setCreatedBy("dev-creator");
        return appDevelopmentEntity;
    }

    private static @NonNull WebContextEntity getWebContextEntity() {
        WebContextEntity webContextEntity = new WebContextEntity();
        webContextEntity.setId(300L);
        webContextEntity.setName("Firmar peticiones");
        webContextEntity.setNameEs("Firmar peticiones ES");
        webContextEntity.setCreatedAt(LocalDateTime.of(2019, 1, 7, 0, 0));
        webContextEntity.setCreatedBy("wc-creator");
        webContextEntity.setUpdatedAt(LocalDateTime.of(2019, 2, 7, 0, 0));
        webContextEntity.setUpdatedBy("wc-updater");
        webContextEntity.setDeletedAt(LocalDateTime.of(2019, 3, 7, 0, 0));
        webContextEntity.setDeletedBy("wc-deleter");
        return webContextEntity;
    }

    private static @NonNull FieldEntity getFieldEntity() {
        FieldEntity fieldEntity = new FieldEntity();
        fieldEntity.setId(400L);
        fieldEntity.setName("Hisenda");
        fieldEntity.setNameEs("Hacienda");
        fieldEntity.setCreatedAt(LocalDateTime.of(2019, 1, 8, 0, 0));
        fieldEntity.setCreatedBy("field-creator");
        fieldEntity.setUpdatedAt(LocalDateTime.of(2019, 2, 8, 0, 0));
        fieldEntity.setUpdatedBy("field-updater");
        fieldEntity.setDeletedAt(LocalDateTime.of(2019, 3, 8, 0, 0));
        fieldEntity.setDeletedBy("field-deleter");
        return fieldEntity;
    }

    private AppWebContextEntity buildDeepEntity() {
        AppWebContextEntity entity = new AppWebContextEntity();
        entity.setId(1L);
        entity.setAppSecurity(getAppSecurityEntity());
        entity.setAppDevelopment(getAppDevelopmentEntity());
        entity.setWebContext(getWebContextEntity());
        entity.setField(getFieldEntity());
        entity.setObservation("Web context observation");
        entity.setValidated(true);
        entity.setValidatedAt(LocalDateTime.of(2024, 1, 1, 0, 0));
        entity.setValidatedBy("validator");
        entity.setValidatedReason("looks fine");
        entity.setCreatedAt(LocalDateTime.of(2017, 1, 1, 0, 0));
        entity.setCreatedBy("awc-creator");
        entity.setUpdatedAt(LocalDateTime.of(2017, 2, 1, 0, 0));
        entity.setUpdatedBy("awc-updater");
        entity.setDeletedAt(LocalDateTime.of(2017, 3, 1, 0, 0));
        entity.setDeletedBy("awc-deleter");
        return entity;
    }

    // ------------------------------------------------------------------
    // Model graph builders
    // ------------------------------------------------------------------

    private static @NonNull Application getApplication() {
        Application application = new Application();
        application.setId(420L);
        application.setCode("APP02");
        application.setPrefix("AP2");
        application.setName("Application Two");
        application.setCreatedAt(LocalDateTime.of(2022, 1, 6, 0, 0));
        application.setCreatedBy("app-creator");
        application.setUpdatedAt(LocalDateTime.of(2022, 2, 6, 0, 0));
        application.setUpdatedBy("app-updater");
        application.setDeletedAt(LocalDateTime.of(2022, 3, 6, 0, 0));
        application.setDeletedBy("app-deleter");
        return application;
    }

    private static @NonNull AppSecurity getAppSecurity() {
        AppSecurity appSecurity = new AppSecurity();
        appSecurity.setId(110L);
        appSecurity.setApplication(getApplication());
        appSecurity.setObservation("Model security observation");
        appSecurity.setCreatedAt(LocalDateTime.of(2023, 1, 1, 0, 0));
        appSecurity.setCreatedBy("sec-creator");
        appSecurity.setUpdatedAt(LocalDateTime.of(2023, 2, 1, 0, 0));
        appSecurity.setUpdatedBy("sec-updater");
        appSecurity.setDeletedAt(LocalDateTime.of(2023, 3, 1, 0, 0));
        appSecurity.setDeletedBy("sec-deleter");
        return appSecurity;
    }

    private static @NonNull AppDevelopment getAppDevelopment() {
        return AppDevelopment.builder()
                .id(160L)
                .application(getApplication())
                .code("https://repo.example/app-two")
                .createdAt(LocalDateTime.of(2023, 1, 2, 0, 0))
                .createdBy("dev-creator")
                .build();
    }

    private static @NonNull WebContext getWebContext() {
        WebContext webContext = new WebContext();
        webContext.setId(310L);
        webContext.setName("Validar dades");
        webContext.setNameEs("Validar datos");
        webContext.setCreatedAt(LocalDateTime.of(2022, 1, 7, 0, 0));
        webContext.setCreatedBy("wc-creator");
        webContext.setUpdatedAt(LocalDateTime.of(2022, 2, 7, 0, 0));
        webContext.setUpdatedBy("wc-updater");
        webContext.setDeletedAt(LocalDateTime.of(2022, 3, 7, 0, 0));
        webContext.setDeletedBy("wc-deleter");
        return webContext;
    }

    private static @NonNull Field getField() {
        Field field = new Field();
        field.setId(410L);
        field.setName("Educacio");
        field.setNameEs("Educacion");
        field.setCreatedAt(LocalDateTime.of(2022, 1, 8, 0, 0));
        field.setCreatedBy("field-creator");
        field.setUpdatedAt(LocalDateTime.of(2022, 2, 8, 0, 0));
        field.setUpdatedBy("field-updater");
        field.setDeletedAt(LocalDateTime.of(2022, 3, 8, 0, 0));
        field.setDeletedBy("field-deleter");
        return field;
    }

    private AppWebContext buildDeepModel() {
        AppWebContext model = new AppWebContext();
        model.setId(2L);
        model.setAppSecurity(getAppSecurity());
        model.setAppDevelopment(getAppDevelopment());
        model.setWebContext(getWebContext());
        model.setField(getField());
        model.setObservation("Model web context observation");
        model.setValidated(true);
        model.setValidatedAt(LocalDateTime.of(2024, 2, 1, 0, 0));
        model.setValidatedBy("model-validator");
        model.setValidatedReason("model looks fine");
        model.setCreatedAt(LocalDateTime.of(2016, 1, 1, 0, 0));
        model.setCreatedBy("awc-creator");
        model.setUpdatedAt(LocalDateTime.of(2016, 2, 1, 0, 0));
        model.setUpdatedBy("awc-updater");
        model.setDeletedAt(LocalDateTime.of(2016, 3, 1, 0, 0));
        model.setDeletedBy("awc-deleter");
        return model;
    }

    // ------------------------------------------------------------------
    // toModel
    // ------------------------------------------------------------------

    @Test
    void toModel_deepEntityGraph_mapsEveryNestedField() {
        AppWebContextEntity entity = buildDeepEntity();

        AppWebContext model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Web context observation", model.getObservation());
        assertTrue(model.isValidated());
        assertEquals(LocalDateTime.of(2024, 1, 1, 0, 0), model.getValidatedAt());
        assertEquals("validator", model.getValidatedBy());
        assertEquals("looks fine", model.getValidatedReason());
        assertEquals("awc-creator", model.getCreatedBy());
        assertEquals("awc-updater", model.getUpdatedBy());
        assertEquals("awc-deleter", model.getDeletedBy());
        assertEquals(LocalDateTime.of(2017, 1, 1, 0, 0), model.getCreatedAt());
        assertEquals(LocalDateTime.of(2017, 2, 1, 0, 0), model.getUpdatedAt());
        assertEquals(LocalDateTime.of(2017, 3, 1, 0, 0), model.getDeletedAt());

        AppSecurity appSecurity = model.getAppSecurity();
        assertEquals(100L, appSecurity.getId());
        assertEquals("Security observation", appSecurity.getObservation());
        assertEquals("sec-creator", appSecurity.getCreatedBy());

        Application application = appSecurity.getApplication();
        assertEquals(205L, application.getId());
        assertEquals("APP01", application.getCode());
        assertEquals("AP1", application.getPrefix());
        assertEquals("Application One", application.getName());

        AppDevelopment appDevelopment = model.getAppDevelopment();
        assertEquals(150L, appDevelopment.getId());
        assertEquals("https://repo.example/app", appDevelopment.getCode());
        assertEquals(205L, appDevelopment.getApplication().getId());

        WebContext webContext = model.getWebContext();
        assertEquals(300L, webContext.getId());
        assertEquals("Firmar peticiones", webContext.getName());
        assertEquals("Firmar peticiones ES", webContext.getNameEs());

        Field field = model.getField();
        assertEquals(400L, field.getId());
        assertEquals("Hisenda", field.getName());
        assertEquals("Hacienda", field.getNameEs());
    }

    @Test
    void toModel_nullNestedForeignKeys_producesNullNestedModelFields() {
        AppWebContextEntity entity = new AppWebContextEntity();
        entity.setId(1L);
        entity.setAppSecurity(null);
        entity.setAppDevelopment(null);
        entity.setWebContext(null);
        entity.setField(null);

        AppWebContext model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertNull(model.getAppSecurity());
        assertNull(model.getAppDevelopment());
        assertNull(model.getWebContext());
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
        AppWebContext model = buildDeepModel();

        AppWebContextEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Model web context observation", entity.getObservation());
        assertTrue(entity.isValidated());
        assertEquals(LocalDateTime.of(2024, 2, 1, 0, 0), entity.getValidatedAt());
        assertEquals("model-validator", entity.getValidatedBy());
        assertEquals("model looks fine", entity.getValidatedReason());
        assertEquals("awc-creator", entity.getCreatedBy());
        assertEquals(LocalDateTime.of(2016, 1, 1, 0, 0), entity.getCreatedAt());

        AppSecurityEntity appSecurityEntity = entity.getAppSecurity();
        assertEquals(110L, appSecurityEntity.getId());
        assertEquals("Model security observation", appSecurityEntity.getObservation());

        ApplicationEntity applicationEntity = appSecurityEntity.getApplication();
        assertEquals(420L, applicationEntity.getId());
        assertEquals("APP02", applicationEntity.getCode());
        assertEquals("Application Two", applicationEntity.getName());

        AppDevelopmentEntity appDevelopmentEntity = entity.getAppDevelopment();
        assertEquals(160L, appDevelopmentEntity.getId());
        assertEquals("https://repo.example/app-two", appDevelopmentEntity.getCode());

        WebContextEntity webContextEntity = entity.getWebContext();
        assertEquals(310L, webContextEntity.getId());
        assertEquals("Validar dades", webContextEntity.getName());
        assertEquals("Validar datos", webContextEntity.getNameEs());

        FieldEntity fieldEntity = entity.getField();
        assertEquals(410L, fieldEntity.getId());
        assertEquals("Educacio", fieldEntity.getName());
        assertEquals("Educacion", fieldEntity.getNameEs());
    }

    @Test
    void toEntity_nullNestedFields_producesNullNestedEntityFields() {
        AppWebContext model = new AppWebContext();
        model.setId(2L);
        model.setAppSecurity(null);
        model.setAppDevelopment(null);
        model.setWebContext(null);
        model.setField(null);

        AppWebContextEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertNull(entity.getAppSecurity());
        assertNull(entity.getAppDevelopment());
        assertNull(entity.getWebContext());
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
        AppWebContext model = buildDeepModel();

        AppWebContextOutputDTO response = mapper.toResponse(model);

        assertEquals(2L, response.getId());
        assertEquals("Model web context observation", response.getObservation());
        assertTrue(response.isValidated());
        assertEquals(LocalDateTime.of(2024, 2, 1, 0, 0), response.getValidatedAt());
        assertEquals("model-validator", response.getValidatedBy());
        assertEquals("model looks fine", response.getValidatedReason());
        assertEquals(LocalDateTime.of(2016, 3, 1, 0, 0), response.getDeletedAt());

        AppSecurityOutputDTO appSecurityOutputDTO = response.getAppSecurity();
        assertEquals(110L, appSecurityOutputDTO.getId());
        assertEquals("Model security observation", appSecurityOutputDTO.getObservation());

        ApplicationOutputDTO applicationOutputDTO = appSecurityOutputDTO.getApplication();
        assertEquals(420L, applicationOutputDTO.getId());
        assertEquals("APP02", applicationOutputDTO.getCode());
        assertEquals("Application Two", applicationOutputDTO.getName());

        DevelopmentOutputDTO developmentOutputDTO = response.getAppDevelopment();
        assertEquals(160L, developmentOutputDTO.getId());
        assertEquals("https://repo.example/app-two", developmentOutputDTO.getCode());

        WebContextOutputDTO webContextOutputDTO = response.getWebContext();
        assertEquals(310L, webContextOutputDTO.getId());
        assertEquals("Validar dades", webContextOutputDTO.getName());
        assertEquals("Validar datos", webContextOutputDTO.getNameEs());

        FieldOutputDTO fieldOutputDTO = response.getField();
        assertEquals(410L, fieldOutputDTO.getId());
        assertEquals("Educacio", fieldOutputDTO.getName());
        assertEquals("Educacion", fieldOutputDTO.getNameEs());
    }

    @Test
    void toResponse_nullNestedFields_leavesResponseNestedFieldsNull() {
        AppWebContext model = new AppWebContext();
        model.setId(3L);
        model.setAppSecurity(null);
        model.setAppDevelopment(null);
        model.setWebContext(null);
        model.setField(null);

        AppWebContextOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertNull(response.getAppSecurity());
        assertNull(response.getAppDevelopment());
        assertNull(response.getWebContext());
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
    void toModelFromInput_resolvesDevelopmentAnchorAndIgnoresSecurityAnchorAndValidationFields() {
        AppWebContextInputDTO inputDTO = new AppWebContextInputDTO(50L, 60L, 70L, "some obs", null);

        AppWebContext model = mapper.toModelFromInput(inputDTO);

        assertEquals(50L, model.getAppDevelopment().getId());
        assertEquals(60L, model.getWebContext().getId());
        assertEquals(70L, model.getField().getId());
        assertEquals("some obs", model.getObservation());
        // The security anchor is never populated from this (Development-only) input DTO - the
        // facade resolves and sets it itself.
        assertNull(model.getAppSecurity());
        // Validation fields are only ever set by Security's validate flow, never from create/update.
        assertFalse(model.isValidated());
        assertNull(model.getValidatedAt());
        assertNull(model.getValidatedBy());
        assertNull(model.getValidatedReason());
        assertNull(model.getId());
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
    void updateModelFromInput_mergesFieldsWithoutTouchingIdOrSecurityAnchor() {
        AppSecurity existingAppSecurity = new AppSecurity();
        existingAppSecurity.setId(13L);
        AppDevelopment existingAppDevelopment = AppDevelopment.builder().id(14L).build();
        WebContext existingWebContext = new WebContext();
        existingWebContext.setId(23L);
        Field existingField = new Field();
        existingField.setId(33L);
        AppWebContext existing = new AppWebContext();
        existing.setId(4L);
        existing.setAppSecurity(existingAppSecurity);
        existing.setAppDevelopment(existingAppDevelopment);
        existing.setWebContext(existingWebContext);
        existing.setField(existingField);
        AppWebContextInputDTO inputDTO = new AppWebContextInputDTO(70L, 80L, 90L, "updated obs", null);

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        // appSecurity is never touched by updateModelFromInput - still the pre-existing anchor.
        assertEquals(13L, existing.getAppSecurity().getId());
        assertEquals(70L, existing.getAppDevelopment().getId());
        assertEquals(80L, existing.getWebContext().getId());
        assertEquals(90L, existing.getField().getId());
        assertEquals("updated obs", existing.getObservation());
    }

    @Test
    void updateModelFromInput_withNullNestedTargets_initializesThem() {
        AppWebContext existing = new AppWebContext();
        existing.setId(5L);
        existing.setAppSecurity(null);
        existing.setAppDevelopment(null);
        existing.setWebContext(null);
        existing.setField(null);
        AppWebContextInputDTO inputDTO = new AppWebContextInputDTO(91L, 92L, 93L, "new obs", null);

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(5L, existing.getId());
        assertNull(existing.getAppSecurity());
        assertEquals(91L, existing.getAppDevelopment().getId());
        assertEquals(92L, existing.getWebContext().getId());
        assertEquals(93L, existing.getField().getId());
    }

    @Test
    void updateModelFromInput_nullInput_doesNothing() {
        AppSecurity existingAppSecurity = new AppSecurity();
        existingAppSecurity.setId(13L);
        AppWebContext existing = new AppWebContext();
        existing.setId(6L);
        existing.setAppSecurity(existingAppSecurity);

        mapper.updateModelFromInput(null, existing);

        assertEquals(6L, existing.getId());
        assertEquals(13L, existing.getAppSecurity().getId());
    }
}
