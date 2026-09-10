package es.caib.invai.back.service.model.application.accessibility;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.maintenance.complianceSituation.ComplianceSituation;
import es.caib.invai.back.service.model.maintenance.classificationSegment.ClassificationSegment;

/**
 * Domain model representing the "Accessibilitat" tab anchor owned by an
 * {@link Application}. {@code ApplicationServiceFacadeBean#isAccessibilityIncomplete} treats this
 * record as incomplete when {@code classificationSegment}, {@code compliance}, {@code
 * publicUrl}, {@code mobileApplication}, or {@code expireDate} is {@code null}, or when {@code
 * mobileApplication} is {@code true} but {@code mobileApplicationName} is {@code null}; {@code
 * nonAccessibleContent} and {@code observations} are never required. Like its
 * AppDevelopment/AppSecurity/AppInformationSystemDb/AppResponsibleAuthorized counterparts, this
 * anchor is auto-created when the owning {@code Application} is created, so it should only be
 * {@code null} for applications created before this anchor was introduced.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppAccessibility {
    /** Unique identification pointer of this accessibility anchor. */
    private Long id;
    /** Corporate application this accessibility anchor belongs to. */
    private Application application;
    /** Compliance situation status catalog entry (situacio de cumpliment). */
    private ComplianceSituation compliance;
    /** Classification segment catalog entry (segment de classificacio). */
    private ClassificationSegment classificationSegment;
    /** Public URL of the web portal. */
    private String publicUrl;
    /** Whether the application has an associated mobile application. */
    private Boolean mobileApplication;
    /** Name of the associated mobile application. */
    private String mobileApplicationName;
    /** Non-accessible content and the justification for why it hasn't been fixed. */
    private String nonAccessibleContent;
    /** Relevant observations regarding accessibility. */
    private String observations;
    /** End-of-validity date for this accessibility evaluation. */
    private LocalDateTime expireDate;
    /** Timestamp at which this record was created. */
    private LocalDateTime createdAt;
    /** Identifier of the user who created this record. */
    private String createdBy;
    /** Timestamp at which this record was last updated. */
    private LocalDateTime updatedAt;
    /** Identifier of the user who last updated this record. */
    private String updatedBy;
    /** Timestamp at which this record was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
    /** Identifier of the user who logically deleted this record, or {@code null} if still active. */
    private String deletedBy;
}
