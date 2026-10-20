package es.caib.invai.back.persistence.model.application.integration.requiredRole;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.application.integration.connection.AppIntegrationConnectionEntity;

import java.io.Serial;

/**
 * Persistent entity mapping a role required by an {@link AppIntegrationConnectionEntity} - only the
 * Soffid role id is stored, never a name/system/description snapshot: the frontend already
 * resolves those via {@code SoffidClient.searchRoles} when the user picks a role, so persisting
 * them again here would be redundant. Deliberately not a foreign key to any local catalog (e.g.
 * {@code SecurityRoleEntity}) either: Soffid is the source of truth.
 *
 * @since 1.0.5
 */
@Entity
@Table(name = "INV_APP_INTEGR_REQ_RL")
@Getter
@Setter
public class AppIntegrationRequiredRoleEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identification pointer of this required role row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_INTEGR_REQ_RL_SEQ")
    @SequenceGenerator(name = "INV_APP_INTEGR_REQ_RL_SEQ", sequenceName = "INV_APP_INTEGR_REQ_RL_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Integration connection this required role belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_INTEGRATION_CONN_ID", nullable = false)
    private AppIntegrationConnectionEntity appIntegrationConnection;

    /** Soffid role's own numeric id. */
    @Column(name = "ROLE_ID", nullable = false)
    private Long roleId;
}
