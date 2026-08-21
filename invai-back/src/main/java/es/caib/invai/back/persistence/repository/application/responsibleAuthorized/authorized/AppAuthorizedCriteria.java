package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Filter criteria DTO encapsulating query search parameters for dynamically constructing
 * JPA Specifications and filtering {@code AppAuthorizedEntity} records.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppAuthorizedCriteria {

    /**
     * Filter by state/status identifier (e.g., 1L for active records with deletedAt IS NULL).
     */
    private Long statusId;

    /**
     * Exact matching filter for the associated person identifier.
     */
    private Long personId;

    /**
     * Free-text global search query pattern across the linked person's name and email.
     */
    private String search;
}
