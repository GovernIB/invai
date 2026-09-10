package es.caib.invai.back.persistence.model.application.security.risk;

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
import es.caib.invai.back.persistence.model.application.security.core.AppSecurityEntity;
import es.caib.invai.back.persistence.model.catalog.securityLevel.LkupSecurityLevelEntity;
import es.caib.invai.back.persistence.model.maintenance.general.field.FieldEntity;
import es.caib.invai.back.persistence.model.BaseEntity;

import java.io.Serial;

/**
 * Persistent entity mapping a security risk identified for an {@link AppSecurityEntity}
 * anchor record.
 *
 * @since 1.0.4
 */
@Entity
@Table(name = "INV_APP_SECURITY_RISK")
@Getter
@Setter
public class AppSecurityRiskEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identification pointer of this security risk. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_SECURITY_RISK_SEQ")
    @SequenceGenerator(name = "INV_APP_SECURITY_RISK_SEQ", sequenceName = "INV_APP_SECURITY_RISK_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Security anchor this risk belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_SECURITY_ID", nullable = false)
    private AppSecurityEntity appSecurity;

    /** Security level lookup classifying the severity of this risk. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LEVEL_ID")
    private LkupSecurityLevelEntity level;

    /** Free-text description of this security risk. */
    @Lob
    @Column(name = "DESCRIPTION")
    private String description;

    /** Functional business field ("Àmbit") this risk is associated with. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FIELD_ID")
    private FieldEntity field;
}
