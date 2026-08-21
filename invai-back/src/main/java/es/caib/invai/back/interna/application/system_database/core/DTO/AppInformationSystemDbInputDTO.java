package es.caib.invai.back.interna.application.system_database.core.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Inbound validation data transport contract containing properties required for
 * instantiation and mutation of application information system database groupings.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppInformationSystemDbInputDTO {

    /** Optional foreign key referencing the parent corporate Application profile; assigned only when present. */
    private Long applicationId;

    /** Free-text narrative notes describing the information system grouping context. */
    private String observation;
}
