package es.caib.invai.back.interna.application.integration.core;

import es.caib.invai.back.interna.application.integration.core.DTO.AppIntegrationInputDTO;
import es.caib.invai.back.interna.application.integration.core.DTO.AppIntegrationOutputDTO;
import es.caib.invai.back.service.facade.application.integration.core.AppIntegrationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Internal REST controller managing the "Integracio" anchor.
 *
 * @since 1.0.5
 */
@Tag(name = "Integracio", description = "Ancoratge de la pestanya d'integracions d'una aplicació.")
@RestController
@Slf4j
@RequestMapping("application/integration")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class AppIntegrationController {

    /** Facade service handling the business use cases for the integration anchor. */
    private final AppIntegrationService appIntegrationService;

    @Autowired
    public AppIntegrationController(AppIntegrationService appIntegrationService) {
        this.appIntegrationService = appIntegrationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppIntegrationOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(appIntegrationService.getById(id));
    }

    @PostMapping
    public ResponseEntity<AppIntegrationOutputDTO> create(@Valid @RequestBody AppIntegrationInputDTO inputDTO) {
        return new ResponseEntity<>(appIntegrationService.create(inputDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppIntegrationOutputDTO> update(@PathVariable Long id, @Valid @RequestBody AppIntegrationInputDTO inputDTO) {
        return ResponseEntity.ok(appIntegrationService.update(id, inputDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appIntegrationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
