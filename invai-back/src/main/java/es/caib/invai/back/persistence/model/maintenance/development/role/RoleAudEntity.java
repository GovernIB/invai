package es.caib.invai.back.persistence.model.maintenance.development.role;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_ROLE} database table.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Entity
@Table(name = "INV_ROLE_AUD")
public class RoleAudEntity {

    /** Primary key of this audit snapshot row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_role_aud_seq")
    @SequenceGenerator(name = "inv_role_aud_seq", sequenceName = "INV_ROLE_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID")
    private Long audId;

    /** Identifier of the original {@code INV_ROLE} record this snapshot refers to. */
    @Column(name = "ROLE_ID")
    private Long roleId;

    /** Snapshot of the role's Catalan name at the time of the audited change. */
    @Column(name = "NAME")
    private String name;

    /** Snapshot of the role's Spanish name at the time of the audited change. */
    @Column(name = "NAME_ES", length = 100)
    private String nameEs;

    /** Timestamp at which the original record was created. */
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    /** Username of the user who created the original record. */
    @Column(name = "CREATED_BY")
    private String createdBy;

    /** Timestamp of the last update to the original record. */
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /** Username of the user who last updated the original record. */
    @Column(name = "UPDATED_BY")
    private String updatedBy;

    /** Timestamp at which the original record was logically deleted, or {@code null} if still active. */
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    /** Username of the user who logically deleted the original record. */
    @Column(name = "DELETED_BY")
    private String deletedBy;

    /** Type of audited action that produced this snapshot (e.g. insert, update, delete). */
    @Column(name = "AUD_ACTION")
    private String audAction;

    /** Timestamp at which this audit snapshot was recorded. */
    @Column(name = "AUDIT_DATE")
    private LocalDateTime auditDate;

    /** Username of the user who triggered the audited action. */
    @Column(name = "AUDIT_USER")
    private String auditUser;
}
