package es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for an AuthorizationType resource.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizationTypeOutputDTO {

    /** The unique data storage database auto-incremented primary key. */
    private Long id;

    /** Authorization type descriptive label string. */
    private String name;

    /** The translated Spanish localized descriptive name of the authorization type. */
    private String nameEs;

    /** Timestamp when the authorization type was logically deleted, or {@code null} if it is still active. */
    private LocalDateTime deletedAt;
}
