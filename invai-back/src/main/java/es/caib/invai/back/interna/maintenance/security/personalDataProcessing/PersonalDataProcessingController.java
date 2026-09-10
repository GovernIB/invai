package es.caib.invai.back.interna.maintenance.security.personalDataProcessing;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.security.personalDataProcessing.DTO.PersonalDataProcessingInputDTO;
import es.caib.invai.back.interna.maintenance.security.personalDataProcessing.DTO.PersonalDataProcessingOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.security.personalDataProcessing.PersonalDataProcessingCriteria;
import es.caib.invai.back.service.facade.maintenance.security.personalDataProcessing.PersonalDataProcessingService;
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
 * Internal REST controller that manages lifecycle endpoints and routing rules for PersonalDataProcessing assets.
 *
 * @since 1.0.4
 */
@Tag(name = "Tractaments de dades personals", description = "Manteniment del catàleg de tractaments de dades personals.")
@RestController
@Slf4j
@RequestMapping("personal-data-processing")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class PersonalDataProcessingController {

    /** Service facade handling PersonalDataProcessing business use cases. */
    private final PersonalDataProcessingService personalDataProcessingService;

    /**
     * Creates the controller with its required service dependency.
     *
     * @param personalDataProcessingService the service facade to delegate business operations to
     */
    @Autowired
    public PersonalDataProcessingController(PersonalDataProcessingService personalDataProcessingService) {
        this.personalDataProcessingService = personalDataProcessingService;
    }

    /**
     * Retrieves a personal data processing entry by its identifier.
     *
     * @param id the personal data processing entry identifier
     * @return the matching personal data processing entry
     */
    @GetMapping("/{id}")
    public ResponseEntity<PersonalDataProcessingOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(personalDataProcessingService.getById(id));
    }

    /**
     * Retrieves a paginated, filtered list of personal data processing entries.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching personal data processing entries
     */
    @GetMapping
    public ResponseEntity<Page<PersonalDataProcessingOutputDTO>> getAll(
            @ModelAttribute PersonalDataProcessingCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(personalDataProcessingService.getAll(filter, pageable));
    }

    /**
     * Creates a new personal data processing entry.
     *
     * @param inputDTO the data for the new personal data processing entry
     * @return the created personal data processing entry, with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<PersonalDataProcessingOutputDTO> create(@Valid @RequestBody PersonalDataProcessingInputDTO inputDTO) {
        return new ResponseEntity<>(personalDataProcessingService.create(inputDTO), HttpStatus.CREATED);
    }

    /**
     * Updates an existing personal data processing entry.
     *
     * @param id the identifier of the personal data processing entry to update
     * @param inputDTO the new data to apply
     * @return the updated personal data processing entry
     */
    @PutMapping("/{id}")
    public ResponseEntity<PersonalDataProcessingOutputDTO> update(@PathVariable Long id, @Valid @RequestBody PersonalDataProcessingInputDTO inputDTO) {
        return ResponseEntity.ok(personalDataProcessingService.update(id, inputDTO));
    }

    /**
     * Logically deletes a personal data processing entry by its identifier.
     *
     * @param id the identifier of the personal data processing entry to delete
     * @return an empty response with HTTP 204 status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        personalDataProcessingService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a previously deleted personal data processing entry.
     *
     * @param id the identifier of the personal data processing entry to reactivate
     * @return the reactivated personal data processing entry
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<PersonalDataProcessingOutputDTO> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(personalDataProcessingService.reactivate(id));
    }
}
