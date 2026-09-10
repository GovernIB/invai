package es.caib.invai.back.persistence.repository.catalog.ensSubject;

import es.caib.invai.back.service.model.catalog.ensSubject.EnsSubject;

import java.util.List;

/**
 * Core business domain outbound Port boundary interface declaring read-only relational
 * persistence mechanisms for the ENS subjection lookup dictionary.
 *
 * @since 1.0.4
 */
public interface EnsSubjectRepository {

    /**
     * Resolves every registered ENS subjection lookup entry, sorted by name.
     *
     * @return the full list of mapped domain {@link EnsSubject} instances
     */
    List<EnsSubject> findAll();

    /**
     * Resolves a single ENS subjection lookup entry by its identifier.
     *
     * @param id the ENS subjection identifier
     * @return the matching domain {@link EnsSubject}, or {@code null} if not found
     */
    EnsSubject findById(Long id);
}
