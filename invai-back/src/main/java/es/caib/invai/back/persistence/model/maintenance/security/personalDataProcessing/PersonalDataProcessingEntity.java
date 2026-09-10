package es.caib.invai.back.persistence.model.maintenance.security.personalDataProcessing;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;

import java.io.Serial;

/**
 * JPA persistent entity representing personal data processing entries assignable within the Manteniments / Seguretat catalog.
 * Implements historical auditing field inheritances by extending {@link BaseEntity}.
 *
 * @since 1.0.4
 */
@Entity
@Table(name = "INV_PERSONAL_DATA_PROCESSING")
@Getter
@Setter
public class PersonalDataProcessingEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique auto-generated primary key of the personal data processing entry. */
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_pers_data_proc_seq")
    @SequenceGenerator(
            name = "inv_pers_data_proc_seq",
            sequenceName = "INV_PERS_DATA_PROC_SEQ",
            allocationSize = 1
    )
    private Long id;

    /** Personal data processing entry descriptive label string (e.g. "Firmar peticiones"). */
    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    /** Spanish localized descriptive name of the personal data processing entry. */
    @Column(name = "NAME_ES", length = 100)
    private String nameEs;
}
