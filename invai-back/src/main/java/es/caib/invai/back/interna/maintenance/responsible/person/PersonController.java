package es.caib.invai.back.interna.maintenance.responsible.person;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.responsible.person.PersonCriteria;
import es.caib.invai.back.service.facade.maintenance.responsible.person.PersonService;

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
 * Internal REST controller handling lifecycle endpoints for managing Person catalog metadata.
 *
 * @since 1.0.3
 */
@Tag(name = "Persones", description = "Manteniment del catàleg de persones.")
@RestController
@Slf4j
@RequestMapping("person")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class PersonController {

    /** Facade service handling the business logic for Person catalog operations. */
    private final PersonService personService;

    /**
     * Creates a new controller instance with the given service dependency.
     *
     * @param personService the facade service used to perform Person operations
     */
    @Autowired
    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    /**
     * Retrieves a paginated list of persons matching the given filter criteria.
     *
     * @param filter   the search criteria used to narrow the results
     * @param pageable the pagination and sorting parameters
     * @return a page of {@link PersonOutputDTO} results
     */
    @GetMapping
    public ResponseEntity<Page<PersonOutputDTO>> getAll(
            @ModelAttribute PersonCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        log.info("REST: Initiating multi-criteria fetch grid search query parameters");
        Page<PersonOutputDTO> multiQueryResult = personService.getAll(filter, pageable);
        return ResponseEntity.ok(multiQueryResult);
    }

    /**
     * Retrieves a person by its unique identifier.
     *
     * @param id the identifier of the person to fetch
     * @return the matching {@link PersonOutputDTO}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PersonOutputDTO> getById(@PathVariable Long id) {
        log.info("REST: Fetching person by ID: {}", id);
        return ResponseEntity.ok(personService.getById(id));
    }

    /**
     * Creates a new person record.
     *
     * @param inputDTO the data used to create the person
     * @return the created person, with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<PersonOutputDTO> create(@Valid @RequestBody PersonInputDTO inputDTO) {
        log.info("REST: Request to create new person");
        PersonOutputDTO created = personService.create(inputDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Updates an existing person record.
     *
     * @param id       the identifier of the person to update
     * @param inputDTO the data used to update the person
     * @return the updated person
     */
    @PutMapping("/{id}")
    public ResponseEntity<PersonOutputDTO> update(@PathVariable Long id, @Valid @RequestBody PersonInputDTO inputDTO) {
        log.info("REST: Request to update person ID: {}", id);
        return ResponseEntity.ok(personService.update(id, inputDTO));
    }

    /**
     * Logically deletes a person record.
     *
     * @param id the identifier of the person to delete
     * @return an empty response with HTTP 204 status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST: Request to logical delete person ID: {}", id);
        personService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a logically deleted person record.
     *
     * @param id the identifier of the person to reactivate
     * @return the reactivated person
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<PersonOutputDTO> reactivate(@PathVariable Long id) {
        log.info("REST: Request to reactivate person ID: {}", id);
        return ResponseEntity.ok(personService.reactivate(id));
    }
}
