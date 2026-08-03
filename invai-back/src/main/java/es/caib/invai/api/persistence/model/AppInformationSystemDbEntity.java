package es.caib.invai.api.persistence.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * Persistent aggregate root entity mapping corporate applications to their
 * respective host information system grouping records.
 *
 * @since 1.0.2
 */
@Entity
@Table(name = "INV_APP_INFORMATION_SYSTEM_DB")
@Getter
@Setter
public class AppInformationSystemDbEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_INF_SYS_DB_SEQ")
    @SequenceGenerator(name = "INV_APP_INF_SYS_DB_SEQ", sequenceName = "INV_APP_INF_SYS_DB_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APPLICATION_ID", nullable = false)
    private ApplicationEntity application;

    @Lob
    @Column(name = "OBSERVATION")
    private String observation;
}
