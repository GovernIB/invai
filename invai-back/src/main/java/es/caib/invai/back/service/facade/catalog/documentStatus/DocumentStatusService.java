package es.caib.invai.back.service.facade.catalog.documentStatus;

import es.caib.invai.back.interna.catalog.documentStatus.DTO.DocumentStatusOutputDTO;

import java.util.List;

/**
 * Service facade boundary declaring read-only access to the document status lookup.
 *
 * @since 1.0.6
 */
public interface DocumentStatusService {

    /**
     * Resolves every document status lookup entry, sorted by name.
     *
     * @return the list of status entries as response DTOs
     */
    List<DocumentStatusOutputDTO> getAll();
}
