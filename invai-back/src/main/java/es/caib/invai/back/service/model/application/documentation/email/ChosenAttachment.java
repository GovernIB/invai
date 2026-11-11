package es.caib.invai.back.service.model.application.documentation.email;

import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationAttachmentVersion;

/**
 * A version chosen to be attached to an e-mail, with the item of the history that records it.
 *
 * @param item    the item of the history
 * @param version the version whose file is attached
 * @since 1.0.7
 */
public record ChosenAttachment(AppEmailItem item, AppDocumentationAttachmentVersion version) {
}
