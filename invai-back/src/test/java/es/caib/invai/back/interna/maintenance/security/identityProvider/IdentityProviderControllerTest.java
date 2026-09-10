package es.caib.invai.back.interna.maintenance.security.identityProvider;

import es.caib.invai.back.interna.maintenance.security.identityProvider.DTO.IdentityProviderInputDTO;
import es.caib.invai.back.interna.maintenance.security.identityProvider.DTO.IdentityProviderOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.security.identityProvider.IdentityProviderCriteria;
import es.caib.invai.back.service.facade.maintenance.security.identityProvider.IdentityProviderService;
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
 * Unit tests for {@link IdentityProviderController}, verifying that every endpoint delegates to
 * {@link IdentityProviderService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class IdentityProviderControllerTest {

    @Mock
    private IdentityProviderService identityProviderService;

    private IdentityProviderController identityProviderController;

    @BeforeEach
    void setUp() {
        identityProviderController = new IdentityProviderController(identityProviderService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        IdentityProviderOutputDTO dto = new IdentityProviderOutputDTO();
        when(identityProviderService.getById(1L)).thenReturn(dto);

        ResponseEntity<IdentityProviderOutputDTO> response = identityProviderController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        IdentityProviderCriteria filter = new IdentityProviderCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<IdentityProviderOutputDTO> page = new PageImpl<>(List.of(new IdentityProviderOutputDTO()));
        when(identityProviderService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<IdentityProviderOutputDTO>> response = identityProviderController.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        IdentityProviderInputDTO inputDTO = new IdentityProviderInputDTO("Name");
        IdentityProviderOutputDTO dto = new IdentityProviderOutputDTO();
        when(identityProviderService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<IdentityProviderOutputDTO> response = identityProviderController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        IdentityProviderInputDTO inputDTO = new IdentityProviderInputDTO("Name");
        IdentityProviderOutputDTO dto = new IdentityProviderOutputDTO();
        when(identityProviderService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<IdentityProviderOutputDTO> response = identityProviderController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = identityProviderController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(identityProviderService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        IdentityProviderOutputDTO dto = new IdentityProviderOutputDTO();
        when(identityProviderService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<IdentityProviderOutputDTO> response = identityProviderController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
