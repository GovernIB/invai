package es.caib.invai.back.persistence.model.application.accessibility;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.maintenance.complianceSituation.ComplianceSituationEntity;
import es.caib.invai.back.persistence.model.maintenance.classificationSegment.ClassificationSegmentEntity;
import es.caib.invai.back.persistence.model.BaseEntity;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * Persistent entity mapping the "Accessibilitat" (WCAG / UNE-EN 301549) tab anchor
 * owned by a corporate {@link ApplicationEntity}.
 *
 * @since 1.0.4
 */
@Entity
@Table(name = "INV_APP_ACCESSIBILITY")
@Getter
@Setter
public class AppAccessibilityEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identification pointer of this accessibility anchor. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_ACCESSIBILITY_SEQ")
    @SequenceGenerator(name = "INV_APP_ACCESSIBILITY_SEQ", sequenceName = "INV_APP_ACCESSIBILITY_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Corporate application this accessibility anchor belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APPLICATION_ID", nullable = false)
    private ApplicationEntity application;

    /** Compliance situation status catalog entry (situacio de cumpliment). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPLIANCE_ID")
    private ComplianceSituationEntity compliance;

    /** Classification segment catalog entry (segment de classificacio). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLASSIFICATION_SEGMENT_ID")
    private ClassificationSegmentEntity classificationSegment;

    /** Public URL of the web portal. */
    @Column(name = "PUBLIC_URL", length = 255)
    private String publicUrl;

    /** Whether the application has an associated mobile application. */
    @Column(name = "MOBILE_APPLICATION")
    private Boolean mobileApplication;

    /** Name of the associated mobile application. */
    @Column(name = "MOBILE_APPLICATION_NAME", length = 255)
    private String mobileApplicationName;

    /** Non-accessible content and the justification for why it hasn't been fixed. */
    @Lob
    @Column(name = "NON_ACCESSIBLE_CONTENT")
    private String nonAccessibleContent;

    /** Relevant observations regarding accessibility. */
    @Lob
    @Column(name = "OBSERVATIONS")
    private String observations;

    /** End-of-validity date for this accessibility evaluation. */
    @Column(name = "EXPIRE_DATE")
    private LocalDateTime expireDate;
}
