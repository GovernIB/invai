package es.caib.invai.api.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * JPA persistent entity representing corporate Administrative Units within the inventory system.
 * Inherits fundamental structural audit capabilities and behavior hooks from {@link BaseEntity}.
 *
 * @since 1.0.0
 */
@Getter
@Setter
@Entity
@Table(name = "INV_ADM_UNIT")
public class AdmUnitEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_adm_unit_seq")
    @SequenceGenerator(name = "inv_adm_unit_seq", sequenceName = "INV_ADM_UNIT_SEQ", allocationSize = 1)
    @Column(name = "ADM_UNIT_ID")
    private Long id;

    @Column(name = "CODE", nullable = false, length = 50)
    private String code;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "NAME_ES", length = 100)
    private String nameEs;

}