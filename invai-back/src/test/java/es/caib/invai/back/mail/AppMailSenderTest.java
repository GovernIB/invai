package es.caib.invai.back.mail;

import es.caib.invai.back.exception.EmailSendException;
import es.caib.invai.back.service.model.application.documentation.email.MailAttachment;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.Provider;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.URLName;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AppMailSender}. The connection to the SMTP server is replaced by an
 * in-memory {@link Session} whose SMTP transport records the messages instead of sending them.
 */
class AppMailSenderTest {

    private static final String APP_ADDRESS = "no-reply-invai@plexus.services";

    private Session session;

    @BeforeEach
    void setUp() throws MessagingException {
        RecordingTransport.sent.clear();
        RecordingTransport.failure = null;
        session = Session.getInstance(new Properties());
        session.setProvider(new Provider(Provider.Type.TRANSPORT, "smtp", RecordingTransport.class.getName(), "test", "1"));
    }

    @AfterEach
    void tearDown() {
        RecordingTransport.sent.clear();
        RecordingTransport.failure = null;
    }

    private static AppMailSender configuredSender(boolean starttls) {
        AppMailSender sender = new AppMailSender();
        ReflectionTestUtils.setField(sender, "host", "correo.plexus.services");
        ReflectionTestUtils.setField(sender, "port", 587);
        ReflectionTestUtils.setField(sender, "starttls", starttls);
        ReflectionTestUtils.setField(sender, "username", APP_ADDRESS);
        ReflectionTestUtils.setField(sender, "password", "secret");
        ReflectionTestUtils.setField(sender, "from", APP_ADDRESS);
        return sender;
    }

    private AppMailSender senderWithSession() {
        AppMailSender sender = new AppMailSender() {
            @Override
            protected JavaMailSenderImpl createSender() {
                JavaMailSenderImpl javaMailSender = super.createSender();
                javaMailSender.setSession(session);
                return javaMailSender;
            }
        };
        ReflectionTestUtils.setField(sender, "host", "correo.plexus.services");
        ReflectionTestUtils.setField(sender, "port", 587);
        ReflectionTestUtils.setField(sender, "from", APP_ADDRESS);
        return sender;
    }

    private static void collect(Part part, List<Part> attachments, StringBuilder text) throws Exception {
        if (part.isMimeType("multipart/*")) {
            MimeMultipart multipart = (MimeMultipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                collect(multipart.getBodyPart(i), attachments, text);
            }
        } else if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
            attachments.add(part);
        } else if (part.isMimeType("text/plain")) {
            text.append(part.getContent());
        }
    }

    private static byte[] bytesOf(Part part) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        part.getDataHandler().writeTo(out);
        return out.toByteArray();
    }

    @Test
    void send_messageWithAttachments_handsItToTheMailServerComplete() throws Exception {
        byte[] pdf = {1, 2, 3, 4};
        byte[] txt = "hola".getBytes(StandardCharsets.UTF_8);

        senderWithSession().send("sender@caib.es", new LinkedHashSet<>(List.of("a@caib.es", "b@caib.es")), "Documentació d'aplicació",
                "Hola\nAdeu ç", List.of(
                        new MailAttachment("manual.pdf", "application/pdf", pdf),
                        new MailAttachment("Guía rápida.txt", "text/plain", txt)));

        assertEquals(1, RecordingTransport.sent.size());
        MimeMessage message = RecordingTransport.sent.get(0);
        assertEquals(APP_ADDRESS, message.getFrom()[0].toString());
        assertEquals("sender@caib.es", message.getReplyTo()[0].toString());
        List<String> to = new ArrayList<>();
        for (Address address : message.getRecipients(Message.RecipientType.TO)) {
            to.add(address.toString());
        }
        assertEquals(List.of("a@caib.es", "b@caib.es"), to);
        assertEquals("Documentació d'aplicació", message.getSubject());

        List<Part> attachments = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        collect(message, attachments, text);
        assertEquals("Hola\nAdeu ç", text.toString());
        assertEquals(2, attachments.size());
        assertEquals("manual.pdf", MimeUtility.decodeText(attachments.get(0).getFileName()));
        assertArrayEquals(pdf, bytesOf(attachments.get(0)));
        assertTrue(attachments.get(0).isMimeType("application/pdf"));
        assertEquals("Guía rápida.txt", MimeUtility.decodeText(attachments.get(1).getFileName()));
        assertArrayEquals(txt, bytesOf(attachments.get(1)));
    }

    @Test
    void send_withoutAttachmentsOrText_stillSendsTheMessage() throws Exception {
        senderWithSession().send("sender@caib.es", new LinkedHashSet<>(List.of("a@caib.es")), "Assumpte", "", List.of());

        assertEquals(1, RecordingTransport.sent.size());
        assertEquals("Assumpte", RecordingTransport.sent.get(0).getSubject());
    }

    @Test
    void createSender_usesTheConfiguredServerWithStartTlsAndAuthentication() {
        JavaMailSenderImpl sender = configuredSender(true).createSender();

        assertEquals("correo.plexus.services", sender.getHost());
        assertEquals(587, sender.getPort());
        assertEquals(APP_ADDRESS, sender.getUsername());
        assertEquals("secret", sender.getPassword());
        assertEquals("true", sender.getJavaMailProperties().get("mail.smtp.auth"));
        assertEquals("true", sender.getJavaMailProperties().get("mail.smtp.starttls.enable"));
        assertEquals("true", sender.getJavaMailProperties().get("mail.smtp.starttls.required"));
    }

    @Test
    void createSender_withoutStartTls_doesNotAskForIt() {
        JavaMailSenderImpl sender = configuredSender(false).createSender();

        assertEquals("false", sender.getJavaMailProperties().get("mail.smtp.starttls.enable"));
        assertEquals("false", sender.getJavaMailProperties().get("mail.smtp.starttls.required"));
    }

    @Test
    void createSender_hasTimeoutsSoAStuckMailServerDoesNotHangTheRequest() {
        JavaMailSenderImpl sender = configuredSender(true).createSender();

        assertEquals("10000", sender.getJavaMailProperties().get("mail.smtp.connectiontimeout"));
        assertEquals("60000", sender.getJavaMailProperties().get("mail.smtp.timeout"));
        assertEquals("60000", sender.getJavaMailProperties().get("mail.smtp.writetimeout"));
    }

    @Test
    void send_mailServerRefusesTheMessage_throwsEmailSendException() {
        RecordingTransport.failure = new MessagingException("550 relay denied");

        EmailSendException ex = assertThrows(EmailSendException.class,
                () -> senderWithSession().send("sender@caib.es", new LinkedHashSet<>(List.of("a@caib.es")), "Assumpte", "text", List.of()));

        assertInstanceOf(org.springframework.mail.MailException.class, ex.getCause());
    }

    @Test
    void send_malformedReplyToAddress_throwsEmailSendException() {
        assertThrows(EmailSendException.class,
                () -> senderWithSession().send("not an address @@", new LinkedHashSet<>(List.of("a@caib.es")), "Assumpte", "text", List.of()));

        assertTrue(RecordingTransport.sent.isEmpty());
    }

    /**
     * Transport that records the messages it is asked to send, or fails with {@link #failure} when set.
     */
    public static class RecordingTransport extends Transport {

        static final List<MimeMessage> sent = new ArrayList<>();
        static volatile MessagingException failure;

        public RecordingTransport(Session session, URLName urlName) {
            super(session, urlName);
        }

        @Override
        protected boolean protocolConnect(String host, int port, String user, String password) {
            return true;
        }

        @Override
        public void sendMessage(Message message, Address[] addresses) throws MessagingException {
            if (failure != null) {
                throw failure;
            }
            sent.add((MimeMessage) message);
        }
    }
}
