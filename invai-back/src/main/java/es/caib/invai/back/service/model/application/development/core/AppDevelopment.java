package es.caib.invai.back.service.model.application.development.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.maintenance.systems.environment.Environment;
import es.caib.invai.back.service.model.catalog.modality.Modality;
import es.caib.invai.back.service.model.catalog.standardAdaption.StandardAdaption;

/**
 * Domain aggregate model representing the main software development module detail
 * for a corporate {@link Application} within a specific deployment {@link Environment}.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppDevelopment {
    /** Primary key of this development record. */
    private Long id;
    /** Parent corporate application this development module detail belongs to. */
    private Application application;
    /** Deployment environment zone targeted by this development. */
    private Environment environment;
    /** Development modality lookup entry. */
    private Modality modality;
    /** Source code repository URL. */
    private String code;
    /** GOIB standards compliance lookup entry. */
    private StandardAdaption standardAdaption;
    /** Date of the latest standards revision. */
    private LocalDateTime revisionDate;
    /** General rich-text observation regarding the development module. */
    private String observation;
    /** Timestamp when the record was created. */
    private LocalDateTime createdAt;
    /** Username of the user who created the record. */
    private String createdBy;
    /** Timestamp of the last update to the record. */
    private LocalDateTime updatedAt;
    /** Username of the user who last updated the record. */
    private String updatedBy;
    /** Timestamp when the record was logically deleted, or null if still active. */
    private LocalDateTime deletedAt;
    /** Username of the user who logically deleted the record. */
    private String deletedBy;
}
