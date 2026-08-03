package es.caib.invai.api.persistence.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

/**
 * JPA persistent entity representing provider roles assignable within application development modules.
 * Implements historical auditing field inheritances by extending {@link BaseEntity}.
 *
 * @since 1.0.2
 */
@Entity
@Table(name = "INV_ROLE")
@Getter
@Setter
public class RoleEntity extends BaseEntity {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_role_seq")
    @SequenceGenerator(
            name = "inv_role_seq",
            sequenceName = "INV_ROLE_SEQ",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "NAME_ES", length = 100)
    private String nameEs;
}
