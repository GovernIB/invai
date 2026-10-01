package es.caib.invai.back.interna.application.responsibleAuthorized.dir3.DTO;

import es.caib.invai.back.utils.Constants;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Inbound payload for manually validating a DIR3 mismatch (promoting {@code NOT_VALIDATED} to
 * {@code MANUAL}), carrying the free-text justification for overriding the automatic
 * Soffid-based check.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Dir3ManualValidateInputDTO {

    /** Free-text reason for manually validating the DIR3 mismatch. */
    @NotBlank(message = "{" + Constants.VALIDATION_DIR3VALIDATION_REASON_REQUIRED + "}")
    private String reason;
}
