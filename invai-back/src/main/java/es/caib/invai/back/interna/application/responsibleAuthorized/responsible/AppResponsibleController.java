package es.caib.invai.back.interna.application.responsibleAuthorized.responsible;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleDeleteDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleOutputDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleCriteria;
import es.caib.invai.back.service.facade.application.responsibleAuthorized.responsible.AppResponsibleService;
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
 * Internal REST controller that manages lifecycle endpoints for AppResponsible assignments
 * on the "Responsables" tab of an application.
 *
 * @since 1.0.3
 */
@Slf4j
@Validated
@Tag(name = "Responsables d'aplicació", description = "Servei de gestió dels responsables associats a les aplicacions.")
@RestController
@RequiredArgsConstructor
@RequestMapping("application/responsible")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class AppResponsibleController {

    /** Facade service handling the business logic for AppResponsible operations. */
    private final AppResponsibleService appResponsibleService;

    /**
     * Retrieves a paginated, filtered listing of responsible assignments scoped to a single
     * "Responsables i Autoritzats" anchor. Unless the inactive status is explicitly requested, the
     * listing is driven by the full responsible type catalog: every registered type is always
     * returned, carrying its active holder when one exists or an empty placeholder otherwise.
     *
     * @param appResponsibleAuthorizedId mandatory parent anchor identifier scoping the result set
     * @param criteria optional filter values (status, person, responsible type, free-text search)
     * @param pageable pagination and sorting parameters
     * @return the matching page of responsible assignment rows
     */
    @GetMapping("/{appResponsibleAuthorizedId}")
    public ResponseEntity<Page<AppResponsibleOutputDTO>> getAllByAppResponsibleAuthorizedId(
            @PathVariable Long appResponsibleAuthorizedId,
            @ModelAttribute AppResponsibleCriteria criteria,
            @PageableDefault(sort = "id") Pageable pageable) {
        Page<AppResponsibleOutputDTO> page = appResponsibleService.getAll(appResponsibleAuthorizedId, criteria, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Creates a new responsible assignment. If another person already holds the requested
     * responsible type on the same anchor, it is automatically deactivated in favor of the new one.
     *
     * @param inputDTO validated create payload
     * @return the newly created assignment, with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<AppResponsibleOutputDTO> create(@Valid @RequestBody AppResponsibleInputDTO inputDTO) {
        AppResponsibleOutputDTO created = appResponsibleService.create(inputDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Updates an active responsible assignment (the responsible type is immutable; only the
     * person and job title can change).
     *
     * @param id identifier of the assignment to update
     * @param inputDTO validated update payload
     * @return the updated assignment
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppResponsibleOutputDTO> update(@PathVariable Long id, @Valid @RequestBody AppResponsibleInputDTO inputDTO) {
        return ResponseEntity.ok(appResponsibleService.update(id, inputDTO));
    }

    /**
     * Soft-deletes an active responsible assignment ("donar de baixa"), optionally capturing a
     * free-text observation.
     *
     * @param id identifier of the assignment to deactivate
     * @param dto optional payload carrying the deletion observation
     * @return an empty response body confirming success status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) AppResponsibleDeleteDTO dto) {
        appResponsibleService.delete(id, dto);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a previously deactivated responsible assignment, rejecting the operation if
     * another active assignment has since taken over the same responsible type on the same anchor.
     *
     * @param id identifier of the assignment to reactivate
     * @return the reactivated assignment
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<AppResponsibleOutputDTO> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(appResponsibleService.reactivate(id));
    }
}
