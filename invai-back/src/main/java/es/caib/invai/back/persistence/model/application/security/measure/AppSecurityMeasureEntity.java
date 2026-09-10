package es.caib.invai.back.persistence.model.application.security.measure;

import lombok.*;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.application.security.core.AppSecurityEntity;
import es.caib.invai.back.persistence.model.maintenance.security.securityMeasureType.SecurityMeasureTypeEntity;
import es.caib.invai.back.persistence.model.maintenance.security.ensRequirement.EnsRequirementEntity;

import java.io.Serial;

/**
 * Persistent entity mapping a security measure applied to an {@link AppSecurityEntity}
 * anchor record.
 *
 * @since 1.0.4
 */
@Entity
@Table(name = "INV_APP_SECURITY_MEASURE")
@Getter
@Setter
public class AppSecurityMeasureEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identification pointer of this security measure. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_SECURITY_MEASURE_SEQ")
    @SequenceGenerator(name = "INV_APP_SECURITY_MEASURE_SEQ", sequenceName = "INV_APP_SECURITY_MEASURE_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Security anchor this measure belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_SECURITY_ID", nullable = false)
    private AppSecurityEntity appSecurity;

    /** Security measure type catalog entry classifying this measure. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TYPE_ID")
    private SecurityMeasureTypeEntity type;

    /** ENS requirement catalog entry this measure is associated with. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ENS_REQUIREMENT_ID")
    private EnsRequirementEntity ensRequirement;

    /** Free-text description of the applied security measure. */
    @Lob
    @Column(name = "DESCRIPTION")
    private String description;
}
