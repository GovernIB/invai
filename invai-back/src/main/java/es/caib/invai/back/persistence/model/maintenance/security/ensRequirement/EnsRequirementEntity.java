package es.caib.invai.back.persistence.model.maintenance.security.ensRequirement;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;

import java.io.Serial;

/**
 * JPA persistent entity representing ENS (Esquema Nacional de Seguridad) requirement entries
 * assignable within the Manteniments / Seguretat catalog.
 * Implements historical auditing field inheritances by extending {@link BaseEntity}.
 *
 * @since 1.0.4
 */
@Entity
@Table(name = "INV_ENS_REQUIREMENT")
@Getter
@Setter
public class EnsRequirementEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique auto-generated primary key of the ENS requirement entry. */
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_ens_requirement_seq")
    @SequenceGenerator(
            name = "inv_ens_requirement_seq",
            sequenceName = "INV_ENS_REQUIREMENT_SEQ",
            allocationSize = 1
    )
    private Long id;

    /** ENS requirement descriptive label string. */
    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    /** Spanish localized descriptive name of the ENS requirement. */
    @Column(name = "NAME_ES", length = 100)
    private String nameEs;
}
