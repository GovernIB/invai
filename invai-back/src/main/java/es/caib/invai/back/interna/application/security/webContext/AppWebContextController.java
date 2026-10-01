package es.caib.invai.back.interna.application.security.webContext;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.application.development.webContext.DTO.AppWebContextOutputDTO;
import es.caib.invai.back.interna.application.security.webContext.DTO.AppWebContextValidateInputDTO;
import es.caib.invai.back.persistence.repository.application.security.webContext.AppWebContextCriteria;
import es.caib.invai.back.service.facade.application.security.webContext.AppWebContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Primary Inbound REST Adapter providing Security's view of the Application-to-Web-Context
 * relational registries: a read-only listing plus the one-way validation action. Creating,
 * editing, and deleting web context assignments is only exposed from Development - see
 * {@code interna.application.development.webContext.AppWebContextController}.
 *
 * @since 1.0.4
 */
@Slf4j
@Validated
@Tag(name = "Contextos web d'aplicació", description = "Servei de consulta i validació dels contextos web assignats a les aplicacions.")
// Explicit bean name: Spring's default naming only looks at the simple class name, which collides
// with interna.application.development.webContext.AppWebContextController otherwise.
@RestController("securityAppWebContextController")
@RequiredArgsConstructor
@RequestMapping("application/security/web-context")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class AppWebContextController {

    /** Service facade handling business operations for application-web context links. */
    private final AppWebContextService appWebContextService;

    /**
     * Retrieves a paginated sequence of application web context registries scoped to a single parent
     * security anchor, additionally filtered by dynamic criteria. The security anchor identifier is a
     * mandatory path variable: this endpoint never lists every web context link in the database.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      the multi-parameter business query filter boundaries
     * @param pageable      pagination structural constraints
     * @return a paginated payload containing corresponding transfer representations
     */
    @GetMapping("/{appSecurityId}")
    public ResponseEntity<Page<AppWebContextOutputDTO>> getAllByAppSecurityId(
            @PathVariable Long appSecurityId,
            @ModelAttribute AppWebContextCriteria criteria,
            @PageableDefault(sort = "id") Pageable pageable) {
        log.debug("REST: Initiating dynamic paginated search operation for appSecurity ID: {} and criteria: {}", appSecurityId, criteria);
        Page<AppWebContextOutputDTO> targetPage = appWebContextService.getAll(appSecurityId, criteria, pageable);
        return ResponseEntity.ok(targetPage);
    }

    /**
     * Marks a web context assignment as validated, recording the moment, the acting user, and the
     * given justification. One-way: once validated, this endpoint refuses to touch it again.
     *
     * @param id       identifier of the web context assignment to validate
     * @param inputDTO payload carrying the mandatory free-text validation justification
     * @return an empty response body confirming success status
     */
    @PutMapping("validate/{id}")
    public ResponseEntity<Void> validate(
            @PathVariable Long id,
            @Valid @RequestBody AppWebContextValidateInputDTO inputDTO) {
        log.info("REST: Request to validate application web context ID: {}", id);
        appWebContextService.validate(id, inputDTO);
        return ResponseEntity.noContent().build();
    }
}
