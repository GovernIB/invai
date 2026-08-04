package es.caib.invai.api.interna.maintenance.role;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.api.interna.maintenance.role.DTO.RoleInputDTO;
import es.caib.invai.api.interna.maintenance.role.DTO.RoleOutputDTO;
import es.caib.invai.api.persistence.repository.role.RoleCriteria;
import es.caib.invai.api.service.facade.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Internal REST controller that manages lifecycle endpoints and routing rules for provider Role assets.
 *
 * @since 1.0.2
 */
@Tag(name = "Rols", description = "Manteniment del catàleg de rols.")
@RestController
@Slf4j
@RequestMapping("role")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<RoleOutputDTO>> getAll(
            @ModelAttribute RoleCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(roleService.getAll(filter, pageable));
    }

    @PostMapping
    public ResponseEntity<RoleOutputDTO> create(@Valid @RequestBody RoleInputDTO inputDTO) {
        return new ResponseEntity<>(roleService.create(inputDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleOutputDTO> update(@PathVariable Long id, @Valid @RequestBody RoleInputDTO inputDTO) {
        return ResponseEntity.ok(roleService.update(id, inputDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("reactivate/{id}")
    public ResponseEntity<RoleOutputDTO> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.reactivate(id));
    }
}
