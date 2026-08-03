package es.caib.invai.api.persistence.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "INV_APP_SYSTEM_AUD")
@Getter
@Setter
public class AppSystemAudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_SYSTEM_AUD_SEQ")
    @SequenceGenerator(name = "INV_APP_SYSTEM_AUD_SEQ", sequenceName = "INV_APP_SYSTEM_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    @Column(name = "APP_SYSTEM_ID", nullable = false)
    private Long appSystemId;

    @Column(name = "INFORMATION_SYSTEM_DB", nullable = false)
    private Long informationSystemDbId;

    @Column(name = "SYSTEM_ID", nullable = false)
    private Long systemId;

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