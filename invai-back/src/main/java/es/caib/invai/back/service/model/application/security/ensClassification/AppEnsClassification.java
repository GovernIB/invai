package es.caib.invai.back.service.model.application.security.ensClassification;

import lombok.*;

import java.time.LocalDateTime;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
import es.caib.invai.back.service.model.maintenance.security.identityProvider.IdentityProvider;
import es.caib.invai.back.service.model.catalog.ensSubject.EnsSubject;
import es.caib.invai.back.service.model.maintenance.security.personalDataProcessing.PersonalDataProcessing;
import es.caib.invai.back.service.model.catalog.securityLevel.SecurityLevel;

/**
 * Domain model representing an application's ENS ("Esquema Nacional de Seguretat")
 * classification, hanging off a parent {@link AppSecurity} anchor.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppEnsClassification {
    /** Unique identification pointer of this ENS classification record. */
    private Long id;
    /** Security anchor this ENS classification belongs to. */
    private AppSecurity appSecurity;
    /** Identity provider used by the application, if applicable. */
    private IdentityProvider identityProvider;
    /** ENS subjection lookup entry applicable to the application. */
    private EnsSubject ensSubject;
    /** Personal data processing entry applicable to the application. */
    private PersonalDataProcessing personalDataProcessing;
    /** Date on which the ENS classification was approved ("Data d'aprovació"). */
    private LocalDateTime approvalDate;
    /** Confidentiality security level. */
    private SecurityLevel confidentiality;
    /** Integrity security level. */
    private SecurityLevel integrity;
    /** Traceability security level. */
    private SecurityLevel traceability;
    /** Availability security level. */
    private SecurityLevel availability;
    /** Authenticity security level. */
    private SecurityLevel authenticity;
    /** Calculated overall ENS adequacy grade ("Grau d'adequació al ENS"). */
    private SecurityLevel overallGrade;
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
