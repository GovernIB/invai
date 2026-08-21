package es.caib.invai.back.persistence.model.catalog.responsibleType;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

/**
 * JPA persistent entity representing responsible type lookup constants assignable within the
 * Manteniments / Responsables catalog. Acts as a static reference dictionary and is not exposed
 * through its own CRUD endpoints.
 *
 * @since 1.0.3
 */
@Entity
@Table(name = "INV_LKUP_RESPONSIBLE_TYPE")
@Getter
@Setter
public class LkupResponsibleTypeEntity {

    /** Primary key of the responsible type lookup row. */
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_LKUP_RESPONSIBLE_TYPE_SEQ")
    @SequenceGenerator(
            name = "INV_LKUP_RESPONSIBLE_TYPE_SEQ",
            sequenceName = "INV_LKUP_RESPONSIBLE_TYPE_SEQ",
            allocationSize = 1
    )
    private Long id;

    /** Display label of the responsible type (typically Catalan). */
    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    /** Display label of the responsible type in Spanish. */
    @Column(name = "NAME_ES", nullable = false, length = 100)
    private String nameEs;

    /** Whether this responsible type may only be held by Personal CAIB persons. */
    @Column(name = "REQUIRES_PERSONAL_CAIB", nullable = false)
    private boolean requiresPersonalCaib;
}
