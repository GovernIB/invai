package es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO;

import es.caib.invai.back.interna.catalog.responsibleType.DTO.ResponsibleTypeOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO.AuthorizationTypeOutputDTO;
import es.caib.invai.back.service.model.maintenance.responsible.roleTransfer.RoleAssignmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Outbound transport payload describing a single active {@code AppResponsible} or
 * {@code AppAuthorized} assignment held by a person, scoped to the application it belongs to.
 * {@code responsibleType} is only populated for {@link RoleAssignmentType#RESPONSIBLE} rows,
 * and {@code authorizationTypes} only for {@link RoleAssignmentType#AUTHORIZED} rows.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignmentOutputDTO {

    /** Identifier of the underlying {@code AppResponsible} or {@code AppAuthorized} row. */
    private Long id;
    /** Discriminates whether this row is a {@code RESPONSIBLE} or {@code AUTHORIZED} assignment. */
    private RoleAssignmentType type;
    /** Identifier of the application this assignment belongs to. */
    private Long applicationId;
    /** Name of the application this assignment belongs to. */
    private String applicationName;
    /** Responsibility type; only populated for {@code RESPONSIBLE} rows. */
    private ResponsibleTypeOutputDTO responsibleType;
    /** Authorization types currently attached; only populated for {@code AUTHORIZED} rows. */
    private List<AuthorizationTypeOutputDTO> authorizationTypes;
}
