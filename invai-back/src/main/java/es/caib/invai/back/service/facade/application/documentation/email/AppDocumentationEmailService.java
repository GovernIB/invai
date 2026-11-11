package es.caib.invai.back.service.facade.application.documentation.email;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.exception.EmailSendException;
import es.caib.invai.back.interna.application.documentation.email.DTO.AppDocumentationEmailInputDTO;

/**
 * Facade service interface for sending by e-mail documentation of an application.
 *
 * @since 1.0.7
 */
public interface AppDocumentationEmailService {

    /**
     * Sends by e-mail the chosen attachments of an application, from the address of the
     * application and with the user who asks for it as Reply-To, and keeps the sending in the history. The sending is synchronous: when
     * it fails nothing is kept in the history.
     *
     * @param inputDTO the recipients, subject, optional message, and the attachments to send
     * @throws BusinessRuleException if the application, a document or a version doesn't exist or
     *                               doesn't belong to the application, is deleted, nothing is selected,
     *                               the attachments are too large, or the address of the sender is unknown
     * @throws EmailSendException    if the message can't be handed to the mail server
     */
    void send(AppDocumentationEmailInputDTO inputDTO);
}
