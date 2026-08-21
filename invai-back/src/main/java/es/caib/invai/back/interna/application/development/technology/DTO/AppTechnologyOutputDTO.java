package es.caib.invai.back.interna.application.development.technology.DTO;

import es.caib.invai.back.interna.maintenance.development.layer.DTO.LayerOutputDTO;
import es.caib.invai.back.interna.maintenance.development.technology.DTO.TechnologyOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Outbound transport payload detailing a technology stack entry linked to a development module.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppTechnologyOutputDTO {

    /** Unique identifier. */
    private Long id;
    /** Architecture layer this technology stack entry belongs to. */
    private LayerOutputDTO layer;
    /** Technology catalog entry used. */
    private TechnologyOutputDTO technology;
    /** Technology version in use. */
    private String version;
    /** Architecture description or notes. */
    private String architecture;
    /** Timestamp when this record was soft-deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}
