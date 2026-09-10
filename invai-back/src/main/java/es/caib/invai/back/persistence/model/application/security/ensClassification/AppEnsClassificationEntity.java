package es.caib.invai.back.persistence.model.application.security.ensClassification;

import lombok.*;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.application.security.core.AppSecurityEntity;
import es.caib.invai.back.persistence.model.maintenance.security.identityProvider.IdentityProviderEntity;
import es.caib.invai.back.persistence.model.catalog.ensSubject.LkupEnsSubjectEntity;
import es.caib.invai.back.persistence.model.maintenance.security.personalDataProcessing.PersonalDataProcessingEntity;
import es.caib.invai.back.persistence.model.catalog.securityLevel.LkupSecurityLevelEntity;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * Persistent entity recording an application's ENS ("Esquema Nacional de Seguretat")
 * classification, hanging off a parent {@link AppSecurityEntity} anchor. Every field except
 * the parent anchor is optional and is meant to be filled in gradually over time.
 *
 * @since 1.0.4
 */
@Entity
@Table(name = "INV_APP_ENS_CLASSIFICATION")
@Getter
@Setter
public class AppEnsClassificationEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identification pointer of this ENS classification record. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_ENS_CLASSIFICATION_SEQ")
    @SequenceGenerator(name = "INV_APP_ENS_CLASSIFICATION_SEQ", sequenceName = "INV_APP_ENS_CLASSIFICATION_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Security anchor this ENS classification belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_SECURITY_ID", nullable = false)
    private AppSecurityEntity appSecurity;

    /** Identity provider used by the application, if applicable. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDENTITY_PROVIDER_ID")
    private IdentityProviderEntity identityProvider;

    /** ENS subjection lookup entry applicable to the application. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ENS_SUBJECT_ID")
    private LkupEnsSubjectEntity ensSubject;

    /** Personal data processing entry applicable to the application. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSONAL_DATA_PROCESSING_ID")
    private PersonalDataProcessingEntity personalDataProcessing;

    /** Date on which the ENS classification was approved ("Data d'aprovació"). */
    @Column(name = "APPROVAL_DATE")
    private LocalDateTime approvalDate;

    /** Confidentiality security level. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONFIDENTIALITY_LEVEL_ID")
    private LkupSecurityLevelEntity confidentiality;

    /** Integrity security level. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INTEGRITY_LEVEL_ID")
    private LkupSecurityLevelEntity integrity;

    /** Traceability security level. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRACEABILITY_LEVEL_ID")
    private LkupSecurityLevelEntity traceability;

    /** Availability security level. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AVAILABILITY_LEVEL_ID")
    private LkupSecurityLevelEntity availability;

    /** Authenticity security level. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AUTHENTICITY_LEVEL_ID")
    private LkupSecurityLevelEntity authenticity;

    /** Calculated overall ENS adequacy grade ("Grau d'adequació al ENS"), stored as a plain column. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OVERALL_GRADE_LEVEL_ID")
    private LkupSecurityLevelEntity overallGrade;
}
