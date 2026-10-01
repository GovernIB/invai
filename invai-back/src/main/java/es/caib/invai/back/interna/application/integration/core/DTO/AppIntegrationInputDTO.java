package es.caib.invai.back.interna.application.integration.core.DTO;

import es.caib.invai.back.utils.Constants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

/**
 * Inbound payload for creating or updating an integration anchor. {@code applicationId} is the
 * only field enforced by bean validation ({@code @NotNull}); {@code observation} is always optional.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppIntegrationInputDTO {

    /** Identifier of the application this anchor belongs to; required, and immutable after creation. */
    @NotNull(message = "{" + Constants.VALIDATION_APPINTEGRATION_APPLICATIONID + "}")
    private Long applicationId;

    /** Free-text observations about this application's integrations; never required. */
    private String observation;
}
