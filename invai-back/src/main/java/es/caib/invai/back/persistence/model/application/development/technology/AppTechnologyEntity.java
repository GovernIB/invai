package es.caib.invai.back.persistence.model.application.development.technology;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import es.caib.invai.back.persistence.model.application.development.core.AppDevelopmentEntity;
import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.maintenance.development.layer.LayerEntity;
import es.caib.invai.back.persistence.model.maintenance.development.technology.TechnologyEntity;

import java.io.Serial;

/**
 * Persistent entity mapping a technology stack entry (layer, technology, version, architecture)
 * assigned to a specific application development module.
 *
 * @since 1.0.2
 */
@Entity
@Table(name = "INV_APP_TECHNOLOGY")
@Getter
@Setter
public class AppTechnologyEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Primary key of this technology stack entry. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_TECHNOLOGY_SEQ")
    @SequenceGenerator(name = "INV_APP_TECHNOLOGY_SEQ", sequenceName = "INV_APP_TECHNOLOGY_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Parent development module this technology entry belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_DEVELOPMENT_ID", nullable = false)
    private AppDevelopmentEntity appDevelopment;

    /** Architecture layer catalog entry. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LAYER", nullable = false)
    private LayerEntity layer;

    /** Technology catalog entry. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TECHNOLOGY", nullable = false)
    private TechnologyEntity technology;

    /** Version release details. */
    @Column(name = "VERSION")
    private String version;

    /** Architectural model description. */
    @Column(name = "ARCHITECTURE")
    private String architecture;
}
