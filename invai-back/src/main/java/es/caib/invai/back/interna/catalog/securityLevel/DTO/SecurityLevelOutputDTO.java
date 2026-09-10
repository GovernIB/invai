package es.caib.invai.back.interna.catalog.securityLevel.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for a
 * Security level lookup entry.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SecurityLevelOutputDTO {

    /** Unique identifier of the security level lookup entry. */
    private Long id;

    /** Display label of the security level (typically Catalan). */
    private String name;

    /** Display label of the security level in Spanish. */
    private String nameEs;

}
