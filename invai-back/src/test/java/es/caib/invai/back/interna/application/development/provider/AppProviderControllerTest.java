package es.caib.invai.back.interna.application.development.provider;

import es.caib.invai.back.interna.application.development.provider.DTO.AppProviderInputDTO;
import es.caib.invai.back.interna.application.development.provider.DTO.AppProviderOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.provider.AppProviderCriteria;
import es.caib.invai.back.service.facade.application.development.provider.AppProviderService;
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
 * Unit tests for {@link AppProviderController}, verifying that every endpoint delegates to
 * {@link AppProviderService} and returns the expected HTTP status code. This is a child "link"
 * entity scoped to a parent application: it has no {@code getById} or {@code reactivate} endpoints.
 */
@ExtendWith(MockitoExtension.class)
class AppProviderControllerTest {

    @Mock
    private AppProviderService appProviderService;

    private AppProviderController appProviderController;

    @BeforeEach
    void setUp() {
        appProviderController = new AppProviderController(appProviderService);
    }

    @Test
    void getAllByApplicationId_returnsOkWithPagedServiceResult() {
        AppProviderCriteria criteria = new AppProviderCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppProviderOutputDTO> page = new PageImpl<>(List.of(new AppProviderOutputDTO()));
        when(appProviderService.getAll(10L, criteria, pageable)).thenReturn(page);

        ResponseEntity<Page<AppProviderOutputDTO>> response =
                appProviderController.getAllByAppDevelopmentId(10L, criteria, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        AppProviderInputDTO inputDTO = new AppProviderInputDTO(1L, "Acme", null, null, null);
        AppProviderOutputDTO dto = new AppProviderOutputDTO();
        when(appProviderService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<AppProviderOutputDTO> response = appProviderController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        AppProviderInputDTO inputDTO = new AppProviderInputDTO(1L, "Acme", null, null, null);
        AppProviderOutputDTO dto = new AppProviderOutputDTO();
        when(appProviderService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<AppProviderOutputDTO> response = appProviderController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = appProviderController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appProviderService).delete(1L);
    }
}
