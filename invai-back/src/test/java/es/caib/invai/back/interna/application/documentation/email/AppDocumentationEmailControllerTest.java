package es.caib.invai.back.interna.application.documentation.email;

import es.caib.invai.back.interna.application.documentation.email.DTO.AppDocumentationEmailAttachmentInputDTO;
import es.caib.invai.back.interna.application.documentation.email.DTO.AppDocumentationEmailInputDTO;
import es.caib.invai.back.service.facade.application.documentation.email.AppDocumentationEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link AppDocumentationEmailController}, verifying that the endpoint delegates to
 * {@link AppDocumentationEmailService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class AppDocumentationEmailControllerTest {

    @Mock
    private AppDocumentationEmailService appDocumentationEmailService;

    private AppDocumentationEmailController controller;

    @BeforeEach
    void setUp() {
        controller = new AppDocumentationEmailController(appDocumentationEmailService);
    }

    @Test
    void send_delegatesToTheServiceAndReturnsNoContent() {
        AppDocumentationEmailInputDTO input = new AppDocumentationEmailInputDTO(3L, List.of("a@caib.es"), "Assumpte", null, List.of(new AppDocumentationEmailAttachmentInputDTO(7L, 70L)));

        ResponseEntity<Void> response = controller.send(input);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(appDocumentationEmailService).send(input);
    }
}
