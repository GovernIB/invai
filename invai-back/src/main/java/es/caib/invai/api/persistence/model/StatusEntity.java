package es.caib.invai.api.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * JPA persistent entity representing lifecycle status constants for managed application profiles.
 * Acts as a static data reference lookup dictionary within the application inventory core schema.
 *
 * @since 1.0.0
 */
@Getter
@Setter
@Entity
@Table(name = "INV_STATUS")
public class StatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_status_seq")
    @SequenceGenerator(name = "inv_status_seq", sequenceName = "INV_STATUS_SEQ", allocationSize = 1)
    @Column(name = "STATUS_ID")
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;
}