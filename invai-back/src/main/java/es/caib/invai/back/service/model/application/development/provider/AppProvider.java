package es.caib.invai.back.service.model.application.development.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import es.caib.invai.back.service.model.maintenance.development.role.Role;

/**
 * Domain aggregate model representing a corporate provider (company) assigned to a
 * specific {@link AppDevelopment} module.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppProvider {
    /** Primary key of this provider record. */
    private Long id;
    /** Parent development module this provider is assigned to. */
    private AppDevelopment appDevelopment;
    /** Corporate name of the provider company. */
    private String companyName;
    /** Provider's role catalog entry. */
    private Role role;
    /** Start date for the provider service contract. */
    private LocalDateTime startDate;
    /** End date for the provider service contract. */
    private LocalDateTime expireDate;
    /** Timestamp when the record was created. */
    private LocalDateTime createdAt;
    /** Username of the user who created the record. */
    private String createdBy;
    /** Timestamp of the last update to the record. */
    private LocalDateTime updatedAt;
    /** Username of the user who last updated the record. */
    private String updatedBy;
    /** Timestamp when the record was logically deleted, or null if still active. */
    private LocalDateTime deletedAt;
    /** Username of the user who logically deleted the record. */
    private String deletedBy;
}
