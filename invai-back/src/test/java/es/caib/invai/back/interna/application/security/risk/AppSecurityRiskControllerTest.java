package es.caib.invai.back.interna.application.security.risk;

import es.caib.invai.back.interna.application.security.risk.DTO.AppSecurityRiskInputDTO;
import es.caib.invai.back.interna.application.security.risk.DTO.AppSecurityRiskOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.risk.AppSecurityRiskCriteria;
import es.caib.invai.back.service.facade.application.security.risk.AppSecurityRiskService;
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
 * Unit tests for {@link AppSecurityRiskController}, verifying that every endpoint delegates to
 * {@link AppSecurityRiskService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class AppSecurityRiskControllerTest {

    @Mock
    private AppSecurityRiskService appSecurityRiskService;

    private AppSecurityRiskController appSecurityRiskController;

    @BeforeEach
    void setUp() {
        appSecurityRiskController = new AppSecurityRiskController(appSecurityRiskService);
    }

    @Test
    void getAllByAppSecurityId_returnsOkWithPagedServiceResult() {
        AppSecurityRiskCriteria criteria = new AppSecurityRiskCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppSecurityRiskOutputDTO> page = new PageImpl<>(List.of(new AppSecurityRiskOutputDTO()));
        when(appSecurityRiskService.getAll(10L, criteria, pageable)).thenReturn(page);

        ResponseEntity<Page<AppSecurityRiskOutputDTO>> response = appSecurityRiskController.getAllByAppSecurityId(10L, criteria, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        AppSecurityRiskInputDTO inputDTO = new AppSecurityRiskInputDTO(5L, 7L, "Description", 9L);
        AppSecurityRiskOutputDTO dto = new AppSecurityRiskOutputDTO();
        when(appSecurityRiskService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<AppSecurityRiskOutputDTO> response = appSecurityRiskController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        AppSecurityRiskInputDTO inputDTO = new AppSecurityRiskInputDTO(5L, 7L, "Description", 9L);
        AppSecurityRiskOutputDTO dto = new AppSecurityRiskOutputDTO();
        when(appSecurityRiskService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<AppSecurityRiskOutputDTO> response = appSecurityRiskController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = appSecurityRiskController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appSecurityRiskService).delete(1L);
    }
}
