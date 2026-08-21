package es.caib.invai.back.service.model.application.responsibleAuthorized.authorized;

import lombok.*;
import java.time.LocalDateTime;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;

/**
 * Domain aggregate model representing a person authorized on a given application's
 * "Responsables i Autoritzats" tab, anchored via {@link AppResponsibleAuthorized}.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppAuthorized {
    /** Unique identifier. */
    private Long id;
    /** Parent "Responsables i Autoritzats" anchor this authorization belongs to. */
    private AppResponsibleAuthorized appResponsibleAuthorized;
    /** Authorized person. */
    private Person person;
    /** Free-text remarks about this authorization. */
    private String observation;
    /** Timestamp when this record was created. */
    private LocalDateTime createdAt;
    /** User who created this record. */
    private String createdBy;
    /** Timestamp of the last update to this record. */
    private LocalDateTime updatedAt;
    /** User who last updated this record. */
    private String updatedBy;
    /** Timestamp when this record was soft-deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
    /** User who soft-deleted this record. */
    private String deletedBy;
}
