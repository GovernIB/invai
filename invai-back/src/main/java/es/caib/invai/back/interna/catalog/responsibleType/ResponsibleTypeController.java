package es.caib.invai.back.interna.catalog.responsibleType;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.catalog.responsibleType.DTO.ResponsibleTypeOutputDTO;
import es.caib.invai.back.service.facade.catalog.responsibleType.ResponsibleTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Internal REST controller exposing read-only access to the Responsible Type lookup dictionary.
 *
 * @since 1.0.3
 */
@Tag(name = "Responsables", description = "Manteniment del catàleg de tipus de responsable.")
@RestController
@Slf4j
@RequestMapping("responsible-type")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class ResponsibleTypeController {

    /** Service facade providing access to the Responsible Type lookup dictionary. */
    private final ResponsibleTypeService responsibleTypeService;

    /**
     * Creates the controller with its required service dependency.
     *
     * @param responsibleTypeService the service facade used to resolve responsible type lookup entries
     */
    @Autowired
    public ResponsibleTypeController(ResponsibleTypeService responsibleTypeService) {
        this.responsibleTypeService = responsibleTypeService;
    }

    /**
     * Resolves every registered responsible type lookup entry, sorted by name.
     *
     * @return a {@link ResponseEntity} wrapping the full list of mapped {@link ResponsibleTypeOutputDTO} entries
     */
    @GetMapping
    public ResponseEntity<List<ResponsibleTypeOutputDTO>> getAll() {
        log.debug("REST: Fetching every responsible type lookup entry");
        return ResponseEntity.ok(responsibleTypeService.getAll());
    }
}
