package es.caib.invai.back.interna.maintenance.general.commission;

import es.caib.invai.back.interna.maintenance.general.commission.DTO.CommissionInputDTO;
import es.caib.invai.back.interna.maintenance.general.commission.DTO.CommissionOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.general.commission.CommissionCriteria;
import es.caib.invai.back.service.facade.maintenance.general.commission.CommissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
 * Unit tests for {@link CommissionController}, verifying that every endpoint delegates to
 * {@link CommissionService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class CommissionControllerTest {

    @Mock
    private CommissionService commissionService;

    @InjectMocks
    private CommissionController commissionController;

    @Test
    void getById_returnsOkWithServiceResult() {
        CommissionOutputDTO dto = new CommissionOutputDTO();
        when(commissionService.getById(1L)).thenReturn(dto);

        ResponseEntity<CommissionOutputDTO> response = commissionController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        CommissionCriteria filter = new CommissionCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<CommissionOutputDTO> page = new PageImpl<>(List.of(new CommissionOutputDTO()));
        when(commissionService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<CommissionOutputDTO>> response = commissionController.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        CommissionInputDTO inputDTO = new CommissionInputDTO();
        CommissionOutputDTO dto = new CommissionOutputDTO();
        when(commissionService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<CommissionOutputDTO> response = commissionController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        CommissionInputDTO inputDTO = new CommissionInputDTO();
        CommissionOutputDTO dto = new CommissionOutputDTO();
        when(commissionService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<CommissionOutputDTO> response = commissionController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = commissionController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(commissionService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        CommissionOutputDTO dto = new CommissionOutputDTO();
        when(commissionService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<CommissionOutputDTO> response = commissionController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
