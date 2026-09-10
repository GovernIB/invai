package es.caib.invai.back.interna.application.security.ensClassification.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Inbound validation data transport contract containing properties required for
 * instantiation and mutation of ENS classification records. Only the parent security
 * anchor identifier is mandatory: every other field is meant to be filled in gradually.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppEnsClassificationInputDTO {

    /** Foreign key unique identification pointer referencing the parent security anchor. */
    @NotNull(message = "{validation.appensclassification.appSecurityId}")
    private Long appSecurityId;

    /** Foreign key unique identification pointer referencing the identity provider. */
    private Long identityProviderId;

    /** Foreign key unique identification pointer referencing the ENS subjection lookup entry. */
    private Long ensSubjectId;

    /** Foreign key unique identification pointer referencing the personal data processing entry. */
    private Long personalDataProcessingId;

    /** Date on which the ENS classification was approved. */
    private LocalDateTime approvalDate;

    /** Foreign key unique identification pointer referencing the confidentiality security level. */
    private Long confidentialityId;

    /** Foreign key unique identification pointer referencing the integrity security level. */
    private Long integrityId;

    /** Foreign key unique identification pointer referencing the traceability security level. */
    private Long traceabilityId;

    /** Foreign key unique identification pointer referencing the availability security level. */
    private Long availabilityId;

    /** Foreign key unique identification pointer referencing the authenticity security level. */
    private Long authenticityId;

    /** Foreign key unique identification pointer referencing the overall ENS adequacy grade security level. */
    private Long overallGradeId;
}
