package es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO;

import es.caib.invai.back.utils.Constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Inbound payload for the "Transferència de rols" bulk operation: moves the selected
 * {@code AppResponsible}/{@code AppAuthorized} assignments to the person identified by
 * {@code toPersonEmailAddress}, or soft-deletes them in place when {@code revoke} is {@code true}
 * (in which case {@code toPersonEmailAddress} is ignored). The destination person is resolved by
 * e-mail: an existing local match is reused as-is; otherwise Soffid is queried and, if found there,
 * a new local {@code Person} row is created from it before the transfer proceeds.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleTransferInputDTO {

    /** Assignment rows selected for transfer or revocation. */
    @NotEmpty(message = "{" + Constants.VALIDATION_ROLETRANSFER_ITEMS + "}")
    @Valid
    private List<RoleTransferItemDTO> items;

    /**
     * E-mail address of the destination person. Required unless {@code revoke} is {@code true}, in
     * which case it is ignored. Resolved first against the local {@code Person} catalog, then
     * against Soffid when no local match exists.
     */
    private String toPersonEmailAddress;

    /** When {@code true}, the selected items are soft-deleted instead of reassigned. */
    private boolean revoke;
}
