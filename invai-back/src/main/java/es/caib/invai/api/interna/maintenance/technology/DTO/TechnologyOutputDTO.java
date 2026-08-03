package es.caib.invai.api.interna.maintenance.technology.DTO;

import es.caib.invai.api.interna.maintenance.layer.DTO.LayerOutputDTO;
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

    private Long id;

    private String name;

    private LayerOutputDTO layer;

    private LocalDateTime deletedAt;
}
