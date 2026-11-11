package es.caib.invai.back.interna.maintenance.development.providerRole;

import es.caib.invai.back.interna.maintenance.development.providerRole.DTO.ProviderRoleInputDTO;
import es.caib.invai.back.interna.maintenance.development.providerRole.DTO.ProviderRoleOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.development.providerRole.ProviderRoleCriteria;
import es.caib.invai.back.service.facade.maintenance.development.providerRole.ProviderRoleService;
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
 * Unit tests for {@link ProviderRoleController}, verifying that every endpoint delegates to
 * {@link ProviderRoleService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class ProviderRoleControllerTest {

    @Mock
    private ProviderRoleService providerRoleService;

    private ProviderRoleController providerRoleController;

    @BeforeEach
    void setUp() {
        providerRoleController = new ProviderRoleController(providerRoleService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        ProviderRoleOutputDTO dto = new ProviderRoleOutputDTO();
        when(providerRoleService.getById(1L)).thenReturn(dto);

        ResponseEntity<ProviderRoleOutputDTO> response = providerRoleController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        ProviderRoleCriteria filter = new ProviderRoleCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<ProviderRoleOutputDTO> page = new PageImpl<>(List.of(new ProviderRoleOutputDTO()));
        when(providerRoleService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<ProviderRoleOutputDTO>> response = providerRoleController.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        ProviderRoleInputDTO inputDTO = new ProviderRoleInputDTO("Name", "Nombre");
        ProviderRoleOutputDTO dto = new ProviderRoleOutputDTO();
        when(providerRoleService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<ProviderRoleOutputDTO> response = providerRoleController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        ProviderRoleInputDTO inputDTO = new ProviderRoleInputDTO("Name", "Nombre");
        ProviderRoleOutputDTO dto = new ProviderRoleOutputDTO();
        when(providerRoleService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<ProviderRoleOutputDTO> response = providerRoleController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = providerRoleController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(providerRoleService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        ProviderRoleOutputDTO dto = new ProviderRoleOutputDTO();
        when(providerRoleService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<ProviderRoleOutputDTO> response = providerRoleController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
