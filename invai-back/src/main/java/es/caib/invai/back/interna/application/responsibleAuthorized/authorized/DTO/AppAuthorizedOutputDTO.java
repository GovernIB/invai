package es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO;

import es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO.AuthorizationTypeOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbound transport payload detailing a person authorized on an application, together with the
 * resolved list of authorization types currently attached to the assignment.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppAuthorizedOutputDTO {

    /** Unique identifier. */
    private Long id;
    /** Identifier of the parent "Responsables i Autoritzats" anchor. */
    private Long appResponsibleAuthorizedId;
    /** Authorized person. */
    private PersonOutputDTO person;
    /** Authorization types currently attached to this assignment. */
    private List<AuthorizationTypeOutputDTO> authorizationTypes;
    /** Free-text remarks about this authorization. */
    private String observation;
    /** Timestamp when this record was soft-deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}
