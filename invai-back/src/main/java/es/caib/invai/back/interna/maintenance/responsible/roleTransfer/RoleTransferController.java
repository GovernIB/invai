package es.caib.invai.back.interna.maintenance.responsible.roleTransfer;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO.RoleAssignmentOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO.RoleTransferInputDTO;
import es.caib.invai.back.service.facade.maintenance.responsible.roleTransfer.RoleTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Internal REST controller for the "Transferència de rols" maintenance screen: lists a person's
 * active role assignments across every application, and moves or revokes a selected batch of them.
 *
 * @since 1.0.3
 */
@Slf4j
@Validated
@Tag(name = "Transferència de rols", description = "Servei de transferència o revocació massiva de rols de responsable/autoritzat d'una persona.")
@RestController
@RequiredArgsConstructor
@RequestMapping("role-transfer")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class RoleTransferController {

    /** Facade service handling the business logic for role transfer operations. */
    private final RoleTransferService roleTransferService;

    /**
     * Lists every active responsible/authorized assignment currently held by the given person,
     * across all applications.
     *
     * @param personId the person identifier to look up
     * @return the matching active assignments
     */
    @GetMapping("/{personId}")
    public ResponseEntity<List<RoleAssignmentOutputDTO>> getAssignmentsByPerson(@PathVariable Long personId) {
        log.info("REST: Fetching role assignments for person ID: {}", personId);
        return ResponseEntity.ok(roleTransferService.getAssignmentsByPerson(personId));
    }

    /**
     * Moves the selected assignments to another person, or revokes (soft-deletes) them in place.
     *
     * @param inputDTO the batch of assignments to transfer or revoke
     * @return an empty response body confirming success
     */
    @PostMapping
    public ResponseEntity<Void> transfer(@Valid @RequestBody RoleTransferInputDTO inputDTO) {
        log.info("REST: Processing role transfer request for {} item(s)", inputDTO.getItems().size());
        roleTransferService.transfer(inputDTO);
        return ResponseEntity.noContent().build();
    }
}
