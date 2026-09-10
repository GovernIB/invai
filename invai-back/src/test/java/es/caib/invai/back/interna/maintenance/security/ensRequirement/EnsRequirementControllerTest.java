package es.caib.invai.back.interna.maintenance.security.ensRequirement;

import es.caib.invai.back.interna.maintenance.security.ensRequirement.DTO.EnsRequirementInputDTO;
import es.caib.invai.back.interna.maintenance.security.ensRequirement.DTO.EnsRequirementOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.security.ensRequirement.EnsRequirementCriteria;
import es.caib.invai.back.service.facade.maintenance.security.ensRequirement.EnsRequirementService;
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
 * Unit tests for {@link EnsRequirementController}, verifying that every endpoint delegates to
 * {@link EnsRequirementService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class EnsRequirementControllerTest {

    @Mock
    private EnsRequirementService ensRequirementService;

    private EnsRequirementController ensRequirementController;

    @BeforeEach
    void setUp() {
        ensRequirementController = new EnsRequirementController(ensRequirementService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        EnsRequirementOutputDTO dto = new EnsRequirementOutputDTO();
        when(ensRequirementService.getById(1L)).thenReturn(dto);

        ResponseEntity<EnsRequirementOutputDTO> response = ensRequirementController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        EnsRequirementCriteria filter = new EnsRequirementCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<EnsRequirementOutputDTO> page = new PageImpl<>(List.of(new EnsRequirementOutputDTO()));
        when(ensRequirementService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<EnsRequirementOutputDTO>> response = ensRequirementController.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        EnsRequirementInputDTO inputDTO = new EnsRequirementInputDTO("Name", "Name ES");
        EnsRequirementOutputDTO dto = new EnsRequirementOutputDTO();
        when(ensRequirementService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<EnsRequirementOutputDTO> response = ensRequirementController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        EnsRequirementInputDTO inputDTO = new EnsRequirementInputDTO("Name", "Name ES");
        EnsRequirementOutputDTO dto = new EnsRequirementOutputDTO();
        when(ensRequirementService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<EnsRequirementOutputDTO> response = ensRequirementController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = ensRequirementController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(ensRequirementService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        EnsRequirementOutputDTO dto = new EnsRequirementOutputDTO();
        when(ensRequirementService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<EnsRequirementOutputDTO> response = ensRequirementController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
