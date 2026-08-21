package es.caib.invai.back.interna.application.system_database.database.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

/**
 * Inbound validation data transport contract containing properties required for
 * instantiation and mutation of application database mappings.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppDatabaseInputDTO {

    /** Foreign key unique identification pointer referencing the parent information system database grouping. */
    @NotNull(message = "{validation.appdatabase.informationSystemDbId}")
    private Long informationSystemDbId;

    /** Foreign key unique identification pointer referencing the target database. */
    @NotNull(message = "{validation.appdatabase.databaseId}")
    private Long databaseId;
}
