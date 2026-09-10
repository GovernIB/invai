package es.caib.invai.back.interna.application.security.measure;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.application.security.measure.DTO.AppSecurityMeasureInputDTO;
import es.caib.invai.back.interna.application.security.measure.DTO.AppSecurityMeasureOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.measure.AppSecurityMeasureCriteria;
import es.caib.invai.back.service.facade.application.security.measure.AppSecurityMeasureService;
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
 * targeting the Application-to-SecurityMeasure relational registries under CAIB governance.
 *
 * @since 1.0.4
 */
@Slf4j
@Validated
@Tag(name = "Mesures de seguretat", description = "Servei de gestió de les mesures de seguretat de les aplicacions.")
@RestController
@RequiredArgsConstructor
@RequestMapping("application/security/measure")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class AppSecurityMeasureController {

    /** Service facade handling business operations for application security measures. */
    private final AppSecurityMeasureService appSecurityMeasureService;

    /**
     * Retrieves a paginated sequence of application security measure registries scoped to a single
     * parent security anchor, additionally filtered by dynamic criteria. The security anchor
     * identifier is a mandatory path variable: this endpoint never lists every measure in the
     * database.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      the multi-parameter business query filter boundaries
     * @param pageable      pagination structural constraints
     * @return a paginated payload containing corresponding transfer representations
     */
    @GetMapping("/{appSecurityId}")
    public ResponseEntity<Page<AppSecurityMeasureOutputDTO>> getAllByAppSecurityId(
            @PathVariable Long appSecurityId,
            @ModelAttribute AppSecurityMeasureCriteria criteria,
            @PageableDefault(sort = "id") Pageable pageable) {
        log.debug("REST: Initiating dynamic paginated search operation for appSecurity ID: {} and criteria: {}", appSecurityId, criteria);
        Page<AppSecurityMeasureOutputDTO> targetPage = appSecurityMeasureService.getAll(appSecurityId, criteria, pageable);
        return ResponseEntity.ok(targetPage);
    }

    /**
     * Executes a transactional instantiation command to persist a new tracking schema.
     *
     * @param inputDTO validated data configuration schema
     * @return outbound structural representation of the newly created entity
     */
    @PostMapping
    public ResponseEntity<AppSecurityMeasureOutputDTO> create(@Valid @RequestBody AppSecurityMeasureInputDTO inputDTO) {
        log.info("REST: Processing persistence request for new structural application security measure relation");
        AppSecurityMeasureOutputDTO completedPayload = appSecurityMeasureService.create(inputDTO);
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
    public ResponseEntity<AppSecurityMeasureOutputDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AppSecurityMeasureInputDTO inputDTO) {
        log.info("REST: Merging mutation parameters for tracking relation reference target identity: {}", id);
        return ResponseEntity.ok(appSecurityMeasureService.update(id, inputDTO));
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
        appSecurityMeasureService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
