package es.caib.invai.back.interna.maintenance.general.systemType;

import es.caib.invai.back.interna.maintenance.general.systemType.DTO.SystemTypeInputDTO;
import es.caib.invai.back.interna.maintenance.general.systemType.DTO.SystemTypeOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.general.systemType.SystemTypeCriteria;
import es.caib.invai.back.service.facade.maintenance.general.systemType.SystemTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
 * Unit tests for {@link SystemTypeController}, verifying that every endpoint delegates to
 * {@link SystemTypeService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class SystemTypeControllerTest {

    @Mock
    private SystemTypeService systemTypeService;

    @InjectMocks
    private SystemTypeController systemTypeController;

    @Test
    void getById_returnsOkWithServiceResult() {
        SystemTypeOutputDTO dto = new SystemTypeOutputDTO();
        when(systemTypeService.getById(1L)).thenReturn(dto);

        ResponseEntity<SystemTypeOutputDTO> response = systemTypeController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        SystemTypeCriteria filter = new SystemTypeCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<SystemTypeOutputDTO> page = new PageImpl<>(List.of(new SystemTypeOutputDTO()));
        when(systemTypeService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<SystemTypeOutputDTO>> response = systemTypeController.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        SystemTypeInputDTO inputDTO = new SystemTypeInputDTO();
        SystemTypeOutputDTO dto = new SystemTypeOutputDTO();
        when(systemTypeService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<SystemTypeOutputDTO> response = systemTypeController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        SystemTypeInputDTO inputDTO = new SystemTypeInputDTO();
        SystemTypeOutputDTO dto = new SystemTypeOutputDTO();
        when(systemTypeService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<SystemTypeOutputDTO> response = systemTypeController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = systemTypeController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(systemTypeService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        SystemTypeOutputDTO dto = new SystemTypeOutputDTO();
        when(systemTypeService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<SystemTypeOutputDTO> response = systemTypeController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
