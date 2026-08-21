package es.caib.invai.back.persistence.model.maintenance.systems.system;

import lombok.*;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.server.ServerEntity;

import java.io.Serial;

/**
 * JPA entity mapping physical system records from the {@code INV_SYSTEM} table.
 *
 * @since 1.0.1
 */
@Entity
@Table(name = "INV_SYSTEM")
@Getter
@Setter
public class SystemEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Primary persistent storage unique sequence row reference. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_SYSTEM_SEQ")
    @SequenceGenerator(name = "INV_SYSTEM_SEQ", sequenceName = "INV_SYSTEM_SEQ", allocationSize = 1)
    @Column(name = "ID", unique = true, nullable = false)
    private Long id;

    /**
     * Foreign key association pointing to the host {@link ServerEntity} this system runs on.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVER_ID", nullable = false)
    private ServerEntity server;

    /** System instance name, connection identifier or node routing string. */
    @Column(name = "INSTANCE", nullable = false, length = 255)
    private String instance;

    /** Operational connection network port number. */
    @Column(name = "PORT")
    private Integer port;

    /** Release version tag representing current deployment state. */
    @Column(name = "VERSION", length = 255)
    private String version;

    /** Short definition narrative detailing system context targets. */
    @Column(name = "DESCRIPTION", length = 255)
    private String description;
}