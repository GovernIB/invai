package es.caib.invai.back.persistence.model.maintenance.general.commission;

import es.caib.invai.back.service.model.maintenance.general.commission.CommissionType;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDate;
import es.caib.invai.back.persistence.model.BaseEntity;

/**
 * JPA persistent entity representing working groups or corporate technical governance Commissions.
 * <p>
 * Map-aligned onto the physical schema 'INV_COMMISSION'. Inherits fundamental transactional audit
 * attributes and automatic database timeline tracking behaviors from {@link BaseEntity}.
 * </p>
 *
 * @author invai-team
 * @since 1.0.1
 */
@Getter
@Setter
@Entity
@Table(name = "INV_COMMISSION")
public class CommissionEntity extends BaseEntity {

    /** The primary surrogate auto-incremented storage index generated through sequence tracking. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_commission_seq")
    @SequenceGenerator(name = "inv_commission_seq", sequenceName = "INV_COMMISSION_SEQ", allocationSize = 1)
    @Column(name = "COMMISSION_ID")
    private Long id;

    /** Core departmental description label sequence. Mapped to a non-nullable column of size 100. */
    @Column(name = "NAME", length = 100, nullable = false)
    private String name;

    /** Translated core description label sequence matching Spanish regional settings. Mapped to a non-nullable column of size 100. */
    @Column(name = "NAME_ES", length = 100, nullable = false)
    private String nameEs;

    /** Unique tracking code assigned to the physical dossier. Mapped to a non-nullable column of size 50. */
    @Column(name = "EXPEDIENT_NUMBER", length = 50, nullable = false)
    private String expedientNumber;

    /** Chronological signature recording the official panel deployment verification. Mapped to a non-nullable column. */
    @Column(name = "APPROVAL_DATE", nullable = false)
    private LocalDate approvalDate;

    /**
     * Persisted text representation of the internal commission type enumeration.
     * Restricts inputs to 'TECNICA' or 'SUPERIOR' matching the physical table check constraints.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "COMMISSION_TYPE", length = 20, nullable = false)
    private CommissionType commissionType;
}