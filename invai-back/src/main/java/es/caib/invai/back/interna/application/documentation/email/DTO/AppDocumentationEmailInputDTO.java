package es.caib.invai.back.interna.application.documentation.email.DTO;

import es.caib.invai.back.utils.Constants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Data Transfer Object (DTO) modeling the inbound payload to send by e-mail documentation of an
 * application: the files, a specific version of each, chosen in the Documentació tab.
 * At least one attachment is required, which is checked when the e-mail is sent.
 *
 * @since 1.0.7
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppDocumentationEmailInputDTO {

    /** Identifier of the application the documentation belongs to. */
    @NotNull(message = "{" + Constants.VALIDATION_EMAIL_APPLICATION_REQUIRED + "}")
    private Long applicationId;

    /** E-mail addresses the message is sent to, one or more. */
    @NotEmpty(message = "{" + Constants.VALIDATION_EMAIL_RECIPIENTS_REQUIRED + "}")
    @Size(max = 20, message = "{" + Constants.VALIDATION_EMAIL_RECIPIENTS_SIZE + "}")
    private List<
            @NotBlank(message = "{" + Constants.VALIDATION_EMAIL_RECIPIENT_INVALID + "}")
            @Size(max = 320, message = "{" + Constants.VALIDATION_EMAIL_RECIPIENT_SIZE + "}")
            @Email(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "{" + Constants.VALIDATION_EMAIL_RECIPIENT_INVALID + "}")
            String> recipients;

    /** Subject of the message, on a single line. */
    @NotBlank(message = "{" + Constants.VALIDATION_EMAIL_SUBJECT_REQUIRED + "}")
    @Size(max = 250, message = "{" + Constants.VALIDATION_EMAIL_SUBJECT_SIZE + "}")
    @Pattern(regexp = "^[^\\r\\n]*$", message = "{" + Constants.VALIDATION_EMAIL_SUBJECT_INVALID + "}")
    private String subject;

    /** Optional text of the message. */
    @Size(max = 4000, message = "{" + Constants.VALIDATION_EMAIL_MESSAGE_SIZE + "}")
    private String message;

    /** Files to attach, a specific version of each document. */
    @Valid
    private List<AppDocumentationEmailAttachmentInputDTO> attachments;
}
