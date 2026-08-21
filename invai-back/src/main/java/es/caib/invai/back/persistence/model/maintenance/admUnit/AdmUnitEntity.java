package es.caib.invai.back.persistence.model.maintenance.admUnit;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;

/**
 * JPA persistent entity representing corporate Administrative Units within the inventory system.
 * Inherits fundamental structural audit capabilities and behavior hooks from {@link BaseEntity}.
 *
 * @since 1.0.1
 */
@Getter
@Setter
@Entity
@Table(name = "INV_ADM_UNIT")
public class AdmUnitEntity extends BaseEntity {

    /** Persistent database primary key identifier, generated from the {@code INV_ADM_UNIT_SEQ} sequence. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_adm_unit_seq")
    @SequenceGenerator(name = "inv_adm_unit_seq", sequenceName = "INV_ADM_UNIT_SEQ", allocationSize = 1)
    @Column(name = "ADM_UNIT_ID")
    private Long id;

    /** Unique corporate alphanumeric code identifying the administrative unit. */
    @Column(name = "CODE", nullable = false, length = 50)
    private String code;

    /** Official localized descriptor name of the administrative unit (Catalan). */
    @Column(name = "NAME", nullable = false)
    private String name;

    /** Translated Spanish localized descriptor name of the administrative unit. */
    @Column(name = "NAME_ES", length = 100)
    private String nameEs;

}