package es.caib.invai.api.persistence.model;

import lombok.*;
import jakarta.persistence.*;

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

    private static final long serialVersionUID = 1L;

    /**
     * Physical database primary key mapped to sequence generator {@code INV_DATABASE_SEQ}.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_DATABASE_SEQ")
    @SequenceGenerator(name = "INV_DATABASE_SEQ", sequenceName = "INV_DATABASE_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVER_ID", nullable = false)
    private ServerEntity server;

    @Column(name = "SERVICE", nullable = false)
    private String service;

    @Column(name = "PORT")
    private Integer port;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DATABASE_TYPE")
    private DatabaseVendorEntity databaseType;

    @Column(name = "DESCRIPTION")
    private String description;
}