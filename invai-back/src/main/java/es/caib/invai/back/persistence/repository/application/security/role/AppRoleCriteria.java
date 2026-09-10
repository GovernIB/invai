package es.caib.invai.back.persistence.repository.application.security.role;

import lombok.*;

/**
 * Filter criteria DTO encapsulating query search parameters for dynamically constructing
 * JPA Specifications and filtering {@code AppRoleEntity} records.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppRoleCriteria {

    /**
     * Filter by state/status identifier (e.g., 1L for active records with deletedAt IS NULL).
     */
    private Long statusId;

    /**
     * Exact matching filter for the associated security anchor identifier.
     */
    private Long appSecurityId;

    /**
     * Exact matching filter for the associated security role identifier.
     */
    private Long securityRoleId;

    /**
     * Case-insensitive partial-match filter against the assigned security role's name.
     */
    private String securityRoleName;

    /**
     * Case-insensitive partial-match filter against the Soffid system (environment) the
     * assigned security role is associated with.
     */
    private String system;

    /**
     * Free-text global search query pattern across numeric IDs and attributes.
     */
    private String search;
}
