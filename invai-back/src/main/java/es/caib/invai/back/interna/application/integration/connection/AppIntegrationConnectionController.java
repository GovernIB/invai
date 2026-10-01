package es.caib.invai.back.interna.application.integration.connection;

import es.caib.invai.back.interna.application.integration.connection.DTO.AppIntegrationConnectionInputDTO;
import es.caib.invai.back.interna.application.integration.connection.DTO.AppIntegrationConnectionOutputDTO;
import es.caib.invai.back.persistence.repository.application.integration.connection.AppIntegrationConnectionCriteria;
import es.caib.invai.back.service.facade.application.integration.connection.AppIntegrationConnectionService;
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
import org.springframework.web.bind.annotation.*;

/**
 * Internal REST controller managing integration connections (rows of the "Integracio" tab's table).
 *
 * @since 1.0.5
 */
@Tag(name = "Integracio - Files", description = "Files de la taula d'integracions d'una aplicació.")
@RestController
@Slf4j
@RequestMapping("application/integration-connection")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class AppIntegrationConnectionController {

    private final AppIntegrationConnectionService appIntegrationConnectionService;

    @Autowired
    public AppIntegrationConnectionController(AppIntegrationConnectionService appIntegrationConnectionService) {
        this.appIntegrationConnectionService = appIntegrationConnectionService;
    }

    @GetMapping
    public ResponseEntity<Page<AppIntegrationConnectionOutputDTO>> getAll(
            @RequestParam Long appIntegrationId,
            @ModelAttribute AppIntegrationConnectionCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(appIntegrationConnectionService.getAll(appIntegrationId, filter, pageable));
    }

    @PostMapping
    public ResponseEntity<AppIntegrationConnectionOutputDTO> create(@Valid @RequestBody AppIntegrationConnectionInputDTO inputDTO) {
        return new ResponseEntity<>(appIntegrationConnectionService.create(inputDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppIntegrationConnectionOutputDTO> update(@PathVariable Long id, @Valid @RequestBody AppIntegrationConnectionInputDTO inputDTO) {
        return ResponseEntity.ok(appIntegrationConnectionService.update(id, inputDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appIntegrationConnectionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
