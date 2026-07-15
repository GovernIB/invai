package es.caib.invai.api.interna.commission;

import es.caib.invai.api.interna.commission.DTO.CommissionInputDTO;
import es.caib.invai.api.interna.commission.DTO.CommissionOutputDTO;
import es.caib.invai.api.service.facade.CommissionService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Internal REST controller exposing operations and lifecycle endpoints for Commission domain resources.
 * <p>
 * Access control restrictions are enforced globally at the class level via Spring Method Security,
 * allowing access only to authenticated users who possess the corporate role {@code ROLE_usuari-tipus-E}.
 * </p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/interna/commission")
@PreAuthorize("hasRole('ROLE_usuari-tipus-E')")
public class CommissionController {

    @Autowired
    private CommissionService commissionService;

    /**
     * Retrieves a paginated and sorted block matrix across all non-deleted Commission data records.
     *
     * @param pageable pagination and sorting parameters supplied by the client query context
     * @return a {@link ResponseEntity} containing a structured {@link Page} of mapped {@link CommissionOutputDTO} items with an HTTP 200 OK status
     */
    @GetMapping
    public ResponseEntity<Page<CommissionOutputDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(commissionService.getAll(pageable));
    }

    /**
     * Resolves matching Commission configuration attributes using a unique identity reference key.
     *
     * @param id persistent index sequence row identifier
     * @return a {@link ResponseEntity} wrapping the matched {@link CommissionOutputDTO} with an HTTP 200 OK status
     */
    @GetMapping("/{id}")
    public ResponseEntity<CommissionOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(commissionService.getById(id));
    }

    /**
     * Persists a new Commission entry profile to the underlying application registries.
     * Evaluates explicit request validation parameters prior to initializing transactional boundaries.
     *
     * @param inputDTO validated data payload capturing target configuration fields for the new commission
     * @return a {@link ResponseEntity} containing the initialized tracking properties as a {@link CommissionOutputDTO} with an HTTP 201 Created status
     */
    @PostMapping
    public ResponseEntity<CommissionOutputDTO> create(@Valid @RequestBody CommissionInputDTO inputDTO) {
        return new ResponseEntity<>(commissionService.create(inputDTO), HttpStatus.CREATED);
    }

    /**
     * Alters configured properties on an active record index matching target primary identifiers.
     *
     * @param id       persistent identity identifier row index targeted for updates
     * @param inputDTO updated properties mapping new configuration datasets fields
     * @return a {@link ResponseEntity} wrapping current modified properties as a {@link CommissionOutputDTO} with an HTTP 200 OK status
     */
    @PutMapping("/{id}")
    public ResponseEntity<CommissionOutputDTO> update(@PathVariable Long id, @Valid @RequestBody CommissionInputDTO inputDTO) {
        return ResponseEntity.ok(commissionService.update(id, inputDTO));
    }

    /**
     * Executes an audit-trail driven logical soft deletion against the target database record index.
     *
     * @param id system reference primary index targeting data deactivation
     * @return an empty {@link ResponseEntity} displaying success state variables with an HTTP 204 No Content status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}