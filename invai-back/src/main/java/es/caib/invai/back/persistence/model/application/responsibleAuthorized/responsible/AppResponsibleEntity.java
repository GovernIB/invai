package es.caib.invai.back.persistence.model.application.responsibleAuthorized.responsible;

import lombok.*;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.core.AppResponsibleAuthorizedEntity;
import es.caib.invai.back.persistence.model.maintenance.responsible.person.PersonEntity;
import es.caib.invai.back.persistence.model.catalog.responsibleType.LkupResponsibleTypeEntity;

import java.io.Serial;

/**
 * JPA entity linking an {@link AppResponsibleAuthorizedEntity} tab anchor with a {@link PersonEntity} through a
 * {@link LkupResponsibleTypeEntity} assignment, mapped onto the {@code INV_APP_RESPONSIBLE} table.
 *
 * @since 1.0.3
 */
@Entity
@Table(name = "INV_APP_RESPONSIBLE")
@Getter
@Setter
public class AppResponsibleEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Primary key unique identifier. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_RESPONSIBLE_SEQ")
    @SequenceGenerator(name = "INV_APP_RESPONSIBLE_SEQ", sequenceName = "INV_APP_RESPONSIBLE_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Parent "Responsables i Autoritzats" anchor this responsible assignment belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_RESPONSIBLE_AUTHORIZED_ID", nullable = false)
    private AppResponsibleAuthorizedEntity appResponsibleAuthorized;

    /** Responsible person. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSON_ID", nullable = false)
    private PersonEntity person;

    /** Responsibility type catalog value assigned to the person. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESPONSIBLE_TYPE_ID", nullable = false)
    private LkupResponsibleTypeEntity responsibleType;

    /** Free-text job title/position held by the responsible person for this assignment. */
    @Column(name = "JOB_TITLE")
    private String jobTitle;

    /** Free-text remarks about this responsible assignment. */
    @Lob
    @Column(name = "OBSERVATION")
    private String observation;
}
