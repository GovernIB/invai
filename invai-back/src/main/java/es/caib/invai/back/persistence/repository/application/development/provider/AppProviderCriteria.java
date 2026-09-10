package es.caib.invai.back.persistence.repository.application.development.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Filter parameters accepted by the AppProvider list endpoint, consumed by
 * {@link AppProviderSpecification#filterByCriteria}: {@code statusId} switches between active
 * ({@code deletedAt IS NULL}) and soft-deleted rows, {@code appDevelopmentId} adds a redundant
 * equality check on top of the mandatory path-parameter scope, and {@code search} matches either
 * an exact numeric id or a case-insensitive {@code companyName} substring.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppProviderCriteria {

    /**
     * Filter by state/status identifier (e.g., 1L for active records with deletedAt IS NULL).
     */
    private Long statusId;

    /**
     * Exact matching filter for the associated development module identifier.
     */
    private Long appDevelopmentId;

    /**
     * Free-text global search query pattern across numeric IDs and company names.
     */
    private String search;
}
