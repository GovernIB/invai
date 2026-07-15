package es.caib.invai.api.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA persistent entity representing an application definition profile asset context within the central repository registry.
 * Maps operational relationships onto metadata taxonomies, ownership nodes, and systemic lifecycle structures.
 *
 * @since 1.0.0
 */
@Getter
@Setter
@Entity
@Table(name = "INV_APPLICATION")
public class ApplicationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_application_seq")
    @SequenceGenerator(
            name = "inv_application_seq",
            sequenceName = "INV_APPLICATION_SEQ",
            allocationSize = 1
    )
    @Column(name = "APP_APPLICATION_ID", nullable = false, updatable = false)
    private Long id;

    @Column(name = "CODE", length = 10, nullable = false, unique = true)
    private String code;

    @Column(name = "PREFIX", length = 3, nullable = false, unique = true)
    private String prefix;

    @Column(name = "NAME", length = 255)
    private String name;

    @ManyToOne
    @JoinColumn(name = "CATEGORY_ID")
    private CategoryEntity category;

    @ManyToOne
    @JoinColumn(name = "SYSTEM_TYPE_ID")
    private SystemTypeEntity systemType;

    @ManyToOne
    @JoinColumn(name = "FIELD_ID")
    private FieldEntity field;

    @ManyToOne
    @JoinColumn(name = "ADM_UNIT_ID")
    private AdmUnitEntity admUnit;

    @ManyToOne
    @JoinColumn(name = "COMMISSION_ID")
    private CommissionEntity csCommission;

    @ManyToOne
    @JoinColumn(name = "STATUS_ID")
    private StatusEntity status;

    @Lob
    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "EXPIRATION_DATE")
    private LocalDateTime expirationDate;

}