package es.caib.invai.back.persistence.model.maintenance.systems.system;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_SYSTEM} database table.
 * Retains flat snapshots of physical fields alongside specialized execution context audit trails.
 *
 * @since 1.0.1
 */
@Getter
@Setter
@Entity
@Table(name = "INV_SYSTEM_AUD")
public class SystemAudEntity {

    /** Primary key of this audit snapshot row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_sys_aud_seq")
    @SequenceGenerator(
            name = "inv_sys_aud_seq",
            sequenceName = "INV_SYSTEM_AUD_SEQ",
            allocationSize = 1
    )
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    /** Identifier of the original {@code INV_SYSTEM} record this snapshot refers to. */
    @Column(name = "SYSTEM_ID", nullable = false)
    private Long systemId;

    /** Snapshot of the host server identifier at the time of the audited change. */
    @Column(name = "SERVER_ID", nullable = false)
    private Long serverId;

    /** Snapshot of the system instance name at the time of the audited change. */
    @Column(name = "INSTANCE", length = 255, nullable = false)
    private String instance;

    /** Snapshot of the operational connection port number at the time of the audited change. */
    @Column(name = "PORT")
    private Integer port;

    /** Snapshot of the release version tag at the time of the audited change. */
    @Column(name = "VERSION", length = 255)
    private String version;

    /** Snapshot of the system description at the time of the audited change. */
    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    /** Timestamp at which the original record was created. */
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    /** Username of the user who created the original record. */
    @Column(name = "CREATED_BY", length = 64)
    private String createdBy;

    /** Timestamp of the last update to the original record. */
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /** Username of the user who last updated the original record. */
    @Column(name = "UPDATED_BY", length = 64)
    private String updatedBy;

    /** Timestamp at which the original record was logically deleted, or {@code null} if still active. */
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    /** Username of the user who logically deleted the original record. */
    @Column(name = "DELETED_BY", length = 64)
    private String deletedBy;

    /** Type of audited action that produced this snapshot (e.g. insert, update, delete). */
    @Column(name = "AUD_ACTION", length = 10, nullable = false)
    private String audAction;

    /** Timestamp at which this audit snapshot was recorded. */
    @Column(name = "AUDIT_DATE", nullable = false)
    private LocalDateTime auditDate;

    /** Username of the user who triggered the audited action. */
    @Column(name = "AUDIT_USER", length = 64)
    private String auditUser;
}