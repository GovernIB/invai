package es.caib.invai.back.persistence.model.catalog.status;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

/**
 * JPA persistent entity representing lifecycle status constants for managed application profiles.
 * Acts as a static data reference lookup dictionary within the application inventory core schema.
 *
 * @since 1.0.1
 */
@Getter
@Setter
@Entity
@Table(name = "INV_LKUP_STATUS")
public class LkupStatusEntity {

    /** Primary key of the status lookup row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_status_seq")
    @SequenceGenerator(name = "inv_status_seq", sequenceName = "INV_STATUS_SEQ", allocationSize = 1)
    @Column(name = "STATUS_ID")
    private Long id;

    /** Display label of the status (typically Catalan). */
    @Column(name = "NAME", nullable = false)
    private String name;

    /** Display label of the status in Spanish. */
    @Column(name = "NAME_ES", nullable = false)
    private String nameEs;
}