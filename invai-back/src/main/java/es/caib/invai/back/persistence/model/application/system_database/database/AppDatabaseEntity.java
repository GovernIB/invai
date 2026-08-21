package es.caib.invai.back.persistence.model.application.system_database.database;

import lombok.*;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.application.system_database.core.AppInformationSystemDbEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.database.DatabaseEntity;

import java.io.Serial;

/**
 * Persistent entity mapping an {@link AppInformationSystemDbEntity} to a
 * {@link DatabaseEntity} it relies on.
 *
 * @since 1.0.2
 */
@Entity
@Table(name = "INV_APP_DATABASE")
@Getter
@Setter
public class AppDatabaseEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identification pointer of this application-database association. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_DATABASE_SEQ")
    @SequenceGenerator(name = "INV_APP_DATABASE_SEQ", sequenceName = "INV_APP_DATABASE_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Information system grouping this association belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INFORMATION_SYSTEM_DB", nullable = false)
    private AppInformationSystemDbEntity informationSystemDb;

    /** Database catalog entry associated with the information system grouping. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DATABASE_ID", nullable = false)
    private DatabaseEntity database;
}