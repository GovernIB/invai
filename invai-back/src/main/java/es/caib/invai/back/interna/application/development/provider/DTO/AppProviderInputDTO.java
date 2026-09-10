package es.caib.invai.back.interna.application.development.provider.DTO;

import es.caib.invai.back.utils.Constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Inbound payload for creating or updating a provider assignment linked to a development module.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppProviderInputDTO {

    /** Identifier of the owning development record. */
    @NotNull(message = "{" + Constants.VALIDATION_PROVIDER_APP_DEVELOPMENT_ID + "}")
    private Long appDevelopmentId;

    /** Name of the provider company. */
    @NotBlank(message = "{" + Constants.VALIDATION_PROVIDER_COMPANY_NAME_REQUIRED + "}")
    @Size(max = 255, message = "{" + Constants.VALIDATION_PROVIDER_COMPANY_NAME_OVERFLOW + "}")
    private String companyName;

    /** Identifier of the provider's role catalog entry; optional — {@code null} maps to no role, see {@code AppProviderMapper#nullifyRoleWhenIdMissing}. */
    private Long roleId;

    /** Start date for the provider service contract. */
    private LocalDateTime startDate;

    /** End date for the provider service contract. */
    private LocalDateTime expireDate;
}
