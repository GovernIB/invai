package es.caib.invai.back.interna.maintenance.integration.externalSystem.DTO;

import es.caib.invai.back.interna.maintenance.responsible.company.DTO.CompanyOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for an External
 * System resource.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExternalSystemOutputDTO {

    /** The unique data storage database auto-incremented primary key. */
    private Long id;

    /** External system name. */
    private String name;

    /** Company responsible for the external system. */
    private CompanyOutputDTO company;

    /** Timestamp at which the external system was logically deleted, or {@code null} if it is active. */
    private LocalDateTime deletedAt;
}
