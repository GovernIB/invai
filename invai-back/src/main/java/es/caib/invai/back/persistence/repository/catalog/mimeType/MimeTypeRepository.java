package es.caib.invai.back.persistence.repository.catalog.mimeType;

import es.caib.invai.back.service.model.catalog.mimeType.MimeType;

import java.util.List;

/**
 * Outbound port declaring read access to the file format lookup.
 *
 * @since 1.0.6
 */
public interface MimeTypeRepository {

    /**
     * Finds every file format entry, sorted by extension.
     *
     * @return the full list of file format entries
     */
    List<MimeType> findAll();

    /**
     * Finds the file format registered for the given extension.
     *
     * @param extension the extension, in lower case and without the dot
     * @return the matching entry, or {@code null} if the extension isn't registered
     */
    MimeType findByExtension(String extension);
}
