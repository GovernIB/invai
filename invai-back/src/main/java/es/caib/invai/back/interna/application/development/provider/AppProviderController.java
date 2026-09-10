package es.caib.invai.back.interna.application.development.provider;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.application.development.provider.DTO.AppProviderInputDTO;
import es.caib.invai.back.interna.application.development.provider.DTO.AppProviderOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.provider.AppProviderCriteria;
import es.caib.invai.back.service.facade.application.development.provider.AppProviderService;
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
 * REST endpoints for the "AppProvider" list: provider (company) assignments hanging many-to-one off
 * a single {@code AppDevelopment} record. All endpoints require {@code ROLE_INV_SUPER}.
 *
 * @since 1.0.2
 */
@Slf4j
@Validated
@Tag(name = "Proveïdors d'aplicació", description = "Servei de gestió dels proveïdors associats als desenvolupaments de les aplicacions.")
@RestController
@RequiredArgsConstructor
@RequestMapping("application/development/provider")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class AppProviderController {

    /** Facade service handling the business logic for AppProvider operations. */
    private final AppProviderService appProviderService;

    /**
     * Fetches the providers assigned to a single development, filtered by {@code criteria} and
     * paged. {@code appDevelopmentId} is always applied, so this never returns providers belonging
     * to another development module.
     *
     * @param appDevelopmentId identifier of the owning development record
     * @param criteria         optional additional filters (status, search)
     * @param pageable         pagination and sorting parameters, defaulting to sort by {@code id}
     * @return 200 with a page of mapped provider DTOs
     */
    @GetMapping("/{appDevelopmentId}")
    public ResponseEntity<Page<AppProviderOutputDTO>> getAllByAppDevelopmentId(
            @PathVariable Long appDevelopmentId,
            @ModelAttribute AppProviderCriteria criteria,
            @PageableDefault(sort = "id") Pageable pageable) {
        log.debug("REST: Initiating dynamic paginated search operation for Development ID: {} and criteria: {}", appDevelopmentId, criteria);
        Page<AppProviderOutputDTO> targetPage = appProviderService.getAll(appDevelopmentId, criteria, pageable);
        return ResponseEntity.ok(targetPage);
    }

    /**
     * Creates a new provider assignment. Duplicates (same company/role on the same development)
     * are not rejected.
     *
     * @param inputDTO the development reference, company name, role, and contract dates
     * @return 201 with the mapped provider DTO
     */
    @PostMapping
    public ResponseEntity<AppProviderOutputDTO> create(@Valid @RequestBody AppProviderInputDTO inputDTO) {
        log.info("REST: Processing persistence request for new provider assignment");
        AppProviderOutputDTO completedPayload = appProviderService.create(inputDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(completedPayload);
    }

    /**
     * Updates an existing provider record in place.
     *
     * @param id       the provider record's own identifier
     * @param inputDTO the replacement field values
     * @return 200 with the mapped provider DTO reflecting the applied changes
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppProviderOutputDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AppProviderInputDTO inputDTO) {
        log.info("REST: Merging mutation parameters for provider target identity: {}", id);
        return ResponseEntity.ok(appProviderService.update(id, inputDTO));
    }

    /**
     * Soft-deletes a provider record (stamps {@code deletedAt}/{@code deletedBy}); the row itself
     * is not removed.
     *
     * @param id the provider record's own identifier
     * @return 204 with no body
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST: Triggering logical deletion lifecycle sequencing for identity context: {}", id);
        appProviderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
