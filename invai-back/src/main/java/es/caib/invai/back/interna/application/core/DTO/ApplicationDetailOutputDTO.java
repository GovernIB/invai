package es.caib.invai.back.interna.application.core.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import es.caib.invai.back.interna.maintenance.general.commission.DTO.CommissionOutputDTO;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Complete outbound representation of an {@code Application}: everything in
 * {@link ApplicationOutputDTO} plus the commission, description, status, audit data, the
 * identifiers of the linked tabs and the completeness flags. Returned only by {@code getById}.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@NoArgsConstructor
public class ApplicationDetailOutputDTO extends ApplicationOutputDTO {

    /** The {@code Commission} overseeing this application. */
    private CommissionOutputDTO csCommission;

    /** Free-text description of the application. */
    private String description;

    /** Lifecycle status of the application (e.g. active/inactive). */
    private StatusEnum status;

    /** Date the application was soft-deleted, mirroring {@code deletedAt}; {@code null} while active. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate expirationDate;

    /** Date the application record was created. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate createdAt;

    /** Username of the user who created the application. */
    private String createdBy;

    /** Date the application record was last updated. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate updatedAt;

    /** Username of the user who last updated the application. */
    private String updatedBy;

    /** Foreign key identifier of the linked "AppInformationSystemDb" tab record. */
    private Long appInformationSystemDbId;

    /** Foreign key identifier of the linked "AppDevelopment" tab record. */
    private Long appDevelopmentId;

    /** Foreign key identifier of the linked "Responsables i Autoritzats" tab record. */
    private Long appResponsibleAuthorizedId;

    /** Foreign key identifier of the linked "Seguretat" tab record. */
    private Long appSecurityId;

    /** Foreign key identifier of the linked "Accessibilitat" tab record. */
    private Long appAccessibilityId;

    /**
     * {@code true} if any registered responsible type has no active holder on this application.
     */
    private Boolean missingResponsibleTypes;

    /** {@code true} if this application has no active authorized person at all. */
    private Boolean missingAuthorized;

    /** {@code true} if the "AppInformationSystemDb" anchor is missing, or has no active system link at all. */
    private Boolean missingSystems;

    /** {@code true} if the "AppInformationSystemDb" anchor is missing, or has no active database link at all. */
    private Boolean missingDatabases;

    /** {@code true} if the linked "AppDevelopment" record is missing, or has any of its required fields (all but {@code observation}) left blank. */
    private Boolean missingDevelopmentFields;

    /** {@code true} if the linked "AppAccessibility" record is missing, or has any of its required fields (all but {@code nonAccessibleContent} and {@code observations}) left blank. */
    private Boolean missingAccessibilityFields;

    /** {@code true} if the "AppSecurity" anchor is missing, or has no active record in any of its Web Context, ENS Classification, or Security Risk child tables. */
    private Boolean missingSecurityData;

    /** {@code true} if the "AppIntegration" anchor is missing, or has no active integration connection at all. */
    private Boolean missingIntegrationData;

    /** {@code true} if any tab has missing or incomplete data — i.e. what the 7 per-tab {@code missingXxx} flags above would OR together. */
    private boolean incomplete;

    /**
     * {@code true} if, as of the last bulk DIR3 audit run, at least one active, Personal CAIB
     * responsible/authorized assignment's DIR3 no longer matches this application's administrative
     * unit. A {@code MANUAL} assignment (a mismatch already reviewed and accepted by a {@code
     * ROLE_INV_SUPER} user) never counts toward this flag.
     */
    private boolean dir3Mismatch;

    /** Foreign key identifier of the linked "Open Data" tab record. */
    private Long appDataId;

    /** Foreign key identifier of the linked "Integration" tab record. */
    private Long appIntegrationId;
}
