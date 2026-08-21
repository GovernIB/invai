package es.caib.invai.back.persistence.model.catalog.modality;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * JPA persistent entity representing development modality lookup constants (e.g. internal,
 * external, or mixed development). Acts as a static reference dictionary and is not exposed
 * through its own CRUD endpoints.
 *
 * @since 1.0.2
 */
@Entity
@Table(name = "INV_LKUP_MODALITY")
@Getter
@Setter
public class LkupModalityEntity {

    /** Primary key of the modality lookup row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_LKUP_MODALITY_SEQ")
    @SequenceGenerator(name = "INV_LKUP_MODALITY_SEQ", sequenceName = "INV_LKUP_MODALITY_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Display label of the modality (typically Catalan). */
    @Column(name = "NAME", nullable = false)
    private String name;

    /** Display label of the modality in Spanish. */
    @Column(name = "NAME_ES", nullable = false)
    private String nameEs;
}
