package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) capturing search filters used to build dynamic database queries
 * targeting the AppResponsible registry scoped to a single "Responsables i Autoritzats" anchor.
 *
 * @since 1.0.3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppResponsibleCriteria {
    /** Filter by state/status identifier (e.g., 1L for active records with deletedAt IS NULL). */
    private Long statusId;
    /** Exact matching filter for the associated person identifier. */
    private Long personId;
    /** Exact matching filter for the associated responsible type identifier. */
    private Long responsibleTypeId;
    /** Free-text global search query pattern across the linked person's name and email. */
    private String search;
}
