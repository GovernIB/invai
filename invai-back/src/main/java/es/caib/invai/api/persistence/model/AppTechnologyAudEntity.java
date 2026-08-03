package es.caib.invai.api.persistence.model;

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
 * to {@link AppTechnologyEntity} records.
 *
 * @since 1.0.2
 */
@Entity
@Table(name = "INV_APP_TECHNOLOGY_AUD")
@Getter
@Setter
public class AppTechnologyAudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_TECHNOLOGY_AUD_SEQ")
    @SequenceGenerator(name = "INV_APP_TECHNOLOGY_AUD_SEQ", sequenceName = "INV_APP_TECHNOLOGY_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    @Column(name = "APP_TECHNOLOGY_ID", nullable = false)
    private Long technologyId;

    @Column(name = "APP_DEVELOPMENT_ID", nullable = false)
    private Long appDevelopmentId;

    @Column(name = "LAYER")
    private Long layerId;

    @Column(name = "TECHNOLOGY")
    private Long technologyCatalogId;

    @Column(name = "VERSION")
    private String version;

    @Column(name = "ARCHITECTURE")
    private String architecture;

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
