package es.caib.invai.back.persistence.model.application.security.role;

import lombok.*;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.application.security.core.AppSecurityEntity;
import es.caib.invai.back.persistence.model.maintenance.security.securityRole.SecurityRoleEntity;

import java.io.Serial;

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

    /** Security role catalog entry assigned to the application's security profile. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SECURITY_ROLE_ID", nullable = false)
    private SecurityRoleEntity securityRole;
}
