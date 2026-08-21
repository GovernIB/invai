package es.caib.invai.back.persistence.model.maintenance.responsible.authorizationType;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;

/**
 * JPA persistent entity representing authorization types assignable within the Manteniments / Responsables catalog.
 * Implements historical auditing field inheritances by extending {@link BaseEntity}.
 *
 * @since 1.0.3
 */
@Entity
@Table(name = "INV_AUTHORIZATION_TYPE")
@Getter
@Setter
public class AuthorizationTypeEntity extends BaseEntity {

    /** Unique auto-generated primary key of the authorization type. */
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_authorization_type_seq")
    @SequenceGenerator(
            name = "inv_authorization_type_seq",
            sequenceName = "INV_AUTHORIZATION_TYPE_SEQ",
            allocationSize = 1
    )
    private Long id;

    /** Authorization type descriptive label string (e.g. "Firmar peticiones"). */
    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    /** Spanish localized descriptive name of the authorization type. */
    @Column(name = "NAME_ES", length = 100)
    private String nameEs;
}
