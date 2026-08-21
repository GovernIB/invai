package es.caib.invai.back.persistence.model.maintenance.general.systemType;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_SYSTEM_TYPE} database table.
 * Records structural variations to catalog configurations for compliance tracking.
 *
 * @since 1.0.1
 */
@Entity
@Table(name = "INV_SYSTEM_TYPE_AUD")
@Getter
@Setter
public class SystemTypeAudEntity {

    /** Unique auto-generated primary key identifying this audit snapshot row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_system_type_aud_seq")
    @SequenceGenerator(name = "inv_system_type_aud_seq", sequenceName = "INV_SYSTEM_TYPE_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID")
    private Long id;

    /** Identifier of the original system type record this snapshot was taken from. */
    @Column(name = "SYSTEM_TYPE_ID", nullable = false)
    private Long systemTypeId;

    /** Snapshot of the system type's Catalan name at the time of the audited action. */
    @Column(name = "NAME", nullable = false)
    private String name;

    /** Snapshot of the system type's Spanish name at the time of the audited action. */
    @Column(name = "NAME_ES", length = 100)
    private String nameEs;

    /** Snapshot of the original record's creation timestamp. */
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    /** Snapshot of the original record's creation author. */
    @Column(name = "CREATED_BY")
    private String createdBy;

    /** Snapshot of the original record's last update timestamp. */
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /** Snapshot of the original record's last update author. */
    @Column(name = "UPDATED_BY")
    private String updatedBy;

    /** Snapshot of the original record's soft-deletion timestamp, if any. */
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    /** Snapshot of the original record's soft-deletion author, if any. */
    @Column(name = "DELETED_BY")
    private String deletedBy;

    /** Type of mutation that triggered this audit snapshot (e.g. INSERT, UPDATE, DELETE). */
    @Column(name = "AUD_ACTION", nullable = false)
    private String audAction;

    /** Timestamp at which this audit snapshot was recorded. */
    @Column(name = "AUDIT_DATE", nullable = false)
    private LocalDateTime auditDate;

    /** Username of the operator responsible for the audited mutation. */
    @Column(name = "AUDIT_USER", nullable = false)
    private String auditUser;
}