package es.caib.invai.back.interna.maintenance.systems.serverType;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.systems.serverType.DTO.ServerTypeOutputDTO;
import es.caib.invai.back.service.facade.maintenance.systems.serverType.ServerTypeService;
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

    /** Facade service handling the business logic for Server Type lookup read operations. */
    private final ServerTypeService serverTypeService;

    /**
     * Creates a new controller instance with the given service dependency.
     *
     * @param serverTypeService the facade service used to read Server Type lookup entries
     */
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
