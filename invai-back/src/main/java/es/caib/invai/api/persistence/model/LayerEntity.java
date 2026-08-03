package es.caib.invai.api.persistence.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

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

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_layer_seq")
    @SequenceGenerator(
            name = "inv_layer_seq",
            sequenceName = "INV_LAYER_SEQ",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;
}
