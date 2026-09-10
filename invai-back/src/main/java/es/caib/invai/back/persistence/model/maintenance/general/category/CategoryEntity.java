package es.caib.invai.back.persistence.model.maintenance.general.category;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;

import java.io.Serial;

/**
 * JPA persistent entity representing classification taxonomy Categories for registered corporate software profiles.
 * Implements historical auditing field inheritances by extending {@link BaseEntity}.
 *
 * @since 1.0.1
 */
@Entity
@Table(name = "INV_CATEGORY")
@Getter
@Setter
public class CategoryEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "CATEGORY_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_category_seq")
    @SequenceGenerator(
            name = "inv_category_seq",
            sequenceName = "INV_CATEGORY_SEQ",
            allocationSize = 1
    )
    /** The unique database auto-generated primary key of the category. */
    private Long id;

    /** The category's descriptive name in Catalan; required and limited to 50 characters. */
    @Column(name = "NAME", nullable = false, length = 50)
    private String name;

    /** The category's descriptive name in Spanish; limited to 100 characters. */
    @Column(name = "NAME_ES", length = 100)
    private String nameEs;
}