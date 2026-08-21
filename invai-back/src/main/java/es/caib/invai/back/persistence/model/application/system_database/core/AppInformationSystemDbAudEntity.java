package es.caib.invai.back.persistence.model.application.system_database.core;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Historical audit trail entity recording every lifecycle mutation applied
 * to {@link AppInformationSystemDbEntity} records.
 *
 * @since 1.0.2
 */
@Entity
@Table(name = "INV_APP_INFORMATION_SYSTEM_DB_AUD")
@Getter
@Setter
public class AppInformationSystemDbAudEntity {

    /** Unique identification pointer of this audit trail row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_INF_SYS_DB_AUD_SEQ")
    @SequenceGenerator(name = "INV_APP_INF_SYS_DB_AUD_SEQ", sequenceName = "INV_APP_INF_SYS_DB_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    /** Identifier of the audited {@code AppInformationSystemDbEntity} record. */
    @Column(name = "ID", nullable = false)
    private Long id;

    /** Identifier of the corporate application at the time of the audited change. */
    @Column(name = "APPLICATION_ID", nullable = false)
    private Long applicationId;

    /** Observation notes at the time of the audited change. */
    @Lob
    @Column(name = "OBSERVATION")
    private String observation;

    /** Timestamp at which the original record was created. */
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    /** Identifier of the user who created the original record. */
    @Column(name = "CREATED_BY")
    private String createdBy;

    /** Timestamp at which the original record was last updated. */
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /** Identifier of the user who last updated the original record. */
    @Column(name = "UPDATED_BY")
    private String updatedBy;

    /** Timestamp at which the original record was logically deleted, or {@code null} if still active. */
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    /** Identifier of the user who logically deleted the original record, or {@code null} if still active. */
    @Column(name = "DELETED_BY")
    private String deletedBy;

    /** Type of audited action (e.g. insert, update, delete). */
    @Column(name = "AUD_ACTION", nullable = false, length = 10)
    private String audAction;

    /** Timestamp at which this audit row was recorded. */
    @Column(name = "AUDIT_DATE", nullable = false)
    private LocalDateTime auditDate;

    /** Identifier of the user who triggered the audited action. */
    @Column(name = "AUDIT_USER", length = 64)
    private String auditUser;
}
