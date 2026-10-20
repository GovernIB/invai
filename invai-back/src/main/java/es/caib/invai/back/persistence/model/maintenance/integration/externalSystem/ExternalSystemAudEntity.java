package es.caib.invai.back.persistence.model.maintenance.integration.externalSystem;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_EXTERNAL_SYSTEM} database table.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@Entity
@Table(name = "INV_EXTERNAL_SYSTEM_AUD")
public class ExternalSystemAudEntity {

    /** Unique auto-generated identifier of this audit snapshot row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_external_system_aud_seq")
    @SequenceGenerator(name = "inv_external_system_aud_seq", sequenceName = "INV_EXTERNAL_SYSTEM_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    /** Identifier of the {@code ExternalSystemEntity} this audit snapshot refers to. */
    @Column(name = "EXTERNAL_SYSTEM_ID", nullable = false)
    private Long externalSystemId;

    /** External system name captured at the time of the audited change. */
    @Column(name = "NAME", length = 150, nullable = false)
    private String name;

    /** Identifier of the responsible company captured at the time of the audited change. */
    @Column(name = "COMPANY_ID", nullable = false)
    private Long companyId;

    /** Timestamp at which the external system record was originally created. */
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    /** Username of the user who originally created the external system record. */
    @Column(name = "CREATED_BY", length = 64)
    private String createdBy;

    /** Timestamp at which the external system record was last updated. */
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /** Username of the user who last updated the external system record. */
    @Column(name = "UPDATED_BY", length = 64)
    private String updatedBy;

    /** Timestamp at which the external system record was logically deleted, or {@code null} if active. */
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    /** Username of the user who logically deleted the external system record. */
    @Column(name = "DELETED_BY", length = 64)
    private String deletedBy;

    /** Type of mutation that produced this audit snapshot (e.g. {@code INSERT}, {@code UPDATE}, {@code DELETE}). */
    @Column(name = "AUD_ACTION", length = 10, nullable = false)
    private String audAction;

    /** Timestamp at which this audit snapshot was recorded. */
    @Column(name = "AUDIT_DATE", nullable = false)
    private LocalDateTime auditDate;

    /** Username of the user who triggered the audited mutation. */
    @Column(name = "AUDIT_USER", length = 64)
    private String auditUser;
}
