package es.caib.invai.api.persistence.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

/**
 * JPA entity mapping physical environment records from the {@code INV_ENVIRONMENT} table.
 *
 * @since 1.0.1
 */
@Entity
@Table(name = "INV_ENVIRONMENT")
@Getter
@Setter
public class EnvironmentEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_ENVIRONMENT_SEQ")
    @SequenceGenerator(name = "INV_ENVIRONMENT_SEQ", sequenceName = "INV_ENVIRONMENT_SEQ", allocationSize = 1)
    @Column(name = "ID", unique = true, nullable = false)
    private Long id;

    @Column(name = "CODE", nullable = false, length = 50)
    private String code;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "NAME_ES", nullable = false, length = 100)
    private String nameEs;
}