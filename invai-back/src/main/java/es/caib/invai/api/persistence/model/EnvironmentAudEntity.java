package es.caib.invai.api.persistence.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_ENVIRONMENT} database table tracking.
 *
 * @since 1.0.1
 */
@Getter
@Setter
@Entity
@Table(name = "INV_ENVIRONMENT_AUD")
public class EnvironmentAudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_env_aud_seq")
    @SequenceGenerator(name = "inv_env_aud_seq", sequenceName = "INV_ENVIRONMENT_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    @Column(name = "ENVIRONMENT_ID", nullable = false)
    private Long environmentId;

    @Column(name = "CODE", length = 50, nullable = false)
    private String code;

    @Column(name = "NAME", length = 100, nullable = false)
    private String name;

    @Column(name = "NAME_ES", length = 100, nullable = false)
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

    @Column(name = "AUDIT_USER", length = 64, nullable = false)
    private String auditUser;
}