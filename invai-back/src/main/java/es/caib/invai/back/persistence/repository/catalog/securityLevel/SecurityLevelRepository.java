package es.caib.invai.back.persistence.repository.catalog.securityLevel;

import es.caib.invai.back.service.model.catalog.securityLevel.SecurityLevel;

import java.util.List;

/**
 * Core business domain outbound Port boundary interface declaring read-only relational
 * persistence mechanisms for the Security level lookup dictionary.
 *
 * @since 1.0.4
 */
public interface SecurityLevelRepository {

    /**
     * Resolves every registered security level lookup entry, sorted by name.
     *
     * @return the full list of mapped domain {@link SecurityLevel} instances
     */
    List<SecurityLevel> findAll();

    /**
     * Resolves a single security level lookup entry by its identifier.
     *
     * @param id the security level identifier
     * @return the matching domain {@link SecurityLevel}, or {@code null} if not found
     */
    SecurityLevel findById(Long id);
}
