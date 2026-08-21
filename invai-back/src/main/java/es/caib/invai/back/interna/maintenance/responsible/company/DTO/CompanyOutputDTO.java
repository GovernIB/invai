package es.caib.invai.back.interna.maintenance.responsible.company.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for a Company resource.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompanyOutputDTO {

    /** The unique data storage database auto-incremented primary key. */
    private Long id;

    /** Company corporate name label string. */
    private String name;

    /** Timestamp at which the company was logically deleted, or {@code null} if it is active. */
    private LocalDateTime deletedAt;
}
