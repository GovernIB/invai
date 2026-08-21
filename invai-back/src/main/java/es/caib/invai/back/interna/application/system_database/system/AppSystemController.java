package es.caib.invai.back.interna.application.system_database.system;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.application.system_database.system.DTO.AppSystemInputDTO;
import es.caib.invai.back.interna.application.system_database.system.DTO.AppSystemOutputDTO;
import es.caib.invai.back.persistence.repository.application.system_database.system.AppSystemCriteria;
import es.caib.invai.back.service.facade.application.system_database.system.AppSystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Internal REST controller handling lifecycle endpoints for managing Application to System assignment profiles.
 * <p>
 * Access is restricted at the type level to corporate users holding the {@code ROLE_usuari-tipus-E} role.
 * </p>
 *
 * @since 1.0.2
 */
@Slf4j
@Validated
@Tag(name = "Sistemes d'aplicació", description = "Servei de gestió dels sistemes associats a les aplicacions.")
@RestController
@RequiredArgsConstructor
@RequestMapping("application/system")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class AppSystemController {

    /** Service facade handling business operations for application-system links. */
    private final AppSystemService appSystemService;

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries and criteria filtering,
     * scoped to a single parent application. The application identifier is a mandatory path
     * variable: this endpoint never lists every system link in the database.
     *
     * @param informationSystemDbId mandatory parent application identifier scoping the result set
     * @param criteria      query filtering parameters DTO
     * @param pageable      pagination layout boundaries (defaults to size 10 sorted ascending by ID)
     * @return a {@link ResponseEntity} wrapping the partitioned {@link Page} matrix containing mapped {@link AppSystemOutputDTO} definitions
     */
    @GetMapping("/{informationSystemDbId}")
    public ResponseEntity<Page<AppSystemOutputDTO>> getAllByInformationSystemDbId(
            @PathVariable Long informationSystemDbId,
            @ModelAttribute AppSystemCriteria criteria,
            @PageableDefault(sort = "id") Pageable pageable) {

        log.info("REST: Fetching paged application-system links for informationSystemDb ID: {} and criteria: {}", informationSystemDbId, criteria);
        Page<AppSystemOutputDTO> page = appSystemService.getAll(informationSystemDbId, criteria, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Persists a new application system infrastructure relationship allocation configuration into the database.
     * Evaluates active constraint descriptors before routing parameters across execution lines.
     *
     * @param inputDTO properties dataset containing link mappings, configurations, variables, parameters data profiles
     * @return a {@link ResponseEntity} wrapping the created snapshot parameters state with an HTTP 201 Created response
     */
    @PostMapping
    public ResponseEntity<AppSystemOutputDTO> create(@Valid @RequestBody AppSystemInputDTO inputDTO) {
        log.info("REST: Request to create new application-system link assignment");
        AppSystemOutputDTO created = appSystemService.create(inputDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Alters active configuration profiles mapping properties corresponding to target database references.
     *
     * @param id       targeted structural identifier element index
     * @param inputDTO property data variables mapping structural items
     * @return a {@link ResponseEntity} containing current modified configuration state properties details with an HTTP 200 OK status
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppSystemOutputDTO> update(@PathVariable Long id, @Valid @RequestBody AppSystemInputDTO inputDTO) {
        log.info("REST: Request to update application-system link ID: {}", id);
        return ResponseEntity.ok(appSystemService.update(id, inputDTO));
    }

    /**
     * Routes explicit request signals targeting domain deactivation soft deletion routines.
     *
     * @param id persistent tracking row database reference index targeting removal execution paths
     * @return a {@link ResponseEntity} providing an empty success feedback with an HTTP 204 No Content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST: Request to logical delete application-system link ID: {}", id);
        appSystemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
