package es.caib.invai.back.persistence.repository.application.security.webContext;

import lombok.*;

/**
 * Filter criteria DTO encapsulating query search parameters for dynamically constructing
 * JPA Specifications and filtering {@code AppWebContextEntity} records.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppWebContextCriteria {

    /**
     * Filter by state/status identifier (e.g., 1L for active records with deletedAt IS NULL).
     */
    private Long statusId;

    /**
     * Exact matching filter for the associated security anchor identifier.
     */
    private Long appSecurityId;

    /**
     * Exact matching filter for the associated web context identifier.
     */
    private Long webContextId;

    /**
     * Exact matching filter for the associated functional field identifier.
     */
    private Long fieldId;

    /**
     * Free-text global search query pattern across numeric IDs and attributes.
     */
    private String search;
}
