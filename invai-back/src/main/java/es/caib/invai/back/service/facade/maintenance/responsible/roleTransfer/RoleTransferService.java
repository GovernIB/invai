package es.caib.invai.back.service.facade.maintenance.responsible.roleTransfer;

import es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO.RoleAssignmentOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO.RoleTransferInputDTO;

import java.util.List;

/**
 * Service Facade boundary interface for the "Transferència de rols" maintenance screen: lists a
 * person's active {@code AppResponsible}/{@code AppAuthorized} assignments across every application,
 * and moves or revokes a selected batch of them in a single operation.
 *
 * @since 1.0.3
 */
public interface RoleTransferService {

    /**
     * Lists every active assignment (responsible or authorized) currently held by the given person,
     * across all applications.
     *
     * @param personId the person identifier to look up
     * @return the matching active assignments
     */
    List<RoleAssignmentOutputDTO> getAssignmentsByPerson(Long personId);

    /**
     * Moves the selected assignments to the person identified by {@code toPersonEmailAddress}
     * (resolved locally, then against Soffid), or soft-deletes them in place when {@code revoke} is
     * set.
     *
     * @param inputDTO the batch of assignments to transfer or revoke
     */
    void transfer(RoleTransferInputDTO inputDTO);
}
