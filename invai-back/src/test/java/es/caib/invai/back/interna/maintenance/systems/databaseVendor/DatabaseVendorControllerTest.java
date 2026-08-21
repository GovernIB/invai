package es.caib.invai.back.interna.maintenance.systems.databaseVendor;

import es.caib.invai.back.interna.maintenance.systems.databaseVendor.DTO.DatabaseVendorInputDTO;
import es.caib.invai.back.interna.maintenance.systems.databaseVendor.DTO.DatabaseVendorOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.systems.databaseVendor.DatabaseVendorCriteria;
import es.caib.invai.back.service.facade.maintenance.systems.databaseVendor.DatabaseVendorService;
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
 * Unit tests for {@link DatabaseVendorController}, verifying that every endpoint delegates to
 * {@link DatabaseVendorService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class DatabaseVendorControllerTest {

    @Mock
    private DatabaseVendorService databaseVendorService;

    private DatabaseVendorController databaseVendorController;

    @BeforeEach
    void setUp() {
        databaseVendorController = new DatabaseVendorController(databaseVendorService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        DatabaseVendorOutputDTO dto = new DatabaseVendorOutputDTO();
        when(databaseVendorService.getById(1L)).thenReturn(dto);

        ResponseEntity<DatabaseVendorOutputDTO> response = databaseVendorController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        DatabaseVendorCriteria filter = new DatabaseVendorCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<DatabaseVendorOutputDTO> page = new PageImpl<>(List.of(new DatabaseVendorOutputDTO()));
        when(databaseVendorService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<DatabaseVendorOutputDTO>> response = databaseVendorController.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        DatabaseVendorInputDTO inputDTO = new DatabaseVendorInputDTO();
        inputDTO.setName("Oracle");
        inputDTO.setDefaultPort(1521);
        DatabaseVendorOutputDTO dto = new DatabaseVendorOutputDTO();
        when(databaseVendorService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<DatabaseVendorOutputDTO> response = databaseVendorController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        DatabaseVendorInputDTO inputDTO = new DatabaseVendorInputDTO();
        inputDTO.setName("Oracle");
        inputDTO.setDefaultPort(1521);
        DatabaseVendorOutputDTO dto = new DatabaseVendorOutputDTO();
        when(databaseVendorService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<DatabaseVendorOutputDTO> response = databaseVendorController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = databaseVendorController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(databaseVendorService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        DatabaseVendorOutputDTO dto = new DatabaseVendorOutputDTO();
        when(databaseVendorService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<DatabaseVendorOutputDTO> response = databaseVendorController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
