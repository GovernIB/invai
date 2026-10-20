package es.caib.invai.back.persistence.model.application.integration.requiredRole;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical audit trail entity recording every lifecycle mutation applied to
 * {@link AppIntegrationRequiredRoleEntity} records.
 *
 * @since 1.0.5
 */
@Entity
@Table(name = "INV_APP_INTEGR_REQ_RL_AUD")
@Getter
@Setter
public class AppIntegrationRequiredRoleAudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_INTEGR_REQ_RL_AUD_SEQ")
    @SequenceGenerator(name = "INV_APP_INTEGR_REQ_RL_AUD_SEQ", sequenceName = "INV_APP_INTEGR_REQ_RL_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    @Column(name = "APP_INTEGRATION_REQ_RL_ID", nullable = false)
    private Long appIntegrationRequiredRoleId;

    @Column(name = "APP_INTEGRATION_CONN_ID", nullable = false)
    private Long appIntegrationConnectionId;

    @Column(name = "ROLE_ID", nullable = false)
    private Long roleId;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "UPDATED_BY")
    private String updatedBy;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    @Column(name = "DELETED_BY")
    private String deletedBy;

    @Column(name = "AUD_ACTION", nullable = false, length = 10)
    private String audAction;

    @Column(name = "AUDIT_DATE", nullable = false)
    private LocalDateTime auditDate;

    @Column(name = "AUDIT_USER", length = 64)
    private String auditUser;
}
