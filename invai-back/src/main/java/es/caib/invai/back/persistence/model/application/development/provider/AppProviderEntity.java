package es.caib.invai.back.persistence.model.application.development.provider;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.io.Serial;
import java.time.LocalDateTime;
import es.caib.invai.back.persistence.model.application.development.core.AppDevelopmentEntity;
import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.maintenance.development.role.RoleEntity;

/**
 * Persistent entity mapping a corporate provider (company) assigned to a specific
 * application development module.
 *
 * @since 1.0.2
 */
@Entity
@Table(name = "INV_APP_PROVIDER")
@Getter
@Setter
public class AppProviderEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Primary key of this provider record. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_PROVIDER_SEQ")
    @SequenceGenerator(name = "INV_APP_PROVIDER_SEQ", sequenceName = "INV_APP_PROVIDER_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Parent development module this provider is assigned to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_DEVELOPMENT_ID", nullable = false)
    private AppDevelopmentEntity appDevelopment;

    /** Corporate name of the provider company. */
    @Column(name = "COMPANY_NAME", nullable = false)
    private String companyName;

    /** Provider's role catalog entry. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROLE")
    private RoleEntity role;

    /** Start date for the provider service contract. */
    @Column(name = "START_DATE")
    private LocalDateTime startDate;

    /** End date for the provider service contract. */
    @Column(name = "EXPIRE_DATE")
    private LocalDateTime expireDate;
}
