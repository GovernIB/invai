package es.caib.invai.back.interna.maintenance.responsible.company;

import es.caib.invai.back.interna.maintenance.responsible.company.DTO.CompanyInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.company.DTO.CompanyOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.responsible.company.CompanyCriteria;
import es.caib.invai.back.service.facade.maintenance.responsible.company.CompanyService;
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
 * Unit tests for {@link CompanyController}, verifying that every endpoint delegates to
 * {@link CompanyService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class CompanyControllerTest {

    @Mock
    private CompanyService companyService;

    private CompanyController companyController;

    @BeforeEach
    void setUp() {
        companyController = new CompanyController(companyService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        CompanyOutputDTO dto = new CompanyOutputDTO();
        when(companyService.getById(1L)).thenReturn(dto);

        ResponseEntity<CompanyOutputDTO> response = companyController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        CompanyCriteria filter = new CompanyCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<CompanyOutputDTO> page = new PageImpl<>(List.of(new CompanyOutputDTO()));
        when(companyService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<CompanyOutputDTO>> response = companyController.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        CompanyInputDTO inputDTO = new CompanyInputDTO("Name");
        CompanyOutputDTO dto = new CompanyOutputDTO();
        when(companyService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<CompanyOutputDTO> response = companyController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        CompanyInputDTO inputDTO = new CompanyInputDTO("Name");
        CompanyOutputDTO dto = new CompanyOutputDTO();
        when(companyService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<CompanyOutputDTO> response = companyController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = companyController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(companyService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        CompanyOutputDTO dto = new CompanyOutputDTO();
        when(companyService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<CompanyOutputDTO> response = companyController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
