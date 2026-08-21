package es.caib.invai.back.interna.application.system_database.core.DTO;

import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
public class AppInformationSystemDbOutputDTO {

    /** Unique identification pointer of this information system grouping. */
    private Long id;
    /** Corporate application this information system grouping belongs to. */
    private ApplicationOutputDTO application;
    /** Free-text observation notes attached to this information system grouping. */
    private String observation;
    /** Timestamp at which this record was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}
