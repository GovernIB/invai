package es.caib.invai.back.interna.application.responsibleAuthorized.responsible;

import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleDeleteDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleOutputDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleCriteria;
import es.caib.invai.back.service.facade.application.responsibleAuthorized.responsible.AppResponsibleService;
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
 * Unit tests for {@link AppResponsibleController}, verifying that every endpoint delegates to
 * {@link AppResponsibleService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class AppResponsibleControllerTest {

    @Mock
    private AppResponsibleService appResponsibleService;

    private AppResponsibleController appResponsibleController;

    @BeforeEach
    void setUp() {
        appResponsibleController = new AppResponsibleController(appResponsibleService);
    }

    @Test
    void getAllByAppResponsibleAuthorizedId_returnsOkWithPagedServiceResult() {
        AppResponsibleCriteria criteria = new AppResponsibleCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppResponsibleOutputDTO> page = new PageImpl<>(List.of(new AppResponsibleOutputDTO()));
        when(appResponsibleService.getAll(40L, criteria, pageable)).thenReturn(page);

        ResponseEntity<Page<AppResponsibleOutputDTO>> response = appResponsibleController.getAllByAppResponsibleAuthorizedId(40L, criteria, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, false);
        AppResponsibleOutputDTO dto = new AppResponsibleOutputDTO();
        when(appResponsibleService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<AppResponsibleOutputDTO> response = appResponsibleController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, false);
        AppResponsibleOutputDTO dto = new AppResponsibleOutputDTO();
        when(appResponsibleService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<AppResponsibleOutputDTO> response = appResponsibleController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        AppResponsibleDeleteDTO dto = new AppResponsibleDeleteDTO("Deixa l'empresa");

        ResponseEntity<Void> response = appResponsibleController.delete(1L, dto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appResponsibleService).delete(1L, dto);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        AppResponsibleOutputDTO dto = new AppResponsibleOutputDTO();
        when(appResponsibleService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<AppResponsibleOutputDTO> response = appResponsibleController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
