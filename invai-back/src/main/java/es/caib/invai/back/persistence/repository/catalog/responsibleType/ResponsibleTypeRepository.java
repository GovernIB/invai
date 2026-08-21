package es.caib.invai.back.persistence.repository.catalog.responsibleType;

import es.caib.invai.back.service.model.catalog.responsibleType.ResponsibleType;

import java.util.List;

/**
 * Core business domain outbound Port boundary interface declaring read-only relational
 * persistence mechanisms for the Responsible Type lookup dictionary.
 *
 * @since 1.0.3
 */
public interface ResponsibleTypeRepository {

    /**
     * Resolves every registered responsible type lookup entry, sorted by name.
     *
     * @return the full list of mapped domain {@link ResponsibleType} instances
     */
    List<ResponsibleType> findAll();

    /**
     * Resolves a single responsible type lookup entry by its identifier.
     *
     * @param id the responsible type identifier
     * @return the matching domain {@link ResponsibleType}, or {@code null} if not found
     */
    ResponsibleType findById(Long id);
}
