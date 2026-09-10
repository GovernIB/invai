package es.caib.invai.back.service.facade.application.security.measure;

import es.caib.invai.back.interna.application.security.measure.DTO.AppSecurityMeasureInputDTO;
import es.caib.invai.back.interna.application.security.measure.DTO.AppSecurityMeasureOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.measure.AppSecurityMeasureCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Domain Boundary Outbound Port interfacing the internal transactional domain operations.
 *
 * @since 1.0.4
 */
public interface AppSecurityMeasureService {

    /**
     * Retrieves a paginated sequence of security measures scoped to a single parent security anchor.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      the multi-parameter business query filter boundaries
     * @param pageable      pagination structural constraints
     * @return a paginated payload containing corresponding transfer representations
     */
    Page<AppSecurityMeasureOutputDTO> getAll(Long appSecurityId, AppSecurityMeasureCriteria criteria, Pageable pageable);

    /**
     * Creates a new security measure from the given input payload.
     *
     * @param inputDTO the creation payload
     * @return the created measure, mapped to its output transfer representation
     */
    AppSecurityMeasureOutputDTO create(AppSecurityMeasureInputDTO inputDTO);

    /**
     * Updates the security measure identified by {@code id} with the given input payload.
     *
     * @param id       identifier of the measure to update
     * @param inputDTO the update payload
     * @return the updated measure, mapped to its output transfer representation
     */
    AppSecurityMeasureOutputDTO update(Long id, AppSecurityMeasureInputDTO inputDTO);

    /**
     * Deletes (logically) the security measure identified by {@code id}.
     *
     * @param id identifier of the measure to delete
     */
    void delete(Long id);
}
