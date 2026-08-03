package es.caib.invai.api.interna.maintenance.database.DTO;

import es.caib.invai.api.interna.maintenance.databaseVendor.DTO.DatabaseVendorOutputDTO;
import es.caib.invai.api.interna.maintenance.server.DTO.ServerOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) wrapping outgoing database system state metadata payload models.
 * <p>
 * Aggregates complete domain entity snapshots along with automated timeline audit log metadata.
 * </p>
 *
 * @since 1.0.2
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseOutputDTO {

    /** Primary persistent storage unique sequence row reference. */
    private Long id;

    /** The linked host server (whose type is DATABASE) hosting this database instance. */
    private ServerOutputDTO server;

    /** Service identification descriptor name. */
    private String service;

    /** Listening connection port. */
    private Integer port;

    /** Resolved database vendor/type catalog snapshot. */
    private DatabaseVendorOutputDTO databaseType;

    /** Detailed administrative description text. */
    private String description;

    private LocalDateTime deletedAt;
}