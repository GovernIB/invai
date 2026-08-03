package es.caib.invai.api.persistence.model;

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
import java.time.LocalDateTime;

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

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_PROVIDER_SEQ")
    @SequenceGenerator(name = "INV_APP_PROVIDER_SEQ", sequenceName = "INV_APP_PROVIDER_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_DEVELOPMENT_ID", nullable = false)
    private DevelopmentEntity appDevelopment;

    @Column(name = "COMPANY_NAME", nullable = false)
    private String companyName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROLE")
    private RoleEntity role;

    @Column(name = "START_DATE")
    private LocalDateTime startDate;

    @Column(name = "EXPIRE_DATE")
    private LocalDateTime expireDate;
}
