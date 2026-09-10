package es.caib.invai.back.interna.application.development.core.DTO;

import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import es.caib.invai.back.interna.maintenance.systems.environment.DTO.EnvironmentOutputDTO;
import es.caib.invai.back.service.model.catalog.modality.Modality;
import es.caib.invai.back.service.model.catalog.standardAdaption.StandardAdaption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Read-side counterpart of {@link DevelopmentInputDTO} for the "AppDevelopment" tab record: the
 * same fields, but with the {@code applicationId}, {@code environmentId}, {@code modalityId} and
 * {@code standardAdaptionId} foreign keys resolved to their full nested DTOs/lookup objects for
 * display, plus {@code id} and {@code deletedAt}.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevelopmentOutputDTO {

    /** Primary key of this development record. */
    private Long id;
    /** The parent application this development record belongs to. */
    private ApplicationOutputDTO application;
    /** Resolved target deployment environment snapshot. */
    private EnvironmentOutputDTO environment;
    /** Resolved development modality lookup entry. */
    private Modality modality;
    /** Source code repository URL. */
    private String code;
    /** Resolved GOIB standards compliance lookup entry. */
    private StandardAdaption standardAdaption;
    /** Date of the latest standards revision. */
    private LocalDateTime revisionDate;
    /** General rich-text observation regarding the development module. */
    private String observation;
    /** Timestamp when the record was logically deleted, or null if still active. */
    private LocalDateTime deletedAt;
}
