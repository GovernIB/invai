package es.caib.invai.back.service.facade.catalog.appSection;

import es.caib.invai.back.interna.catalog.appSection.DTO.AppSectionOutputDTO;

import java.util.List;

/**
 * Service facade boundary declaring read-only access to the application section lookup.
 *
 * @since 1.0.6
 */
public interface AppSectionService {

    /**
     * Resolves every application section lookup entry, sorted by name.
     *
     * @return the list of section entries as response DTOs
     */
    List<AppSectionOutputDTO> getAll();
}
