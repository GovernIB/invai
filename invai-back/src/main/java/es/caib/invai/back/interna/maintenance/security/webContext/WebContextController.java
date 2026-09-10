package es.caib.invai.back.interna.maintenance.security.webContext;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.security.webContext.DTO.WebContextInputDTO;
import es.caib.invai.back.interna.maintenance.security.webContext.DTO.WebContextOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.security.webContext.WebContextCriteria;
import es.caib.invai.back.service.facade.maintenance.security.webContext.WebContextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Internal REST controller that manages lifecycle endpoints and routing rules for WebContext assets.
 *
 * @since 1.0.4
 */
@Tag(name = "Contextos web", description = "Manteniment del catàleg de contextos web.")
@RestController
@Slf4j
@RequestMapping("web-context")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class WebContextController {

    /** Service facade handling WebContext business use cases. */
    private final WebContextService webContextService;

    /**
     * Creates the controller with its required service dependency.
     *
     * @param webContextService the service facade to delegate business operations to
     */
    @Autowired
    public WebContextController(WebContextService webContextService) {
        this.webContextService = webContextService;
    }

    /**
     * Retrieves a web context by its identifier.
     *
     * @param id the web context identifier
     * @return the matching web context
     */
    @GetMapping("/{id}")
    public ResponseEntity<WebContextOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(webContextService.getById(id));
    }

    /**
     * Retrieves a paginated, filtered list of web contexts.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching web contexts
     */
    @GetMapping
    public ResponseEntity<Page<WebContextOutputDTO>> getAll(
            @ModelAttribute WebContextCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(webContextService.getAll(filter, pageable));
    }

    /**
     * Creates a new web context.
     *
     * @param inputDTO the data for the new web context
     * @return the created web context, with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<WebContextOutputDTO> create(@Valid @RequestBody WebContextInputDTO inputDTO) {
        return new ResponseEntity<>(webContextService.create(inputDTO), HttpStatus.CREATED);
    }

    /**
     * Updates an existing web context.
     *
     * @param id the identifier of the web context to update
     * @param inputDTO the new data to apply
     * @return the updated web context
     */
    @PutMapping("/{id}")
    public ResponseEntity<WebContextOutputDTO> update(@PathVariable Long id, @Valid @RequestBody WebContextInputDTO inputDTO) {
        return ResponseEntity.ok(webContextService.update(id, inputDTO));
    }

    /**
     * Logically deletes a web context by its identifier.
     *
     * @param id the identifier of the web context to delete
     * @return an empty response with HTTP 204 status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        webContextService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a previously deleted web context.
     *
     * @param id the identifier of the web context to reactivate
     * @return the reactivated web context
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<WebContextOutputDTO> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(webContextService.reactivate(id));
    }
}
