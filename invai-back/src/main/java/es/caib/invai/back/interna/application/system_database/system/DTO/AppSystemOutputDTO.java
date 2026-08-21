package es.caib.invai.back.interna.application.system_database.system.DTO;

import es.caib.invai.back.interna.application.system_database.core.DTO.AppInformationSystemDbOutputDTO;
import es.caib.invai.back.interna.maintenance.systems.system.DTO.SystemOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) wrapping outgoing structural application-system assignment metadata profiles.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppSystemOutputDTO {

    /** Primary persistent storage unique sequence row reference link key. */
    private Long id;

    /** Registered parent information system database grouping tracking profile. */
    private AppInformationSystemDbOutputDTO informationSystemDb;

    /** Registered target infrastructure hardware server tracking node. */
    private SystemOutputDTO system;

    /** Timestamp at which this record was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}
