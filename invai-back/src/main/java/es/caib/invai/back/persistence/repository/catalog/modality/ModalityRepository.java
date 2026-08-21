package es.caib.invai.back.persistence.repository.catalog.modality;

import es.caib.invai.back.service.model.catalog.modality.Modality;

import java.util.List;

/**
 * Core business domain outbound Port boundary interface declaring read-only relational
 * persistence mechanisms for the Modality lookup dictionary.
 *
 * @since 1.0.3
 */
public interface ModalityRepository {

    /**
     * Resolves every registered modality lookup entry, sorted by name.
     *
     * @return the full list of mapped domain {@link Modality} instances
     */
    List<Modality> findAll();
}
