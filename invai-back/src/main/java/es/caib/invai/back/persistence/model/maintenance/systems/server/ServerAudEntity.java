package es.caib.invai.back.persistence.model.maintenance.systems.server;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_SERVER} database table.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Entity
@Table(name = "INV_SERVER_AUD")
public class ServerAudEntity {

    /** Primary key of this audit snapshot row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_server_aud_seq")
    @SequenceGenerator(
            name = "inv_server_aud_seq",
            sequenceName = "INV_SERVER_AUD_SEQ",
            allocationSize = 1
    )
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    /** Identifier of the original {@code INV_SERVER} record this snapshot refers to. */
    @Column(name = "SERVER_ID", nullable = false)
    private Long serverId;

    /** Snapshot of the server's name at the time of the audited change. */
    @Column(name = "NAME", length = 255, nullable = false)
    private String name;

    /** Snapshot of the environment identifier at the time of the audited change. */
    @Column(name = "ENVIRONMENT_ID", nullable = false)
    private Long environmentId;

    /** Snapshot of the server type identifier at the time of the audited change. */
    @Column(name = "SERVER_TYPE_ID", nullable = false)
    private Long serverTypeId;

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
