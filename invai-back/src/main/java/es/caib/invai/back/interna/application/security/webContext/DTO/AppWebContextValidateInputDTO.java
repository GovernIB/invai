package es.caib.invai.back.interna.application.security.webContext.DTO;

import es.caib.invai.back.utils.Constants;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Inbound payload for Security's one-way validation of an application web context assignment
 * (created and maintained exclusively from the Development side), carrying the free-text
 * justification recorded alongside the validation.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppWebContextValidateInputDTO {

    /** Free-text reason given for validating this web context. */
    @NotBlank(message = "{" + Constants.VALIDATION_APPWEBCONTEXT_VALIDATE_REASON_REQUIRED + "}")
    private String reason;
}
