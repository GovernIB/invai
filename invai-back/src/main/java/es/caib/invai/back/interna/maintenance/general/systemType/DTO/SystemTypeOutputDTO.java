package es.caib.invai.back.interna.maintenance.general.systemType.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload
 * for a System Type resource.
 * <p>
 * Decouples internal persistent schemas from the API contract by presenting
 * only public-facing structural properties back to frontend clients.
 * </p>
 *
 * @since 1.0.1
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SystemTypeOutputDTO {

    /** The unique data storage database persistent primary key. */
    private Long id;

    /** The official localized descriptive name of the system type (Catalan). */
    private String name;

    /** The translated Spanish localized descriptive name of the system type. */
    private String nameEs;

    /** Timestamp marking when the system type was logically soft-deleted, or {@code null} if active. */
    private LocalDateTime deletedAt;
}