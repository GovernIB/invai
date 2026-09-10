package es.caib.invai.back.persistence.model.maintenance.security.securityRole;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;

import java.io.Serial;

/**
 * JPA persistent entity representing application security roles cached locally within the
 * Manteniments / Seguretat catalog. Inherits fundamental structural audit capabilities and
 * behavior hooks from {@link BaseEntity}.
 * <p>
 * Acts as a locally-populated cache table of Soffid roles, keyed by {@link #roleId} (Soffid's
 * own numeric role identifier). Currently populated by no write path yet: only the Soffid
 * search proxy exists, so this table stays empty until a future iteration adds the
 * resolve-or-create flow that feeds {@code INV_APP_ROLE} assignments.
 * </p>
 *
 * @since 1.0.4
 */
@Getter
@Setter
@Entity
@Table(name = "INV_SECURITY_ROLE")
public class SecurityRoleEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Persistent database primary key identifier, generated from the {@code INV_SECURITY_ROLE_SEQ} sequence. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_SECURITY_ROLE_SEQ")
    @SequenceGenerator(name = "INV_SECURITY_ROLE_SEQ", sequenceName = "INV_SECURITY_ROLE_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Soffid's own numeric role identifier (as returned by GET scim2/v1/Role/). */
    @Column(name = "ROLE_ID", nullable = false)
    private Long roleId;

    /** Security role descriptive label, as copied from Soffid. */
    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    /** Soffid system (environment) this role is associated with, as copied from Soffid. */
    @Column(name = "SYSTEM", length = 150)
    private String system;

    /** Human-readable role description, as copied from Soffid. */
    @Column(name = "DESCRIPTION", length = 500)
    private String description;

}
