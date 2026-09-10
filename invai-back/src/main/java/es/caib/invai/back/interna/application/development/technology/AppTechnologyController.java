package es.caib.invai.back.interna.application.development.technology;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.application.development.technology.DTO.AppTechnologyInputDTO;
import es.caib.invai.back.interna.application.development.technology.DTO.AppTechnologyOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.technology.AppTechnologyCriteria;
import es.caib.invai.back.service.facade.application.development.technology.AppTechnologyService;
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
 * REST endpoints for the "AppTechnology" list: technology stack entries hanging many-to-one off a
 * single {@code AppDevelopment} record. All endpoints require {@code ROLE_INV_SUPER}.
 *
 * @since 1.0.2
 */
@Slf4j
@Validated
@Tag(name = "Tecnologies d'aplicació", description = "Servei de gestió de les tecnologies associades als desenvolupaments de les aplicacions.")
@RestController
@RequiredArgsConstructor
@RequestMapping("application/development/technology")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class AppTechnologyController {

    /** Facade service handling the business logic for AppTechnology operations. */
    private final AppTechnologyService appTechnologyService;

    /**
     * Fetches the technology entries assigned to a single development, filtered by {@code criteria}
     * and paged. {@code appDevelopmentId} is always applied, so this never returns entries belonging
     * to another development module.
     *
     * @param appDevelopmentId identifier of the owning development record
     * @param criteria         optional additional filters (status, search)
     * @param pageable         pagination and sorting parameters, defaulting to sort by {@code id}
     * @return 200 with a page of mapped technology DTOs
     */
    @GetMapping("/{appDevelopmentId}")
    public ResponseEntity<Page<AppTechnologyOutputDTO>> getAllByAppDevelopmentId(
            @PathVariable Long appDevelopmentId,
            @ModelAttribute AppTechnologyCriteria criteria,
            @PageableDefault(sort = "id") Pageable pageable) {
        log.debug("REST: Initiating dynamic paginated search operation for Development ID: {} and criteria: {}", appDevelopmentId, criteria);
        Page<AppTechnologyOutputDTO> targetPage = appTechnologyService.getAll(appDevelopmentId, criteria, pageable);
        return ResponseEntity.ok(targetPage);
    }

    /**
     * Creates a new technology stack entry. Duplicates (same layer/technology on the same
     * development) are not rejected.
     *
     * @param inputDTO the development reference, layer, technology, version and architecture
     * @return 201 with the mapped technology DTO
     */
    @PostMapping
    public ResponseEntity<AppTechnologyOutputDTO> create(@Valid @RequestBody AppTechnologyInputDTO inputDTO) {
        log.info("REST: Processing persistence request for new technology entry");
        AppTechnologyOutputDTO completedPayload = appTechnologyService.create(inputDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(completedPayload);
    }

    /**
     * Updates an existing technology record in place.
     *
     * @param id       the technology record's own identifier
     * @param inputDTO the replacement field values
     * @return 200 with the mapped technology DTO reflecting the applied changes
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppTechnologyOutputDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AppTechnologyInputDTO inputDTO) {
        log.info("REST: Merging mutation parameters for technology target identity: {}", id);
        return ResponseEntity.ok(appTechnologyService.update(id, inputDTO));
    }

    /**
     * Soft-deletes a technology record (stamps {@code deletedAt}/{@code deletedBy}); the row itself
     * is not removed.
     *
     * @param id the technology record's own identifier
     * @return 204 with no body
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST: Triggering logical deletion lifecycle sequencing for identity context: {}", id);
        appTechnologyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
