package es.caib.invai.back.persistence.repository.catalog.dir3Status;

import es.caib.invai.back.service.model.catalog.dir3Status.Dir3Status;

import java.util.List;

/**
 * Core business domain outbound Port boundary interface declaring read-only relational
 * persistence mechanisms for the DIR3 validation status lookup dictionary.
 *
 * @since 1.0.5
 */
public interface Dir3StatusRepository {

    /**
     * Resolves every registered DIR3 status lookup entry, sorted by name.
     *
     * @return the full list of mapped domain {@link Dir3Status} instances
     */
    List<Dir3Status> findAll();
}
