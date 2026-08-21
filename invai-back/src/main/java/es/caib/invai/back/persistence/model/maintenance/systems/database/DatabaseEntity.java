package es.caib.invai.back.persistence.model.maintenance.systems.database;

import lombok.*;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.databaseVendor.DatabaseVendorEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.server.ServerEntity;

import java.io.Serial;

/**
 * JPA entity mapping physical database records from the {@code INV_DATABASE} table.
 * Integrates with sequence-based surrogate key generation.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Entity
@Table(name = "INV_DATABASE")
public class DatabaseEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Physical database primary key mapped to sequence generator {@code INV_DATABASE_SEQ}.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_DATABASE_SEQ")
    @SequenceGenerator(name = "INV_DATABASE_SEQ", sequenceName = "INV_DATABASE_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** The host server (whose type must be DATABASE) hosting this database instance. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVER_ID", nullable = false)
    private ServerEntity server;

    /** Database service schema identifier name. */
    @Column(name = "SERVICE", nullable = false)
    private String service;

    /** Listening connection port. */
    @Column(name = "PORT")
    private Integer port;

    /** Database vendor/type catalog entry linked to this database. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DATABASE_TYPE")
    private DatabaseVendorEntity databaseType;

    /** Detailed administrative description text. */
    @Column(name = "DESCRIPTION")
    private String description;
}