package es.caib.invai.back.service.model.application.responsibleAuthorized.responsible;

import lombok.*;
import java.time.LocalDateTime;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
import es.caib.invai.back.service.model.catalog.responsibleType.ResponsibleType;

/**
 * Domain model representing a person assigned as a responsible of a given type for an
 * application's "Responsables" tab, anchored to an {@link AppResponsibleAuthorized} tab record.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppResponsible {
    /** Unique identifier. */
    private Long id;
    /** Parent "Responsables i Autoritzats" anchor this responsible assignment belongs to. */
    private AppResponsibleAuthorized appResponsibleAuthorized;
    /** Responsible person. */
    private Person person;
    /** Responsibility type catalog value assigned to the person. */
    private ResponsibleType responsibleType;
    /** Free-text job title/position held by the responsible person for this assignment. */
    private String jobTitle;
    /** Free-text remarks about this responsible assignment. */
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
