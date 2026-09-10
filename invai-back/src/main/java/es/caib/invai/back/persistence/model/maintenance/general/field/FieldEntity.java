package es.caib.invai.back.persistence.model.maintenance.general.field;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;

import java.io.Serial;

/**
 * JPA persistent entity representing operational business Fields or structural areas of expertise.
 * Inherits standard record validation workflows and automatic interceptor hooks from {@link BaseEntity}.
 *
 * @since 1.0.1
 */
@Getter
@Setter
@Entity
@Table(name = "INV_FIELD")
public class FieldEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Primary key unique identifier. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_field_seq")
    @SequenceGenerator(name = "inv_field_seq", sequenceName = "INV_FIELD_SEQ", allocationSize = 1)
    @Column(name = "FIELD_ID")
    private Long id;

    /** Field name (Catalan). */
    @Column(name = "NAME", nullable = false)
    private String name;

    /** Field name in Spanish. */
    @Column(name = "NAME_ES", length = 100)
    private String nameEs;
}