package es.caib.invai.back.persistence.model.catalog.securityLevel;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

/**
 * JPA persistent entity representing security level lookup constants assignable within the
 * Manteniments / Seguretat catalog. Acts as a static reference dictionary and is not exposed
 * through its own CRUD endpoints.
 *
 * @since 1.0.4
 */
@Entity
@Table(name = "INV_LKUP_SECURITY_LEVEL")
@Getter
@Setter
public class LkupSecurityLevelEntity {

    /** Primary key of the security level lookup row. */
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_LKUP_SECURITY_LEVEL_SEQ")
    @SequenceGenerator(
            name = "INV_LKUP_SECURITY_LEVEL_SEQ",
            sequenceName = "INV_LKUP_SECURITY_LEVEL_SEQ",
            allocationSize = 1
    )
    private Long id;

    /** Display label of the security level (typically Catalan). */
    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    /** Display label of the security level in Spanish. */
    @Column(name = "NAME_ES", nullable = false, length = 100)
    private String nameEs;

}
