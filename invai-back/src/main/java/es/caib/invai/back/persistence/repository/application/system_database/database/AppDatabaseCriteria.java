package es.caib.invai.back.persistence.repository.application.system_database.database;

import lombok.*;

/**
 * Filter criteria DTO encapsulating query search parameters for dynamically constructing
 * JPA Specifications and filtering {@code AppDatabaseEntity} records.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppDatabaseCriteria {

    /**
     * Filter by state/status identifier (e.g., 1L for active records with deletedAt IS NULL).
     */
    private Long statusId;

    /**
     * Exact matching filter for the associated information system database grouping identifier.
     */
    private Long informationSystemDbId;

    /**
     * Exact matching filter for the associated database identifier.
     */
    private Long databaseId;

    /**
     * Free-text global search query pattern across numeric IDs and attributes.
     */
    private String search;
}
