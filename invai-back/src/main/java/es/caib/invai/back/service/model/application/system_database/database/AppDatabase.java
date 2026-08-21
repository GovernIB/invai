package es.caib.invai.back.service.model.application.system_database.database;

import lombok.*;

import java.time.LocalDateTime;
import es.caib.invai.back.service.model.application.system_database.core.AppInformationSystemDb;
import es.caib.invai.back.service.model.maintenance.systems.database.Database;

/**
 * Domain model representing the association between an {@link AppInformationSystemDb}
 * and a {@link Database} it relies on.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppDatabase {
    /** Unique identification pointer of this application-database association. */
    private Long id;
    /** Information system grouping this association belongs to. */
    private AppInformationSystemDb informationSystemDb;
    /** Database catalog entry associated with the information system grouping. */
    private Database database;
    /** Timestamp at which this record was created. */
    private LocalDateTime createdAt;
    /** Identifier of the user who created this record. */
    private String createdBy;
    /** Timestamp at which this record was last updated. */
    private LocalDateTime updatedAt;
    /** Identifier of the user who last updated this record. */
    private String updatedBy;
    /** Timestamp at which this record was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
    /** Identifier of the user who logically deleted this record, or {@code null} if still active. */
    private String deletedBy;
}