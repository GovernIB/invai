package es.caib.invai.api.persistence.repository.application.system_database.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Filter criteria DTO encapsulating query search parameters for dynamically constructing
 * JPA Specifications and filtering {@code AppSystemEntity} records.
 *
 * @since 1.0.2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppSystemCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Filter by state/status identifier (e.g., 1L for active records with deletedAt IS NULL).
     */
    private Long statusId;

    /**
     * Exact matching filter for the associated information system database grouping identifier.
     */
    private Long informationSystemDbId;

    /**
     * Exact matching filter for the associated system identifier.
     */
    private Long systemId;

    /**
     * Free-text global search query pattern across numeric IDs and attributes.
     */
    private String search;
}
