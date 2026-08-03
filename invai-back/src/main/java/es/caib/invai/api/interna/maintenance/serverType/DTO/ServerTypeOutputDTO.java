package es.caib.invai.api.interna.maintenance.serverType.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for a Server Type
 * lookup entry (e.g. DATABASE, APPLICATION).
 *
 * @since 1.0.2
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServerTypeOutputDTO {

    private Long id;

    private String code;

    private String name;

    private String nameEs;
}
