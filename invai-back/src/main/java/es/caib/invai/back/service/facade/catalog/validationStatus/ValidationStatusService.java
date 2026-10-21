package es.caib.invai.back.service.facade.catalog.validationStatus;

import es.caib.invai.back.interna.catalog.validationStatus.DTO.ValidationStatusOutputDTO;

import java.util.List;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting the validation status lookup dictionary.
 *
 * @since 1.0.5
 */
public interface ValidationStatusService {

    /**
     * Resolves every registered validation status lookup entry, sorted by name.
     *
     * @return the full list of mapped {@link ValidationStatusOutputDTO} entries
     */
    List<ValidationStatusOutputDTO> getAll();
}
