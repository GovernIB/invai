package es.caib.invai.api.persistence.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA historical snapshot audit entity capturing transactional database mutations
 * on the {@code INV_DATABASE} table.
 *
 * @since 1.0.2
 */
@Entity
@Table(name = "INV_DATABASE_AUD")
@Getter
@Setter
public class DatabaseAudEntity {

    /**
     * Unique audit record surrogate identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_DATABASE_AUD_SEQ")
    @SequenceGenerator(name = "INV_DATABASE_AUD_SEQ", sequenceName = "INV_DATABASE_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    @Column(name = "DATABASE_ID", nullable = false)
    private Long databaseId;

    @Column(name = "SERVER_ID", nullable = false)
    private Long serverId;

    @Column(name = "SERVICE", nullable = false)
    private String service;

    @Column(name = "PORT")
    private Integer port;

    @Column(name = "DATABASE_TYPE")
    private Long databaseTypeId;

    @Column(name = "DESCRIPTION")
    private String description;

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