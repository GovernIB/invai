package es.caib.invai.back.interna.maintenance.general.field;

import es.caib.invai.back.interna.maintenance.general.field.DTO.FieldInputDTO;
import es.caib.invai.back.interna.maintenance.general.field.DTO.FieldOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.general.field.FieldCriteria;
import es.caib.invai.back.service.facade.maintenance.general.field.FieldService;
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
 * Unit tests for {@link FieldController}, verifying that every endpoint delegates to
 * {@link FieldService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class FieldControllerTest {

    @Mock
    private FieldService fieldService;

    private FieldController fieldController;

    @BeforeEach
    void setUp() {
        fieldController = new FieldController(fieldService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        FieldOutputDTO dto = new FieldOutputDTO();
        when(fieldService.getById(1L)).thenReturn(dto);

        ResponseEntity<FieldOutputDTO> response = fieldController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        FieldCriteria filter = new FieldCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<FieldOutputDTO> page = new PageImpl<>(List.of(new FieldOutputDTO()));
        when(fieldService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<FieldOutputDTO>> response = fieldController.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        FieldInputDTO inputDTO = new FieldInputDTO("Name", "Nombre");
        FieldOutputDTO dto = new FieldOutputDTO();
        when(fieldService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<FieldOutputDTO> response = fieldController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        FieldInputDTO inputDTO = new FieldInputDTO("Name", "Nombre");
        FieldOutputDTO dto = new FieldOutputDTO();
        when(fieldService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<FieldOutputDTO> response = fieldController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = fieldController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(fieldService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        FieldOutputDTO dto = new FieldOutputDTO();
        when(fieldService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<FieldOutputDTO> response = fieldController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
