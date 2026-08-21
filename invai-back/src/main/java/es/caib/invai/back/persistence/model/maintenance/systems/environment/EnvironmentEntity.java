package es.caib.invai.back.persistence.model.maintenance.systems.environment;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;

import java.io.Serial;

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

    @Serial
    private static final long serialVersionUID = 1L;

    /** Primary key mapped to sequence generator {@code INV_ENVIRONMENT_SEQ}. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_ENVIRONMENT_SEQ")
    @SequenceGenerator(name = "INV_ENVIRONMENT_SEQ", sequenceName = "INV_ENVIRONMENT_SEQ", allocationSize = 1)
    @Column(name = "ID", unique = true, nullable = false)
    private Long id;

    /** Mnemonic alphanumeric code identifying the execution environment. */
    @Column(name = "CODE", nullable = false, length = 50)
    private String code;

    /** Descriptive display name in Catalan language. */
    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    /** Descriptive display name in Spanish language. */
    @Column(name = "NAME_ES", nullable = false, length = 100)
    private String nameEs;
}