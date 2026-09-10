package es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO;

import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonOutputDTO;
import es.caib.invai.back.service.model.catalog.responsibleType.ResponsibleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Outbound payload describing a responsible assignment, flattening the parent anchor down to its
 * identifier and embedding the resolved person and responsible type. When returned by the
 * catalog-driven default listing for a responsible type with no active holder, {@code id} and
 * {@code person} are {@code null} (see {@code AppResponsibleServiceFacadeBean#buildRow}).
 *
 * @since 1.0.3
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppResponsibleOutputDTO {

    /** Unique identifier. */
    private Long id;
    /** Identifier of the parent "Responsables i Autoritzats" anchor. */
    private Long appResponsibleAuthorizedId;
    /** Responsible person. */
    private PersonOutputDTO person;
    /** Responsibility type catalog value assigned to the person. */
    private ResponsibleType responsibleType;
    /** Free-text job title/position held by the responsible person for this assignment. */
    private String jobTitle;
    /** Free-text remarks about this responsible assignment. */
    private String observation;
    /** Timestamp when this record was soft-deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}
