package es.caib.invai.back.interna.maintenance.security.webContext.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for an WebContext resource.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WebContextOutputDTO {

    /** The unique data storage database auto-incremented primary key. */
    private Long id;

    /** Web context descriptive label string. */
    private String name;

    /** The translated Spanish localized descriptive name of the web context. */
    private String nameEs;

    /** Timestamp when the web context was logically deleted, or {@code null} if it is still active. */
    private LocalDateTime deletedAt;
}
