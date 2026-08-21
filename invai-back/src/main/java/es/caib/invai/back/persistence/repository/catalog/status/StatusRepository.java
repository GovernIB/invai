package es.caib.invai.back.persistence.repository.catalog.status;

import es.caib.invai.back.service.model.catalog.status.Status;

import java.util.List;

/**
 * Core business domain outbound Port boundary interface declaring read-only relational
 * persistence mechanisms for the Status lookup dictionary.
 *
 * @since 1.0.3
 */
public interface StatusRepository {

    /**
     * Resolves every registered status lookup entry, sorted by name.
     *
     * @return the full list of mapped domain {@link Status} instances
     */
    List<Status> findAll();
}
