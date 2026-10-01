package es.caib.invai.back.persistence.repository.application.integration.connection;

import lombok.*;

/**
 * Filter criteria DTO encapsulating query search parameters for dynamically constructing
 * JPA Specifications and filtering {@code AppIntegrationConnectionEntity} records.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppIntegrationConnectionCriteria {

    /** Filter by state/status identifier (e.g., 1L for active records with deletedAt IS NULL). */
    private Long statusId;

    /** Exact matching filter for the referenced inventory application identifier. */
    private Long applicationId;

    /** Exact matching filter for the referenced external system identifier. */
    private Long externalSystemId;

    /** Exact matching filter for the referenced technology identifier. */
    private Long technologyId;
}
