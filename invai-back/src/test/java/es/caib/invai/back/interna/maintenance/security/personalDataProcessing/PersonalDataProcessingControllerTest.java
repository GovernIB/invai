package es.caib.invai.back.interna.maintenance.security.personalDataProcessing;

import es.caib.invai.back.interna.maintenance.security.personalDataProcessing.DTO.PersonalDataProcessingInputDTO;
import es.caib.invai.back.interna.maintenance.security.personalDataProcessing.DTO.PersonalDataProcessingOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.security.personalDataProcessing.PersonalDataProcessingCriteria;
import es.caib.invai.back.service.facade.maintenance.security.personalDataProcessing.PersonalDataProcessingService;
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
 * Unit tests for {@link PersonalDataProcessingController}, verifying that every endpoint delegates to
 * {@link PersonalDataProcessingService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class PersonalDataProcessingControllerTest {

    @Mock
    private PersonalDataProcessingService personalDataProcessingService;

    private PersonalDataProcessingController personalDataProcessingController;

    @BeforeEach
    void setUp() {
        personalDataProcessingController = new PersonalDataProcessingController(personalDataProcessingService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        PersonalDataProcessingOutputDTO dto = new PersonalDataProcessingOutputDTO();
        when(personalDataProcessingService.getById(1L)).thenReturn(dto);

        ResponseEntity<PersonalDataProcessingOutputDTO> response = personalDataProcessingController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        PersonalDataProcessingCriteria filter = new PersonalDataProcessingCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<PersonalDataProcessingOutputDTO> page = new PageImpl<>(List.of(new PersonalDataProcessingOutputDTO()));
        when(personalDataProcessingService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<PersonalDataProcessingOutputDTO>> response = personalDataProcessingController.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        PersonalDataProcessingInputDTO inputDTO = new PersonalDataProcessingInputDTO("Name", "Name ES");
        PersonalDataProcessingOutputDTO dto = new PersonalDataProcessingOutputDTO();
        when(personalDataProcessingService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<PersonalDataProcessingOutputDTO> response = personalDataProcessingController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        PersonalDataProcessingInputDTO inputDTO = new PersonalDataProcessingInputDTO("Name", "Name ES");
        PersonalDataProcessingOutputDTO dto = new PersonalDataProcessingOutputDTO();
        when(personalDataProcessingService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<PersonalDataProcessingOutputDTO> response = personalDataProcessingController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = personalDataProcessingController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(personalDataProcessingService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        PersonalDataProcessingOutputDTO dto = new PersonalDataProcessingOutputDTO();
        when(personalDataProcessingService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<PersonalDataProcessingOutputDTO> response = personalDataProcessingController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
