package es.caib.invai.back.persistence.model.maintenance.systems.server;

import es.caib.invai.back.persistence.model.maintenance.systems.serverType.LkupServerTypeEntity;
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
import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.environment.EnvironmentEntity;

import java.io.Serial;

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

    @Serial
    private static final long serialVersionUID = 1L;

    /** Primary persistent storage unique sequence row reference. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_SERVER_SEQ")
    @SequenceGenerator(name = "INV_SERVER_SEQ", sequenceName = "INV_SERVER_SEQ", allocationSize = 1)
    @Column(name = "ID", unique = true, nullable = false)
    private Long id;

    /** Host name or server identification label. */
    @Column(name = "NAME", nullable = false, length = 255)
    private String name;

    /** Deployment environment zone this server belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ENVIRONMENT_ID", nullable = false)
    private EnvironmentEntity environment;

    /** Server type lookup entry (e.g. DATABASE, APPLICATION) this server is classified as. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVER_TYPE_ID", nullable = false)
    private LkupServerTypeEntity serverType;
}
