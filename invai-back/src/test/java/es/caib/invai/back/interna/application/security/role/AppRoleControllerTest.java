package es.caib.invai.back.interna.application.security.role;

import es.caib.invai.back.interna.application.security.role.DTO.AppRoleInputDTO;
import es.caib.invai.back.interna.application.security.role.DTO.AppRoleOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.role.AppRoleCriteria;
import es.caib.invai.back.service.facade.application.security.role.AppRoleService;
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
 * Unit tests for {@link AppRoleController}, verifying that every endpoint delegates to
 * {@link AppRoleService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class AppRoleControllerTest {

    @Mock
    private AppRoleService appRoleService;

    private AppRoleController appRoleController;

    @BeforeEach
    void setUp() {
        appRoleController = new AppRoleController(appRoleService);
    }

    @Test
    void getAllByAppSecurityId_returnsOkWithPagedServiceResult() {
        AppRoleCriteria criteria = new AppRoleCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppRoleOutputDTO> page = new PageImpl<>(List.of(new AppRoleOutputDTO()));
        when(appRoleService.getAll(10L, criteria, pageable)).thenReturn(page);

        ResponseEntity<Page<AppRoleOutputDTO>> response = appRoleController.getAllByAppSecurityId(10L, criteria, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        AppRoleInputDTO inputDTO = new AppRoleInputDTO(5L, 7L);
        AppRoleOutputDTO dto = new AppRoleOutputDTO();
        when(appRoleService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<AppRoleOutputDTO> response = appRoleController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        AppRoleInputDTO inputDTO = new AppRoleInputDTO(5L, 7L);
        AppRoleOutputDTO dto = new AppRoleOutputDTO();
        when(appRoleService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<AppRoleOutputDTO> response = appRoleController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = appRoleController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appRoleService).delete(1L);
    }
}
