package es.caib.invai.api.persistence.model;

import es.caib.invai.api.persistence.model.catalog.LkupModalityEntity;
import es.caib.invai.api.persistence.model.catalog.LkupStandardAdaptionEntity;
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
import java.time.LocalDateTime;

/**
 * Persistent aggregate root entity mapping the main software development module detail
 * for a corporate application within a specific deployment environment.
 *
 * @since 1.0.2
 */
@Entity
@Table(name = "INV_APP_DEVELOPMENT")
@Getter
@Setter
public class DevelopmentEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_DEVELOPMENT_SEQ")
    @SequenceGenerator(name = "INV_APP_DEVELOPMENT_SEQ", sequenceName = "INV_APP_DEVELOPMENT_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APPLICATION_ID", nullable = false)
    private ApplicationEntity application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ENVIRONMENT_ID", nullable = false)
    private EnvironmentEntity environment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MODALITY_ID")
    private LkupModalityEntity modality;

    @Column(name = "CODE", length = 1000)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STANDARD_ADAPTION")
    private LkupStandardAdaptionEntity standardAdaption;

    @Column(name = "REVISION_DATE")
    private LocalDateTime revisionDate;

    @Lob
    @Column(name = "OBSERVATION")
    private String observation;
}
