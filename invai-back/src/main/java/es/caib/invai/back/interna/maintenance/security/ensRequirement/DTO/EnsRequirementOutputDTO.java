package es.caib.invai.back.interna.maintenance.security.ensRequirement.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for an EnsRequirement resource.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EnsRequirementOutputDTO {

    /** The unique data storage database auto-incremented primary key. */
    private Long id;

    /** ENS requirement descriptive label string. */
    private String name;

    /** The translated Spanish localized descriptive name of the ENS requirement entry. */
    private String nameEs;

    /** Timestamp when the ENS requirement entry was logically deleted, or {@code null} if it is still active. */
    private LocalDateTime deletedAt;
}
