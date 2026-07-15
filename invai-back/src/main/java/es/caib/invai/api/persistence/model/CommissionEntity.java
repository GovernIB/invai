package es.caib.invai.api.persistence.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;

/**
 * JPA persistent entity representing working groups or technical Commissions.
 * Inherits fundamental transactional audit attributes and automatic timeline tracking
 * behaviors from {@link BaseEntity}.
 *
 * @since 1.0.0
 */
@Getter
@Setter
@Entity
@Table(name = "INV_COMMISSION")
public class CommissionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_commission_seq")
    @SequenceGenerator(name = "inv_commission_seq", sequenceName = "INV_COMMISSION_SEQ", allocationSize = 1)
    @Column(name = "COMMISSION_ID")
    private Long id;

    @Column(name = "NAME", length = 100)
    private String name;

    @Column(name = "NAME_ES", length = 100)
    private String nameEs;
}