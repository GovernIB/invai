package es.caib.invai.back.interna.application.development.core.DTO;

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
 * Inbound payload for creating or updating the "AppDevelopment" tab record (one per application).
 *
 * @since 1.0.2
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DevelopmentInputDTO {

    /** Identifier of the owning application; the facade rejects create if it already has an active development record. */
    @NotNull(message = "{" + Constants.VALIDATION_DEVELOPMENT_APPLICATION_ID + "}")
    private Long applicationId;

    /** Identifier of the target deployment environment. */
    @NotNull(message = "{" + Constants.VALIDATION_DEVELOPMENT_ENVIRONMENT_ID + "}")
    private Long environmentId;

    /** Identifier of the development modality lookup entry. */
    @NotNull(message = "{" + Constants.VALIDATION_DEVELOPMENT_MODALITY_ID + "}")
    private Long modalityId;

    /** Source code repository URL. */
    @NotBlank(message = "{" + Constants.VALIDATION_DEVELOPMENT_CODE_REQUIRED + "}")
    @Size(max = 1000, message = "{" + Constants.VALIDATION_DEVELOPMENT_CODE_OVERFLOW + "}")
    private String code;

    /** Identifier of the GOIB standards compliance lookup entry. */
    @NotNull(message = "{" + Constants.VALIDATION_DEVELOPMENT_STANDARD_ADAPTION_ID + "}")
    private Long standardAdaptionId;

    /** Date of the latest standards revision. */
    @NotNull(message = "{" + Constants.VALIDATION_DEVELOPMENT_REVISION_DATE + "}")
    private LocalDateTime revisionDate;

    /** General rich-text observation regarding the development module. */
    private String observation;
}
