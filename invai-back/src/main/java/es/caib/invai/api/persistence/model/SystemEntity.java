package es.caib.invai.api.persistence.model;

import lombok.*;
import jakarta.persistence.*;

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

    private static final long serialVersionUID = 1L;

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

    @Column(name = "INSTANCE", nullable = false, length = 255)
    private String instance;

    @Column(name = "PORT")
    private Integer port;

    @Column(name = "VERSION", length = 255)
    private String version;

    @Column(name = "DESCRIPTION", length = 255)
    private String description;
}