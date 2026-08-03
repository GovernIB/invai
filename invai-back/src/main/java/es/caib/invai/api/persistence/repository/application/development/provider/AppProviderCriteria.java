package es.caib.invai.api.persistence.repository.application.development.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Filter criteria DTO encapsulating query search parameters for dynamically constructing
 * JPA Specifications and filtering {@code ProviderEntity} records.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppProviderCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

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
