package es.caib.invai.back.interna.application.security.measure;

import es.caib.invai.back.interna.application.security.measure.DTO.AppSecurityMeasureInputDTO;
import es.caib.invai.back.interna.application.security.measure.DTO.AppSecurityMeasureOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.measure.AppSecurityMeasureCriteria;
import es.caib.invai.back.service.facade.application.security.measure.AppSecurityMeasureService;
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
 * Unit tests for {@link AppSecurityMeasureController}, verifying that every endpoint delegates to
 * {@link AppSecurityMeasureService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class AppSecurityMeasureControllerTest {

    @Mock
    private AppSecurityMeasureService appSecurityMeasureService;

    private AppSecurityMeasureController appSecurityMeasureController;

    @BeforeEach
    void setUp() {
        appSecurityMeasureController = new AppSecurityMeasureController(appSecurityMeasureService);
    }

    @Test
    void getAllByAppSecurityId_returnsOkWithPagedServiceResult() {
        AppSecurityMeasureCriteria criteria = new AppSecurityMeasureCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppSecurityMeasureOutputDTO> page = new PageImpl<>(List.of(new AppSecurityMeasureOutputDTO()));
        when(appSecurityMeasureService.getAll(10L, criteria, pageable)).thenReturn(page);

        ResponseEntity<Page<AppSecurityMeasureOutputDTO>> response = appSecurityMeasureController.getAllByAppSecurityId(10L, criteria, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        AppSecurityMeasureInputDTO inputDTO = new AppSecurityMeasureInputDTO(5L, 7L, 8L, "desc");
        AppSecurityMeasureOutputDTO dto = new AppSecurityMeasureOutputDTO();
        when(appSecurityMeasureService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<AppSecurityMeasureOutputDTO> response = appSecurityMeasureController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        AppSecurityMeasureInputDTO inputDTO = new AppSecurityMeasureInputDTO(5L, 7L, 8L, "desc");
        AppSecurityMeasureOutputDTO dto = new AppSecurityMeasureOutputDTO();
        when(appSecurityMeasureService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<AppSecurityMeasureOutputDTO> response = appSecurityMeasureController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = appSecurityMeasureController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appSecurityMeasureService).delete(1L);
    }
}
