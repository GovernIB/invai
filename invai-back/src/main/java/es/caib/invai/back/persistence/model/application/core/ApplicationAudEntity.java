package es.caib.invai.back.persistence.model.application.core;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_APPLICATION} database table.
 * Retains flat snapshots of structural relationships alongside specialized execution
 * context audit trails.
 *
 * @since 1.0.1
 */
@Getter
@Setter
@Entity
@Table(name = "INV_APPLICATION_AUD")
public class ApplicationAudEntity {

    /** Primary sequence identifier of this audit snapshot row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_app_aud_seq")
    @SequenceGenerator(
            name = "inv_app_aud_seq",
            sequenceName = "INV_APPLICATION_AUD_SEQ",
            allocationSize = 1
    )
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    /** Identifier of the original application record this audit snapshot refers to. */
    @Column(name = "APP_APPLICATION_ID", nullable = false)
    private Long appApplicationId;

    /** Snapshot of the application's unique code at the time of the audited change. */
    @Column(name = "CODE", length = 10, nullable = false)
    private String code;

    /** Snapshot of the application's prefix at the time of the audited change. */
    @Column(name = "PREFIX", length = 3, nullable = false)
    private String prefix;

    /** Snapshot of the application's name at the time of the audited change. */
    @Column(name = "NAME")
    private String name;

    /** Snapshot of the application's category foreign key at the time of the audited change. */
    @Column(name = "CATEGORY_ID")
    private Long categoryId;

    /** Snapshot of the application's system type foreign key at the time of the audited change. */
    @Column(name = "SYSTEM_TYPE_ID")
    private Long systemTypeId;

    /** Snapshot of the application's field foreign key at the time of the audited change. */
    @Column(name = "FIELD_ID")
    private Long fieldId;

    /** Snapshot of the application's administrative unit DIR3CAIB code at the time of the audited change. */
    @Column(name = "ADM_UNIT_CODE", length = 20)
    private String admUnitCode;

    /** Snapshot of the application's commission foreign key at the time of the audited change. */
    @Column(name = "COMMISSION_ID")
    private Long commissionId;

    /** Snapshot of the application's status foreign key at the time of the audited change. */
    @Column(name = "STATUS_ID")
    private Long statusId;

    /** Snapshot of the application's description at the time of the audited change. */
    @Lob
    @Column(name = "DESCRIPTION")
    private String description;

    /** Snapshot of the application's creation timestamp at the time of the audited change. */
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    /** Snapshot of the username that created the application. */
    @Column(name = "CREATED_BY", length = 64)
    private String createdBy;

    /** Snapshot of the application's last update timestamp at the time of the audited change. */
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /** Snapshot of the username that last updated the application. */
    @Column(name = "UPDATED_BY", length = 64)
    private String updatedBy;

    /** Snapshot of the application's soft-deletion timestamp at the time of the audited change. */
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    /** Snapshot of the username that soft-deleted the application. */
    @Column(name = "DELETED_BY", length = 64)
    private String deletedBy;

    /** Snapshot of the application's expiration date at the time of the audited change. */
    @Column(name = "EXPIRATION_DATE")
    private LocalDateTime expirationDate;

    /** Type of mutation that triggered this audit record (e.g., INSERT, UPDATE, DELETE). */
    @Column(name = "AUD_ACTION", length = 10, nullable = false)
    private String audAction;

    /** Timestamp at which this audit record was generated. */
    @Column(name = "AUDIT_DATE", nullable = false)
    private LocalDateTime auditDate;

    /** Username of the actor responsible for the audited mutation. */
    @Column(name = "AUDIT_USER", length = 64)
    private String auditUser;
}