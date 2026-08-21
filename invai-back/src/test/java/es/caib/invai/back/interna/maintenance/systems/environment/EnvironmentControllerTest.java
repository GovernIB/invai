package es.caib.invai.back.interna.maintenance.systems.environment;

import es.caib.invai.back.interna.maintenance.systems.environment.DTO.EnvironmentInputDTO;
import es.caib.invai.back.interna.maintenance.systems.environment.DTO.EnvironmentOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.systems.environment.EnvironmentCriteria;
import es.caib.invai.back.service.facade.maintenance.systems.environment.EnvironmentService;
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
 * Unit tests for {@link EnvironmentController}, verifying that every endpoint delegates to
 * {@link EnvironmentService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class EnvironmentControllerTest {

    @Mock
    private EnvironmentService environmentService;

    private EnvironmentController environmentController;

    @BeforeEach
    void setUp() {
        environmentController = new EnvironmentController(environmentService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        EnvironmentOutputDTO dto = new EnvironmentOutputDTO();
        when(environmentService.getById(1L)).thenReturn(dto);

        ResponseEntity<EnvironmentOutputDTO> response = environmentController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        EnvironmentCriteria filter = new EnvironmentCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<EnvironmentOutputDTO> page = new PageImpl<>(List.of(new EnvironmentOutputDTO()));
        when(environmentService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<EnvironmentOutputDTO>> response = environmentController.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        EnvironmentInputDTO inputDTO = new EnvironmentInputDTO();
        EnvironmentOutputDTO dto = new EnvironmentOutputDTO();
        when(environmentService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<EnvironmentOutputDTO> response = environmentController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        EnvironmentInputDTO inputDTO = new EnvironmentInputDTO();
        EnvironmentOutputDTO dto = new EnvironmentOutputDTO();
        when(environmentService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<EnvironmentOutputDTO> response = environmentController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = environmentController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(environmentService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        EnvironmentOutputDTO dto = new EnvironmentOutputDTO();
        when(environmentService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<EnvironmentOutputDTO> response = environmentController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
