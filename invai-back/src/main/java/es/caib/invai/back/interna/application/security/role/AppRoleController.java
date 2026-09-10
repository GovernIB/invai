package es.caib.invai.back.interna.application.security.role;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.application.security.role.DTO.AppRoleInputDTO;
import es.caib.invai.back.interna.application.security.role.DTO.AppRoleOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.role.AppRoleCriteria;
import es.caib.invai.back.service.facade.application.security.role.AppRoleService;
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
 * targeting the Application-to-Security-Role relational registries under CAIB governance.
 *
 * @since 1.0.4
 */
@Slf4j
@Validated
@Tag(name = "Rols d'aplicació", description = "Servei de gestió dels rols de seguretat assignats a les aplicacions.")
@RestController
@RequiredArgsConstructor
@RequestMapping("application/security/role")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class AppRoleController {

    /** Service facade handling business operations for application-role assignments. */
    private final AppRoleService appRoleService;

    /**
     * Retrieves a paginated sequence of application role registries scoped to a single parent
     * security anchor, additionally filtered by dynamic criteria. The security anchor identifier is a
     * mandatory path variable: this endpoint never lists every role assignment in the database.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      the multi-parameter business query filter boundaries
     * @param pageable      pagination structural constraints
     * @return a paginated payload containing corresponding transfer representations
     */
    @GetMapping("/{appSecurityId}")
    public ResponseEntity<Page<AppRoleOutputDTO>> getAllByAppSecurityId(
            @PathVariable Long appSecurityId,
            @ModelAttribute AppRoleCriteria criteria,
            @PageableDefault(sort = "id") Pageable pageable) {
        log.debug("REST: Initiating dynamic paginated search operation for appSecurity ID: {} and criteria: {}", appSecurityId, criteria);
        Page<AppRoleOutputDTO> targetPage = appRoleService.getAll(appSecurityId, criteria, pageable);
        return ResponseEntity.ok(targetPage);
    }

    /**
     * Executes a transactional instantiation command to persist a new tracking schema.
     *
     * @param inputDTO validated data configuration schema
     * @return outbound structural representation of the newly created entity
     */
    @PostMapping
    public ResponseEntity<AppRoleOutputDTO> create(@Valid @RequestBody AppRoleInputDTO inputDTO) {
        log.info("REST: Processing persistence request for new structural application role relation");
        AppRoleOutputDTO completedPayload = appRoleService.create(inputDTO);
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
    public ResponseEntity<AppRoleOutputDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AppRoleInputDTO inputDTO) {
        log.info("REST: Merging mutation parameters for tracking relation reference target identity: {}", id);
        return ResponseEntity.ok(appRoleService.update(id, inputDTO));
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
        appRoleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
