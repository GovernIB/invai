package es.caib.invai.back.interna.maintenance.systems.database;

import es.caib.invai.back.interna.maintenance.systems.database.DTO.DatabaseInputDTO;
import es.caib.invai.back.interna.maintenance.systems.database.DTO.DatabaseOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.systems.database.DatabaseCriteria;
import es.caib.invai.back.service.facade.maintenance.systems.database.DatabaseService;
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
 * Unit tests for {@link DatabaseController}, verifying that every endpoint delegates to
 * {@link DatabaseService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class DatabaseControllerTest {

    @Mock
    private DatabaseService databaseService;

    private DatabaseController databaseController;

    @BeforeEach
    void setUp() {
        databaseController = new DatabaseController(databaseService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        DatabaseOutputDTO dto = new DatabaseOutputDTO();
        when(databaseService.getById(1L)).thenReturn(dto);

        ResponseEntity<DatabaseOutputDTO> response = databaseController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        DatabaseCriteria filter = new DatabaseCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<DatabaseOutputDTO> page = new PageImpl<>(List.of(new DatabaseOutputDTO()));
        when(databaseService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<DatabaseOutputDTO>> response = databaseController.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        DatabaseInputDTO inputDTO = new DatabaseInputDTO();
        DatabaseOutputDTO dto = new DatabaseOutputDTO();
        when(databaseService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<DatabaseOutputDTO> response = databaseController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        DatabaseInputDTO inputDTO = new DatabaseInputDTO();
        DatabaseOutputDTO dto = new DatabaseOutputDTO();
        when(databaseService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<DatabaseOutputDTO> response = databaseController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = databaseController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(databaseService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        DatabaseOutputDTO dto = new DatabaseOutputDTO();
        when(databaseService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<DatabaseOutputDTO> response = databaseController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
