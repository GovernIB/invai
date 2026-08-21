package es.caib.invai.back.persistence.model.maintenance.systems.serverType;

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
 * JPA persistent entity representing server type lookup constants (e.g. DATABASE, APPLICATION).
 * Acts as a static reference dictionary and is not exposed through its own CRUD endpoints.
 *
 * @since 1.0.2
 */
@Entity
@Table(name = "INV_LKUP_SERVER_TYPE")
@Getter
@Setter
public class LkupServerTypeEntity {

    /** Primary persistent storage unique sequence row reference. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_LKUP_SERVER_TYPE_SEQ")
    @SequenceGenerator(name = "INV_LKUP_SERVER_TYPE_SEQ", sequenceName = "INV_LKUP_SERVER_TYPE_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Unique short code identifying the server type (e.g. DATABASE, APPLICATION). */
    @Column(name = "CODE", nullable = false)
    private String code;

    /** Catalan localized descriptive name of the server type. */
    @Column(name = "NAME", nullable = false)
    private String name;

    /** Spanish localized descriptive name of the server type. */
    @Column(name = "NAME_ES", nullable = false)
    private String nameEs;
}
