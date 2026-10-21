package es.caib.invai.back.interna.catalog.appSection;

import es.caib.invai.back.interna.catalog.appSection.DTO.AppSectionOutputDTO;
import es.caib.invai.back.service.facade.catalog.appSection.AppSectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Internal REST controller exposing read-only access to the application section lookup.
 *
 * @since 1.0.6
 */
@Tag(name = "Catàleg", description = "Consulta del catàleg de seccions de l'aplicació.")
@RestController
@Slf4j
@RequestMapping("app-section")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class AppSectionController {

    /** Service facade providing access to the section lookup. */
    private final AppSectionService appSectionService;

    /**
     * Creates the controller with its required service dependency.
     *
     * @param appSectionService the service facade used to resolve section lookup entries
     */
    @Autowired
    public AppSectionController(AppSectionService appSectionService) {
        this.appSectionService = appSectionService;
    }

    /**
     * Resolves every application section lookup entry, sorted by name.
     *
     * @return HTTP 200 with the full list of section entries
     */
    @GetMapping
    public ResponseEntity<List<AppSectionOutputDTO>> getAll() {
        log.debug("REST: Fetching every application section lookup entry");
        return ResponseEntity.ok(appSectionService.getAll());
    }
}
