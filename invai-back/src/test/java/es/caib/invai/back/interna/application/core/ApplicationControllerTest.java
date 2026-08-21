package es.caib.invai.back.interna.application.core;

import es.caib.invai.back.interna.application.core.DTO.ApplicationInputDTO;
import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import es.caib.invai.back.persistence.repository.application.core.ApplicationCriteria;
import es.caib.invai.back.service.facade.application.core.ApplicationService;
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
 * Unit tests for {@link ApplicationController}, verifying that every endpoint delegates to
 * {@link ApplicationService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class ApplicationControllerTest {

    @Mock
    private ApplicationService applicationService;

    private ApplicationController applicationController;

    @BeforeEach
    void setUp() {
        applicationController = new ApplicationController(applicationService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        ApplicationOutputDTO dto = new ApplicationOutputDTO();
        when(applicationService.getById(1L)).thenReturn(dto);

        ResponseEntity<ApplicationOutputDTO> response = applicationController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<ApplicationOutputDTO> page = new PageImpl<>(List.of(new ApplicationOutputDTO()));
        when(applicationService.getAll(criteria, pageable)).thenReturn(page);

        ResponseEntity<Page<ApplicationOutputDTO>> response = applicationController.getAll(criteria, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        ApplicationInputDTO inputDTO = new ApplicationInputDTO();
        ApplicationOutputDTO dto = new ApplicationOutputDTO();
        when(applicationService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<ApplicationOutputDTO> response = applicationController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        ApplicationInputDTO inputDTO = new ApplicationInputDTO();
        ApplicationOutputDTO dto = new ApplicationOutputDTO();
        when(applicationService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<ApplicationOutputDTO> response = applicationController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = applicationController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(applicationService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        ApplicationOutputDTO dto = new ApplicationOutputDTO();
        when(applicationService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<ApplicationOutputDTO> response = applicationController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
