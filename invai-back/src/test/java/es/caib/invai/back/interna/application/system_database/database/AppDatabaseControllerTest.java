package es.caib.invai.back.interna.application.system_database.database;

import es.caib.invai.back.interna.application.system_database.database.DTO.AppDatabaseInputDTO;
import es.caib.invai.back.interna.application.system_database.database.DTO.AppDatabaseOutputDTO;
import es.caib.invai.back.persistence.repository.application.system_database.database.AppDatabaseCriteria;
import es.caib.invai.back.service.facade.application.system_database.database.AppDatabaseService;
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
 * Unit tests for {@link AppDatabaseController}, verifying that every endpoint delegates to
 * {@link AppDatabaseService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class AppDatabaseControllerTest {

    @Mock
    private AppDatabaseService appDatabaseService;

    private AppDatabaseController appDatabaseController;

    @BeforeEach
    void setUp() {
        appDatabaseController = new AppDatabaseController(appDatabaseService);
    }

    @Test
    void getAllByApplicationId_returnsOkWithPagedServiceResult() {
        AppDatabaseCriteria criteria = new AppDatabaseCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppDatabaseOutputDTO> page = new PageImpl<>(List.of(new AppDatabaseOutputDTO()));
        when(appDatabaseService.getAll(10L, criteria, pageable)).thenReturn(page);

        ResponseEntity<Page<AppDatabaseOutputDTO>> response = appDatabaseController.getAllByInformationSystemDbId(10L, criteria, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        AppDatabaseInputDTO inputDTO = new AppDatabaseInputDTO(5L, 7L);
        AppDatabaseOutputDTO dto = new AppDatabaseOutputDTO();
        when(appDatabaseService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<AppDatabaseOutputDTO> response = appDatabaseController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        AppDatabaseInputDTO inputDTO = new AppDatabaseInputDTO(5L, 7L);
        AppDatabaseOutputDTO dto = new AppDatabaseOutputDTO();
        when(appDatabaseService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<AppDatabaseOutputDTO> response = appDatabaseController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = appDatabaseController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appDatabaseService).delete(1L);
    }
}
