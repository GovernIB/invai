package es.caib.invai.back.persistence.model.application.integration.connection;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical audit trail entity recording every lifecycle mutation applied to
 * {@link AppIntegrationConnectionEntity} records.
 *
 * @since 1.0.5
 */
@Entity
@Table(name = "INV_APP_INTEGRATION_CONN_AUD")
@Getter
@Setter
public class AppIntegrationConnectionAudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_INTEGR_CONN_AUD_SEQ")
    @SequenceGenerator(name = "INV_APP_INTEGR_CONN_AUD_SEQ", sequenceName = "INV_APP_INTEGR_CONN_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    @Column(name = "APP_INTEGRATION_CONN_ID", nullable = false)
    private Long appIntegrationConnectionId;

    @Column(name = "APP_INTEGRATION_ID", nullable = false)
    private Long appIntegrationId;

    @Column(name = "APPLICATION_ID")
    private Long applicationId;

    @Column(name = "EXTERNAL_SYSTEM_ID")
    private Long externalSystemId;

    @Column(name = "TECHNOLOGY_ID", nullable = false)
    private Long technologyId;

    @Column(name = "USERNAME", nullable = false)
    private String username;

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
