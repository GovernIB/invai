package es.caib.invai.back.service.facade.catalog.ensSubject;

import es.caib.invai.back.interna.catalog.ensSubject.DTO.EnsSubjectOutputDTO;

import java.util.List;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting the ENS subjection lookup dictionary.
 *
 * @since 1.0.4
 */
public interface EnsSubjectService {

    /**
     * Resolves every registered ENS subjection lookup entry, sorted by name.
     *
     * @return the full list of mapped {@link EnsSubjectOutputDTO} entries
     */
    List<EnsSubjectOutputDTO> getAll();
}
