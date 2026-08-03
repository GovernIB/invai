package es.caib.invai.api.persistence.model;

import es.caib.invai.api.persistence.model.catalog.LkupServerTypeEntity;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * JPA entity mapping physical or virtual host server records from the {@code INV_SERVER} table.
 *
 * @since 1.0.2
 */
@Entity
@Table(name = "INV_SERVER")
@Getter
@Setter
public class ServerEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_SERVER_SEQ")
    @SequenceGenerator(name = "INV_SERVER_SEQ", sequenceName = "INV_SERVER_SEQ", allocationSize = 1)
    @Column(name = "ID", unique = true, nullable = false)
    private Long id;

    @Column(name = "NAME", nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ENVIRONMENT_ID", nullable = false)
    private EnvironmentEntity environment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVER_TYPE_ID", nullable = false)
    private LkupServerTypeEntity serverType;
}
