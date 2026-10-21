package es.caib.invai.back.persistence.model.maintenance.documentation.documentCategory;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;

import java.io.Serial;

/**
 * JPA persistent entity representing a documentation category or subcategory. Both live in the
 * same table: a category has no parent, a subcategory points to the category it belongs to.
 *
 * @since 1.0.6
 */
@Entity
@Table(name = "INV_DOCUMENT_CATEGORY")
@Getter
@Setter
public class DocumentCategoryEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique auto-generated database identifier of the category or subcategory. */
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_document_category_seq")
    @SequenceGenerator(
            name = "inv_document_category_seq",
            sequenceName = "INV_DOCUMENT_CATEGORY_SEQ",
            allocationSize = 1
    )
    private Long id;

    /** Name of the category or subcategory (Catalan). */
    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    /** Name of the category or subcategory in Spanish. */
    @Column(name = "NAME_ES", nullable = false, length = 150)
    private String nameEs;

    /** Category this record belongs to, or {@code null} if this record is itself a category. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    private DocumentCategoryEntity parent;
}
