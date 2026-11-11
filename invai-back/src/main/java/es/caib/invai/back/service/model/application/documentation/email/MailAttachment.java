package es.caib.invai.back.service.model.application.documentation.email;

/**
 * A file attached to an e-mail.
 *
 * @param fileName name the file gets in the message
 * @param mimeType media type of the file, e.g. {@code application/pdf}
 * @param content  bytes of the file
 * @since 1.0.7
 */
public record MailAttachment(String fileName, String mimeType, byte[] content) {
}
