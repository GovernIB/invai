package es.caib.invai.back.interna.application.system_database.database.DTO;

import es.caib.invai.back.interna.application.system_database.core.DTO.AppInformationSystemDbOutputDTO;
import es.caib.invai.back.interna.maintenance.systems.database.DTO.DatabaseOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Outbound transport payload detailing technical context mappings of the target
 * database entity definitions.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppDatabaseOutputDTO {

    /** Unique identification pointer of this application-database association. */
    private Long id;
    /** Information system grouping this association belongs to. */
    private AppInformationSystemDbOutputDTO informationSystemDb;
    /** Database catalog entry associated with the information system grouping. */
    private DatabaseOutputDTO database;
    /** Timestamp at which this record was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}
