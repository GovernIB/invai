package es.caib.invai.back.interna.application.documentation.link;

import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkInputDTO;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkOutputDTO;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkUpdateInputDTO;
import es.caib.invai.back.persistence.repository.application.documentation.link.AppDocumentationLinkCriteria;
import es.caib.invai.back.service.facade.application.documentation.link.AppDocumentationLinkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppDocumentationLinkController}, verifying that every endpoint delegates to {@link
 * AppDocumentationLinkService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class AppDocumentationLinkControllerTest {

    @Mock
    private AppDocumentationLinkService appDocumentationLinkService;

    private AppDocumentationLinkController controller;

    @BeforeEach
    void setUp() {
        controller = new AppDocumentationLinkController(appDocumentationLinkService);
    }

    @Test
    void getAll_scopesTheFilterByTheApplicationAndReturnsOk() {
        AppDocumentationLinkCriteria filter = new AppDocumentationLinkCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Page<AppDocumentationLinkOutputDTO> page = new PageImpl<>(List.of(new AppDocumentationLinkOutputDTO()));
        when(appDocumentationLinkService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<AppDocumentationLinkOutputDTO>> response = controller.getAll(3L, filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
        assertEquals(3L, filter.getApplicationId());
    }

    @Test
    void create_returnsCreatedWithTheLink() {
        AppDocumentationLinkInputDTO inputDTO = new AppDocumentationLinkInputDTO(3L, "Manual", null, 5L, "https://intranet.caib.es/manual");
        AppDocumentationLinkOutputDTO dto = new AppDocumentationLinkOutputDTO();
        when(appDocumentationLinkService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<AppDocumentationLinkOutputDTO> response = controller.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithTheLink() {
        AppDocumentationLinkUpdateInputDTO inputDTO = new AppDocumentationLinkUpdateInputDTO("Manual", null, 5L, "https://intranet.caib.es/manual");
        AppDocumentationLinkOutputDTO dto = new AppDocumentationLinkOutputDTO();
        when(appDocumentationLinkService.update(10L, inputDTO)).thenReturn(dto);

        ResponseEntity<AppDocumentationLinkOutputDTO> response = controller.update(10L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContent() {
        ResponseEntity<Void> response = controller.delete(10L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(appDocumentationLinkService).delete(10L);
    }
}
