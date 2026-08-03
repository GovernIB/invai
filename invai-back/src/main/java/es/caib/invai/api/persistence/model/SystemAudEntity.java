package es.caib.invai.api.persistence.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_SYSTEM} database table.
 * Retains flat snapshots of physical fields alongside specialized execution context audit trails.
 *
 * @since 1.0.1
 */
@Getter
@Setter
@Entity
@Table(name = "INV_SYSTEM_AUD")
public class SystemAudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_sys_aud_seq")
    @SequenceGenerator(
            name = "inv_sys_aud_seq",
            sequenceName = "INV_SYSTEM_AUD_SEQ",
            allocationSize = 1
    )
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    @Column(name = "SYSTEM_ID", nullable = false)
    private Long systemId;

    @Column(name = "SERVER_ID", nullable = false)
    private Long serverId;

    @Column(name = "INSTANCE", length = 255, nullable = false)
    private String instance;

    @Column(name = "PORT")
    private Integer port;

    @Column(name = "VERSION", length = 255)
    private String version;

    @Column(name = "DESCRIPTION", length = 255)
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

    @Column(name = "AUD_ACTION", length = 10, nullable = false)
    private String audAction;

    @Column(name = "AUDIT_DATE", nullable = false)
    private LocalDateTime auditDate;

    @Column(name = "AUDIT_USER", length = 64)
    private String auditUser;
}