package es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Inbound payload for the "Transferència de rols" bulk operation: moves the selected
 * {@code AppResponsible}/{@code AppAuthorized} assignments to {@code toPersonId}, or soft-deletes
 * them in place when {@code revoke} is {@code true} (in which case {@code toPersonId} is ignored).
 *
 * @since 1.0.3
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleTransferInputDTO {

    /** Assignment rows selected for transfer or revocation. */
    @NotEmpty(message = "{validation.roletransfer.items}")
    @Valid
    private List<RoleTransferItemDTO> items;

    /**
     * Destination person identifier. Required unless {@code revoke} is {@code true}, in which case
     * it is ignored.
     */
    private Long toPersonId;

    /** When {@code true}, the selected items are soft-deleted instead of reassigned. */
    private boolean revoke;
}
