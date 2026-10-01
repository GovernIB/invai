package es.caib.invai.back.persistence.model.maintenance.integration.externalSystem;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.maintenance.responsible.company.CompanyEntity;

import java.io.Serial;

/**
 * JPA persistent entity representing an external system (outside the inventory) that an
 * application can integrate with, assignable within the "Integració" tab's "Sistema" column.
 *
 * @since 1.0.5
 */
@Entity
@Table(name = "INV_EXTERNAL_SYSTEM")
@Getter
@Setter
public class ExternalSystemEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique auto-generated database identifier of the external system. */
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_external_system_seq")
    @SequenceGenerator(
            name = "inv_external_system_seq",
            sequenceName = "INV_EXTERNAL_SYSTEM_SEQ",
            allocationSize = 1
    )
    private Long id;

    /** External system name. */
    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    /** Company responsible for the external system. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPANY_ID", nullable = false)
    private CompanyEntity company;
}
