package es.caib.invai.back.persistence.model.application.system_database.system;

import lombok.*;

import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.application.system_database.core.AppInformationSystemDbEntity;
import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.system.SystemEntity;

import java.io.Serial;

/**
 * Persistent entity mapping an {@link AppInformationSystemDbEntity} to a
 * {@link SystemEntity} it relies on.
 *
 * @since 1.0.2
 */
@Entity
@Table(name = "INV_APP_SYSTEM")
@Getter
@Setter
public class AppSystemEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identification pointer of this application-system association. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_SYSTEM_SEQ")
    @SequenceGenerator(name = "INV_APP_SYSTEM_SEQ", sequenceName = "INV_APP_SYSTEM_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Information system grouping this association belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INFORMATION_SYSTEM_DB", nullable = false)
    private AppInformationSystemDbEntity informationSystemDb;

    /** System catalog entry associated with the information system grouping. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SYSTEM_ID", nullable = false)
    private SystemEntity system;
}