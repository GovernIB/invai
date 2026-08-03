package es.caib.invai.api.interna.application.system_database.core.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

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
public class AppInformationSystemDbInputDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Optional foreign key referencing the parent corporate Application profile; assigned only when present. */
    private Long applicationId;

    /** Free-text narrative notes describing the information system grouping context. */
    private String observation;
}
