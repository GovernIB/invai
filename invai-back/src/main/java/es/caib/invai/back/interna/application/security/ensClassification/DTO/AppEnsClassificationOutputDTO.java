package es.caib.invai.back.interna.application.security.ensClassification.DTO;

import es.caib.invai.back.interna.application.security.core.DTO.AppSecurityOutputDTO;
import es.caib.invai.back.interna.maintenance.security.identityProvider.DTO.IdentityProviderOutputDTO;
import es.caib.invai.back.interna.catalog.ensSubject.DTO.EnsSubjectOutputDTO;
import es.caib.invai.back.interna.maintenance.security.personalDataProcessing.DTO.PersonalDataProcessingOutputDTO;
import es.caib.invai.back.interna.catalog.securityLevel.DTO.SecurityLevelOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Outbound transport payload detailing technical context mappings of the target
 * ENS classification entity definitions.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppEnsClassificationOutputDTO {

    /** Unique identification pointer of this ENS classification record. */
    private Long id;
    /** Security anchor this ENS classification belongs to. */
    private AppSecurityOutputDTO appSecurity;
    /** Identity provider used by the application, if applicable. */
    private IdentityProviderOutputDTO identityProvider;
    /** ENS subjection lookup entry applicable to the application. */
    private EnsSubjectOutputDTO ensSubject;
    /** Personal data processing entry applicable to the application. */
    private PersonalDataProcessingOutputDTO personalDataProcessing;
    /** Date on which the ENS classification was approved. */
    private LocalDateTime approvalDate;
    /** Confidentiality security level. */
    private SecurityLevelOutputDTO confidentiality;
    /** Integrity security level. */
    private SecurityLevelOutputDTO integrity;
    /** Traceability security level. */
    private SecurityLevelOutputDTO traceability;
    /** Availability security level. */
    private SecurityLevelOutputDTO availability;
    /** Authenticity security level. */
    private SecurityLevelOutputDTO authenticity;
    /** Calculated overall ENS adequacy grade. */
    private SecurityLevelOutputDTO overallGrade;
    /** Timestamp at which this record was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}
