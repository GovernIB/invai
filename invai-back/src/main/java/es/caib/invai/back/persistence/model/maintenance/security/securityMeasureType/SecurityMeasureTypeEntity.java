package es.caib.invai.back.persistence.model.maintenance.security.securityMeasureType;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;

import java.io.Serial;

/**
 * JPA persistent entity representing security measure type entries assignable within the Manteniments / Seguretat catalog
 * (e.g. organizational, technical, procedural measures).
 * Implements historical auditing field inheritances by extending {@link BaseEntity}.
 *
 * @since 1.0.4
 */
@Entity
@Table(name = "INV_SECURITY_MEASURE_TYPE")
@Getter
@Setter
public class SecurityMeasureTypeEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique auto-generated primary key of the security measure type entry. */
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_security_measure_type_seq")
    @SequenceGenerator(
            name = "inv_security_measure_type_seq",
            sequenceName = "INV_SECURITY_MEASURE_TYPE_SEQ",
            allocationSize = 1
    )
    private Long id;

    /** Security measure type descriptive label string (e.g. "Organitzativa"). */
    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    /** Spanish localized descriptive name of the security measure type. */
    @Column(name = "NAME_ES", length = 100)
    private String nameEs;
}
