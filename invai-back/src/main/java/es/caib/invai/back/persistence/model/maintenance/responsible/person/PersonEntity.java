package es.caib.invai.back.persistence.model.maintenance.responsible.person;

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
import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.maintenance.responsible.company.CompanyEntity;

import java.io.Serial;

/**
 * JPA entity mapping person records from the {@code INV_PERSON} table, each one linked to a company.
 *
 * @since 1.0.3
 */
@Entity
@Table(name = "INV_PERSON")
@Getter
@Setter
public class PersonEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Primary persistent storage unique sequence row reference. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_PERSON_SEQ")
    @SequenceGenerator(name = "INV_PERSON_SEQ", sequenceName = "INV_PERSON_SEQ", allocationSize = 1)
    @Column(name = "ID", unique = true, nullable = false)
    private Long id;

    /** Company this person is associated with. Nullable when {@link #personalCaib} is {@code true}. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPANY_ID")
    private CompanyEntity company;

    /** Person first name or identification label. */
    @Column(name = "FIRST_NAME", nullable = false, length = 150)
    private String firstName;

    /** Person last name(s). */
    @Column(name = "LAST_NAME", nullable = false, length = 150)
    private String lastName;

    /** Contact e-mail address of the person. */
    @Column(name = "EMAIL", nullable = false, length = 150)
    private String email;

    /** Whether this person is CAIB staff (sourced via the future DIR3 integration), rather than an external company contact. */
    @Column(name = "IS_PERSONAL_CAIB", nullable = false)
    private boolean personalCaib;
}
