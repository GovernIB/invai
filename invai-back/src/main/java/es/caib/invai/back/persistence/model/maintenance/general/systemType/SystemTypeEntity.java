package es.caib.invai.back.persistence.model.maintenance.general.systemType;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;

import java.io.Serial;

/**
 * JPA persistent entity representing technical architectural System Types (e.g., REST Microservices, Web Apps).
 * Extends {@link BaseEntity} to enable native tracking vectors, timestamps, and audit structures.
 *
 * @since 1.0.1
 */
@Entity
@Table(name = "INV_SYSTEM_TYPE")
@Getter
@Setter
public class SystemTypeEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique auto-generated primary key identifying the system type record. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_system_type_seq")
    @SequenceGenerator(name = "inv_system_type_seq", sequenceName = "INV_SYSTEM_TYPE_SEQ", allocationSize = 1)
    @Column(name = "SYSTEM_TYPE_ID")
    private Long id;

    /** The Catalan descriptive name of the system type. */
    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    /** The Spanish descriptive name of the system type. */
    @Column(name = "NAME_ES", length = 100)
    private String nameEs;
}