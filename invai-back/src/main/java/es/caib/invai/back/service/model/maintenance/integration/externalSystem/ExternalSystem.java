package es.caib.invai.back.service.model.maintenance.integration.externalSystem;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import es.caib.invai.back.service.model.maintenance.responsible.company.Company;

/**
 * Domain model representing an external system (outside the inventory) that an application can
 * integrate with.
 *
 * @since 1.0.5
 */
@Getter
@Setter
public class ExternalSystem {

    /** Unique database external system index identifier key. */
    private Long id;

    /** External system name. */
    private String name;

    /** Company responsible for the external system. */
    private Company company;

    /** Timestamp at which the external system was created. */
    private LocalDateTime createdAt;
    /** Username of the user who created the external system. */
    private String createdBy;
    /** Timestamp at which the external system was last updated. */
    private LocalDateTime updatedAt;
    /** Username of the user who last updated the external system. */
    private String updatedBy;
    /** Timestamp at which the external system was logically deleted, or {@code null} if it is active. */
    private LocalDateTime deletedAt;
    /** Username of the user who logically deleted the external system. */
    private String deletedBy;
}
