package es.caib.invai.api.interna.application.system_database.core;

import es.caib.invai.api.interna.application.system_database.core.DTO.AppInformationSystemDbInputDTO;
import es.caib.invai.api.interna.application.system_database.core.DTO.AppInformationSystemDbOutputDTO;
import es.caib.invai.api.service.facade.AppInformationSystemDbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Primary Inbound REST Adapter providing exposed endpoints for administrative operations
 * targeting the Application-to-Information-System-Database groupings under CAIB governance.
 *
 * @since 1.0.2
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("application/system-database")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class AppInformationSystemDbController {

    private final AppInformationSystemDbService appInformationSystemDbService;

    /**
     * Retrieves a paginated sequence of application information system database groupings scoped to
     * a single parent application. The application identifier is a mandatory path variable: this
     * endpoint never lists every grouping in the database.
     *
     * @param id mandatory parent application identifier scoping the result set
     * @return a paginated payload containing corresponding transfer representations
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppInformationSystemDbOutputDTO> getAllInformationSystemDbId(
            @PathVariable Long id) {
        log.info("REST: Initiating dynamic paginated search operation for Application ID: {}", id);
        AppInformationSystemDbOutputDTO targetPage = appInformationSystemDbService.getById(id);
        return ResponseEntity.ok(targetPage);
    }

    /**
     * Executes a transactional instantiation command to persist a new tracking schema.
     *
     * @param inputDTO validated data configuration schema
     * @return outbound structural representation of the newly created entity
     */
    @PostMapping
    public ResponseEntity<AppInformationSystemDbOutputDTO> create(@Valid @RequestBody AppInformationSystemDbInputDTO inputDTO) {
        log.info("REST: Processing persistence request for new application information system database grouping");
        AppInformationSystemDbOutputDTO completedPayload = appInformationSystemDbService.create(inputDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(completedPayload);
    }

    /**
     * Updates an active relational registry with modified enterprise metadata parameters.
     *
     * @param id       primary corporate tracking reference key
     * @param inputDTO mutated parameter dataset structures
     * @return updated transfer data mapping payload state
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppInformationSystemDbOutputDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AppInformationSystemDbInputDTO inputDTO) {
        log.info("REST: Merging mutation parameters for tracking reference target identity: {}", id);
        return ResponseEntity.ok(appInformationSystemDbService.update(id, inputDTO));
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
        appInformationSystemDbService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
