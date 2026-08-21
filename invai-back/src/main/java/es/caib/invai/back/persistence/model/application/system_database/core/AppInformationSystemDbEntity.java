package es.caib.invai.back.persistence.model.application.system_database.core;

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
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.BaseEntity;

import java.io.Serial;

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

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identification pointer of this information system grouping. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_INF_SYS_DB_SEQ")
    @SequenceGenerator(name = "INV_APP_INF_SYS_DB_SEQ", sequenceName = "INV_APP_INF_SYS_DB_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Corporate application this information system grouping belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APPLICATION_ID", nullable = false)
    private ApplicationEntity application;

    /** Free-text observation notes attached to this information system grouping. */
    @Lob
    @Column(name = "OBSERVATION")
    private String observation;
}
