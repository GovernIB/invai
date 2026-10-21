package es.caib.invai.back.persistence.repository.catalog.appSection;

import es.caib.invai.back.service.model.catalog.appSection.AppSection;

import java.util.List;

/**
 * Outbound port declaring read access to the application section lookup.
 *
 * @since 1.0.6
 */
public interface AppSectionRepository {

    /**
     * Finds every section entry, sorted by name.
     *
     * @return the full list of section entries
     */
    List<AppSection> findAll();

    /**
     * Finds a section entry by its identifier.
     *
     * @param id the identifier
     * @return the matching entry, or {@code null} if none is found
     */
    AppSection findById(Long id);
}
