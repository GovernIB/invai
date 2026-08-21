package es.caib.invai.back.persistence.repository.catalog.standardAdaption;

import es.caib.invai.back.service.model.catalog.standardAdaption.StandardAdaption;

import java.util.List;

/**
 * Core business domain outbound Port boundary interface declaring read-only relational
 * persistence mechanisms for the Standard Adaption lookup dictionary.
 *
 * @since 1.0.3
 */
public interface StandardAdaptionRepository {

    /**
     * Resolves every registered standard adaption lookup entry, sorted by name.
     *
     * @return the full list of mapped domain {@link StandardAdaption} instances
     */
    List<StandardAdaption> findAll();
}
