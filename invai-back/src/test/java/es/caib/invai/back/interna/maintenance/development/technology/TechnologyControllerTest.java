package es.caib.invai.back.interna.maintenance.development.technology;

import es.caib.invai.back.interna.maintenance.development.technology.DTO.TechnologyInputDTO;
import es.caib.invai.back.interna.maintenance.development.technology.DTO.TechnologyOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.development.technology.TechnologyCriteria;
import es.caib.invai.back.service.facade.maintenance.development.technology.TechnologyService;
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
 * Unit tests for {@link TechnologyController}, verifying that every endpoint delegates to
 * {@link TechnologyService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class TechnologyControllerTest {

    @Mock
    private TechnologyService technologyService;

    private TechnologyController technologyController;

    @BeforeEach
    void setUp() {
        technologyController = new TechnologyController(technologyService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        TechnologyOutputDTO dto = new TechnologyOutputDTO();
        when(technologyService.getById(1L)).thenReturn(dto);

        ResponseEntity<TechnologyOutputDTO> response = technologyController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        TechnologyCriteria filter = new TechnologyCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<TechnologyOutputDTO> page = new PageImpl<>(List.of(new TechnologyOutputDTO()));
        when(technologyService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<TechnologyOutputDTO>> response = technologyController.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        TechnologyInputDTO inputDTO = new TechnologyInputDTO("React", 1L);
        TechnologyOutputDTO dto = new TechnologyOutputDTO();
        when(technologyService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<TechnologyOutputDTO> response = technologyController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        TechnologyInputDTO inputDTO = new TechnologyInputDTO("React", 1L);
        TechnologyOutputDTO dto = new TechnologyOutputDTO();
        when(technologyService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<TechnologyOutputDTO> response = technologyController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = technologyController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(technologyService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        TechnologyOutputDTO dto = new TechnologyOutputDTO();
        when(technologyService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<TechnologyOutputDTO> response = technologyController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
