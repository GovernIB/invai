package es.caib.invai.back.persistence.model.maintenance.responsible.company;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;

/**
 * JPA persistent entity representing companies assignable within the Manteniments / Responsables catalog.
 * Implements historical auditing field inheritances by extending {@link BaseEntity}.
 *
 * @since 1.0.3
 */
@Entity
@Table(name = "INV_COMPANY")
@Getter
@Setter
public class CompanyEntity extends BaseEntity {

    /** Unique auto-generated database identifier of the company. */
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_company_seq")
    @SequenceGenerator(
            name = "inv_company_seq",
            sequenceName = "INV_COMPANY_SEQ",
            allocationSize = 1
    )
    private Long id;

    /** Company corporate name label string. */
    @Column(name = "NAME", nullable = false, length = 150)
    private String name;
}
