package es.caib.invai.back.interna.maintenance.development.role.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for a Role resource.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoleOutputDTO {

    /** The unique data storage database auto-incremented primary key. */
    private Long id;

    /** The official localized descriptor name of the role (Catalan). */
    private String name;

    /** The translated Spanish localized descriptive name of the role. */
    private String nameEs;

    /** Timestamp at which the role was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}
