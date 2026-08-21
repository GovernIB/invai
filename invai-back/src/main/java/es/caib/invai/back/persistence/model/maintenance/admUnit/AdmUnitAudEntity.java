package es.caib.invai.back.persistence.model.maintenance.admUnit;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_ADM_UNIT} database table.
 * Unlike standard operational entities, this tracking row encapsulates metadata regarding
 * the exact database operations and execution contexts triggering the modification.
 *
 * @since 1.0.1
 */
@Getter
@Setter
@Entity
@Table(name = "INV_ADM_UNIT_AUD")
public class AdmUnitAudEntity {

    /** Persistent database primary key identifier of this audit record, generated from the {@code INV_ADM_UNIT_AUD_SEQ} sequence. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_adm_unit_aud_seq")
    @SequenceGenerator(name = "inv_adm_unit_aud_seq", sequenceName = "INV_ADM_UNIT_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID")
    private Long audId;

    /** Identifier of the operational administrative unit record this audit snapshot refers to. */
    @Column(name = "ADM_UNIT_ID")
    private Long admUnitId;

    /** Snapshot of the administrative unit's alphanumeric code at the time of the tracked operation. */
    @Column(name = "CODE")
    private String code;

    /** Snapshot of the administrative unit's Catalan name at the time of the tracked operation. */
    @Column(name = "NAME")
    private String name;

    /** Snapshot of the administrative unit's Spanish name at the time of the tracked operation. */
    @Column(name = "NAME_ES", length = 100)
    private String nameEs;

    /** Snapshot of the creation timestamp of the operational record. */
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    /** Snapshot of the username who created the operational record. */
    @Column(name = "CREATED_BY")
    private String createdBy;

    /** Snapshot of the last update timestamp of the operational record. */
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /** Snapshot of the username who last updated the operational record. */
    @Column(name = "UPDATED_BY")
    private String updatedBy;

    /** Snapshot of the logical soft-deletion timestamp of the operational record, if any. */
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    /** Snapshot of the username who logically soft-deleted the operational record, if any. */
    @Column(name = "DELETED_BY")
    private String deletedBy;

    /** Type of database mutation that triggered this audit entry (e.g., INSERT, UPDATE, DELETE). */
    @Column(name = "AUD_ACTION")
    private String audAction;

    /** Timestamp at which this audit entry was recorded. */
    @Column(name = "AUDIT_DATE")
    private LocalDateTime auditDate;

    /** Username of the user whose action triggered this audit entry. */
    @Column(name = "AUDIT_USER")
    private String auditUser;
}