package es.caib.invai.back.service.model.maintenance.systems.databaseVendor;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Domain Model object representing pure database vendor/type catalog logic schemas.
 *
 * @since 1.0.2
 */
@Getter
@Setter
public class DatabaseVendor {

    /** The unique identity of the database vendor model. */
    private Long id;

    /** Name of the database vendor/type (e.g., Oracle, PostgreSQL). */
    private String name;

    /** Default connection port for the database vendor. */
    private Integer defaultPort;

    /** Domain record creation timestamp. */
    private LocalDateTime createdAt;

    /** Domain record creator identity. */
    private String createdBy;

    /** Domain record last update timestamp. */
    private LocalDateTime updatedAt;

    /** Domain record last updater identity. */
    private String updatedBy;

    /** Domain record logical soft-deletion timestamp. */
    private LocalDateTime deletedAt;

    /** Identity of the user who performed the logical soft-deletion. */
    private String deletedBy;
}
