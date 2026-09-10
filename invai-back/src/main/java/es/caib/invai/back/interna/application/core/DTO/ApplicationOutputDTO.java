package es.caib.invai.back.interna.application.core.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import es.caib.invai.back.interna.integrations.dir3.admUnit.DTO.AdmUnitOutputDTO;
import es.caib.invai.back.interna.maintenance.general.category.DTO.CategoryOutputDTO;
import es.caib.invai.back.interna.maintenance.general.commission.DTO.CommissionOutputDTO;
import es.caib.invai.back.interna.maintenance.general.field.DTO.FieldOutputDTO;
import es.caib.invai.back.interna.maintenance.general.systemType.DTO.SystemTypeOutputDTO;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Outbound representation of an {@code Application}, returned by all of {@code getAll}, {@code
 * getById}, {@code create}, {@code update}, and {@code reactivate}.
 *
 * @since 1.0.1
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationOutputDTO {

    /** Primary key of the application. */
    private Long id;

    /** Unique alphanumeric code identifying the application. */
    private String code;

    /** Short acronym used to prefix identifiers generated for this application. */
    private String prefix;

    /** Display name of the application. */
    private String name;

    /** The {@code Category} this application is classified under. */
    private CategoryOutputDTO category;

    /** The {@code SystemType} (infrastructure/architecture classification) of this application. */
    private SystemTypeOutputDTO systemType;

    /** The {@code Field} (business domain) this application belongs to. */
    private FieldOutputDTO field;

    /**
     * Administrative unit responsible for the application, resolved live from DIR3CAIB by the
     * stored {@code admUnitCode} — only the code is persisted, not this object. {@code null} when
     * no admin unit is linked, or the linked code has no current DIR3CAIB match.
     */
    private AdmUnitOutputDTO admUnit;

    /**
     * Department (Conselleria) ancestor of {@link #admUnit} in the DIR3CAIB hierarchy, derived
     * live at fetch time rather than stored — only the {@code admUnit} reference is persisted, so a
     * government reorganization is reflected immediately without any data migration. {@code null}
     * when {@code admUnit} is unset, has no DIR3CAIB match, or has no department-level ancestor.
     * Only populated on the {@code getById} response; {@code create}/{@code update}/{@code
     * reactivate}/{@code getAll} leave it {@code null}.
     */
    private AdmUnitOutputDTO department;

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
     * Per-tab detail only: populated on {@code getById}, left {@code null} on {@code getAll} — a
     * list row never computes any single tab's status, only the aggregate {@link #incomplete}.
     */
    private Boolean missingResponsibleTypes;

    /** {@code true} if this application has no active authorized person at all. Per-tab detail only — see {@link #missingResponsibleTypes}. */
    private Boolean missingAuthorized;

    /** {@code true} if the "AppInformationSystemDb" anchor is missing, or has no active system link at all. Per-tab detail only — see {@link #missingResponsibleTypes}. */
    private Boolean missingSystems;

    /** {@code true} if the "AppInformationSystemDb" anchor is missing, or has no active database link at all. Per-tab detail only — see {@link #missingResponsibleTypes}. */
    private Boolean missingDatabases;

    /** {@code true} if the linked "AppDevelopment" record is missing, or has any of its required fields (all but {@code observation}) left blank. Per-tab detail only — see {@link #missingResponsibleTypes}. */
    private Boolean missingDevelopmentFields;

    /** {@code true} if the linked "AppAccessibility" record is missing, or has any of its required fields (all but {@code nonAccessibleContent} and {@code observations}) left blank. Per-tab detail only — see {@link #missingResponsibleTypes}. */
    private Boolean missingAccessibilityFields;

    /** {@code true} if the "AppSecurity" anchor is missing, or has no active record in any of its Web Context, ENS Classification, or Security Risk child tables. Per-tab detail only — see {@link #missingResponsibleTypes}. */
    private Boolean missingSecurityData;

    /** {@code true} if any tab has missing or incomplete data — i.e. what the 7 per-tab {@code missingXxx} flags above would OR together. Unlike those, always populated, both here and in list results. */
    private boolean incomplete;
}