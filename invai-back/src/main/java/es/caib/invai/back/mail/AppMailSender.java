package es.caib.invai.back.mail;

import es.caib.invai.back.exception.EmailSendException;
import es.caib.invai.back.service.model.application.documentation.email.MailAttachment;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Sends e-mails through the SMTP server configured in the properties of the application
 * ({@code es.caib.invai.mail.*}). The messages are sent from the fixed address of the application
 * (the account it authenticates with), because a mail server refuses, or marks as spoofed, a
 * message whose sender is not that account; the user who asks for the sending goes in Reply-To.
 * The sending is synchronous: it returns when the mail server has accepted the message.
 *
 * @since 1.0.7
 */
@Component
@Slf4j
public class AppMailSender {

    /** Milliseconds to wait for the connection to the mail server. */
    private static final int CONNECT_TIMEOUT_MS = 10_000;

    /** Milliseconds to wait for the mail server to answer, or to take what it is sent. */
    private static final int IO_TIMEOUT_MS = 60_000;

    /** Host of the SMTP server. */
    @Value("${es.caib.invai.mail.host}")
    private String host;

    /** Port of the SMTP server. */
    @Value("${es.caib.invai.mail.port}")
    private int port;

    /** Whether the connection is encrypted with STARTTLS (and refused if the server doesn't offer it). */
    @Value("${es.caib.invai.mail.starttls}")
    private boolean starttls;

    /** User the application authenticates with in the SMTP server. */
    @Value("${es.caib.invai.mail.username}")
    private String username;

    /** Password of that user. */
    @Value("${es.caib.invai.mail.password}")
    private String password;

    /** Address the messages are sent from: the account of the application in the mail server. */
    @Value("${es.caib.invai.mail.from}")
    private String from;

    /**
     * Sends a plain text message with attachments.
     *
     * @param replyTo     address of the user who sends the documentation, where the replies go
     * @param recipients  addresses the message is sent to
     * @param subject     subject of the message
     * @param body        text of the message
     * @param attachments files attached to the message, possibly none
     * @throws EmailSendException if the message can't be built or handed to the mail server
     */
    public void send(String replyTo, Set<String> recipients, String subject, String body, List<MailAttachment> attachments) {
        log.debug("Mail: Sending message with {} recipients and {} attachments", recipients.size(), attachments.size());
        try {
            JavaMailSenderImpl sender = createSender();
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setReplyTo(replyTo);
            helper.setTo(recipients.toArray(String[]::new));
            helper.setSubject(subject);
            helper.setText(body, false);
            for (MailAttachment attachment : attachments) {
                helper.addAttachment(MimeUtility.encodeText(attachment.fileName()), new ByteArrayResource(attachment.content()), attachment.mimeType());
            }
            sender.send(message);
        } catch (MessagingException | MailException | UnsupportedEncodingException ex) {
            log.error("Mail error: Failure sending the message", ex);
            throw new EmailSendException(ex);
        }
    }

    /**
     * Builds the sender that connects to the configured SMTP server, authenticating with the
     * configured user and, if so configured, encrypting the connection with STARTTLS.
     *
     * @return the sender
     */
    protected JavaMailSenderImpl createSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);
        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", String.valueOf(starttls));
        properties.put("mail.smtp.starttls.required", String.valueOf(starttls));
        properties.put("mail.smtp.connectiontimeout", String.valueOf(CONNECT_TIMEOUT_MS));
        properties.put("mail.smtp.timeout", String.valueOf(IO_TIMEOUT_MS));
        properties.put("mail.smtp.writetimeout", String.valueOf(IO_TIMEOUT_MS));
        return sender;
    }
}
