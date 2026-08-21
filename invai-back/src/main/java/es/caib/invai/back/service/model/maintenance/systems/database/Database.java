package es.caib.invai.back.service.model.maintenance.systems.database;

import lombok.*;

import java.time.LocalDateTime;
import es.caib.invai.back.service.model.maintenance.systems.databaseVendor.DatabaseVendor;
import es.caib.invai.back.service.model.maintenance.systems.server.Server;

/**
 * Pure domain model representing a Database asset within the business layer.
 * Decouples the application's core business logic from physical JPA persistence structures.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Database {

    /**
     * The unique identity of the database model.
     */
    private Long id;

    /**
     * The linked host server (whose type must be DATABASE) hosting this database instance.
     */
    private Server server;

    /**
     * Service identification descriptor.
     */
    private String service;

    /**
     * Listening connection port.
     */
    private Integer port;

    /**
     * Database vendor/type catalog reference.
     */
    private DatabaseVendor databaseType;

    /**
     * Detailed administrative description.
     */
    private String description;

    /**
     * Domain record creation timestamp.
     */
    private LocalDateTime createdAt;

    /**
     * Domain record creator identity.
     */
    private String createdBy;

    /**
     * Domain record last update timestamp.
     */
    private LocalDateTime updatedAt;

    /**
     * Domain record last updater identity.
     */
    private String updatedBy;

    /**
     * Domain record logical soft-deletion timestamp.
     */
    private LocalDateTime deletedAt;

    /**
     * Identity of the user who performed the logical soft-deletion.
     */
    private String deletedBy;
}