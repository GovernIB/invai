package es.caib.invai.back.interna.application.security.webContext.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

/**
 * Inbound validation data transport contract containing properties required for
 * instantiation and mutation of application web context assignments.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppWebContextInputDTO {

    /** Foreign key unique identification pointer referencing the parent security anchor. */
    @NotNull(message = "{validation.appwebcontext.appSecurityId}")
    private Long appSecurityId;

    /** Foreign key unique identification pointer referencing the target web context. */
    @NotNull(message = "{validation.appwebcontext.webContextId}")
    private Long webContextId;

    /** Foreign key unique identification pointer referencing the target functional field. */
    @NotNull(message = "{validation.appwebcontext.fieldId}")
    private Long fieldId;

    /** Free-text observation notes attached to this association. */
    private String observation;
}
