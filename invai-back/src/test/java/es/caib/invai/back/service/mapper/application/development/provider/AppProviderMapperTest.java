package es.caib.invai.back.service.mapper.application.development.provider;

import es.caib.invai.back.interna.application.development.provider.DTO.AppProviderInputDTO;
import es.caib.invai.back.interna.application.development.provider.DTO.AppProviderOutputDTO;
import es.caib.invai.back.interna.maintenance.development.role.DTO.RoleOutputDTO;
import es.caib.invai.back.persistence.model.application.development.core.AppDevelopmentEntity;
import es.caib.invai.back.persistence.model.application.development.provider.AppProviderEntity;
import es.caib.invai.back.persistence.model.maintenance.development.role.RoleEntity;
import es.caib.invai.back.service.model.application.development.provider.AppProvider;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import es.caib.invai.back.service.model.maintenance.development.role.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;
import es.caib.invai.back.service.mapper.maintenance.development.role.RoleMapper;

/**
 * Unit tests for the generated {@link AppProviderMapperImpl}, exercising every conversion
 * direction declared on {@link AppProviderMapper}, including the {@code @AfterMapping}
 * {@code nullifyRoleWhenIdMissing} guard.
 *
 * <p>{@link AppProviderMapper} declares {@code uses = {DevelopmentMapper.class, RoleMapper.class}},
 * so the generated implementation depends on those two collaborators via field injection. Since
 * there is no Spring context in a pure unit test, the collaborators are mocked and wired into the
 * private {@code @Autowired} fields via reflection.
 */
@ExtendWith(MockitoExtension.class)
class AppProviderMapperTest {

    @Mock
    private es.caib.invai.back.service.mapper.application.development.core.AppDevelopmentMapper appDevelopmentMapper;

    @Mock
    private RoleMapper roleMapper;

    private AppProviderMapper mapper;

    @BeforeEach
    void setUp() throws Exception {
        AppProviderMapperImpl impl = new AppProviderMapperImpl();
        setField(impl, "appDevelopmentMapper", appDevelopmentMapper);
        setField(impl, "roleMapper", roleMapper);
        mapper = impl;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void toModel_mapsEveryEntityFieldAndDelegatesNestedAssociations() {
        AppProviderEntity entity = new AppProviderEntity();
        entity.setId(1L);
        entity.setCompanyName("Acme Corp");
        entity.setStartDate(LocalDateTime.of(2024, 1, 1, 0, 0));
        entity.setExpireDate(LocalDateTime.of(2025, 1, 1, 0, 0));
        entity.setCreatedAt(LocalDateTime.of(2023, 1, 1, 0, 0));
        entity.setCreatedBy("creator");
        entity.setDeletedAt(LocalDateTime.of(2023, 6, 1, 0, 0));
        entity.setDeletedBy("deleter");
        AppDevelopmentEntity devEntity = new AppDevelopmentEntity();
        RoleEntity roleEntity = new RoleEntity();
        entity.setAppDevelopment(devEntity);
        entity.setRole(roleEntity);

        AppDevelopment devModel = new AppDevelopment();
        Role roleModel = new Role();
        when(appDevelopmentMapper.toModel(devEntity)).thenReturn(devModel);
        when(roleMapper.toModel(roleEntity)).thenReturn(roleModel);

        AppProvider model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Acme Corp", model.getCompanyName());
        assertEquals(entity.getStartDate(), model.getStartDate());
        assertEquals(entity.getExpireDate(), model.getExpireDate());
        assertEquals("creator", model.getCreatedBy());
        assertEquals("deleter", model.getDeletedBy());
        assertSame(devModel, model.getAppDevelopment());
        assertSame(roleModel, model.getRole());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelFieldAndDelegatesNestedAssociations() {
        AppProvider model = new AppProvider();
        model.setId(2L);
        model.setCompanyName("Beta Ltd");
        model.setStartDate(LocalDateTime.of(2024, 2, 2, 0, 0));
        model.setExpireDate(LocalDateTime.of(2025, 2, 2, 0, 0));
        AppDevelopment devModel = new AppDevelopment();
        Role roleModel = new Role();
        model.setAppDevelopment(devModel);
        model.setRole(roleModel);

        AppDevelopmentEntity devEntity = new AppDevelopmentEntity();
        RoleEntity roleEntity = new RoleEntity();
        when(appDevelopmentMapper.toEntity(devModel)).thenReturn(devEntity);
        when(roleMapper.toEntity(roleModel)).thenReturn(roleEntity);

        AppProviderEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Beta Ltd", entity.getCompanyName());
        assertEquals(model.getStartDate(), entity.getStartDate());
        assertEquals(model.getExpireDate(), entity.getExpireDate());
        assertSame(devEntity, entity.getAppDevelopment());
        assertSame(roleEntity, entity.getRole());
    }

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toResponse_flattensDomainModelAndDelegatesRoleMapping() {
        AppProvider model = new AppProvider();
        model.setId(3L);
        model.setCompanyName("Gamma SA");
        model.setStartDate(LocalDateTime.of(2024, 3, 3, 0, 0));
        model.setExpireDate(LocalDateTime.of(2025, 3, 3, 0, 0));
        model.setDeletedAt(LocalDateTime.of(2024, 6, 1, 0, 0));
        Role roleModel = new Role();
        model.setRole(roleModel);

        RoleOutputDTO roleOutputDTO = new RoleOutputDTO();
        when(roleMapper.toResponse(roleModel)).thenReturn(roleOutputDTO);

        AppProviderOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("Gamma SA", response.getCompanyName());
        assertEquals(model.getStartDate(), response.getStartDate());
        assertEquals(model.getExpireDate(), response.getExpireDate());
        assertEquals(model.getDeletedAt(), response.getDeletedAt());
        assertSame(roleOutputDTO, response.getRole());
    }

    @Test
    void toModelFromInput_withRoleId_setsShallowIdReferences() {
        AppProviderInputDTO inputDTO = new AppProviderInputDTO(
                2L, "New Provider", 5L, LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2025, 1, 1, 0, 0));

        AppProvider model = mapper.toModelFromInput(inputDTO);

        assertNull(model.getId());
        assertEquals("New Provider", model.getCompanyName());
        assertEquals(2L, model.getAppDevelopment().getId());
        assertEquals(5L, model.getRole().getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void toModelFromInput_nullRoleId_buildsRoleWithNullId() {
        // NOTE: unlike updateModelFromInput, MapStruct does not apply the @AfterMapping
        // nullifyRoleWhenIdMissing guard here because toModelFromInput constructs the result via
        // AppProvider.builder() rather than mutating a @MappingTarget instance. As a result, a
        // create() call with no roleId still yields a non-null Role reference with a null id,
        // instead of a fully null role - a latent inconsistency between create and update paths.
        AppProviderInputDTO inputDTO = new AppProviderInputDTO(2L, "New Provider", null, null, null);

        AppProvider model = mapper.toModelFromInput(inputDTO);

        assertNotNull(model.getRole());
        assertNull(model.getRole().getId());
    }

    @Test
    void updateModelFromInput_withRoleId_mergesFieldsWithoutTouchingId() {
        AppProvider existing = new AppProvider();
        existing.setId(9L);
        existing.setCompanyName("Old Name");
        existing.setAppDevelopment(AppDevelopment.builder().id(1L).build());
        existing.setRole(new Role());
        AppProviderInputDTO inputDTO = new AppProviderInputDTO(
                4L, "Updated Name", 6L, LocalDateTime.of(2024, 4, 4, 0, 0), LocalDateTime.of(2025, 4, 4, 0, 0));

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(9L, existing.getId());
        assertEquals("Updated Name", existing.getCompanyName());
        assertEquals(4L, existing.getAppDevelopment().getId());
        assertNotNull(existing.getRole());
        assertEquals(6L, existing.getRole().getId());
        assertEquals(inputDTO.getStartDate(), existing.getStartDate());
        assertEquals(inputDTO.getExpireDate(), existing.getExpireDate());
    }

    @Test
    void updateModelFromInput_nullRoleId_nullifiesRoleReference() {
        AppProvider existing = new AppProvider();
        existing.setId(9L);
        existing.setRole(new Role());
        AppProviderInputDTO inputDTO = new AppProviderInputDTO(4L, "Updated Name", null, null, null);

        mapper.updateModelFromInput(inputDTO, existing);

        assertNull(existing.getRole());
    }
}
