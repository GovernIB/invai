package es.caib.invai.back.service.mapper.application.security.measure;

import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import es.caib.invai.back.interna.application.security.core.DTO.AppSecurityOutputDTO;
import es.caib.invai.back.interna.application.security.measure.DTO.AppSecurityMeasureInputDTO;
import es.caib.invai.back.interna.application.security.measure.DTO.AppSecurityMeasureOutputDTO;
import es.caib.invai.back.interna.maintenance.security.ensRequirement.DTO.EnsRequirementOutputDTO;
import es.caib.invai.back.interna.maintenance.security.securityMeasureType.DTO.SecurityMeasureTypeOutputDTO;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.application.security.core.AppSecurityEntity;
import es.caib.invai.back.persistence.model.application.security.measure.AppSecurityMeasureEntity;
import es.caib.invai.back.persistence.model.maintenance.security.ensRequirement.EnsRequirementEntity;
import es.caib.invai.back.persistence.model.maintenance.security.securityMeasureType.SecurityMeasureTypeEntity;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapperImpl;
import es.caib.invai.back.service.mapper.application.security.core.AppSecurityMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.category.CategoryMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.commission.CommissionMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.field.FieldMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.systemType.SystemTypeMapperImpl;
import es.caib.invai.back.service.mapper.catalog.status.StatusMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.security.ensRequirement.EnsRequirementMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.security.securityMeasureType.SecurityMeasureTypeMapperImpl;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
import es.caib.invai.back.service.model.application.security.measure.AppSecurityMeasure;
import es.caib.invai.back.service.model.maintenance.security.ensRequirement.EnsRequirement;
import es.caib.invai.back.service.model.maintenance.security.securityMeasureType.SecurityMeasureType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link AppSecurityMeasureMapperImpl}, exercising every conversion
 * direction declared on {@link AppSecurityMeasureMapper}.
 * <p>
 * {@link AppSecurityMeasureMapper} declares {@code uses = {AppSecurityMapper.class,
 * SecurityMeasureTypeMapper.class, EnsRequirementMapper.class}}, so the generated implementation
 * carries {@code @Autowired} fields for each of those sub-mappers. Since no Spring context is
 * bootstrapped here, each is wired manually via {@link ReflectionTestUtils}, including the deeper
 * {@link AppSecurityMapperImpl}, which itself requires a fully wired {@link ApplicationMapperImpl}.
 * </p>
 */
class AppSecurityMeasureMapperTest {

    private AppSecurityMeasureMapper mapper;

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

        AppSecurityMeasureMapperImpl impl = new AppSecurityMeasureMapperImpl();
        ReflectionTestUtils.setField(impl, "appSecurityMapper", appSecurityMapperImpl);
        ReflectionTestUtils.setField(impl, "securityMeasureTypeMapper", new SecurityMeasureTypeMapperImpl());
        ReflectionTestUtils.setField(impl, "ensRequirementMapper", new EnsRequirementMapperImpl());
        mapper = impl;
    }

    // ------------------------------------------------------------------
    // Entity/model graph builders
    // ------------------------------------------------------------------

    private static ApplicationEntity buildApplicationEntity() {
        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(205L);
        applicationEntity.setCode("APP01");
        applicationEntity.setName("Application One");
        return applicationEntity;
    }

    private static AppSecurityEntity buildAppSecurityEntity() {
        AppSecurityEntity appSecurityEntity = new AppSecurityEntity();
        appSecurityEntity.setId(10L);
        appSecurityEntity.setApplication(buildApplicationEntity());
        appSecurityEntity.setObservation("Security anchor observation");
        appSecurityEntity.setCreatedAt(LocalDateTime.of(2024, 1, 1, 0, 0));
        appSecurityEntity.setCreatedBy("sec-creator");
        return appSecurityEntity;
    }

    private static SecurityMeasureTypeEntity buildTypeEntity() {
        SecurityMeasureTypeEntity type = new SecurityMeasureTypeEntity();
        type.setId(20L);
        type.setName("Organitzativa");
        type.setNameEs("Organizativa");
        type.setCreatedAt(LocalDateTime.of(2024, 1, 2, 0, 0));
        type.setCreatedBy("type-creator");
        return type;
    }

    private static EnsRequirementEntity buildEnsRequirementEntity() {
        EnsRequirementEntity ensRequirement = new EnsRequirementEntity();
        ensRequirement.setId(30L);
        ensRequirement.setName("op.acc.5");
        ensRequirement.setNameEs("op.acc.5 ES");
        ensRequirement.setCreatedAt(LocalDateTime.of(2024, 1, 3, 0, 0));
        ensRequirement.setCreatedBy("ens-creator");
        return ensRequirement;
    }

    private static AppSecurityMeasureEntity buildDeepEntity() {
        AppSecurityMeasureEntity entity = new AppSecurityMeasureEntity();
        entity.setId(1L);
        entity.setAppSecurity(buildAppSecurityEntity());
        entity.setType(buildTypeEntity());
        entity.setEnsRequirement(buildEnsRequirementEntity());
        entity.setDescription("Encryption at rest applied");
        entity.setCreatedAt(LocalDateTime.of(2024, 2, 1, 0, 0));
        entity.setCreatedBy("measure-creator");
        entity.setUpdatedAt(LocalDateTime.of(2024, 2, 2, 0, 0));
        entity.setUpdatedBy("measure-updater");
        entity.setDeletedAt(LocalDateTime.of(2024, 2, 3, 0, 0));
        entity.setDeletedBy("measure-deleter");
        return entity;
    }

    private static Application buildApplicationModel() {
        Application application = new Application();
        application.setId(206L);
        application.setCode("APP02");
        application.setName("Application Two");
        return application;
    }

    private static AppSecurity buildAppSecurityModel() {
        AppSecurity appSecurity = new AppSecurity();
        appSecurity.setId(11L);
        appSecurity.setApplication(buildApplicationModel());
        appSecurity.setObservation("Model security anchor observation");
        return appSecurity;
    }

    private static SecurityMeasureType buildTypeModel() {
        SecurityMeasureType type = new SecurityMeasureType();
        type.setId(21L);
        type.setName("Tecnica");
        type.setNameEs("Tecnica ES");
        return type;
    }

    private static EnsRequirement buildEnsRequirementModel() {
        EnsRequirement ensRequirement = new EnsRequirement();
        ensRequirement.setId(31L);
        ensRequirement.setName("op.exp.8");
        ensRequirement.setNameEs("op.exp.8 ES");
        return ensRequirement;
    }

    private static AppSecurityMeasure buildDeepModel() {
        AppSecurityMeasure model = new AppSecurityMeasure();
        model.setId(2L);
        model.setAppSecurity(buildAppSecurityModel());
        model.setType(buildTypeModel());
        model.setEnsRequirement(buildEnsRequirementModel());
        model.setDescription("Multi-factor authentication enforced");
        model.setCreatedAt(LocalDateTime.of(2024, 3, 1, 0, 0));
        model.setCreatedBy("measure-creator2");
        model.setUpdatedAt(LocalDateTime.of(2024, 3, 2, 0, 0));
        model.setUpdatedBy("measure-updater2");
        model.setDeletedAt(LocalDateTime.of(2024, 3, 3, 0, 0));
        model.setDeletedBy("measure-deleter2");
        return model;
    }

    // ------------------------------------------------------------------
    // toModel
    // ------------------------------------------------------------------

    @Test
    void toModel_deepEntityGraph_mapsEveryNestedField() {
        AppSecurityMeasureEntity entity = buildDeepEntity();

        AppSecurityMeasure model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Encryption at rest applied", model.getDescription());
        assertEquals("measure-creator", model.getCreatedBy());
        assertEquals("measure-updater", model.getUpdatedBy());
        assertEquals("measure-deleter", model.getDeletedBy());

        AppSecurity appSecurity = model.getAppSecurity();
        assertEquals(10L, appSecurity.getId());
        assertEquals("Security anchor observation", appSecurity.getObservation());
        assertEquals(205L, appSecurity.getApplication().getId());
        assertEquals("APP01", appSecurity.getApplication().getCode());
        assertEquals("Application One", appSecurity.getApplication().getName());

        SecurityMeasureType type = model.getType();
        assertEquals(20L, type.getId());
        assertEquals("Organitzativa", type.getName());
        assertEquals("Organizativa", type.getNameEs());

        EnsRequirement ensRequirement = model.getEnsRequirement();
        assertEquals(30L, ensRequirement.getId());
        assertEquals("op.acc.5", ensRequirement.getName());
        assertEquals("op.acc.5 ES", ensRequirement.getNameEs());
    }

    @Test
    void toModel_nullNestedOptionalForeignKeys_producesNullNestedModelFields() {
        AppSecurityMeasureEntity entity = new AppSecurityMeasureEntity();
        entity.setId(1L);
        entity.setAppSecurity(buildAppSecurityEntity());
        entity.setType(null);
        entity.setEnsRequirement(null);

        AppSecurityMeasure model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals(10L, model.getAppSecurity().getId());
        assertNull(model.getType());
        assertNull(model.getEnsRequirement());
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
        AppSecurityMeasure model = buildDeepModel();

        AppSecurityMeasureEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Multi-factor authentication enforced", entity.getDescription());
        assertEquals("measure-creator2", entity.getCreatedBy());

        AppSecurityEntity appSecurityEntity = entity.getAppSecurity();
        assertEquals(11L, appSecurityEntity.getId());
        assertEquals("Model security anchor observation", appSecurityEntity.getObservation());
        assertEquals(206L, appSecurityEntity.getApplication().getId());
        assertEquals("APP02", appSecurityEntity.getApplication().getCode());

        SecurityMeasureTypeEntity typeEntity = entity.getType();
        assertEquals(21L, typeEntity.getId());
        assertEquals("Tecnica", typeEntity.getName());

        EnsRequirementEntity ensRequirementEntity = entity.getEnsRequirement();
        assertEquals(31L, ensRequirementEntity.getId());
        assertEquals("op.exp.8", ensRequirementEntity.getName());
    }

    @Test
    void toEntity_nullNestedOptionalFields_producesNullNestedEntityFields() {
        AppSecurityMeasure model = new AppSecurityMeasure();
        model.setId(2L);
        model.setAppSecurity(buildAppSecurityModel());
        model.setType(null);
        model.setEnsRequirement(null);

        AppSecurityMeasureEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals(11L, entity.getAppSecurity().getId());
        assertNull(entity.getType());
        assertNull(entity.getEnsRequirement());
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
        AppSecurityMeasure model = buildDeepModel();

        AppSecurityMeasureOutputDTO response = mapper.toResponse(model);

        assertEquals(2L, response.getId());
        assertEquals("Multi-factor authentication enforced", response.getDescription());
        assertEquals(LocalDateTime.of(2024, 3, 3, 0, 0), response.getDeletedAt());

        AppSecurityOutputDTO appSecurityOutputDTO = response.getAppSecurity();
        assertEquals(11L, appSecurityOutputDTO.getId());
        assertEquals("Model security anchor observation", appSecurityOutputDTO.getObservation());

        ApplicationOutputDTO applicationOutputDTO = appSecurityOutputDTO.getApplication();
        assertEquals(206L, applicationOutputDTO.getId());
        assertEquals("APP02", applicationOutputDTO.getCode());
        assertEquals("Application Two", applicationOutputDTO.getName());

        SecurityMeasureTypeOutputDTO typeOutputDTO = response.getType();
        assertEquals(21L, typeOutputDTO.getId());
        assertEquals("Tecnica", typeOutputDTO.getName());

        EnsRequirementOutputDTO ensRequirementOutputDTO = response.getEnsRequirement();
        assertEquals(31L, ensRequirementOutputDTO.getId());
        assertEquals("op.exp.8", ensRequirementOutputDTO.getName());
    }

    @Test
    void toResponse_nullNestedOptionalFields_leavesResponseNestedFieldsNull() {
        AppSecurityMeasure model = new AppSecurityMeasure();
        model.setId(3L);
        model.setAppSecurity(buildAppSecurityModel());
        model.setType(null);
        model.setEnsRequirement(null);

        AppSecurityMeasureOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertNull(response.getType());
        assertNull(response.getEnsRequirement());
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
        AppSecurityMeasureInputDTO inputDTO = new AppSecurityMeasureInputDTO(50L, 60L, 70L, "some description");

        AppSecurityMeasure model = mapper.toModelFromInput(inputDTO);

        assertEquals(50L, model.getAppSecurity().getId());
        assertEquals(60L, model.getType().getId());
        assertEquals(70L, model.getEnsRequirement().getId());
        assertEquals("some description", model.getDescription());
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
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        AppSecurity existingAppSecurity = new AppSecurity();
        existingAppSecurity.setId(13L);
        SecurityMeasureType existingType = new SecurityMeasureType();
        existingType.setId(23L);
        EnsRequirement existingEnsRequirement = new EnsRequirement();
        existingEnsRequirement.setId(33L);
        AppSecurityMeasure existing = new AppSecurityMeasure();
        existing.setId(4L);
        existing.setAppSecurity(existingAppSecurity);
        existing.setType(existingType);
        existing.setEnsRequirement(existingEnsRequirement);
        existing.setDescription("old description");
        AppSecurityMeasureInputDTO inputDTO = new AppSecurityMeasureInputDTO(70L, 80L, 90L, "new description");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals(70L, existing.getAppSecurity().getId());
        assertEquals(80L, existing.getType().getId());
        assertEquals(90L, existing.getEnsRequirement().getId());
        assertEquals("new description", existing.getDescription());
    }

    @Test
    void updateModelFromInput_withNullOptionalTypeAndEnsRequirementIds_preservesExistingNestedIds() {
        AppSecurity existingAppSecurity = new AppSecurity();
        existingAppSecurity.setId(13L);
        SecurityMeasureType existingType = new SecurityMeasureType();
        existingType.setId(23L);
        EnsRequirement existingEnsRequirement = new EnsRequirement();
        existingEnsRequirement.setId(33L);
        AppSecurityMeasure existing = new AppSecurityMeasure();
        existing.setId(5L);
        existing.setAppSecurity(existingAppSecurity);
        existing.setType(existingType);
        existing.setEnsRequirement(existingEnsRequirement);
        AppSecurityMeasureInputDTO inputDTO = new AppSecurityMeasureInputDTO(91L, null, null, "kept description");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(5L, existing.getId());
        assertEquals(91L, existing.getAppSecurity().getId());
        // Optional FK ids: source is null and the mapping declares
        // NullValuePropertyMappingStrategy.IGNORE, so the existing nested ids are preserved.
        assertEquals(23L, existing.getType().getId());
        assertEquals(33L, existing.getEnsRequirement().getId());
        assertEquals("kept description", existing.getDescription());
    }

    @Test
    void updateModelFromInput_nullInput_doesNothing() {
        AppSecurity existingAppSecurity = new AppSecurity();
        existingAppSecurity.setId(13L);
        AppSecurityMeasure existing = new AppSecurityMeasure();
        existing.setId(6L);
        existing.setAppSecurity(existingAppSecurity);

        mapper.updateModelFromInput(null, existing);

        assertEquals(6L, existing.getId());
        assertEquals(13L, existing.getAppSecurity().getId());
    }
}
