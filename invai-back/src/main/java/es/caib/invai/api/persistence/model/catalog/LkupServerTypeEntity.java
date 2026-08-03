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

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_LKUP_SERVER_TYPE_SEQ")
    @SequenceGenerator(name = "INV_LKUP_SERVER_TYPE_SEQ", sequenceName = "INV_LKUP_SERVER_TYPE_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "CODE", nullable = false)
    private String code;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "NAME_ES", nullable = false)
    private String nameEs;
}
