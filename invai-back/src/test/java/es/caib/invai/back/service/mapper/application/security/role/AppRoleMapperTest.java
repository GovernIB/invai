package es.caib.invai.back.service.mapper.application.security.role;

import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import es.caib.invai.back.interna.application.security.core.DTO.AppSecurityOutputDTO;
import es.caib.invai.back.interna.application.security.role.DTO.AppRoleInputDTO;
import es.caib.invai.back.interna.application.security.role.DTO.AppRoleOutputDTO;
import es.caib.invai.back.interna.maintenance.security.securityRole.DTO.SecurityRoleOutputDTO;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.application.security.core.AppSecurityEntity;
import es.caib.invai.back.persistence.model.application.security.role.AppRoleEntity;
import es.caib.invai.back.persistence.model.maintenance.security.securityRole.SecurityRoleEntity;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapperImpl;
import es.caib.invai.back.service.mapper.application.security.core.AppSecurityMapperImpl;
import es.caib.invai.back.service.mapper.catalog.status.StatusMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.category.CategoryMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.commission.CommissionMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.field.FieldMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.systemType.SystemTypeMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.security.securityRole.SecurityRoleMapperImpl;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
import es.caib.invai.back.service.model.application.security.role.AppRole;
import es.caib.invai.back.service.model.maintenance.security.securityRole.SecurityRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link AppRoleMapperImpl}, exercising every conversion
 * direction declared on {@link AppRoleMapper}.
 * <p>
 * {@link AppRoleMapper} declares {@code uses = {AppSecurityMapper.class, SecurityRoleMapper.class}},
 * so the generated implementation carries {@code @Autowired} {@link es.caib.invai.back.service.mapper.application.security.core.AppSecurityMapper}
 * and {@link es.caib.invai.back.service.mapper.maintenance.security.securityRole.SecurityRoleMapper} fields.
 * Since no Spring context is bootstrapped here, a fully wired {@link AppSecurityMapperImpl} (with its own
 * {@link ApplicationMapperImpl} sub-mapper) and a plain {@link SecurityRoleMapperImpl} are injected manually
 * via {@link ReflectionTestUtils}.
 * </p>
 */
class AppRoleMapperTest {

    private AppRoleMapper mapper;

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

        AppRoleMapperImpl impl = new AppRoleMapperImpl();
        ReflectionTestUtils.setField(impl, "appSecurityMapper", appSecurityMapperImpl);
        ReflectionTestUtils.setField(impl, "securityRoleMapper", new SecurityRoleMapperImpl());
        mapper = impl;
    }

    // ------------------------------------------------------------------
    // Entity graph builders
    // ------------------------------------------------------------------

    private AppRoleEntity buildDeepEntity() {
        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(205L);
        applicationEntity.setCode("APP01");
        applicationEntity.setPrefix("AP1");
        applicationEntity.setName("Application One");
        applicationEntity.setCreatedBy("app-creator");

        AppSecurityEntity appSecurityEntity = new AppSecurityEntity();
        appSecurityEntity.setId(10L);
        appSecurityEntity.setApplication(applicationEntity);
        appSecurityEntity.setObservation("Some observation");
        appSecurityEntity.setCreatedAt(LocalDateTime.of(2018, 1, 1, 0, 0));
        appSecurityEntity.setCreatedBy("sec-creator");
        appSecurityEntity.setUpdatedAt(LocalDateTime.of(2018, 2, 1, 0, 0));
        appSecurityEntity.setUpdatedBy("sec-updater");
        appSecurityEntity.setDeletedAt(LocalDateTime.of(2018, 3, 1, 0, 0));
        appSecurityEntity.setDeletedBy("sec-deleter");

        SecurityRoleEntity securityRoleEntity = new SecurityRoleEntity();
        securityRoleEntity.setId(20L);
        securityRoleEntity.setRoleId(120L);
        securityRoleEntity.setName("Administrator");
        securityRoleEntity.setSystem("OWS");
        securityRoleEntity.setDescription("Administrator role");
        securityRoleEntity.setCreatedAt(LocalDateTime.of(2019, 1, 1, 0, 0));
        securityRoleEntity.setCreatedBy("role-creator");

        AppRoleEntity entity = new AppRoleEntity();
        entity.setId(1L);
        entity.setAppSecurity(appSecurityEntity);
        entity.setSecurityRole(securityRoleEntity);
        entity.setCreatedAt(LocalDateTime.of(2020, 1, 1, 0, 0));
        entity.setCreatedBy("approle-creator");
        entity.setUpdatedAt(LocalDateTime.of(2020, 2, 1, 0, 0));
        entity.setUpdatedBy("approle-updater");
        entity.setDeletedAt(LocalDateTime.of(2020, 3, 1, 0, 0));
        entity.setDeletedBy("approle-deleter");
        return entity;
    }

    private AppRole buildDeepModel() {
        Application application = new Application();
        application.setId(305L);
        application.setCode("APP02");
        application.setPrefix("AP2");
        application.setName("Application Two");
        application.setCreatedBy("app-creator2");

        AppSecurity appSecurity = new AppSecurity();
        appSecurity.setId(11L);
        appSecurity.setApplication(application);
        appSecurity.setObservation("Model observation");
        appSecurity.setCreatedAt(LocalDateTime.of(2021, 1, 1, 0, 0));
        appSecurity.setCreatedBy("sec-creator2");
        appSecurity.setUpdatedAt(LocalDateTime.of(2021, 2, 1, 0, 0));
        appSecurity.setUpdatedBy("sec-updater2");
        appSecurity.setDeletedAt(LocalDateTime.of(2021, 3, 1, 0, 0));
        appSecurity.setDeletedBy("sec-deleter2");

        SecurityRole securityRole = new SecurityRole();
        securityRole.setId(21L);
        securityRole.setRoleId(121L);
        securityRole.setName("Viewer");
        securityRole.setSystem("APPX");
        securityRole.setDescription("Viewer role");

        AppRole model = new AppRole();
        model.setId(2L);
        model.setAppSecurity(appSecurity);
        model.setSecurityRole(securityRole);
        model.setCreatedAt(LocalDateTime.of(2023, 1, 1, 0, 0));
        model.setCreatedBy("approle-creator2");
        model.setUpdatedAt(LocalDateTime.of(2023, 2, 1, 0, 0));
        model.setUpdatedBy("approle-updater2");
        model.setDeletedAt(LocalDateTime.of(2023, 3, 1, 0, 0));
        model.setDeletedBy("approle-deleter2");
        return model;
    }

    // ------------------------------------------------------------------
    // toModel
    // ------------------------------------------------------------------

    @Test
    void toModel_deepEntityGraph_mapsEveryNestedField() {
        AppRoleEntity entity = buildDeepEntity();

        AppRole model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("approle-creator", model.getCreatedBy());
        assertEquals("approle-updater", model.getUpdatedBy());
        assertEquals("approle-deleter", model.getDeletedBy());
        assertEquals(LocalDateTime.of(2020, 1, 1, 0, 0), model.getCreatedAt());
        assertEquals(LocalDateTime.of(2020, 3, 1, 0, 0), model.getDeletedAt());

        AppSecurity appSecurity = model.getAppSecurity();
        assertEquals(10L, appSecurity.getId());
        assertEquals("Some observation", appSecurity.getObservation());
        assertEquals("sec-creator", appSecurity.getCreatedBy());

        Application application = appSecurity.getApplication();
        assertEquals(205L, application.getId());
        assertEquals("APP01", application.getCode());
        assertEquals("Application One", application.getName());

        SecurityRole securityRole = model.getSecurityRole();
        assertEquals(20L, securityRole.getId());
        assertEquals(120L, securityRole.getRoleId());
        assertEquals("Administrator", securityRole.getName());
        assertEquals("OWS", securityRole.getSystem());
        assertEquals("Administrator role", securityRole.getDescription());
    }

    @Test
    void toModel_nullNestedForeignKeys_producesNullNestedModelFields() {
        AppRoleEntity entity = new AppRoleEntity();
        entity.setId(1L);
        entity.setAppSecurity(null);
        entity.setSecurityRole(null);

        AppRole model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertNull(model.getAppSecurity());
        assertNull(model.getSecurityRole());
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
        AppRole model = buildDeepModel();

        AppRoleEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("approle-creator2", entity.getCreatedBy());

        AppSecurityEntity appSecurityEntity = entity.getAppSecurity();
        assertEquals(11L, appSecurityEntity.getId());
        assertEquals("Model observation", appSecurityEntity.getObservation());

        ApplicationEntity applicationEntity = appSecurityEntity.getApplication();
        assertEquals(305L, applicationEntity.getId());
        assertEquals("APP02", applicationEntity.getCode());
        assertEquals("Application Two", applicationEntity.getName());

        SecurityRoleEntity securityRoleEntity = entity.getSecurityRole();
        assertEquals(21L, securityRoleEntity.getId());
        assertEquals(121L, securityRoleEntity.getRoleId());
        assertEquals("Viewer", securityRoleEntity.getName());
        assertEquals("APPX", securityRoleEntity.getSystem());
        assertEquals("Viewer role", securityRoleEntity.getDescription());
    }

    @Test
    void toEntity_nullNestedFields_producesNullNestedEntityFields() {
        AppRole model = new AppRole();
        model.setId(2L);
        model.setAppSecurity(null);
        model.setSecurityRole(null);

        AppRoleEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertNull(entity.getAppSecurity());
        assertNull(entity.getSecurityRole());
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
        AppRole model = buildDeepModel();

        AppRoleOutputDTO response = mapper.toResponse(model);

        assertEquals(2L, response.getId());
        assertEquals(LocalDateTime.of(2023, 3, 1, 0, 0), response.getDeletedAt());

        AppSecurityOutputDTO appSecurityOutputDTO = response.getAppSecurity();
        assertEquals(11L, appSecurityOutputDTO.getId());
        assertEquals("Model observation", appSecurityOutputDTO.getObservation());

        ApplicationOutputDTO applicationOutputDTO = appSecurityOutputDTO.getApplication();
        assertEquals(305L, applicationOutputDTO.getId());
        assertEquals("APP02", applicationOutputDTO.getCode());
        assertEquals("Application Two", applicationOutputDTO.getName());

        SecurityRoleOutputDTO securityRoleOutputDTO = response.getSecurityRole();
        assertEquals(21L, securityRoleOutputDTO.getId());
        assertEquals(121L, securityRoleOutputDTO.getRoleId());
        assertEquals("Viewer", securityRoleOutputDTO.getName());
        assertEquals("APPX", securityRoleOutputDTO.getSystem());
        assertEquals("Viewer role", securityRoleOutputDTO.getDescription());
    }

    @Test
    void toResponse_nullNestedFields_leavesResponseNestedFieldsNull() {
        AppRole model = new AppRole();
        model.setId(3L);
        model.setAppSecurity(null);
        model.setSecurityRole(null);

        AppRoleOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertNull(response.getAppSecurity());
        assertNull(response.getSecurityRole());
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
        AppRoleInputDTO inputDTO = new AppRoleInputDTO(50L, 60L);

        AppRole model = mapper.toModelFromInput(inputDTO);

        assertEquals(50L, model.getAppSecurity().getId());
        assertEquals(60L, model.getSecurityRole().getId());
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
        SecurityRole existingSecurityRole = new SecurityRole();
        existingSecurityRole.setId(23L);
        AppRole existing = new AppRole();
        existing.setId(4L);
        existing.setAppSecurity(existingAppSecurity);
        existing.setSecurityRole(existingSecurityRole);
        AppRoleInputDTO inputDTO = new AppRoleInputDTO(70L, 80L);

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals(70L, existing.getAppSecurity().getId());
        assertEquals(80L, existing.getSecurityRole().getId());
    }

    @Test
    void updateModelFromInput_withNullNestedTargets_initializesThem() {
        AppRole existing = new AppRole();
        existing.setId(5L);
        existing.setAppSecurity(null);
        existing.setSecurityRole(null);
        AppRoleInputDTO inputDTO = new AppRoleInputDTO(90L, 91L);

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(5L, existing.getId());
        assertEquals(90L, existing.getAppSecurity().getId());
        assertEquals(91L, existing.getSecurityRole().getId());
    }

    @Test
    void updateModelFromInput_nullInput_doesNothing() {
        AppSecurity existingAppSecurity = new AppSecurity();
        existingAppSecurity.setId(13L);
        AppRole existing = new AppRole();
        existing.setId(6L);
        existing.setAppSecurity(existingAppSecurity);

        mapper.updateModelFromInput(null, existing);

        assertEquals(6L, existing.getId());
        assertEquals(13L, existing.getAppSecurity().getId());
    }
}
