package es.caib.invai.back.interna.catalog.mimeType;

import es.caib.invai.back.interna.catalog.mimeType.DTO.MimeTypeOutputDTO;
import es.caib.invai.back.service.facade.catalog.mimeType.MimeTypeService;
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
 * Internal REST controller exposing read-only access to the file format lookup.
 *
 * @since 1.0.6
 */
@Tag(name = "Catàleg", description = "Consulta del catàleg de formats d'arxiu admesos.")
@RestController
@Slf4j
@RequestMapping("mime-type")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class MimeTypeController {

    /** Service facade providing access to the file format lookup. */
    private final MimeTypeService mimeTypeService;

    /**
     * Creates the controller with its required service dependency.
     *
     * @param mimeTypeService the service facade used to resolve file format lookup entries
     */
    @Autowired
    public MimeTypeController(MimeTypeService mimeTypeService) {
        this.mimeTypeService = mimeTypeService;
    }

    /**
     * Resolves every file format lookup entry, sorted by extension.
     *
     * @return HTTP 200 with the full list of file format entries
     */
    @GetMapping
    public ResponseEntity<List<MimeTypeOutputDTO>> getAll() {
        log.debug("REST: Fetching every file format lookup entry");
        return ResponseEntity.ok(mimeTypeService.getAll());
    }
}
