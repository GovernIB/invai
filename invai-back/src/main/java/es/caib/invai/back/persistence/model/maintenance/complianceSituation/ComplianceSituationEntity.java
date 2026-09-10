package es.caib.invai.back.persistence.model.maintenance.complianceSituation;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;

import java.io.Serial;

/**
 * JPA persistent entity representing compliance situation entries assignable within the Manteniments / Seguretat catalog
 * (e.g. organizational, technical, procedural measures).
 * Implements historical auditing field inheritances by extending {@link BaseEntity}.
 *
 * @since 1.0.4
 */
@Entity
@Table(name = "INV_COMPLIANCE_SITUATION")
@Getter
@Setter
public class ComplianceSituationEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique auto-generated primary key of the compliance situation entry. */
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_compliance_situation_seq")
    @SequenceGenerator(
            name = "inv_compliance_situation_seq",
            sequenceName = "INV_COMPLIANCE_SITUATION_SEQ",
            allocationSize = 1
    )
    private Long id;

    /** Compliance situation descriptive label string (e.g. "Conforme"). */
    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    /** Spanish localized descriptive name of the compliance situation. */
    @Column(name = "NAME_ES", length = 100)
    private String nameEs;
}
