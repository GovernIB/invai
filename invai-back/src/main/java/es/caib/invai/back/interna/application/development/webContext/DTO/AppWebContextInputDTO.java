package es.caib.invai.back.interna.application.development.webContext.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Inbound validation data transport contract containing properties required for creating or
 * updating an application web context assignment. Exclusively used from the Development side:
 * the parent security anchor is resolved automatically from the given development's application,
 * never accepted from the client (see {@code AppWebContextServiceFacadeBean.create} under
 * {@code ejb.application.development.webContext}).
 *
 * @since 1.0.5
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppWebContextInputDTO {

    /** Foreign key unique identification pointer referencing the parent development anchor. */
    @NotNull(message = "{validation.appwebcontext.appDevelopmentId}")
    private Long appDevelopmentId;

    /** Foreign key unique identification pointer referencing the target web context. */
    @NotNull(message = "{validation.appwebcontext.webContextId}")
    private Long webContextId;

    /** Foreign key unique identification pointer referencing the target functional field. */
    @NotNull(message = "{validation.appwebcontext.fieldId}")
    private Long fieldId;

    /** Free-text observation notes attached to this association. */
    private String observation;

    /** URL of the web context. Optional, but must be a well-formed http(s) URL when given. */
    @Size(max = 255, message = "{validation.appwebcontext.url.size}")
    @Pattern(regexp = "^https?://.+", message = "{validation.appwebcontext.url.format}")
    private String url;
}
