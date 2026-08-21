package es.caib.invai.back.persistence.model.application.responsibleAuthorized.authorized;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.core.AppResponsibleAuthorizedEntity;
import es.caib.invai.back.persistence.model.maintenance.responsible.person.PersonEntity;
import es.caib.invai.back.persistence.model.BaseEntity;

import java.io.Serial;

/**
 * JPA persistent anchor entity representing a person authorized on a given application's
 * "Responsables i Autoritzats" tab. At most one active anchor per (appResponsibleAuthorized, person)
 * pair is enforced at the service layer.
 *
 * @since 1.0.3
 */
@Entity
@Table(name = "INV_APP_AUTHORIZED")
@Getter
@Setter
public class AppAuthorizedEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Primary key unique identifier. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_AUTHORIZED_SEQ")
    @SequenceGenerator(name = "INV_APP_AUTHORIZED_SEQ", sequenceName = "INV_APP_AUTHORIZED_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Parent "Responsables i Autoritzats" anchor this authorization belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_RESPONSIBLE_AUTHORIZED_ID", nullable = false)
    private AppResponsibleAuthorizedEntity appResponsibleAuthorized;

    /** Authorized person. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSON_ID", nullable = false)
    private PersonEntity person;

    /** Free-text remarks about this authorization. */
    @Lob
    @Column(name = "OBSERVATION")
    private String observation;
}
