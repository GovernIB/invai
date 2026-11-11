package es.caib.invai.back.interna.application.documentation.email;

import es.caib.invai.back.interna.application.documentation.email.DTO.AppDocumentationEmailInputDTO;
import es.caib.invai.back.service.facade.application.documentation.email.AppDocumentationEmailService;
import es.caib.invai.back.utils.Constants;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal REST controller to send by e-mail documentation of applications: the attachments, a
 * specific version of each, of the Documentació tab.
 *
 * @since 1.0.7
 */
@Tag(name = "Enviament per correu", description = "Enviament per correu de documentació de les aplicacions.")
@RestController
@Slf4j
@RequestMapping("application/documentation/email")
@PreAuthorize(Constants.PRE_AUTH_ANY_ROLE)
public class AppDocumentationEmailController {

    /** Facade service handling the business use cases of the sending. */
    private final AppDocumentationEmailService appDocumentationEmailService;

    /**
     * Creates a controller instance with the given service dependency injected.
     *
     * @param appDocumentationEmailService the facade service used to send the e-mails
     */
    @Autowired
    public AppDocumentationEmailController(AppDocumentationEmailService appDocumentationEmailService) {
        this.appDocumentationEmailService = appDocumentationEmailService;
    }

    /**
     * Sends by e-mail attachments of an application, from the address of the application
     * and with the logged-in user as Reply-To, and keeps the sending in the history. It answers once the mail server has accepted the message.
     *
     * @param inputDTO the recipients, subject, optional message, and the attachments to send
     * @return HTTP 204 with no content
     */
    @PostMapping
    public ResponseEntity<Void> send(@Valid @RequestBody AppDocumentationEmailInputDTO inputDTO) {
        appDocumentationEmailService.send(inputDTO);
        return ResponseEntity.noContent().build();
    }
}
