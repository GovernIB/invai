package es.caib.invai.back.interna.maintenance.security.securityMeasureType;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.security.securityMeasureType.DTO.SecurityMeasureTypeInputDTO;
import es.caib.invai.back.interna.maintenance.security.securityMeasureType.DTO.SecurityMeasureTypeOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.security.securityMeasureType.SecurityMeasureTypeCriteria;
import es.caib.invai.back.service.facade.maintenance.security.securityMeasureType.SecurityMeasureTypeService;
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
 * Internal REST controller that manages lifecycle endpoints and routing rules for SecurityMeasureType assets.
 *
 * @since 1.0.4
 */
@Tag(name = "Tipus de mesures de seguretat", description = "Manteniment del catàleg de tipus de mesures de seguretat.")
@RestController
@Slf4j
@RequestMapping("security-measure-type")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class SecurityMeasureTypeController {

    /** Service facade handling SecurityMeasureType business use cases. */
    private final SecurityMeasureTypeService securityMeasureTypeService;

    /**
     * Creates the controller with its required service dependency.
     *
     * @param securityMeasureTypeService the service facade to delegate business operations to
     */
    @Autowired
    public SecurityMeasureTypeController(SecurityMeasureTypeService securityMeasureTypeService) {
        this.securityMeasureTypeService = securityMeasureTypeService;
    }

    /**
     * Retrieves a security measure type entry by its identifier.
     *
     * @param id the security measure type entry identifier
     * @return the matching security measure type entry
     */
    @GetMapping("/{id}")
    public ResponseEntity<SecurityMeasureTypeOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(securityMeasureTypeService.getById(id));
    }

    /**
     * Retrieves a paginated, filtered list of security measure type entries.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching security measure type entries
     */
    @GetMapping
    public ResponseEntity<Page<SecurityMeasureTypeOutputDTO>> getAll(
            @ModelAttribute SecurityMeasureTypeCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(securityMeasureTypeService.getAll(filter, pageable));
    }

    /**
     * Creates a new security measure type entry.
     *
     * @param inputDTO the data for the new security measure type entry
     * @return the created security measure type entry, with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<SecurityMeasureTypeOutputDTO> create(@Valid @RequestBody SecurityMeasureTypeInputDTO inputDTO) {
        return new ResponseEntity<>(securityMeasureTypeService.create(inputDTO), HttpStatus.CREATED);
    }

    /**
     * Updates an existing security measure type entry.
     *
     * @param id the identifier of the security measure type entry to update
     * @param inputDTO the new data to apply
     * @return the updated security measure type entry
     */
    @PutMapping("/{id}")
    public ResponseEntity<SecurityMeasureTypeOutputDTO> update(@PathVariable Long id, @Valid @RequestBody SecurityMeasureTypeInputDTO inputDTO) {
        return ResponseEntity.ok(securityMeasureTypeService.update(id, inputDTO));
    }

    /**
     * Logically deletes a security measure type entry by its identifier.
     *
     * @param id the identifier of the security measure type entry to delete
     * @return an empty response with HTTP 204 status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        securityMeasureTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a previously deleted security measure type entry.
     *
     * @param id the identifier of the security measure type entry to reactivate
     * @return the reactivated security measure type entry
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<SecurityMeasureTypeOutputDTO> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(securityMeasureTypeService.reactivate(id));
    }
}
