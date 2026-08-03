package es.caib.invai.api.interna.application.system_database.database;

import es.caib.invai.api.interna.application.system_database.database.DTO.AppDatabaseInputDTO;
import es.caib.invai.api.interna.application.system_database.database.DTO.AppDatabaseOutputDTO;
import es.caib.invai.api.persistence.repository.application.system_database.database.AppDatabaseCriteria;
import es.caib.invai.api.service.facade.AppDatabaseService;
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
 * Primary Inbound REST Adapter providing exposed endpoints for administrative operations
 * targeting the Application-to-Database relational registries under CAIB governance.
 *
 * @since 1.0.2
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("application/database")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class AppDatabaseController {

    private final AppDatabaseService appDatabaseService;

    /**
     * Retrieves a paginated sequence of application database registries scoped to a single parent
     * application, additionally filtered by dynamic criteria. The application identifier is a
     * mandatory path variable: this endpoint never lists every database link in the database.
     *
     * @param informationSystemDbId mandatory parent application identifier scoping the result set
     * @param criteria      the multi-parameter business query filter boundaries
     * @param pageable      pagination structural constraints
     * @return a paginated payload containing corresponding transfer representations
     */
    @GetMapping("/{informationSystemDbId}")
    public ResponseEntity<Page<AppDatabaseOutputDTO>> getAllByInformationSystemDbId(
            @PathVariable Long informationSystemDbId,
            @ModelAttribute AppDatabaseCriteria criteria,
            @PageableDefault(sort = "id") Pageable pageable) {
        log.info("REST: Initiating dynamic paginated search operation for informationSystemDb ID: {} and criteria: {}", informationSystemDbId, criteria);
        Page<AppDatabaseOutputDTO> targetPage = appDatabaseService.getAll(informationSystemDbId, criteria, pageable);
        return ResponseEntity.ok(targetPage);
    }

    /**
     * Executes a transactional instantiation command to persist a new tracking schema.
     *
     * @param inputDTO validated data configuration schema
     * @return outbound structural representation of the newly created entity
     */
    @PostMapping
    public ResponseEntity<AppDatabaseOutputDTO> create(@Valid @RequestBody AppDatabaseInputDTO inputDTO) {
        log.info("REST: Processing persistence request for new structural application database relation");
        AppDatabaseOutputDTO completedPayload = appDatabaseService.create(inputDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(completedPayload);
    }

    /**
     * Updates an active relational registry with modified enterprise metadata parameters.
     *
     * @param id primary corporate tracking reference key
     * @param inputDTO mutated parameter dataset structures
     * @return updated transfer data mapping payload state
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppDatabaseOutputDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AppDatabaseInputDTO inputDTO) {
        log.info("REST: Merging mutation parameters for tracking relation reference target identity: {}", id);
        return ResponseEntity.ok(appDatabaseService.update(id, inputDTO));
    }

    /**
     * Transitions a target tracking record into an inactive state by enforcing logical deletion structures.
     *
     * @param id target primary structural key to process for deprecation
     * @return an empty response body confirming success status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST: Triggering logical deletion lifecycle sequencing for identity context: {}", id);
        appDatabaseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
