package es.caib.invai.back.interna.maintenance.development.technology.DTO;

import es.caib.invai.back.interna.maintenance.development.layer.DTO.LayerOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for a Technology resource.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TechnologyOutputDTO {

    /** Unique identification pointer of the technology. */
    private Long id;

    /** Name of the technology. */
    private String name;

    /** Architecture layer this technology belongs to. */
    private LayerOutputDTO layer;

    /** Timestamp at which the technology was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}
