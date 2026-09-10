package es.caib.invai.back.service.facade.catalog.securityLevel;

import es.caib.invai.back.interna.catalog.securityLevel.DTO.SecurityLevelOutputDTO;

import java.util.List;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting the Security level lookup dictionary.
 *
 * @since 1.0.4
 */
public interface SecurityLevelService {

    /**
     * Resolves every registered security level lookup entry, sorted by name.
     *
     * @return the full list of mapped {@link SecurityLevelOutputDTO} entries
     */
    List<SecurityLevelOutputDTO> getAll();
}
