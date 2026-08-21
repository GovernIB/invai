package es.caib.invai.back.service.model.application.responsibleAuthorized.type;

import lombok.*;

/**
 * Domain aggregate model representing a single authorization type attached to an
 * {@code AppAuthorized} anchor. Intermediate join row: no audit fields.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppAuthorizedTypeLink {
    /** Unique identifier. */
    private Long id;
    /** Identifier of the authorized person anchor this type is attached to. */
    private Long appAuthorizedId;
    /** Identifier of the attached authorization type catalog value. */
    private Long authorizationTypeId;
}
