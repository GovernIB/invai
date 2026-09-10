package es.caib.invai.back.interna.maintenance.security.identityProvider.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for an IdentityProvider resource.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IdentityProviderOutputDTO {

    /** The unique data storage database auto-incremented primary key. */
    private Long id;

    /** Identity provider descriptive label string. */
    private String name;

    /** Timestamp when the identity provider was logically deleted, or {@code null} if it is still active. */
    private LocalDateTime deletedAt;
}
