package es.caib.invai.back.interna.application.system_database.system;

import es.caib.invai.back.interna.application.system_database.system.DTO.AppSystemInputDTO;
import es.caib.invai.back.interna.application.system_database.system.DTO.AppSystemOutputDTO;
import es.caib.invai.back.persistence.repository.application.system_database.system.AppSystemCriteria;
import es.caib.invai.back.service.facade.application.system_database.system.AppSystemService;
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
 * Unit tests for {@link AppSystemController}, verifying that every endpoint delegates to
 * {@link AppSystemService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class AppSystemControllerTest {

    @Mock
    private AppSystemService appSystemService;

    private AppSystemController appSystemController;

    @BeforeEach
    void setUp() {
        appSystemController = new AppSystemController(appSystemService);
    }

    @Test
    void getAllByApplicationId_returnsOkWithPagedServiceResult() {
        AppSystemCriteria criteria = new AppSystemCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppSystemOutputDTO> page = new PageImpl<>(List.of(new AppSystemOutputDTO()));
        when(appSystemService.getAll(10L, criteria, pageable)).thenReturn(page);

        ResponseEntity<Page<AppSystemOutputDTO>> response = appSystemController.getAllByInformationSystemDbId(10L, criteria, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        AppSystemInputDTO inputDTO = new AppSystemInputDTO(5L, 7L);
        AppSystemOutputDTO dto = new AppSystemOutputDTO();
        when(appSystemService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<AppSystemOutputDTO> response = appSystemController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        AppSystemInputDTO inputDTO = new AppSystemInputDTO(5L, 7L);
        AppSystemOutputDTO dto = new AppSystemOutputDTO();
        when(appSystemService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<AppSystemOutputDTO> response = appSystemController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = appSystemController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appSystemService).delete(1L);
    }
}
