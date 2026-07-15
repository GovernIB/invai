package es.caib.invai.api.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * JPA persistent entity representing technical architectural System Types (e.g., REST Microservices, Web Apps).
 * Extends {@link BaseEntity} to enable native tracking vectors, timestamps, and audit structures.
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "INV_SYSTEM_TYPE")
@Getter
@Setter
public class SystemTypeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_system_type_seq")
    @SequenceGenerator(name = "inv_system_type_seq", sequenceName = "INV_SYSTEM_TYPE_SEQ", allocationSize = 1)
    @Column(name = "SYSTEM_TYPE_ID")
    private Long id;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "NAME_ES", length = 100)
    private String nameEs;
}