package es.caib.invai.api.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * JPA persistent entity representing operational business Fields or structural areas of expertise.
 * Inherits standard record validation workflows and automatic interceptor hooks from {@link BaseEntity}.
 *
 * @since 1.0.0
 */
@Getter
@Setter
@Entity
@Table(name = "INV_FIELD")
public class FieldEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_field_seq")
    @SequenceGenerator(name = "inv_field_seq", sequenceName = "INV_FIELD_SEQ", allocationSize = 1)
    @Column(name = "FIELD_ID")
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "NAME_ES", length = 100)
    private String nameEs;
}