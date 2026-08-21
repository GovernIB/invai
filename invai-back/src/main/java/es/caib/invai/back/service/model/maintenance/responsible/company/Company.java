package es.caib.invai.back.service.model.maintenance.responsible.company;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Domain entity model configuring companies assignable within the Manteniments / Responsables catalog.
 *
 * @since 1.0.3
 */
@Getter
@Setter
public class Company {

    /** Unique database company index identifier key. */
    private Long id;

    /** Company corporate name label string. */
    private String name;

    /** Timestamp at which the company was created. */
    private LocalDateTime createdAt;
    /** Username of the user who created the company. */
    private String createdBy;
    /** Timestamp at which the company was last updated. */
    private LocalDateTime updatedAt;
    /** Username of the user who last updated the company. */
    private String updatedBy;
    /** Timestamp at which the company was logically deleted, or {@code null} if it is active. */
    private LocalDateTime deletedAt;
    /** Username of the user who logically deleted the company. */
    private String deletedBy;
}
