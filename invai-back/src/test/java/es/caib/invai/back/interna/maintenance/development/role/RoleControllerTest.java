package es.caib.invai.back.interna.maintenance.development.role;

import es.caib.invai.back.interna.maintenance.development.role.DTO.RoleInputDTO;
import es.caib.invai.back.interna.maintenance.development.role.DTO.RoleOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.development.role.RoleCriteria;
import es.caib.invai.back.service.facade.maintenance.development.role.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RoleController}, verifying that every endpoint delegates to
 * {@link RoleService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock
    private RoleService roleService;

    private RoleController roleController;

    @BeforeEach
    void setUp() {
        roleController = new RoleController(roleService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        RoleOutputDTO dto = new RoleOutputDTO();
        when(roleService.getById(1L)).thenReturn(dto);

        ResponseEntity<RoleOutputDTO> response = roleController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        RoleCriteria filter = new RoleCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<RoleOutputDTO> page = new PageImpl<>(List.of(new RoleOutputDTO()));
        when(roleService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<RoleOutputDTO>> response = roleController.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        RoleInputDTO inputDTO = new RoleInputDTO("Name", "Nombre");
        RoleOutputDTO dto = new RoleOutputDTO();
        when(roleService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<RoleOutputDTO> response = roleController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        RoleInputDTO inputDTO = new RoleInputDTO("Name", "Nombre");
        RoleOutputDTO dto = new RoleOutputDTO();
        when(roleService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<RoleOutputDTO> response = roleController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = roleController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(roleService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        RoleOutputDTO dto = new RoleOutputDTO();
        when(roleService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<RoleOutputDTO> response = roleController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
