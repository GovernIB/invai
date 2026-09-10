package es.caib.invai.back.service.facade.application.security.risk;

import es.caib.invai.back.interna.application.security.risk.DTO.AppSecurityRiskInputDTO;
import es.caib.invai.back.interna.application.security.risk.DTO.AppSecurityRiskOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.risk.AppSecurityRiskCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Domain Boundary Outbound Port interfacing the internal transactional domain operations.
 *
 * @since 1.0.4
 */
public interface AppSecurityRiskService {

    /**
     * Retrieves a paginated sequence of security risks scoped to a single parent security anchor.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      the multi-parameter business query filter boundaries
     * @param pageable      pagination structural constraints
     * @return a paginated payload containing corresponding transfer representations
     */
    Page<AppSecurityRiskOutputDTO> getAll(Long appSecurityId, AppSecurityRiskCriteria criteria, Pageable pageable);

    /**
     * Creates a new security risk from the given input payload.
     *
     * @param inputDTO the creation payload
     * @return the created risk, mapped to its output transfer representation
     */
    AppSecurityRiskOutputDTO create(AppSecurityRiskInputDTO inputDTO);

    /**
     * Updates the security risk identified by {@code id} with the given
     * input payload.
     *
     * @param id       identifier of the risk to update
     * @param inputDTO the update payload
     * @return the updated risk, mapped to its output transfer representation
     */
    AppSecurityRiskOutputDTO update(Long id, AppSecurityRiskInputDTO inputDTO);

    /**
     * Deletes (logically) the security risk identified by {@code id}.
     *
     * @param id identifier of the risk to delete
     */
    void delete(Long id);
}
