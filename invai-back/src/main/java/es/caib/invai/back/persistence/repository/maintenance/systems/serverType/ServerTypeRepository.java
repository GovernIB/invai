package es.caib.invai.back.persistence.repository.maintenance.systems.serverType;

import es.caib.invai.back.service.model.maintenance.systems.serverType.ServerType;

import java.util.List;

/**
 * Core business domain outbound Port boundary interface declaring read-only relational
 * persistence mechanisms for the Server Type lookup dictionary.
 *
 * @since 1.0.2
 */
public interface ServerTypeRepository {

    /**
     * Resolves every registered server type lookup entry, sorted by name.
     *
     * @return the full list of mapped domain {@link ServerType} instances
     */
    List<ServerType> findAll();
}
