package es.caib.invai.back.persistence.model.application.development.core;

import es.caib.invai.back.persistence.model.catalog.modality.LkupModalityEntity;
import es.caib.invai.back.persistence.model.catalog.standardAdaption.LkupStandardAdaptionEntity;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.io.Serial;
import java.time.LocalDateTime;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.environment.EnvironmentEntity;

/**
 * JPA entity backing the "AppDevelopment" tab: one row per {@link ApplicationEntity}, a
 * relationship enforced at the facade level (see
 * {@link es.caib.invai.back.ejb.application.development.core.AppDevelopmentServiceFacadeBean#create})
 * rather than by a database constraint. Extends {@link BaseEntity} for the created/updated
 * timestamps and the soft-delete ({@code deletedAt}/{@code deletedBy}) columns; every insert,
 * update and soft-delete performed through {@code AppDevelopmentRepositoryAdapter} additionally
 * writes a snapshot row to {@link AppDevelopmentAudEntity}.
 *
 * @since 1.0.2
 */
@Entity
@Table(name = "INV_APP_DEVELOPMENT")
@Getter
@Setter
public class AppDevelopmentEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Primary key of this development record. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_DEVELOPMENT_SEQ")
    @SequenceGenerator(name = "INV_APP_DEVELOPMENT_SEQ", sequenceName = "INV_APP_DEVELOPMENT_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** The application this development record belongs to (at most one per application). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APPLICATION_ID", nullable = false)
    private ApplicationEntity application;

    /** Deployment environment zone targeted by this development. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ENVIRONMENT_ID", nullable = false)
    private EnvironmentEntity environment;

    /** Development modality lookup entry. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MODALITY_ID")
    private LkupModalityEntity modality;

    /** Source code repository URL. */
    @Column(name = "CODE", length = 1000)
    private String code;

    /** GOIB standards compliance lookup entry. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STANDARD_ADAPTION")
    private LkupStandardAdaptionEntity standardAdaption;

    /** Date of the latest standards revision. */
    @Column(name = "REVISION_DATE")
    private LocalDateTime revisionDate;

    /** General rich-text observation regarding the development module. */
    @Lob
    @Column(name = "OBSERVATION")
    private String observation;
}
