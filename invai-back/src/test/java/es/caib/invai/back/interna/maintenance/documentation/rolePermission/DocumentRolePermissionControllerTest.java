package es.caib.invai.back.interna.maintenance.documentation.rolePermission;

import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentRolePermissionInputDTO;
import es.caib.invai.back.interna.maintenance.documentation.category.DTO.DocumentCategoryOutputDTO;
import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentRoleCategoryOutputDTO;
import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentRolePermissionOutputDTO;
import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentRolePermissionUpdateInputDTO;
import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentUserPermissionOutputDTO;
import es.caib.invai.back.service.facade.maintenance.documentation.documentRolePermission.DocumentRolePermissionService;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DocumentRolePermissionController}, verifying that every endpoint delegates
 * to {@link DocumentRolePermissionService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class DocumentRolePermissionControllerTest {

    @Mock
    private DocumentRolePermissionService documentRolePermissionService;

    private DocumentRolePermissionController controller;

    @BeforeEach
    void setUp() {
        controller = new DocumentRolePermissionController(documentRolePermissionService);
    }

    @Test
    void getByCategory_returnsOkWithServiceResult() {
        List<DocumentRolePermissionOutputDTO> permissions = List.of(new DocumentRolePermissionOutputDTO());
        when(documentRolePermissionService.getByCategory(20L)).thenReturn(permissions);

        ResponseEntity<List<DocumentRolePermissionOutputDTO>> response = controller.getByCategory(20L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(permissions, response.getBody());
    }

    @Test
    void getByRole_returnsOkWithServiceResult() {
        List<DocumentRoleCategoryOutputDTO> permissions = List.of(new DocumentRoleCategoryOutputDTO(new DocumentCategoryOutputDTO(), true, false, false, false));
        when(documentRolePermissionService.getByRole("INV_SISTEMES")).thenReturn(permissions);

        ResponseEntity<List<DocumentRoleCategoryOutputDTO>> response = controller.getByRole("INV_SISTEMES");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(permissions, response.getBody());
    }

    @Test
    void getCurrentUserPermissions_returnsOkWithServiceResult() {
        List<DocumentUserPermissionOutputDTO> permissions = List.of(new DocumentUserPermissionOutputDTO(20L, true, false, true, false));
        when(documentRolePermissionService.getCurrentUserPermissions()).thenReturn(permissions);

        ResponseEntity<List<DocumentUserPermissionOutputDTO>> response = controller.getCurrentUserPermissions();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(permissions, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        DocumentRolePermissionUpdateInputDTO inputDTO = new DocumentRolePermissionUpdateInputDTO(
                List.of(new DocumentRolePermissionInputDTO(10L, true, true, true, true)));
        List<DocumentRolePermissionOutputDTO> permissions = List.of(new DocumentRolePermissionOutputDTO());
        when(documentRolePermissionService.update(20L, inputDTO)).thenReturn(permissions);

        ResponseEntity<List<DocumentRolePermissionOutputDTO>> response = controller.update(20L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(permissions, response.getBody());
    }

    @Test
    void getCurrentUserPermissions_hasNoMoreRestrictionThanTheClass() throws NoSuchMethodException {
        assertNull(DocumentRolePermissionController.class
                .getMethod("getCurrentUserPermissions").getAnnotation(PreAuthorize.class));
    }

    @Test
    void endpoints_areOpenToEveryRole_exceptTheUpdateThatIsRestrictedToSuperUsers() throws NoSuchMethodException {
        PreAuthorize classLevel = DocumentRolePermissionController.class.getAnnotation(PreAuthorize.class);

        assertNotNull(classLevel);
        assertEquals(Constants.PRE_AUTH_ANY_ROLE, classLevel.value());
        assertNull(DocumentRolePermissionController.class
                .getMethod("getByCategory", Long.class).getAnnotation(PreAuthorize.class));
        assertNull(DocumentRolePermissionController.class
                .getMethod("getByRole", String.class).getAnnotation(PreAuthorize.class));
        PreAuthorize update = DocumentRolePermissionController.class
                .getMethod("update", Long.class, DocumentRolePermissionUpdateInputDTO.class).getAnnotation(PreAuthorize.class);
        assertNotNull(update);
        assertEquals(Constants.PRE_AUTH_SUPER, update.value());
    }
}
