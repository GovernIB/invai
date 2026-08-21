package es.caib.invai.back.interna.maintenance.systems.serverType.DTO;

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

    /** Primary persistent storage unique sequence row reference. */
    private Long id;

    /** Unique short code identifying the server type (e.g. DATABASE, APPLICATION). */
    private String code;

    /** Catalan localized descriptive name of the server type. */
    private String name;

    /** Spanish localized descriptive name of the server type. */
    private String nameEs;
}
