package es.caib.invai.api.interna.application.development.technology.DTO;

import es.caib.invai.api.interna.maintenance.layer.DTO.LayerOutputDTO;
import es.caib.invai.api.interna.maintenance.technology.DTO.TechnologyOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
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
public class AppTechnologyOutputDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private LayerOutputDTO layer;
    private TechnologyOutputDTO technology;
    private String version;
    private String architecture;
    private LocalDateTime deletedAt;
}
