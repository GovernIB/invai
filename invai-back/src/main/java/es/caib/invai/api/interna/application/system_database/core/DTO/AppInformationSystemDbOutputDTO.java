package es.caib.invai.api.interna.application.system_database.core.DTO;

import es.caib.invai.api.interna.application.core.DTO.ApplicationOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Outbound transport payload detailing technical context mappings of the target
 * application information system database grouping entity definitions.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppInformationSystemDbOutputDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private ApplicationOutputDTO application;
    private String observation;
    private LocalDateTime deletedAt;
}
