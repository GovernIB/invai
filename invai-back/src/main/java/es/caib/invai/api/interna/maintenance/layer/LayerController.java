package es.caib.invai.api.interna.maintenance.layer;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.api.interna.maintenance.layer.DTO.LayerInputDTO;
import es.caib.invai.api.interna.maintenance.layer.DTO.LayerOutputDTO;
import es.caib.invai.api.persistence.repository.layer.LayerCriteria;
import es.caib.invai.api.service.facade.LayerService;
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

    private final LayerService layerService;

    @Autowired
    public LayerController(LayerService layerService) {
        this.layerService = layerService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<LayerOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(layerService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<LayerOutputDTO>> getAll(
            @ModelAttribute LayerCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(layerService.getAll(filter, pageable));
    }

    @PostMapping
    public ResponseEntity<LayerOutputDTO> create(@Valid @RequestBody LayerInputDTO inputDTO) {
        return new ResponseEntity<>(layerService.create(inputDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LayerOutputDTO> update(@PathVariable Long id, @Valid @RequestBody LayerInputDTO inputDTO) {
        return ResponseEntity.ok(layerService.update(id, inputDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        layerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("reactivate/{id}")
    public ResponseEntity<LayerOutputDTO> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(layerService.reactivate(id));
    }
}
