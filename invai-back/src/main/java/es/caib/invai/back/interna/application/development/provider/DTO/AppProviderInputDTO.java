package es.caib.invai.back.interna.application.development.provider.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Inbound validation data transport contract containing properties required for
 * instantiation and mutation of a provider assignment linked to a development module.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppProviderInputDTO {

    /** Foreign key unique identification pointer referencing the parent development module entry. */
    @NotNull(message = "{validation.provider.appDevelopmentId}")
    private Long appDevelopmentId;

    /** Corporate name of the provider company. */
    @NotBlank(message = "{validation.provider.companyName.required}")
    @Size(max = 255, message = "{validation.provider.companyName.overflow}")
    private String companyName;

    /** Foreign key unique identification pointer referencing the provider's role catalog entry. */
    private Long roleId;

    /** Start date for the provider service contract. */
    private LocalDateTime startDate;

    /** End date for the provider service contract. */
    private LocalDateTime expireDate;
}
