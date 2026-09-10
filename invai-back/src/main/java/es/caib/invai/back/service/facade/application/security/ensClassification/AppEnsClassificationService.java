package es.caib.invai.back.service.facade.application.security.ensClassification;

import es.caib.invai.back.interna.application.security.ensClassification.DTO.AppEnsClassificationInputDTO;
import es.caib.invai.back.interna.application.security.ensClassification.DTO.AppEnsClassificationOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.ensClassification.AppEnsClassificationCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Domain Boundary Outbound Port interfacing the internal transactional domain operations.
 *
 * @since 1.0.4
 */
public interface AppEnsClassificationService {

    /**
     * Retrieves a paginated sequence of ENS classification records scoped to a single parent
     * security anchor.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      the multi-parameter business query filter boundaries
     * @param pageable      pagination structural constraints
     * @return a paginated payload containing corresponding transfer representations
     */
    Page<AppEnsClassificationOutputDTO> getAll(Long appSecurityId, AppEnsClassificationCriteria criteria, Pageable pageable);

    /**
     * Creates a new ENS classification record from the given input payload.
     *
     * @param inputDTO the creation payload
     * @return the created record, mapped to its output transfer representation
     */
    AppEnsClassificationOutputDTO create(AppEnsClassificationInputDTO inputDTO);

    /**
     * Updates the ENS classification record identified by {@code id} with the given
     * input payload.
     *
     * @param id       identifier of the record to update
     * @param inputDTO the update payload
     * @return the updated record, mapped to its output transfer representation
     */
    AppEnsClassificationOutputDTO update(Long id, AppEnsClassificationInputDTO inputDTO);

    /**
     * Deletes (logically) the ENS classification record identified by {@code id}.
     *
     * @param id identifier of the record to delete
     */
    void delete(Long id);
}
