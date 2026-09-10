package es.caib.invai.back.persistence.repository.application.security.ensClassification;

import lombok.*;

/**
 * Filter criteria DTO encapsulating query search parameters for dynamically constructing
 * JPA Specifications and filtering {@code AppEnsClassificationEntity} records.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppEnsClassificationCriteria {

    /**
     * Filter by state/status identifier (e.g., 1L for active records with deletedAt IS NULL).
     */
    private Long statusId;

    /**
     * Exact matching filter for the associated security anchor identifier.
     */
    private Long appSecurityId;

    /**
     * Exact matching filter for the associated identity provider identifier.
     */
    private Long identityProviderId;

    /**
     * Exact matching filter for the associated ENS subjection lookup identifier.
     */
    private Long ensSubjectId;

    /**
     * Exact matching filter for the associated personal data processing entry identifier.
     */
    private Long personalDataProcessingId;
}
