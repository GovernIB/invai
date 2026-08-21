package es.caib.invai.back.interna.maintenance.systems.database.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) capturing incoming payload attributes required to register or update
 * a Database software asset record within the enterprise inventory system framework.
 * <p>
 * Enforces declarative bean validation attributes tied to localization message bundles and map structures.
 * </p>
 *
 * @since 1.0.2
 */
@Getter
@Setter
public class DatabaseInputDTO {

    /** The network host or IP address of the database server. */
    @NotNull(message = "{validation.database.server}")
    private Long serverId;

    /** System identifier or cataloged instance service name. */
    @NotBlank(message = "{validation.database.service}")
    @Size(max = 255, message = "{validation.database.service.overflow}")
    private String service;

    /** Port connection integer of the database listener. */
    @NotNull(message = "{validation.database.port}")
    private Integer port;

    /** Foreign key unique identification pointer referencing the database vendor/type catalog entry. */
    @NotNull(message = "{validation.database.databaseTypeId}")
    private Long databaseTypeId;

    /** Verbose description narrative text highlighting operational scopes or technical parameters. */
    private String description;
}