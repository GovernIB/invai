package es.caib.invai.back.persistence.model.application.security.role;

import lombok.*;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.application.security.core.AppSecurityEntity;
import es.caib.invai.back.persistence.model.catalog.validationStatus.LkupValidationStatusEntity;
import es.caib.invai.back.persistence.model.maintenance.security.securityRole.SecurityRoleEntity;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * Persistent entity mapping a security role assigned to an {@link AppSecurityEntity}
 * anchor's security profile.
 *
 * @since 1.0.4
 */
@Entity
@Table(name = "INV_APP_ROLE")
@Getter
@Setter
public class AppRoleEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identification pointer of this application-role association. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_ROLE_SEQ")
    @SequenceGenerator(name = "INV_APP_ROLE_SEQ", sequenceName = "INV_APP_ROLE_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Security anchor this role assignment belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_SECURITY_ID", nullable = false)
    private AppSecurityEntity appSecurity;

    /**
     * Row holding the data of the Soffid role assigned to the application's security profile. It is
     * never handled on its own: it is saved, and rewritten, together with this assignment.
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "SECURITY_ROLE_ID", nullable = false)
    private SecurityRoleEntity securityRole;

    /**
     * Validation state of this assignment against the application's prefix: {@code VALIDATED} if
     * the role's name starts with the prefix followed by an underscore, {@code NOT_VALIDATED} if it
     * doesn't, {@code MANUAL} if a person vouched for it anyway. Shares the lookup the DIR3
     * validation of responsibles and authorized uses (same four fixed states).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VALIDATION_STATUS_ID", nullable = false)
    private LkupValidationStatusEntity validationStatus;

    /** Timestamp of the manual validation, populated only when the state is {@code MANUAL}. */
    @Column(name = "MANUAL_VALIDATED_AT")
    private LocalDateTime manualValidatedAt;

    /** User who validated the assignment manually, populated only when the state is {@code MANUAL}. */
    @Column(name = "MANUAL_VALIDATED_BY", length = 64)
    private String manualValidatedBy;
}
