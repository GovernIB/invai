package es.caib.invai.api.interna.application.development.provider;

import es.caib.invai.api.interna.application.development.provider.DTO.AppProviderInputDTO;
import es.caib.invai.api.interna.application.development.provider.DTO.AppProviderOutputDTO;
import es.caib.invai.api.persistence.repository.application.development.provider.AppProviderCriteria;
import es.caib.invai.api.service.facade.AppProviderService;
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
 * targeting provider assignments linked to Application Development modules under CAIB governance.
 *
 * @since 1.0.2
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("application/development/provider")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class AppProviderController {

    private final AppProviderService appProviderService;

    /**
     * Retrieves a paginated sequence of provider records scoped to a single parent development
     * module, additionally filtered by dynamic criteria. The development identifier is a mandatory
     * path variable: this endpoint never lists every provider in the database.
     *
     * @param appDevelopmentId mandatory parent development identifier scoping the result set
     * @param criteria         the multi-parameter business query filter boundaries
     * @param pageable         pagination structural constraints
     * @return a paginated payload containing corresponding transfer representations
     */
    @GetMapping("/{appDevelopmentId}")
    public ResponseEntity<Page<AppProviderOutputDTO>> getAllByAppDevelopmentId(
            @PathVariable Long appDevelopmentId,
            @ModelAttribute AppProviderCriteria criteria,
            @PageableDefault(sort = "id") Pageable pageable) {
        log.info("REST: Initiating dynamic paginated search operation for Development ID: {} and criteria: {}", appDevelopmentId, criteria);
        Page<AppProviderOutputDTO> targetPage = appProviderService.getAll(appDevelopmentId, criteria, pageable);
        return ResponseEntity.ok(targetPage);
    }

    /**
     * Executes a transactional instantiation command to persist a new provider record.
     *
     * @param inputDTO validated data configuration schema
     * @return outbound structural representation of the newly created entity
     */
    @PostMapping
    public ResponseEntity<AppProviderOutputDTO> create(@Valid @RequestBody AppProviderInputDTO inputDTO) {
        log.info("REST: Processing persistence request for new provider assignment");
        AppProviderOutputDTO completedPayload = appProviderService.create(inputDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(completedPayload);
    }

    /**
     * Updates an active provider registry with modified metadata parameters.
     *
     * @param id       primary corporate tracking reference key
     * @param inputDTO mutated parameter dataset structures
     * @return updated transfer data mapping payload state
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppProviderOutputDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AppProviderInputDTO inputDTO) {
        log.info("REST: Merging mutation parameters for provider target identity: {}", id);
        return ResponseEntity.ok(appProviderService.update(id, inputDTO));
    }

    /**
     * Transitions a target provider record into an inactive state by enforcing logical deletion structures.
     *
     * @param id target primary structural key to process for deprecation
     * @return an empty response body confirming success status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST: Triggering logical deletion lifecycle sequencing for identity context: {}", id);
        appProviderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
