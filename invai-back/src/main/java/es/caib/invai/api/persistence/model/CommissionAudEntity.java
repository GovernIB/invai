package es.caib.invai.api.persistence.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_COMMISSION} database table.
 * Preserves point-in-time states alongside contextual execution logs for administrative
 * audit trails.
 *
 * @since 1.0.0
 */
@Getter
@Setter
@Entity
@Table(name = "INV_COMMISSION_AUD")
public class CommissionAudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_commission_aud_seq")
    @SequenceGenerator(name = "inv_commission_aud_seq", sequenceName = "INV_COMMISSION_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID")
    private Long auditId;

    @Column(name = "COMMISSION_ID", nullable = false)
    private Long commissionId;

    @Column(name = "NAME", length = 100)
    private String name;

    @Column(name = "NAME_ES", length = 100)
    private String nameEs;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "CREATED_BY", length = 64)
    private String createdBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "UPDATED_BY", length = 64)
    private String updatedBy;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    @Column(name = "DELETED_BY", length = 64)
    private String deletedBy;

    @Column(name = "AUD_ACTION", length = 10, nullable = false)
    private String audAction;

    @Column(name = "AUDIT_DATE", nullable = false)
    private LocalDateTime auditDate;

    @Column(name = "AUDIT_USER", length = 64)
    private String auditUser;
}