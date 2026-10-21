package es.caib.invai.back.interna.catalog.documentStatus;

import es.caib.invai.back.interna.catalog.documentStatus.DTO.DocumentStatusOutputDTO;
import es.caib.invai.back.service.facade.catalog.documentStatus.DocumentStatusService;
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
 * Internal REST controller exposing read-only access to the document status lookup.
 *
 * @since 1.0.6
 */
@Tag(name = "Catàleg", description = "Consulta del catàleg d'estats del document.")
@RestController
@Slf4j
@RequestMapping("document-status")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class DocumentStatusController {

    /** Service facade providing access to the document status lookup. */
    private final DocumentStatusService documentStatusService;

    /**
     * Creates the controller with its required service dependency.
     *
     * @param documentStatusService the service facade used to resolve document status lookup entries
     */
    @Autowired
    public DocumentStatusController(DocumentStatusService documentStatusService) {
        this.documentStatusService = documentStatusService;
    }

    /**
     * Resolves every document status lookup entry, sorted by name.
     *
     * @return HTTP 200 with the full list of status entries
     */
    @GetMapping
    public ResponseEntity<List<DocumentStatusOutputDTO>> getAll() {
        log.debug("REST: Fetching every document status lookup entry");
        return ResponseEntity.ok(documentStatusService.getAll());
    }
}
