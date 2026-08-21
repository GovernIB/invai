package es.caib.invai.back.interna.application.development.technology;

import es.caib.invai.back.interna.application.development.technology.DTO.AppTechnologyInputDTO;
import es.caib.invai.back.interna.application.development.technology.DTO.AppTechnologyOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.technology.AppTechnologyCriteria;
import es.caib.invai.back.service.facade.application.development.technology.AppTechnologyService;
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
 * Unit tests for {@link AppTechnologyController}, verifying that every endpoint delegates to
 * {@link AppTechnologyService} and returns the expected HTTP status code. This is a child "link"
 * entity scoped to a parent application: it has no {@code getById} or {@code reactivate} endpoints.
 */
@ExtendWith(MockitoExtension.class)
class AppTechnologyControllerTest {

    @Mock
    private AppTechnologyService appTechnologyService;

    private AppTechnologyController appTechnologyController;

    @BeforeEach
    void setUp() {
        appTechnologyController = new AppTechnologyController(appTechnologyService);
    }

    @Test
    void getAllByApplicationId_returnsOkWithPagedServiceResult() {
        AppTechnologyCriteria criteria = new AppTechnologyCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppTechnologyOutputDTO> page = new PageImpl<>(List.of(new AppTechnologyOutputDTO()));
        when(appTechnologyService.getAll(10L, criteria, pageable)).thenReturn(page);

        ResponseEntity<Page<AppTechnologyOutputDTO>> response =
                appTechnologyController.getAllByAppDevelopmentId(10L, criteria, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        AppTechnologyInputDTO inputDTO = new AppTechnologyInputDTO(1L, 2L, 3L, "1.0", "arch");
        AppTechnologyOutputDTO dto = new AppTechnologyOutputDTO();
        when(appTechnologyService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<AppTechnologyOutputDTO> response = appTechnologyController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        AppTechnologyInputDTO inputDTO = new AppTechnologyInputDTO(1L, 2L, 3L, "1.0", "arch");
        AppTechnologyOutputDTO dto = new AppTechnologyOutputDTO();
        when(appTechnologyService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<AppTechnologyOutputDTO> response = appTechnologyController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = appTechnologyController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appTechnologyService).delete(1L);
    }
}
