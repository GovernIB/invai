package es.caib.invai.api.interna.maintenance.technology;

import es.caib.invai.api.interna.maintenance.technology.DTO.TechnologyInputDTO;
import es.caib.invai.api.interna.maintenance.technology.DTO.TechnologyOutputDTO;
import es.caib.invai.api.persistence.repository.technology.TechnologyCriteria;
import es.caib.invai.api.service.facade.TechnologyService;
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
 * Internal REST controller that manages lifecycle endpoints and routing rules for catalog Technology assets.
 *
 * @since 1.0.2
 */
@RestController
@Slf4j
@RequestMapping("technology")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class TechnologyController {

    private final TechnologyService technologyService;

    @Autowired
    public TechnologyController(TechnologyService technologyService) {
        this.technologyService = technologyService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TechnologyOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(technologyService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<TechnologyOutputDTO>> getAll(
            @ModelAttribute TechnologyCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(technologyService.getAll(filter, pageable));
    }

    @PostMapping
    public ResponseEntity<TechnologyOutputDTO> create(@Valid @RequestBody TechnologyInputDTO inputDTO) {
        return new ResponseEntity<>(technologyService.create(inputDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TechnologyOutputDTO> update(@PathVariable Long id, @Valid @RequestBody TechnologyInputDTO inputDTO) {
        return ResponseEntity.ok(technologyService.update(id, inputDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        technologyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("reactivate/{id}")
    public ResponseEntity<TechnologyOutputDTO> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(technologyService.reactivate(id));
    }
}
