package es.caib.invai.api.interna.admUnit;

import es.caib.invai.api.interna.admUnit.DTO.AdmUnitInputDTO;
import es.caib.invai.api.interna.admUnit.DTO.AdmUnitOutputDTO;
import es.caib.invai.api.service.facade.AdmUnitService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Internal REST controller handling the lifecycle and CRUD endpoints for Administrative Units.
 * <p>
 * Secured at the class level via Spring Method Security permissions, restricting execution
 * exclusively to users holding the role {@code ROLE_usuari-tipus-E}.
 * </p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/interna/adm-unit")
@PreAuthorize("hasRole('ROLE_usuari-tipus-E')")
public class AdmUnitController {

    @Autowired
    private AdmUnitService admUnitService;

    /**
     * Retrieves a paginated matrix list of all registered administrative units.
     *
     * @param pageable pagination parameters (page, size, sorting fields) supplied by the client request
     * @return a {@link ResponseEntity} wrapping a {@link Page} of {@link AdmUnitOutputDTO} elements with an HTTP 200 status
     */
    @GetMapping
    public ResponseEntity<Page<AdmUnitOutputDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(admUnitService.getAll(pageable));
    }

    /**
     * Retrieves specific configuration parameters of a single administrative unit by its unique database primary key.
     *
     * @param id the unique primary identifier targeting the record
     * @return a {@link ResponseEntity} containing the matched {@link AdmUnitOutputDTO} payload with an HTTP 200 status
     * @throws javax.persistence.EntityNotFoundException if no matching administrative unit record is resolved
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdmUnitOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(admUnitService.getById(id));
    }

    /**
     * Registers a new administrative unit record in the system.
     * Evaluates explicit bean constraint definitions prior to dispatching transaction scopes.
     *
     * @param inputDTO data payload modeling the target properties to be created
     * @return a {@link ResponseEntity} wrapping the initialized {@link AdmUnitOutputDTO} with an HTTP 201 Created status
     */
    @PostMapping
    public ResponseEntity<AdmUnitOutputDTO> create(@Valid @RequestBody AdmUnitInputDTO inputDTO) {
        return new ResponseEntity<>(admUnitService.create(inputDTO), HttpStatus.CREATED);
    }

    /**
     * Modifies properties on an existing administrative unit record matching the specified reference index.
     *
     * @param id       the persistent identity identifier to modify
     * @param inputDTO the updated data payload containing fields to map onto the entity
     * @return a {@link ResponseEntity} wrapping the modified state properties as an {@link AdmUnitOutputDTO} with an HTTP 200 status
     * @throws javax.persistence.EntityNotFoundException if the record targeted for modifications does not exist
     */
    @PutMapping("/{id}")
    public ResponseEntity<AdmUnitOutputDTO> update(@PathVariable Long id, @Valid @RequestBody AdmUnitInputDTO inputDTO) {
        return ResponseEntity.ok(admUnitService.update(id, inputDTO));
    }

    /**
     * Executes a logical audit-driven soft deletion against the target database record index.
     *
     * @param id system reference primary index targeting data deactivation
     * @return a {@link ResponseEntity} conveying a success state structure containing an HTTP 204 No Content status
     * @throws javax.persistence.EntityNotFoundException if the record targeted for deactivation does not exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        admUnitService.delete(id);
        return ResponseEntity.noContent().build();
    }
}