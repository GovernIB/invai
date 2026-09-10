package es.caib.invai.back.persistence.model.maintenance.security.identityProvider;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;

import java.io.Serial;

/**
 * JPA persistent entity representing identity providers assignable within the Manteniments / Seguretat catalog.
 * Implements historical auditing field inheritances by extending {@link BaseEntity}.
 *
 * @since 1.0.4
 */
@Entity
@Table(name = "INV_IDENTITY_PROVIDER")
@Getter
@Setter
public class IdentityProviderEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique auto-generated primary key of the identity provider. */
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_identity_provider_seq")
    @SequenceGenerator(
            name = "inv_identity_provider_seq",
            sequenceName = "INV_IDENTITY_PROVIDER_SEQ",
            allocationSize = 1
    )
    private Long id;

    /** Identity provider descriptive label string (e.g. "Cl@ve"). */
    @Column(name = "NAME", nullable = false, length = 150)
    private String name;
}
