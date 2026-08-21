package es.caib.invai.back.interna.maintenance.development.layer;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.development.layer.DTO.LayerInputDTO;
import es.caib.invai.back.interna.maintenance.development.layer.DTO.LayerOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.development.layer.LayerCriteria;
import es.caib.invai.back.service.facade.maintenance.development.layer.LayerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Internal REST controller that manages lifecycle endpoints and routing rules for Layer assets.
 *
 * @since 1.0.2
 */
@Tag(name = "Capes", description = "Manteniment del catàleg de capes tecnològiques.")
@RestController
@Slf4j
@RequestMapping("layer")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class LayerController {

    /** Facade service handling the business logic for Layer catalog operations. */
    private final LayerService layerService;

    /**
     * Creates a new controller instance with the given service dependency.
     *
     * @param layerService the facade service used to perform Layer operations
     */
    @Autowired
    public LayerController(LayerService layerService) {
        this.layerService = layerService;
    }

    /**
     * Retrieves a layer by its unique identifier.
     *
     * @param id the identifier of the layer to fetch
     * @return the matching {@link LayerOutputDTO}
     */
    @GetMapping("/{id}")
    public ResponseEntity<LayerOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(layerService.getById(id));
    }

    /**
     * Retrieves a paginated list of layers matching the given filter criteria.
     *
     * @param filter   the search criteria used to narrow the results
     * @param pageable the pagination and sorting parameters
     * @return a page of {@link LayerOutputDTO} results
     */
    @GetMapping
    public ResponseEntity<Page<LayerOutputDTO>> getAll(
            @ModelAttribute LayerCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(layerService.getAll(filter, pageable));
    }

    /**
     * Creates a new layer record.
     *
     * @param inputDTO the data used to create the layer
     * @return the created layer, with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<LayerOutputDTO> create(@Valid @RequestBody LayerInputDTO inputDTO) {
        return new ResponseEntity<>(layerService.create(inputDTO), HttpStatus.CREATED);
    }

    /**
     * Updates an existing layer record.
     *
     * @param id       the identifier of the layer to update
     * @param inputDTO the data used to update the layer
     * @return the updated layer
     */
    @PutMapping("/{id}")
    public ResponseEntity<LayerOutputDTO> update(@PathVariable Long id, @Valid @RequestBody LayerInputDTO inputDTO) {
        return ResponseEntity.ok(layerService.update(id, inputDTO));
    }

    /**
     * Logically deletes a layer record.
     *
     * @param id the identifier of the layer to delete
     * @return an empty response with HTTP 204 status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        layerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a logically deleted layer record.
     *
     * @param id the identifier of the layer to reactivate
     * @return the reactivated layer
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<LayerOutputDTO> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(layerService.reactivate(id));
    }
}
