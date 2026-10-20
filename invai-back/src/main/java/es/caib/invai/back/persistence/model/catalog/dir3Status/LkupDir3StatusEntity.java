package es.caib.invai.back.persistence.model.catalog.dir3Status;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

/**
 * JPA persistent entity representing the fixed DIR3 validation state dictionary (VALIDATED,
 * NOT_VALIDATED, MANUAL, NOT_APPLY). Acts as a static data reference lookup dictionary, mirroring
 * {@code LkupStatusEntity}.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@Entity
@Table(name = "INV_LKUP_DIR3_STATUS")
public class LkupDir3StatusEntity {

    /** Primary key of the DIR3 status lookup row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_LKUP_DIR3_STATUS_SEQ")
    @SequenceGenerator(name = "INV_LKUP_DIR3_STATUS_SEQ", sequenceName = "INV_LKUP_DIR3_STATUS_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Fixed technical code for the state (e.g. {@code "VALIDATED"}). */
    @Column(name = "CODE", nullable = false)
    private String code;

    /** Display label of the state (typically Catalan). */
    @Column(name = "NAME", nullable = false)
    private String name;

    /** Display label of the state in Spanish. */
    @Column(name = "NAME_ES", nullable = false)
    private String nameEs;
}
