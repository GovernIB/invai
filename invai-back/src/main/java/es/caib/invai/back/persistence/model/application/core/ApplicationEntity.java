package es.caib.invai.back.persistence.model.application.core;

import es.caib.invai.back.persistence.model.catalog.status.LkupStatusEntity;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.io.Serial;
import java.time.LocalDateTime;
import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.maintenance.general.category.CategoryEntity;
import es.caib.invai.back.persistence.model.maintenance.general.commission.CommissionEntity;
import es.caib.invai.back.persistence.model.maintenance.general.field.FieldEntity;
import es.caib.invai.back.persistence.model.maintenance.general.systemType.SystemTypeEntity;

/**
 * JPA persistent entity representing an application definition profile asset context within the central repository registry.
 * Maps operational relationships onto metadata taxonomies, ownership nodes, and systemic lifecycle structures.
 *
 * @since 1.0.1
 */
@Getter
@Setter
@Entity
@Table(name = "INV_APPLICATION")
public class ApplicationEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Primary sequence identifier of the application record. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_application_seq")
    @SequenceGenerator(
            name = "inv_application_seq",
            sequenceName = "INV_APPLICATION_SEQ",
            allocationSize = 1
    )
    @Column(name = "APP_APPLICATION_ID", nullable = false, updatable = false)
    private Long id;

    /** Unique administrative identification code of the application. */
    @Column(name = "CODE", length = 10, nullable = false, unique = true)
    private String code;

    /** Unique technical acronym prefix identifying the application. */
    @Column(name = "PREFIX", length = 3, nullable = false, unique = true)
    private String prefix;

    /** Descriptive name of the application. */
    @Column(name = "NAME")
    private String name;

    /** Taxonomy category the application is classified under. */
    @ManyToOne
    @JoinColumn(name = "CATEGORY_ID")
    private CategoryEntity category;

    /** Architectural system type the application is built upon. */
    @ManyToOne
    @JoinColumn(name = "SYSTEM_TYPE_ID")
    private SystemTypeEntity systemType;

    /** Functional business field the application belongs to. */
    @ManyToOne
    @JoinColumn(name = "FIELD_ID")
    private FieldEntity field;

    /** DIR3CAIB code of the administrative unit responsible for the application, resolved live against the external directory — no local FK. */
    @Column(name = "ADM_UNIT_CODE", length = 20)
    private String admUnitCode;

    /** Governance commission supervising the application. */
    @ManyToOne
    @JoinColumn(name = "COMMISSION_ID")
    private CommissionEntity csCommission;

    /** Current lifecycle status of the application. */
    @ManyToOne
    @JoinColumn(name = "STATUS_ID")
    private LkupStatusEntity status;

    /** Free-text description of the application's scope and purpose. */
    @Lob
    @Column(name = "DESCRIPTION")
    private String description;

    /** Date on which the application's lifecycle is scheduled to expire. */
    @Column(name = "EXPIRATION_DATE")
    private LocalDateTime expirationDate;
}