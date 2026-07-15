package es.caib.invai.api.persistence.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;

/**
 * JPA persistent entity representing classification taxonomy Categories for registered corporate software profiles.
 * Implements historical auditing field inheritances by extending {@link BaseEntity}.
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "INV_CATEGORY")
@Getter
@Setter
public class CategoryEntity extends BaseEntity {

    @Id
    @Column(name = "CATEGORY_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_category_seq")
    @SequenceGenerator(
            name = "inv_category_seq",
            sequenceName = "INV_CATEGORY_SEQ",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "NAME", nullable = false, length = 50)
    private String name;

    @Column(name = "NAME_ES", length = 100)
    private String nameEs;
}