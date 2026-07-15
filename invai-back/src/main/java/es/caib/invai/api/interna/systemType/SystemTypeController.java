package es.caib.invai.api.interna.systemType;

import es.caib.invai.api.interna.systemType.DTO.SystemTypeInputDTO;
import es.caib.invai.api.interna.systemType.DTO.SystemTypeOutputDTO;
import es.caib.invai.api.service.facade.SystemTypeService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Internal REST controller exposing CRUD operations and lifecycle routing endpoints
 * for managing architectural System Type records.
 * <p>
 * Access control restrictions are enforced globally at the class level via Spring Method Security,
 * restricting all operations exclusively to authenticated operators holding the corporate
 * role {@code ROLE_usuari-tipus-E}.
 * </p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/interna/system-type")
@PreAuthorize("hasRole('ROLE_usuari-tipus-E')")
public class SystemTypeController {

    @Autowired
    private SystemTypeService systemTypeService;

    /**
     * Retrieves a paginated and sorted block matrix across all active, non-deleted System Type records.
     *
     * @param pageable pagination and sorting parameters supplied by the client query context
     * @return a {@link ResponseEntity} containing a structured {@link Page} of mapped {@link SystemTypeOutputDTO} elements with an HTTP 200 OK status
     */
    @GetMapping
    public ResponseEntity<Page<SystemTypeOutputDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(systemTypeService.getAll(pageable));
    }

    /**
     * Resolves single System Type configuration attributes using its unique database primary reference key.
     *
     * @param id persistent sequence row identity identifier targeting the record
     * @return a {@link ResponseEntity} wrapping the matched {@link SystemTypeOutputDTO} details with an HTTP 200 OK status
     */
    @GetMapping("/{id}")
    public ResponseEntity<SystemTypeOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(systemTypeService.getById(id));
    }

    /**
     * Commits a new System Type classification profile to the underlying application registries.
     * Evaluates explicit payload bean constraints before initializing transactional boundaries.
     *
     * @param inputDTO validated data payload capturing fields for the new system type descriptor
     * @return a {@link ResponseEntity} wrapping the initialized tracking model properties as a {@link SystemTypeOutputDTO} with an HTTP 201 Created status
     */
    @PostMapping
    public ResponseEntity<SystemTypeOutputDTO> create(@Valid @RequestBody SystemTypeInputDTO inputDTO) {
        return new ResponseEntity<>(systemTypeService.create(inputDTO), HttpStatus.CREATED);
    }

    /**
     * Modifies configured properties on an active record index matching target primary identifiers.
     *
     * @param id       persistent identity identifier row index targeted for updates
     * @param inputDTO property payload schema mapping updated dataset fields
     * @return a {@link ResponseEntity} wrapping current modified configuration variables as a {@link SystemTypeOutputDTO} with an HTTP 200 OK status
     */
    @PutMapping("/{id}")
    public ResponseEntity<SystemTypeOutputDTO> update(@PathVariable Long id, @Valid @RequestBody SystemTypeInputDTO inputDTO) {
        return ResponseEntity.ok(systemTypeService.update(id, inputDTO));
    }

    /**
     * Triggers a logical audit-driven soft deletion framework to disable a target unique tracking record index.
     *
     * @param id system reference primary index targeting data deactivation
     * @return an empty {@link ResponseEntity} confirming success parameters with an HTTP 204 No Content status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        systemTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}