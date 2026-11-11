package es.caib.invai.back.service.mapper.maintenance.documentation.documentRolePermission;

import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentRolePermissionInputDTO;
import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentRolePermissionOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.documentation.documentCategory.DocumentCategoryEntity;
import es.caib.invai.back.persistence.model.maintenance.documentation.documentRolePermission.DocumentRolePermissionEntity;
import es.caib.invai.back.persistence.model.maintenance.role.RoleEntity;
import es.caib.invai.back.service.model.maintenance.documentation.documentRolePermission.DocumentRolePermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the generated {@link DocumentRolePermissionMapperImpl}, exercising every
 * conversion direction declared on {@link DocumentRolePermissionMapper}.
 */
class DocumentRolePermissionMapperTest {

    private DocumentRolePermissionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DocumentRolePermissionMapperImpl();
    }

    @Test
    void toModel_flattensTheRoleAndTheCategory() {
        DocumentCategoryEntity category = new DocumentCategoryEntity();
        category.setId(20L);
        RoleEntity role = new RoleEntity();
        role.setId(10L);
        role.setName("INV_SISTEMES");
        role.setDescription("Usuarios Sistemas");
        DocumentRolePermissionEntity entity = new DocumentRolePermissionEntity();
        entity.setId(1L);
        entity.setRole(role);
        entity.setCategory(category);
        entity.setCanRead(true);
        entity.setCanEdit(true);

        DocumentRolePermission model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals(10L, model.getRoleId());
        assertEquals(20L, model.getCategoryId());
        assertEquals("INV_SISTEMES", model.getRoleName());
        assertEquals("Usuarios Sistemas", model.getRoleDescription());
        assertTrue(model.isCanRead());
        assertFalse(model.isCanUpload());
        assertTrue(model.isCanEdit());
        assertFalse(model.isCanDelete());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_carriesTheRoleAndTheCategoryAsReferencesWithOnlyTheirId() {
        DocumentRolePermission model = new DocumentRolePermission();
        model.setId(2L);
        model.setRoleId(10L);
        model.setCategoryId(20L);
        model.setCanUpload(true);
        model.setCanDelete(true);

        DocumentRolePermissionEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals(10L, entity.getRole().getId());
        assertNull(entity.getRole().getName());
        assertEquals(20L, entity.getCategory().getId());
        assertNull(entity.getCategory().getName());
        assertFalse(entity.isCanRead());
        assertTrue(entity.isCanUpload());
        assertTrue(entity.isCanDelete());
    }

    @Test
    void toEntity_withoutRoleId_leavesTheRoleNull() {
        assertNull(mapper.toEntity(new DocumentRolePermission()).getRole());
    }

    @Test
    void toEntity_withoutCategoryId_leavesTheCategoryNull() {
        assertNull(mapper.toEntity(new DocumentRolePermission()).getCategory());
    }

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toResponse_renamesTheRoleFields() {
        DocumentRolePermission model = new DocumentRolePermission();
        model.setRoleId(10L);
        model.setRoleName("INV_LECTOR");
        model.setRoleDescription("Usuarios Lectores");
        model.setCanRead(true);

        DocumentRolePermissionOutputDTO dto = mapper.toResponse(model);

        assertEquals(10L, dto.getRoleId());
        assertEquals("INV_LECTOR", dto.getName());
        assertEquals("Usuarios Lectores", dto.getDescription());
        assertTrue(dto.isCanRead());
        assertFalse(dto.isCanUpload());
    }

    @Test
    void toResponse_null_returnsNull() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void updateModelFromInput_setsOnlyThePermissions() {
        DocumentRolePermission model = new DocumentRolePermission();
        model.setId(5L);
        model.setRoleId(10L);
        model.setRoleName("INV_ADMIN");
        model.setCanRead(true);

        mapper.updateModelFromInput(new DocumentRolePermissionInputDTO(99L, false, true, true, false), model);

        assertEquals(5L, model.getId());
        assertEquals(10L, model.getRoleId());
        assertEquals("INV_ADMIN", model.getRoleName());
        assertFalse(model.isCanRead());
        assertTrue(model.isCanUpload());
        assertTrue(model.isCanEdit());
        assertFalse(model.isCanDelete());
    }

    @Test
    void updateModelFromInput_nullInput_changesNothing() {
        DocumentRolePermission model = new DocumentRolePermission();
        model.setCanRead(true);

        mapper.updateModelFromInput(null, model);

        assertTrue(model.isCanRead());
    }
}
