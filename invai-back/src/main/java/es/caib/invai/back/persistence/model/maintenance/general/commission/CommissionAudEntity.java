package es.caib.invai.back.persistence.model.maintenance.general.commission;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_COMMISSION} database table.
 * Preserves point-in-time states alongside contextual execution logs for administrative
 * audit trails.
 * <p>
 * Enhanced to track the refactored tracking parameters including administrative expedients,
 * formal decision timelines, and commission ranking configurations.
 * </p>
 *
 * @author invai-team
 * @since 1.0.1
 */
@Getter
@Setter
@Entity
@Table(name = "INV_COMMISSION_AUD")
public class CommissionAudEntity {

    /** Surrogate primary key of this audit snapshot row, generated via sequence. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_commission_aud_seq")
    @SequenceGenerator(name = "inv_commission_aud_seq", sequenceName = "INV_COMMISSION_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID")
    private Long auditId;

    /** Identifier of the live {@code INV_COMMISSION} record this snapshot corresponds to. */
    @Column(name = "COMMISSION_ID", nullable = false)
    private Long commissionId;

    /** Snapshot of the commission's descriptive name at the time of the audited change. */
    @Column(name = "NAME", length = 100)
    private String name;

    /** Snapshot of the commission's Spanish descriptive name at the time of the audited change. */
    @Column(name = "NAME_ES", length = 100)
    private String nameEs;

    /** Snapshot of the commission's expedient dossier tracking code at the time of the audited change. */
    @Column(name = "EXPEDIENT_NUMBER", length = 50)
    private String expedientNumber;

    /** Snapshot of the commission's approval date at the time of the audited change. */
    @Column(name = "APPROVAL_DATE")
    private LocalDate approvalDate;

    /** Snapshot of the commission's type, stored as text, at the time of the audited change. */
    @Column(name = "COMMISSION_TYPE", length = 20)
    private String commissionType;

    /** Timestamp marking when the underlying commission was created. */
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    /** Username (or system fallback identifier) of the actor who created the underlying commission. */
    @Column(name = "CREATED_BY", length = 64)
    private String createdBy;

    /** Timestamp marking the most recent update to the underlying commission, or {@code null} if never updated. */
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /** Username (or system fallback identifier) of the actor who last updated the underlying commission. */
    @Column(name = "UPDATED_BY", length = 64)
    private String updatedBy;

    /** Timestamp marking when the underlying commission was soft-deleted, or {@code null} if not deleted. */
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    /** Username (or system fallback identifier) of the actor who soft-deleted the underlying commission. */
    @Column(name = "DELETED_BY", length = 64)
    private String deletedBy;

    /** Type of mutation that produced this snapshot (e.g. {@code INSERT}, {@code UPDATE}, {@code DELETE}). */
    @Column(name = "AUD_ACTION", length = 10, nullable = false)
    private String audAction;

    /** Timestamp at which this audit snapshot was recorded. */
    @Column(name = "AUDIT_DATE", nullable = false)
    private LocalDateTime auditDate;

    /** Username (or system fallback identifier) of the actor who triggered the audited mutation. */
    @Column(name = "AUDIT_USER", length = 64)
    private String auditUser;
}