package es.caib.invai.back.interna.maintenance.admUnit;

import es.caib.invai.back.interna.maintenance.admUnit.DTO.AdmUnitInputDTO;
import es.caib.invai.back.interna.maintenance.admUnit.DTO.AdmUnitOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.admUnit.AdmUnitCriteria;
import es.caib.invai.back.service.facade.maintenance.admUnit.AdmUnitService;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AdmUnitController}, verifying that every endpoint delegates to
 * {@link AdmUnitService} and returns the expected HTTP status code.
 * <p>
 * {@link AdmUnitController} wires its collaborator via field injection (no constructor is
 * declared), so the mocked service is bound in via {@link ReflectionTestUtils}.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class AdmUnitControllerTest {

    @Mock
    private AdmUnitService admUnitService;

    private AdmUnitController admUnitController;

    @BeforeEach
    void setUp() {
        admUnitController = new AdmUnitController();
        ReflectionTestUtils.setField(admUnitController, "admUnitService", admUnitService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        AdmUnitOutputDTO dto = new AdmUnitOutputDTO();
        when(admUnitService.getById(1L)).thenReturn(dto);

        ResponseEntity<AdmUnitOutputDTO> response = admUnitController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        AdmUnitCriteria filter = new AdmUnitCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AdmUnitOutputDTO> page = new PageImpl<>(List.of(new AdmUnitOutputDTO()));
        when(admUnitService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<AdmUnitOutputDTO>> response = admUnitController.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        AdmUnitInputDTO inputDTO = new AdmUnitInputDTO("PRO", "Name", "Nombre");
        AdmUnitOutputDTO dto = new AdmUnitOutputDTO();
        when(admUnitService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<AdmUnitOutputDTO> response = admUnitController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        AdmUnitInputDTO inputDTO = new AdmUnitInputDTO("PRO", "Name", "Nombre");
        AdmUnitOutputDTO dto = new AdmUnitOutputDTO();
        when(admUnitService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<AdmUnitOutputDTO> response = admUnitController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = admUnitController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(admUnitService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        AdmUnitOutputDTO dto = new AdmUnitOutputDTO();
        when(admUnitService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<AdmUnitOutputDTO> response = admUnitController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
