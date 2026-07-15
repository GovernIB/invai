package es.caib.invai.api.interna.field;

import es.caib.invai.api.interna.field.DTO.FieldInputDTO;
import es.caib.invai.api.interna.field.DTO.FieldOutputDTO;
import es.caib.invai.api.service.facade.FieldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Internal REST controller exposing CRUD operations and lifecycle routing endpoints
 * for managing operational Field records.
 * <p>
 * Authorization rules are handled at the controller level using Spring Method Security,
 * limiting all operations to authenticated corporate users carrying the {@code ROLE_usuari-tipus-E} role.
 * </p>
 *
 * @since 1.0.0
 */
@RestController
@Slf4j
@RequestMapping("interna/field")
@PreAuthorize("hasRole('ROLE_usuari-tipus-E')")
public class FieldController {

    private final FieldService fieldService;

    /**
     * Constructs a new FieldController with its required transaction-bounded orchestration service engine.
     *
     * @param fieldService the underlying business service engine managing data validation tasks
     */
    @Autowired
    public FieldController(FieldService fieldService) {
        this.fieldService = fieldService;
    }

    /**
     * Resolves single Field configuration attributes matching a specific primary database reference key.
     *
     * @param id persistent sequence row identity index
     * @return a {@link ResponseEntity} wrapping the matched {@link FieldOutputDTO} details with an HTTP 200 OK status
     */
    @GetMapping("/{id}")
    public ResponseEntity<FieldOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(fieldService.getById(id));
    }

    /**
     * Gets a paginated and sorted block matrix containing all active non-deleted Field data profiles.
     *
     * @param pageable sorting and pagination parameters managed by the incoming web request context
     * @return a {@link ResponseEntity} wrapping a structured {@link Page} of converted {@link FieldOutputDTO} instances with an HTTP 200 OK status
     */
    @GetMapping
    public ResponseEntity<Page<FieldOutputDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(fieldService.getAll(pageable));
    }

    /**
     * Registers a new operational Field asset within the database systems.
     * Evaluates explicit payload validation parameters beforehand.
     *
     * @param inputDTO validated data transfer object capturing metadata for the new record
     * @return a {@link ResponseEntity} detailing the persisted snapshot properties as a {@link FieldOutputDTO} with an HTTP 201 Created status
     */
    @PostMapping
    public ResponseEntity<FieldOutputDTO> create(@Valid @RequestBody FieldInputDTO inputDTO) {
        return new ResponseEntity<>(fieldService.create(inputDTO), HttpStatus.CREATED);
    }

    /**
     * Updates an active field configuration profile matching the requested database row index.
     *
     * @param id       persistent identity identifier row index targeted for modification
     * @param inputDTO property payload schema mapping updated dataset values
     * @return a {@link ResponseEntity} wrapping the newly modified configuration states as a {@link FieldOutputDTO} with an HTTP 200 OK status
     */
    @PutMapping("/{id}")
    public ResponseEntity<FieldOutputDTO> update(@PathVariable Long id, @Valid @RequestBody FieldInputDTO inputDTO) {
        return ResponseEntity.ok(fieldService.update(id, inputDTO));
    }

    /**
     * Executes a soft logical deactivation against a specific target tracking record index.
     *
     * @param id reference index specifying the target architectural parameter row to disable
     * @return an empty {@link ResponseEntity} confirming success with an HTTP 204 No Content status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fieldService.delete(id);
        return ResponseEntity.noContent().build();
    }
}