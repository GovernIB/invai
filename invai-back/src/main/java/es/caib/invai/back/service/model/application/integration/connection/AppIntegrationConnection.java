package es.caib.invai.back.service.model.application.integration.connection;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.integration.core.AppIntegration;
import es.caib.invai.back.service.model.maintenance.development.technology.Technology;
import es.caib.invai.back.service.model.maintenance.integration.externalSystem.ExternalSystem;

/**
 * Domain model representing one row of the "Integracio" tab's table.
 *
 * @since 1.0.5
 */
@Getter
@Setter
public class AppIntegrationConnection {

    /** Unique identification pointer of this integration connection. */
    private Long id;

    /** Integration anchor this connection belongs to. */
    private AppIntegration appIntegration;

    /** The other system, when it is an application already registered in the inventory. */
    private Application application;

    /** The other system, when it is outside the inventory. */
    private ExternalSystem externalSystem;

    /** Technology used by this integration. */
    private Technology technology;

    /** Snapshot of the Soffid username at the time it was selected. */
    private String username;

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
