package es.caib.invai.api.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_SYSTEM_TYPE} database table.
 * Records structural variations to catalog configurations for compliance tracking.
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "INV_SYSTEM_TYPE_AUD")
@Getter
@Setter
public class SystemTypeAudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_system_type_aud_seq")
    @SequenceGenerator(name = "inv_system_type_aud_seq", sequenceName = "INV_SYSTEM_TYPE_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID")
    private Long id;

    @Column(name = "SYSTEM_TYPE_ID", nullable = false)
    private Long systemTypeId;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "NAME_ES", length = 100)
    private String nameEs;

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

    @Column(name = "AUD_ACTION", nullable = false)
    private String audAction;

    @Column(name = "AUDIT_DATE", nullable = false)
    private LocalDateTime auditDate;

    @Column(name = "AUDIT_USER", nullable = false)
    private String auditUser;
}