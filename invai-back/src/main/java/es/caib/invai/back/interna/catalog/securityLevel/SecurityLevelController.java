package es.caib.invai.back.interna.catalog.securityLevel;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.catalog.securityLevel.DTO.SecurityLevelOutputDTO;
import es.caib.invai.back.service.facade.catalog.securityLevel.SecurityLevelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Internal REST controller exposing read-only access to the Security level lookup dictionary.
 *
 * @since 1.0.4
 */
@Tag(name = "Catàleg", description = "Consulta del catàleg de nivells de seguretat.")
@RestController
@Slf4j
@RequestMapping("security-level")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class SecurityLevelController {

    /** Service facade providing access to the Security level lookup dictionary. */
    private final SecurityLevelService securityLevelService;

    /**
     * Creates the controller with its required service dependency.
     *
     * @param securityLevelService the service facade used to resolve security level lookup entries
     */
    @Autowired
    public SecurityLevelController(SecurityLevelService securityLevelService) {
        this.securityLevelService = securityLevelService;
    }

    /**
     * Resolves every registered security level lookup entry, sorted by name.
     *
     * @return a {@link ResponseEntity} wrapping the full list of mapped {@link SecurityLevelOutputDTO} entries
     */
    @GetMapping
    public ResponseEntity<List<SecurityLevelOutputDTO>> getAll() {
        log.debug("REST: Fetching every security level lookup entry");
        return ResponseEntity.ok(securityLevelService.getAll());
    }
}
