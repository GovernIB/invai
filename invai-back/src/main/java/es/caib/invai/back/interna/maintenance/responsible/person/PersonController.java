package es.caib.invai.back.interna.maintenance.responsible.person;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonCombinedSearchOutputDTO;
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
    @GetMapping("database-search")
    public ResponseEntity<Page<PersonOutputDTO>> getAll(
            @ModelAttribute PersonCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        log.debug("REST: Initiating multi-criteria fetch grid search query parameters");
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
        log.debug("REST: Fetching person by ID: {}", id);
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

    /**
     * Lists CAIB personnel against the Soffid SCIM API - a {@code getAll} when {@code fullName} is
     * omitted, or restricted to matches when given - feeding both a general listing and the
     * "assign a responsible/authorized person" typeahead. Does not touch the local Person catalog
     * listing. Always paginated, since an unfiltered listing spans Soffid's entire personnel
     * directory.
     *
     * @param fullName the text to search for, matched (word by word) against the full name, or
     * omitted to list every active Soffid user
     * @param pageable the pagination parameters
     * @return the requested page of matching Soffid candidates, mapped into {@link PersonOutputDTO}
     * with a {@code null} id
     */
    @GetMapping("soffid-search")
    public ResponseEntity<Page<PersonOutputDTO>> searchSoffid(
            @RequestParam(required = false) String fullName,
            @PageableDefault Pageable pageable) {
        log.debug("REST: Request to list/search Soffid personnel");
        return ResponseEntity.ok(personService.searchSoffid(fullName, pageable));
    }

    /**
     * Lists/searches persons combining both the local Person catalog and Soffid, each independently
     * paginated with the same page number/size and sorted alphabetically. Feeds the "persona destí"
     * picker on the role-transfer screen, where the target may or may not have a local
     * {@code Person} row yet.
     *
     * @param search free text matched against first name, last name and e-mail (locally) or full
     * name (Soffid), or omitted to list both sources unfiltered
     * @param pageable the pagination parameters, applied identically to both sources
     * @return the local and Soffid pages for the given request
     */
    @GetMapping("all")
    public ResponseEntity<PersonCombinedSearchOutputDTO> searchCombined(
            @RequestParam(required = false) String search,
            @PageableDefault(sort = {"firstName", "lastName"}) Pageable pageable) {
        log.debug("REST: Request to list/search persons combining local catalog and Soffid");
        return ResponseEntity.ok(personService.searchCombined(search, pageable));
    }
}
