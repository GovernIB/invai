package es.caib.invai.back.persistence.model.maintenance.classificationSegment;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;

import java.io.Serial;

/**
 * JPA persistent entity representing classification segment entries assignable within the Manteniments / Seguretat catalog
 * (e.g. organizational, technical, procedural measures).
 * Implements historical auditing field inheritances by extending {@link BaseEntity}.
 *
 * @since 1.0.4
 */
@Entity
@Table(name = "INV_CLASSIFICATION_SEGMENT")
@Getter
@Setter
public class ClassificationSegmentEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique auto-generated primary key of the classification segment entry. */
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_classification_segment_seq")
    @SequenceGenerator(
            name = "inv_classification_segment_seq",
            sequenceName = "INV_CLASSIFICATION_SEGMENT_SEQ",
            allocationSize = 1
    )
    private Long id;

    /** Classification segment descriptive label string (e.g. "Segment I"). */
    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    /** Spanish localized descriptive name of the classification segment. */
    @Column(name = "NAME_ES", length = 100)
    private String nameEs;
}
