package es.caib.invai.back.service.model.application.integration.core;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import es.caib.invai.back.service.model.application.core.Application;

/**
 * Domain model representing the "Integracio" anchor owned by an {@link Application}. Like its
 * AppData/AppSecurity counterparts, this anchor is auto-created when the owning {@code
 * Application} is created, so it should only be {@code null} for applications created before this
 * anchor was introduced.
 *
 * @since 1.0.5
 */
@Getter
@Setter
public class AppIntegration {

    /** Unique identification pointer of this integration anchor. */
    private Long id;

    /** Corporate application this integration anchor belongs to. */
    private Application application;

    /** Free-text observations about this application's integrations. */
    private String observation;

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
