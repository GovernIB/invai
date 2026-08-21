package es.caib.invai.back.persistence.model.maintenance.development.layer;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;

/**
 * JPA persistent entity representing architecture layers assignable to technologies and
 * application development technology stack entries.
 * Implements historical auditing field inheritances by extending {@link BaseEntity}.
 *
 * @since 1.0.2
 */
@Entity
@Table(name = "INV_LAYER")
@Getter
@Setter
public class LayerEntity extends BaseEntity {

    /** Primary key unique identifier. */
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_layer_seq")
    @SequenceGenerator(
            name = "inv_layer_seq",
            sequenceName = "INV_LAYER_SEQ",
            allocationSize = 1
    )
    private Long id;

    /** Layer name. */
    @Column(name = "NAME", nullable = false, length = 100)
    private String name;
}
