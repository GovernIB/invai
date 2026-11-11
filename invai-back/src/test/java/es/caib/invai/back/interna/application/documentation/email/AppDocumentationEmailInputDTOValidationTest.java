package es.caib.invai.back.interna.application.documentation.email;

import es.caib.invai.back.interna.application.documentation.email.DTO.AppDocumentationEmailAttachmentInputDTO;
import es.caib.invai.back.interna.application.documentation.email.DTO.AppDocumentationEmailInputDTO;
import es.caib.invai.back.utils.Constants;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests of the bean validation rules of {@link AppDocumentationEmailInputDTO}. The messages are
 * the keys in braces, the ones the application resolves against its message bundles.
 */
class AppDocumentationEmailInputDTOValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        factory = Validation.byDefaultProvider().configure()
                .messageInterpolator(new ParameterMessageInterpolator()).buildValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeFactory() {
        factory.close();
    }

    private static AppDocumentationEmailInputDTO valid() {
        return new AppDocumentationEmailInputDTO(3L, new ArrayList<>(List.of("a@caib.es")), "Assumpte", "Hola",
                new ArrayList<>(List.of(new AppDocumentationEmailAttachmentInputDTO(7L, 70L))));
    }

    private static Set<String> messagesOf(AppDocumentationEmailInputDTO dto) {
        Set<String> messages = new TreeSet<>();
        for (ConstraintViolation<AppDocumentationEmailInputDTO> violation : validator.validate(dto)) {
            messages.add(violation.getMessage());
        }
        return messages;
    }

    private static Set<String> keys(String... keys) {
        return new TreeSet<>(Arrays.stream(keys).map(key -> "{" + key + "}").toList());
    }

    @Test
    void validInput_hasNoViolations() {
        assertTrue(messagesOf(valid()).isEmpty());
    }

    @Test
    void optionalFieldsMissing_areAllowedByTheBeanAsTheFacadeChecksTheItems() {
        AppDocumentationEmailInputDTO dto = valid();
        dto.setMessage(null);
        dto.setAttachments(null);

        assertTrue(messagesOf(dto).isEmpty());
    }

    @Test
    void missingApplication_isRejected() {
        AppDocumentationEmailInputDTO dto = valid();
        dto.setApplicationId(null);

        assertEquals(keys(Constants.VALIDATION_EMAIL_APPLICATION_REQUIRED), messagesOf(dto));
    }

    @Test
    void noRecipients_isRejected() {
        AppDocumentationEmailInputDTO dto = valid();
        dto.setRecipients(List.of());

        assertEquals(keys(Constants.VALIDATION_EMAIL_RECIPIENTS_REQUIRED), messagesOf(dto));
    }

    @Test
    void moreThanTwentyRecipients_isRejected() {
        AppDocumentationEmailInputDTO dto = valid();
        List<String> many = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            many.add("user" + i + "@caib.es");
        }
        dto.setRecipients(many);

        assertEquals(keys(Constants.VALIDATION_EMAIL_RECIPIENTS_SIZE), messagesOf(dto));
    }

    @Test
    void invalidRecipientAddresses_areRejected() {
        for (String address : new String[]{"", "   ", "plain", "a@b", "a b@caib.es", "@caib.es"}) {
            AppDocumentationEmailInputDTO dto = valid();
            dto.setRecipients(List.of(address));

            assertEquals(keys(Constants.VALIDATION_EMAIL_RECIPIENT_INVALID), messagesOf(dto), "address: [" + address + "]");
        }
    }

    @Test
    void recipientLongerThan320_isRejectedBySize() {
        AppDocumentationEmailInputDTO dto = valid();
        dto.setRecipients(List.of("a".repeat(320) + "@caib.es"));

        assertTrue(messagesOf(dto).contains("{" + Constants.VALIDATION_EMAIL_RECIPIENT_SIZE + "}"));
    }

    @Test
    void blankSubject_isRejected() {
        AppDocumentationEmailInputDTO dto = valid();
        dto.setSubject("  ");

        assertEquals(keys(Constants.VALIDATION_EMAIL_SUBJECT_REQUIRED), messagesOf(dto));
    }

    @Test
    void subjectLongerThan250_isRejected() {
        AppDocumentationEmailInputDTO dto = valid();
        dto.setSubject("a".repeat(251));

        assertEquals(keys(Constants.VALIDATION_EMAIL_SUBJECT_SIZE), messagesOf(dto));
    }

    @Test
    void subjectWithLineBreak_isRejectedToAvoidHeaderInjection() {
        for (String subject : new String[]{"one\ntwo", "one\rtwo", "one\r\nBcc: x@y.com"}) {
            AppDocumentationEmailInputDTO dto = valid();
            dto.setSubject(subject);

            assertEquals(keys(Constants.VALIDATION_EMAIL_SUBJECT_INVALID), messagesOf(dto));
        }
    }

    @Test
    void messageLongerThan4000_isRejected() {
        AppDocumentationEmailInputDTO dto = valid();
        dto.setMessage("a".repeat(4001));

        assertEquals(keys(Constants.VALIDATION_EMAIL_MESSAGE_SIZE), messagesOf(dto));
    }

    @Test
    void attachmentWithoutDocumentOrVersion_isRejected() {
        AppDocumentationEmailInputDTO dto = valid();
        dto.setAttachments(List.of(new AppDocumentationEmailAttachmentInputDTO(null, null)));

        assertEquals(keys(Constants.VALIDATION_EMAIL_DOCUMENT_REQUIRED, Constants.VALIDATION_EMAIL_VERSION_REQUIRED), messagesOf(dto));
    }
}
