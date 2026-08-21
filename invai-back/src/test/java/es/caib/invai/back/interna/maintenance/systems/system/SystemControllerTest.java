package es.caib.invai.back.interna.maintenance.systems.system;

import es.caib.invai.back.interna.maintenance.systems.system.DTO.SystemInputDTO;
import es.caib.invai.back.interna.maintenance.systems.system.DTO.SystemOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.systems.system.SystemCriteria;
import es.caib.invai.back.service.facade.maintenance.systems.system.SystemService;
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
 * Unit tests for {@link SystemController}, verifying that every endpoint delegates to
 * {@link SystemService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class SystemControllerTest {

    @Mock
    private SystemService systemService;

    private SystemController systemController;

    @BeforeEach
    void setUp() {
        systemController = new SystemController(systemService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        SystemOutputDTO dto = new SystemOutputDTO();
        when(systemService.getById(1L)).thenReturn(dto);

        ResponseEntity<SystemOutputDTO> response = systemController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        SystemCriteria filter = new SystemCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<SystemOutputDTO> page = new PageImpl<>(List.of(new SystemOutputDTO()));
        when(systemService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<SystemOutputDTO>> response = systemController.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        SystemInputDTO inputDTO = new SystemInputDTO();
        inputDTO.setServerId(1L);
        inputDTO.setInstance("INST1");
        inputDTO.setPort(1521);
        inputDTO.setVersion("1.0");
        SystemOutputDTO dto = new SystemOutputDTO();
        when(systemService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<SystemOutputDTO> response = systemController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        SystemInputDTO inputDTO = new SystemInputDTO();
        inputDTO.setServerId(1L);
        inputDTO.setInstance("INST1");
        inputDTO.setPort(1521);
        inputDTO.setVersion("1.0");
        SystemOutputDTO dto = new SystemOutputDTO();
        when(systemService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<SystemOutputDTO> response = systemController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = systemController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(systemService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        SystemOutputDTO dto = new SystemOutputDTO();
        when(systemService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<SystemOutputDTO> response = systemController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
