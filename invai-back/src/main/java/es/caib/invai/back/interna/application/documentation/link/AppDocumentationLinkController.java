package es.caib.invai.back.interna.application.documentation.link;

import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkInputDTO;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkOutputDTO;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkUpdateInputDTO;
import es.caib.invai.back.persistence.repository.application.documentation.link.AppDocumentationLinkCriteria;
import es.caib.invai.back.service.facade.application.documentation.link.AppDocumentationLinkService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal REST controller exposing the links of applications (the "Enllaços" list of the
 * Documentació tab).
 *
 * @since 1.0.6
 */
@Tag(name = "Enllaços", description = "Gestió dels enllaços de les aplicacions.")
@RestController
@Slf4j
@RequestMapping("application/documentation/link")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class AppDocumentationLinkController {

    /** Facade service handling the business use cases of the links. */
    private final AppDocumentationLinkService appDocumentationLinkService;

    /**
     * Creates a controller instance with the given service dependency injected.
     *
     * @param appDocumentationLinkService the facade service used to perform link operations
     */
    @Autowired
    public AppDocumentationLinkController(AppDocumentationLinkService appDocumentationLinkService) {
        this.appDocumentationLinkService = appDocumentationLinkService;
    }

    /**
     * Retrieves a paginated list of the links of an application. It lists the deleted links too
     * ({@code deletedAt} tells them apart); the {@code deleted} filter keeps only the deleted ones
     * ({@code true}) or only the others ({@code false}). It can be filtered by {@code title},
     * {@code description}, {@code sectionId} and {@code url}, searched with {@code quickSearch}
     * across all of them, and sorted by {@code title}, {@code description}, {@code section} and {@code url}.
     *
     * @param applicationId identifier of the application whose links are listed
     * @param filter        search and filter criteria
     * @param pageable      pagination and sorting information
     * @return HTTP 200 with the matching page of links
     */
    @GetMapping
    public ResponseEntity<Page<AppDocumentationLinkOutputDTO>> getAll(
            @RequestParam Long applicationId,
            @ModelAttribute AppDocumentationLinkCriteria filter,
            @PageableDefault(sort = "title") Pageable pageable) {
        filter.setApplicationId(applicationId);
        return ResponseEntity.ok(appDocumentationLinkService.getAll(filter, pageable));
    }

    /**
     * Creates a link of an application.
     *
     * @param inputDTO the data of the link
     * @return HTTP 201 with the created link
     */
    @PostMapping
    public ResponseEntity<AppDocumentationLinkOutputDTO> create(@Valid @RequestBody AppDocumentationLinkInputDTO inputDTO) {
        return new ResponseEntity<>(appDocumentationLinkService.create(inputDTO), HttpStatus.CREATED);
    }

    /**
     * Updates a link.
     *
     * @param id       the identifier of the link
     * @param inputDTO the new data
     * @return HTTP 200 with the updated link
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppDocumentationLinkOutputDTO> update(@PathVariable Long id, @Valid @RequestBody AppDocumentationLinkUpdateInputDTO inputDTO) {
        return ResponseEntity.ok(appDocumentationLinkService.update(id, inputDTO));
    }

    /**
     * Logically deletes a link.
     *
     * @param id the identifier of the link
     * @return HTTP 204 with no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appDocumentationLinkService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
