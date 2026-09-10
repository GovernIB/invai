package es.caib.invai.back.interna.maintenance.complianceSituation;

import es.caib.invai.back.interna.maintenance.complianceSituation.DTO.ComplianceSituationInputDTO;
import es.caib.invai.back.interna.maintenance.complianceSituation.DTO.ComplianceSituationOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.complianceSituation.ComplianceSituationCriteria;
import es.caib.invai.back.service.facade.maintenance.complianceSituation.ComplianceSituationService;
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
 * Unit tests for {@link ComplianceSituationController}, verifying that every endpoint delegates to
 * {@link ComplianceSituationService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class ComplianceSituationControllerTest {

    @Mock
    private ComplianceSituationService complianceSituationService;

    private ComplianceSituationController complianceSituationController;

    @BeforeEach
    void setUp() {
        complianceSituationController = new ComplianceSituationController(complianceSituationService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        ComplianceSituationOutputDTO dto = new ComplianceSituationOutputDTO();
        when(complianceSituationService.getById(1L)).thenReturn(dto);

        ResponseEntity<ComplianceSituationOutputDTO> response = complianceSituationController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        ComplianceSituationCriteria filter = new ComplianceSituationCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<ComplianceSituationOutputDTO> page = new PageImpl<>(List.of(new ComplianceSituationOutputDTO()));
        when(complianceSituationService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<ComplianceSituationOutputDTO>> response = complianceSituationController.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        ComplianceSituationInputDTO inputDTO = new ComplianceSituationInputDTO("Name", "Name ES");
        ComplianceSituationOutputDTO dto = new ComplianceSituationOutputDTO();
        when(complianceSituationService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<ComplianceSituationOutputDTO> response = complianceSituationController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        ComplianceSituationInputDTO inputDTO = new ComplianceSituationInputDTO("Name", "Name ES");
        ComplianceSituationOutputDTO dto = new ComplianceSituationOutputDTO();
        when(complianceSituationService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<ComplianceSituationOutputDTO> response = complianceSituationController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = complianceSituationController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(complianceSituationService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        ComplianceSituationOutputDTO dto = new ComplianceSituationOutputDTO();
        when(complianceSituationService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<ComplianceSituationOutputDTO> response = complianceSituationController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
