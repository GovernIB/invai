package es.caib.invai.api.persistence.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_APPLICATION} database table.
 * Retains flat snapshots of structural relationships alongside specialized execution
 * context audit trails.
 *
 * @since 1.0.0
 */
@Getter
@Setter
@Entity
@Table(name = "INV_APPLICATION_AUD")
public class ApplicationAudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_app_aud_seq")
    @SequenceGenerator(
            name = "inv_app_aud_seq",
            sequenceName = "INV_APPLICATION_AUD_SEQ",
            allocationSize = 1
    )
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    @Column(name = "APP_APPLICATION_ID", nullable = false)
    private Long appApplicationId;

    @Column(name = "CODE", length = 10, nullable = false)
    private String code;

    @Column(name = "PREFIX", length = 3, nullable = false)
    private String prefix;

    @Column(name = "NAME")
    private String name;

    @Column(name = "CATEGORY_ID")
    private Long categoryId;

    @Column(name = "SYSTEM_TYPE_ID")
    private Long systemTypeId;

    @Column(name = "FIELD_ID")
    private Long fieldId;

    @Column(name = "ADM_UNIT_ID")
    private Long admUnitId;

    @Column(name = "COMMISSION_ID")
    private Long commissionId;

    @Column(name = "STATUS_ID")
    private Long statusId;

    @Lob
    @Column(name = "DESCRIPTION")
    private String description;

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

    @Column(name = "EXPIRATION_DATE")
    private LocalDateTime expirationDate;

    @Column(name = "AUD_ACTION", length = 10, nullable = false)
    private String audAction;

    @Column(name = "AUDIT_DATE", nullable = false)
    private LocalDateTime auditDate;

    @Column(name = "AUDIT_USER", length = 64)
    private String auditUser;
}