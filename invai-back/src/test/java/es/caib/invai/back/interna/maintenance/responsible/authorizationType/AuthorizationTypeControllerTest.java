package es.caib.invai.back.interna.maintenance.responsible.authorizationType;

import es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO.AuthorizationTypeInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO.AuthorizationTypeOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.responsible.authorizationType.AuthorizationTypeCriteria;
import es.caib.invai.back.service.facade.maintenance.responsible.authorizationType.AuthorizationTypeService;
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
 * Unit tests for {@link AuthorizationTypeController}, verifying that every endpoint delegates to
 * {@link AuthorizationTypeService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class AuthorizationTypeControllerTest {

    @Mock
    private AuthorizationTypeService authorizationTypeService;

    private AuthorizationTypeController authorizationTypeController;

    @BeforeEach
    void setUp() {
        authorizationTypeController = new AuthorizationTypeController(authorizationTypeService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        AuthorizationTypeOutputDTO dto = new AuthorizationTypeOutputDTO();
        when(authorizationTypeService.getById(1L)).thenReturn(dto);

        ResponseEntity<AuthorizationTypeOutputDTO> response = authorizationTypeController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        AuthorizationTypeCriteria filter = new AuthorizationTypeCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AuthorizationTypeOutputDTO> page = new PageImpl<>(List.of(new AuthorizationTypeOutputDTO()));
        when(authorizationTypeService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<AuthorizationTypeOutputDTO>> response = authorizationTypeController.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        AuthorizationTypeInputDTO inputDTO = new AuthorizationTypeInputDTO("Name", "Name ES");
        AuthorizationTypeOutputDTO dto = new AuthorizationTypeOutputDTO();
        when(authorizationTypeService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<AuthorizationTypeOutputDTO> response = authorizationTypeController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        AuthorizationTypeInputDTO inputDTO = new AuthorizationTypeInputDTO("Name", "Name ES");
        AuthorizationTypeOutputDTO dto = new AuthorizationTypeOutputDTO();
        when(authorizationTypeService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<AuthorizationTypeOutputDTO> response = authorizationTypeController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = authorizationTypeController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(authorizationTypeService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        AuthorizationTypeOutputDTO dto = new AuthorizationTypeOutputDTO();
        when(authorizationTypeService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<AuthorizationTypeOutputDTO> response = authorizationTypeController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
