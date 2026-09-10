package es.caib.invai.back.interna.maintenance.classificationSegment;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.classificationSegment.DTO.ClassificationSegmentInputDTO;
import es.caib.invai.back.interna.maintenance.classificationSegment.DTO.ClassificationSegmentOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.classificationSegment.ClassificationSegmentCriteria;
import es.caib.invai.back.service.facade.maintenance.classificationSegment.ClassificationSegmentService;
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
 * Internal REST controller that manages lifecycle endpoints and routing rules for ClassificationSegment assets.
 *
 * @since 1.0.4
 */
@Tag(name = "Segment de classificació", description = "Manteniment del catàleg de segments de classificació del portal.")
@RestController
@Slf4j
@RequestMapping("classification-segment")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class ClassificationSegmentController {

    /** Service facade handling ClassificationSegment business use cases. */
    private final ClassificationSegmentService classificationSegmentService;

    /**
     * Creates the controller with its required service dependency.
     *
     * @param classificationSegmentService the service facade to delegate business operations to
     */
    @Autowired
    public ClassificationSegmentController(ClassificationSegmentService classificationSegmentService) {
        this.classificationSegmentService = classificationSegmentService;
    }

    /**
     * Retrieves a classification segment entry by its identifier.
     *
     * @param id the classification segment entry identifier
     * @return the matching classification segment entry
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClassificationSegmentOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(classificationSegmentService.getById(id));
    }

    /**
     * Retrieves a paginated, filtered list of classification segment entries.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching classification segment entries
     */
    @GetMapping
    public ResponseEntity<Page<ClassificationSegmentOutputDTO>> getAll(
            @ModelAttribute ClassificationSegmentCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(classificationSegmentService.getAll(filter, pageable));
    }

    /**
     * Creates a new classification segment entry.
     *
     * @param inputDTO the data for the new classification segment entry
     * @return the created classification segment entry, with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<ClassificationSegmentOutputDTO> create(@Valid @RequestBody ClassificationSegmentInputDTO inputDTO) {
        return new ResponseEntity<>(classificationSegmentService.create(inputDTO), HttpStatus.CREATED);
    }

    /**
     * Updates an existing classification segment entry.
     *
     * @param id the identifier of the classification segment entry to update
     * @param inputDTO the new data to apply
     * @return the updated classification segment entry
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClassificationSegmentOutputDTO> update(@PathVariable Long id, @Valid @RequestBody ClassificationSegmentInputDTO inputDTO) {
        return ResponseEntity.ok(classificationSegmentService.update(id, inputDTO));
    }

    /**
     * Logically deletes a classification segment entry by its identifier.
     *
     * @param id the identifier of the classification segment entry to delete
     * @return an empty response with HTTP 204 status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        classificationSegmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a previously deleted classification segment entry.
     *
     * @param id the identifier of the classification segment entry to reactivate
     * @return the reactivated classification segment entry
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<ClassificationSegmentOutputDTO> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(classificationSegmentService.reactivate(id));
    }
}
