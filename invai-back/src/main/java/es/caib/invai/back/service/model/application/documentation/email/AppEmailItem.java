package es.caib.invai.back.service.model.application.documentation.email;

import lombok.Getter;
import lombok.Setter;

/**
 * Domain model representing an attachment of a sent e-mail. The text fields keep what was
 * sent, so the history stays accurate even if the document changes later.
 *
 * @since 1.0.7
 */
@Getter
@Setter
public class AppEmailItem {

    /** Identifier of the version of the document that was attached. */
    private Long versionId;

    /** Title of the document when it was sent. */
    private String title;

    /** Version label of the attached file when it was sent. */
    private String versionLabel;

    /** Name of the attached file when it was sent. */
    private String fileName;
}
