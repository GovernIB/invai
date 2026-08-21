package es.caib.invai.back.persistence.model.maintenance.development.technology;

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
import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.maintenance.development.layer.LayerEntity;

/**
 * JPA persistent entity representing catalog technologies (e.g. React, Spring) organized
 * under an architecture layer.
 * Implements historical auditing field inheritances by extending {@link BaseEntity}.
 *
 * @since 1.0.2
 */
@Entity
@Table(name = "INV_TECHNOLOGY")
@Getter
@Setter
public class TechnologyEntity extends BaseEntity {

    /** Unique identification pointer of the technology. */
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_technology_seq")
    @SequenceGenerator(
            name = "inv_technology_seq",
            sequenceName = "INV_TECHNOLOGY_SEQ",
            allocationSize = 1
    )
    private Long id;

    /** Name of the technology. */
    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    /** Architecture layer this technology belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LAYER", nullable = false)
    private LayerEntity layer;
}
