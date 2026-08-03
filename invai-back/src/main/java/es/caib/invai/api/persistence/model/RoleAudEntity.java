package es.caib.invai.api.persistence.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_ROLE} database table.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Entity
@Table(name = "INV_ROLE_AUD")
public class RoleAudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_role_aud_seq")
    @SequenceGenerator(name = "inv_role_aud_seq", sequenceName = "INV_ROLE_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID")
    private Long audId;

    @Column(name = "ROLE_ID")
    private Long roleId;

    @Column(name = "NAME")
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

    @Column(name = "AUD_ACTION")
    private String audAction;

    @Column(name = "AUDIT_DATE")
    private LocalDateTime auditDate;

    @Column(name = "AUDIT_USER")
    private String auditUser;
}
