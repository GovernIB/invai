package es.caib.invai.back.service.model.application.documentation.email;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Domain model representing an e-mail sent from the Documentació tab of an application with
 * attachments, as it is kept in the history.
 *
 * @since 1.0.7
 */
@Getter
@Setter
public class AppEmail {

    /** Unique database identifier key, set once the e-mail is stored in the history. */
    private Long id;

    /** Identifier of the application the documentation was sent from. */
    private Long applicationId;

    /** E-mail address of the user who sent it, used as the Reply-To of the message. */
    private String senderEmail;

    /** Subject of the message. */
    private String subject;

    /** Optional text the user wrote in the message. */
    private String message;

    /** E-mail addresses the message was sent to. */
    private Set<String> recipients = new HashSet<>();

    /** Attachments the message carries. */
    private List<AppEmailItem> items = new ArrayList<>();
}
