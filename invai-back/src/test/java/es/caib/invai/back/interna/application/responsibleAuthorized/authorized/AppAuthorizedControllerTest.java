package es.caib.invai.back.interna.application.responsibleAuthorized.authorized;

import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedDeleteDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedOutputDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedCriteria;
import es.caib.invai.back.service.facade.application.responsibleAuthorized.authorized.AppAuthorizedService;
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
 * Unit tests for {@link AppAuthorizedController}, verifying that every endpoint delegates to
 * {@link AppAuthorizedService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class AppAuthorizedControllerTest {

    @Mock
    private AppAuthorizedService appAuthorizedService;

    private AppAuthorizedController appAuthorizedController;

    @BeforeEach
    void setUp() {
        appAuthorizedController = new AppAuthorizedController(appAuthorizedService);
    }

    @Test
    void getAllByAppResponsibleAuthorizedId_returnsOkWithPagedServiceResult() {
        AppAuthorizedCriteria criteria = new AppAuthorizedCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppAuthorizedOutputDTO> page = new PageImpl<>(List.of(AppAuthorizedOutputDTO.builder().id(1L).build()));
        when(appAuthorizedService.getAll(10L, criteria, pageable)).thenReturn(page);

        ResponseEntity<Page<AppAuthorizedOutputDTO>> response =
                appAuthorizedController.getAllByAppResponsibleAuthorizedId(10L, criteria, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, 7L, null, null, null, null, List.of(2L), null, false);
        AppAuthorizedOutputDTO dto = AppAuthorizedOutputDTO.builder().id(1L).build();
        when(appAuthorizedService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<AppAuthorizedOutputDTO> response = appAuthorizedController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, 7L, null, null, null, null, List.of(2L), null, false);
        AppAuthorizedOutputDTO dto = AppAuthorizedOutputDTO.builder().id(1L).build();
        when(appAuthorizedService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<AppAuthorizedOutputDTO> response = appAuthorizedController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        AppAuthorizedDeleteDTO dto = new AppAuthorizedDeleteDTO("reason");

        ResponseEntity<Void> response = appAuthorizedController.delete(1L, dto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appAuthorizedService).delete(1L, dto);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        AppAuthorizedOutputDTO dto = AppAuthorizedOutputDTO.builder().id(1L).build();
        when(appAuthorizedService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<AppAuthorizedOutputDTO> response = appAuthorizedController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
