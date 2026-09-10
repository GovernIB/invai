package es.caib.invai.back.interna.application.accessibility.DTO;

import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import es.caib.invai.back.interna.maintenance.complianceSituation.DTO.ComplianceSituationOutputDTO;
import es.caib.invai.back.interna.maintenance.classificationSegment.DTO.ClassificationSegmentOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Outbound representation of an accessibility anchor, with its compliance situation and
 * classification segment catalog references resolved to their nested DTOs. Carries no
 * completeness flag of its own — the "Accessibilitat" tab's missing/incomplete status is
 * computed by {@code ApplicationServiceFacadeBean#isAccessibilityIncomplete} and surfaced instead
 * on {@code ApplicationOutputDTO}.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppAccessibilityOutputDTO {

    /** Unique identification pointer of this accessibility anchor. */
    private Long id;
    /** Corporate application this accessibility anchor belongs to. */
    private ApplicationOutputDTO application;
    /** Compliance situation status catalog entry (situacio de cumpliment). */
    private ComplianceSituationOutputDTO compliance;
    /** Classification segment catalog entry (segment de classificacio). */
    private ClassificationSegmentOutputDTO classificationSegment;
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
    /** Timestamp at which this record was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}
