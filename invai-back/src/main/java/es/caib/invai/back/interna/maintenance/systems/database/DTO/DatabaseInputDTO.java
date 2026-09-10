package es.caib.invai.back.interna.maintenance.systems.database.DTO;

import es.caib.invai.back.utils.Constants;

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
    @NotNull(message = "{" + Constants.VALIDATION_DATABASE_SERVER + "}")
    private Long serverId;

    /** System identifier or cataloged instance service name. */
    @NotBlank(message = "{" + Constants.VALIDATION_DATABASE_SERVICE + "}")
    @Size(max = 255, message = "{" + Constants.VALIDATION_DATABASE_SERVICE_OVERFLOW + "}")
    private String service;

    /** Port connection integer of the database listener. */
    @NotNull(message = "{" + Constants.VALIDATION_DATABASE_PORT + "}")
    private Integer port;

    /** Foreign key unique identification pointer referencing the database vendor/type catalog entry. */
    @NotNull(message = "{" + Constants.VALIDATION_DATABASE_DATABASE_TYPE_ID + "}")
    private Long databaseTypeId;

    /** Verbose description narrative text highlighting operational scopes or technical parameters. */
    private String description;
}