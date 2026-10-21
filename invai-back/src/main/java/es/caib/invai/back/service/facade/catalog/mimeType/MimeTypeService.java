package es.caib.invai.back.service.facade.catalog.mimeType;

import es.caib.invai.back.interna.catalog.mimeType.DTO.MimeTypeOutputDTO;

import java.util.List;

/**
 * Service facade boundary declaring read-only access to the file format lookup.
 *
 * @since 1.0.6
 */
public interface MimeTypeService {

    /**
     * Resolves every file format lookup entry, sorted by extension.
     *
     * @return the list of file format entries as response DTOs
     */
    List<MimeTypeOutputDTO> getAll();
}
