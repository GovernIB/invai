package es.caib.invai.back.persistence.model.application.development.provider;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Historical audit trail entity recording every lifecycle mutation applied
 * to {@link AppProviderEntity} records.
 *
 * @since 1.0.2
 */
@Entity
@Table(name = "INV_APP_PROVIDER_AUD")
@Getter
@Setter
public class AppProviderAudEntity {

    /** Primary key of this audit trail row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_PROVIDER_AUD_SEQ")
    @SequenceGenerator(name = "INV_APP_PROVIDER_AUD_SEQ", sequenceName = "INV_APP_PROVIDER_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    /** Identifier of the original provider record this audit snapshot pertains to. */
    @Column(name = "APP_PROVIDER_ID", nullable = false)
    private Long providerId;

    /** Foreign key snapshot of the parent development module. */
    @Column(name = "APP_DEVELOPMENT_ID", nullable = false)
    private Long appDevelopmentId;

    /** Snapshot of the provider company name. */
    @Column(name = "COMPANY_NAME", nullable = false)
    private String companyName;

    /** Foreign key snapshot of the provider's role catalog entry. */
    @Column(name = "ROLE")
    private Long roleId;

    /** Snapshot of the provider service contract start date. */
    @Column(name = "START_DATE")
    private LocalDateTime startDate;

    /** Snapshot of the provider service contract end date. */
    @Column(name = "EXPIRE_DATE")
    private LocalDateTime expireDate;

    /** Timestamp when the original record was created. */
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

    /** Timestamp when the original record was logically deleted, or null if still active. */
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    /** Username of the user who logically deleted the original record. */
    @Column(name = "DELETED_BY")
    private String deletedBy;

    /** Type of lifecycle mutation that triggered this audit row (INSERT, UPDATE, or DELETE). */
    @Column(name = "AUD_ACTION", nullable = false, length = 10)
    private String audAction;

    /** Timestamp when this audit row was recorded. */
    @Column(name = "AUDIT_DATE", nullable = false)
    private LocalDateTime auditDate;

    /** Username of the user who triggered the audited mutation. */
    @Column(name = "AUDIT_USER", length = 64)
    private String auditUser;
}
