package es.caib.invai.api.interna.maintenance.databaseVendor.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) wrapping outgoing database vendor/type catalog state metadata payload models.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseVendorOutputDTO {

    /** Primary persistent storage unique sequence row reference. */
    private Long id;

    /** Name of the database vendor/type (e.g., Oracle, PostgreSQL). */
    private String name;

    /** Default connection port for the database vendor. */
    private Integer defaultPort;

    private LocalDateTime deletedAt;
}
