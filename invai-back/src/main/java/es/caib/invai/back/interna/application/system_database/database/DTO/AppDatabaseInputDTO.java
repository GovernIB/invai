package es.caib.invai.back.interna.application.system_database.database.DTO;

import es.caib.invai.back.utils.Constants;

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
    @NotNull(message = "{" + Constants.VALIDATION_APPDATABASE_INFORMATION_SYSTEM_DB_ID + "}")
    private Long informationSystemDbId;

    /** Foreign key unique identification pointer referencing the target database. */
    @NotNull(message = "{" + Constants.VALIDATION_APPDATABASE_DATABASE_ID + "}")
    private Long databaseId;
}
