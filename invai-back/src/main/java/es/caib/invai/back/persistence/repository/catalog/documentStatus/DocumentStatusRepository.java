package es.caib.invai.back.persistence.repository.catalog.documentStatus;

import es.caib.invai.back.service.model.catalog.documentStatus.DocumentStatus;

import java.util.List;

/**
 * Outbound port declaring read access to the document status lookup.
 *
 * @since 1.0.6
 */
public interface DocumentStatusRepository {

    /**
     * Finds every status entry, sorted by name.
     *
     * @return the full list of status entries
     */
    List<DocumentStatus> findAll();

    /**
     * Finds a status entry by its identifier.
     *
     * @param id the identifier
     * @return the matching entry, or {@code null} if none is found
     */
    DocumentStatus findById(Long id);
}
