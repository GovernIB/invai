package es.caib.invai.api.interna.maintenance.serverType;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.api.interna.maintenance.serverType.DTO.ServerTypeOutputDTO;
import es.caib.invai.api.service.facade.ServerTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Internal REST controller exposing read-only access to the Server Type lookup dictionary
 * (e.g. DATABASE, APPLICATION).
 * <p>
 * Access is restricted at the type level to corporate users holding the {@code ROLE_usuari-tipus-E} role.
 * </p>
 *
 * @since 1.0.2
 */
@Tag(name = "Tipus de servidor", description = "Manteniment del catàleg de tipus de servidor.")
@RestController
@Slf4j
@RequestMapping("server-type")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class ServerTypeController {

    private final ServerTypeService serverTypeService;

    @Autowired
    public ServerTypeController(ServerTypeService serverTypeService) {
        this.serverTypeService = serverTypeService;
    }

    /**
     * Resolves every registered server type lookup entry, sorted by name.
     *
     * @return a {@link ResponseEntity} wrapping the full list of mapped {@link ServerTypeOutputDTO} entries
     */
    @GetMapping
    public ResponseEntity<List<ServerTypeOutputDTO>> getAll() {
        log.info("REST: Fetching every server type lookup entry");
        return ResponseEntity.ok(serverTypeService.getAll());
    }
}
