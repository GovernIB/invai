package es.caib.invai.api.interna.application.system_database.database.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

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
public class AppDatabaseInputDTO implements Serializable {

    private static final long serialVersionUID = 1948104859302184L;

    @NotNull(message = "{validation.appdatabase.informationSystemDbId}")
    private Long informationSystemDbId;

    @NotNull(message = "{validation.appdatabase.databaseId}")
    private Long databaseId;
}
