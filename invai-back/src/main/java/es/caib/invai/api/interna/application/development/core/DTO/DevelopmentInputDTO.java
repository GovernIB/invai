package es.caib.invai.api.interna.application.development.core.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Inbound validation data transport contract containing properties required for
 * instantiation and mutation of the main application development module detail.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DevelopmentInputDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Optional foreign key referencing the parent corporate Application profile; assigned only when present. */
    private Long applicationId;

    /** Foreign key unique identification pointer referencing the target deployment Environment zone. */
    @NotNull(message = "{validation.development.environmentId}")
    private Long environmentId;

    /** Foreign key reference pointing to the development modality lookup entry. */
    @NotNull(message = "{validation.development.modalityId}")
    private Long modalityId;

    /** Source code repository URL. */
    @NotBlank(message = "{validation.development.code.required}")
    @Size(max = 1000, message = "{validation.development.code.overflow}")
    private String code;

    /** Foreign key reference pointing to the GOIB standards compliance lookup entry. */
    @NotNull(message = "{validation.development.standardAdaptionId}")
    private Long standardAdaptionId;

    /** Date of the latest standards revision. */
    @NotNull(message = "{validation.development.revisionDate}")
    private LocalDateTime revisionDate;

    /** General rich-text observation regarding the development module. */
    @NotBlank(message = "{validation.development.observation.required}")
    private String observation;
}
