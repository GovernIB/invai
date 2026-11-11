package es.caib.invai.back.persistence.model.maintenance.development.providerRole;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;

import java.io.Serial;

/**
 * JPA persistent entity representing provider roles assignable within application development modules.
 * Implements historical auditing field inheritances by extending {@link BaseEntity}.
 *
 * @since 1.0.2
 */
@Entity
@Table(name = "INV_PROVIDER_ROLE")
@Getter
@Setter
public class ProviderRoleEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Primary persistent storage unique sequence row reference. */
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_provider_role_seq")
    @SequenceGenerator(
            name = "inv_provider_role_seq",
            sequenceName = "INV_PROVIDER_ROLE_SEQ",
            allocationSize = 1
    )
    private Long id;

    /** Localized descriptive role title label string (typically in Catalan). */
    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    /** Secondary translated description text variant explicitly matching Spanish locale records. */
    @Column(name = "NAME_ES", length = 100)
    private String nameEs;
}
