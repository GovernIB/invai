package es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO;

import es.caib.invai.back.service.model.maintenance.responsible.roleTransfer.RoleAssignmentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

/**
 * Identifies a single {@code AppResponsible} or {@code AppAuthorized} assignment row selected for
 * transfer or revocation, as part of a {@link RoleTransferInputDTO} batch.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleTransferItemDTO {

    /** Primary key of the {@code AppResponsible} or {@code AppAuthorized} row selected. */
    @NotNull(message = "{validation.roletransfer.item.id}")
    private Long id;

    /** Discriminates whether {@code id} refers to an {@code AppResponsible} or {@code AppAuthorized} row. */
    @NotNull(message = "{validation.roletransfer.item.type}")
    private RoleAssignmentType type;
}
