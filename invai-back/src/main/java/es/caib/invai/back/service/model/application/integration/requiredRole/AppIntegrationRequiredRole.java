package es.caib.invai.back.service.model.application.integration.requiredRole;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import es.caib.invai.back.service.model.application.integration.connection.AppIntegrationConnection;

/**
 * Domain model representing a role required by an integration connection - only the Soffid role id is
 * kept, not a reference to any local catalog.
 *
 * @since 1.0.5
 */
@Getter
@Setter
public class AppIntegrationRequiredRole {

    /** Unique identification pointer of this required role row. */
    private Long id;

    /** Integration connection this required role belongs to. */
    private AppIntegrationConnection appIntegrationConnection;

    /** Soffid role's own numeric id. */
    private Long roleId;

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
