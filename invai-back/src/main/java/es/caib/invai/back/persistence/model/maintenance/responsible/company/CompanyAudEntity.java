package es.caib.invai.back.persistence.model.maintenance.responsible.company;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_COMPANY} database table.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@Entity
@Table(name = "INV_COMPANY_AUD")
public class CompanyAudEntity {

    /** Unique auto-generated identifier of this audit snapshot row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_company_aud_seq")
    @SequenceGenerator(name = "inv_company_aud_seq", sequenceName = "INV_COMPANY_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    /** Identifier of the {@code CompanyEntity} this audit snapshot refers to. */
    @Column(name = "COMPANY_ID", nullable = false)
    private Long companyId;

    /** Company corporate name captured at the time of the audited change. */
    @Column(name = "NAME", length = 150, nullable = false)
    private String name;

    /** Timestamp at which the company record was originally created. */
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    /** Username of the user who originally created the company record. */
    @Column(name = "CREATED_BY", length = 64)
    private String createdBy;

    /** Timestamp at which the company record was last updated. */
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /** Username of the user who last updated the company record. */
    @Column(name = "UPDATED_BY", length = 64)
    private String updatedBy;

    /** Timestamp at which the company record was logically deleted, or {@code null} if active. */
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    /** Username of the user who logically deleted the company record. */
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
