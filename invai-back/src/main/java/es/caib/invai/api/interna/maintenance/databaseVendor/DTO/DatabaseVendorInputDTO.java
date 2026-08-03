package es.caib.invai.api.interna.maintenance.databaseVendor.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) capturing incoming payload attributes required to register or update
 * a database vendor/type catalog record within the enterprise inventory system framework.
 *
 * @since 1.0.2
 */
@Getter
@Setter
public class DatabaseVendorInputDTO {

    /** Name of the database vendor/type (e.g., Oracle, PostgreSQL). */
    @NotBlank(message = "{validation.databasevendor.name}")
    @Size(max = 100, message = "{validation.databasevendor.name.overflow}")
    private String name;

    /** Default connection port for the database vendor. */
    @NotNull(message = "{validation.databasevendor.defaultPort}")
    private Integer defaultPort;
}
