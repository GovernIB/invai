package es.caib.invai.back.persistence.repository.application.development.technology;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Optional filters layered on top of the mandatory {@code appDevelopmentId} scope: {@code statusId}
 * selects active ({@code deletedAt} null) or inactive rows, {@code appDevelopmentId} here re-applies
 * a (redundant) development scope, and {@code search} matches either an exact numeric id or a
 * case-insensitive substring of the linked technology's name. See
 * {@link AppTechnologySpecification#filterByCriteria} for the exact predicate logic.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppTechnologyCriteria {

    /**
     * Filter by state/status identifier (e.g., 1L for active records with deletedAt IS NULL).
     */
    private Long statusId;

    /**
     * Exact matching filter for the associated development module identifier.
     */
    private Long appDevelopmentId;

    /**
     * Free-text global search query pattern across numeric IDs and technology names.
     */
    private String search;
}
