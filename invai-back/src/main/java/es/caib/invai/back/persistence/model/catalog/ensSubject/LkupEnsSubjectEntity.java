package es.caib.invai.back.persistence.model.catalog.ensSubject;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

/**
 * JPA persistent entity representing ENS subjection lookup constants assignable within the
 * Manteniments / Seguretat catalog. Acts as a static reference dictionary and is not exposed
 * through its own CRUD endpoints.
 *
 * @since 1.0.4
 */
@Entity
@Table(name = "INV_LKUP_ENS_SUBJECT")
@Getter
@Setter
public class LkupEnsSubjectEntity {

    /** Primary key of the ENS subjection lookup row. */
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_LKUP_ENS_SUBJECT_SEQ")
    @SequenceGenerator(
            name = "INV_LKUP_ENS_SUBJECT_SEQ",
            sequenceName = "INV_LKUP_ENS_SUBJECT_SEQ",
            allocationSize = 1
    )
    private Long id;

    /** Display label of the ENS subjection (typically Catalan). */
    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    /** Display label of the ENS subjection in Spanish. */
    @Column(name = "NAME_ES", nullable = false, length = 100)
    private String nameEs;

}
