package es.caib.invai.back.persistence.repository.application.documentation.email;

import es.caib.invai.back.service.model.application.documentation.email.AppEmail;

/**
 * Outbound repository port for the history of the e-mails sent from the Documentació tab of the
 * applications.
 *
 * @since 1.0.7
 */
public interface AppEmailRepository {

    /**
     * Stores an e-mail in the history, with its recipients and its attachments.
     *
     * @param email the e-mail to store
     * @return the stored e-mail, with its identifier
     */
    AppEmail create(AppEmail email);
}
