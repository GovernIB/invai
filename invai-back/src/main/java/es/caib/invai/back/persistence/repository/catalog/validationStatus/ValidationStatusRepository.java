package es.caib.invai.back.persistence.repository.catalog.validationStatus;

import es.caib.invai.back.service.model.catalog.validationStatus.ValidationStatus;

import java.util.List;

/**
 * Core business domain outbound Port boundary interface declaring read-only relational
 * persistence mechanisms for the validation status lookup dictionary.
 *
 * @since 1.0.5
 */
public interface ValidationStatusRepository {

    /**
     * Resolves every registered validation status lookup entry, sorted by name.
     *
     * @return the full list of mapped domain {@link ValidationStatus} instances
     */
    List<ValidationStatus> findAll();
}
