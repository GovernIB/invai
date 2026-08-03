package es.caib.invai.api.persistence.model.catalog;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * JPA persistent entity representing GOIB standards compliance level lookup constants.
 * Acts as a static reference dictionary and is not exposed through its own CRUD endpoints.
 *
 * @since 1.0.2
 */
@Entity
@Table(name = "INV_LKUP_STANDARD_ADAPTION")
@Getter
@Setter
public class LkupStandardAdaptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_LKUP_STANDARD_ADAPTION_SEQ")
    @SequenceGenerator(name = "INV_LKUP_STANDARD_ADAPTION_SEQ", sequenceName = "INV_LKUP_STANDARD_ADAPTION_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "NAME_ES", nullable = false)
    private String nameEs;
}
