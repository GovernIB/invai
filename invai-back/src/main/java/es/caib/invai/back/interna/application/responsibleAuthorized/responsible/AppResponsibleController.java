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
     * Retrieves a paginated sequence of responsible assignment records scoped to a single parent
     * "Responsables i Autoritzats" anchor, additionally filtered by dynamic criteria.
     *
     * @param appResponsibleAuthorizedId mandatory parent anchor identifier scoping the result set
     * @param criteria the multi-parameter business query filter boundaries
     * @param pageable pagination structural constraints
     * @return a paginated payload containing corresponding transfer representations
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
     * Registers a new responsible assignment.
     *
     * @param inputDTO validated data configuration schema
     * @return outbound structural representation of the newly created assignment, with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<AppResponsibleOutputDTO> create(@Valid @RequestBody AppResponsibleInputDTO inputDTO) {
        AppResponsibleOutputDTO created = appResponsibleService.create(inputDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Updates an active responsible assignment.
     *
     * @param id primary tracking reference key
     * @param inputDTO mutated parameter dataset
     * @return the updated assignment
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppResponsibleOutputDTO> update(@PathVariable Long id, @Valid @RequestBody AppResponsibleInputDTO inputDTO) {
        return ResponseEntity.ok(appResponsibleService.update(id, inputDTO));
    }

    /**
     * Logically deletes a responsible assignment ("donar de baixa"), optionally capturing a
     * free-text observation.
     *
     * @param id target primary structural key to delete
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
     * Reactivates a previously deleted responsible assignment.
     *
     * @param id target primary structural key to reactivate
     * @return the reactivated assignment
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<AppResponsibleOutputDTO> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(appResponsibleService.reactivate(id));
    }
}
