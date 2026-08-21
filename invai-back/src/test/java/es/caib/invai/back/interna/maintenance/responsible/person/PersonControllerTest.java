package es.caib.invai.back.interna.maintenance.responsible.person;

import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.responsible.person.PersonCriteria;
import es.caib.invai.back.service.facade.maintenance.responsible.person.PersonService;
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
 * Unit tests for {@link PersonController}, verifying that every endpoint delegates to
 * {@link PersonService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class PersonControllerTest {

    @Mock
    private PersonService personService;

    private PersonController personController;

    @BeforeEach
    void setUp() {
        personController = new PersonController(personService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        PersonOutputDTO dto = new PersonOutputDTO();
        when(personService.getById(1L)).thenReturn(dto);

        ResponseEntity<PersonOutputDTO> response = personController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        PersonCriteria filter = new PersonCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<PersonOutputDTO> page = new PageImpl<>(List.of(new PersonOutputDTO()));
        when(personService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<PersonOutputDTO>> response = personController.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        PersonInputDTO inputDTO = new PersonInputDTO();
        inputDTO.setCompanyId(1L);
        inputDTO.setFirstName("Joan");
        inputDTO.setLastName("Puig");
        inputDTO.setEmail("joan.puig@example.com");
        PersonOutputDTO dto = new PersonOutputDTO();
        when(personService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<PersonOutputDTO> response = personController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        PersonInputDTO inputDTO = new PersonInputDTO();
        inputDTO.setCompanyId(1L);
        inputDTO.setFirstName("Joan");
        inputDTO.setLastName("Puig");
        inputDTO.setEmail("joan.puig@example.com");
        PersonOutputDTO dto = new PersonOutputDTO();
        when(personService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<PersonOutputDTO> response = personController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = personController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(personService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        PersonOutputDTO dto = new PersonOutputDTO();
        when(personService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<PersonOutputDTO> response = personController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
