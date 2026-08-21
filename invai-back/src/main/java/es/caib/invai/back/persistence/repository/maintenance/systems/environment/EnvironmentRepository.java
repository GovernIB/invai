package es.caib.invai.back.persistence.repository.maintenance.systems.environment;

import es.caib.invai.back.service.model.maintenance.systems.environment.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Outbound Port boundary interface declaring relational persistence mechanisms
 * for the environment profile domain model layer.
 *
 * @since 1.0.1
 */
public interface EnvironmentRepository {

    /**
     * Registers a new operational environment profile structure inside relational tracking systems.
     */
    Environment create(Environment environment);

    /**
     * Commits changes onto active domain records maps identified by a target primary sequence index.
     */
    Environment update(Environment environment, Long id);

    /**
     * Flags tracking profiles context records as soft-deleted inside persistence layers.
     */
    void delete(Environment environment);

    /**
     * Resolves an active environment record profile matching an identification primary index.
     */
    Environment findById(Long id);

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries.
     */
    Page<Environment> findAll(EnvironmentCriteria filter, Pageable pageable);

    /**
     * Confirms uniqueness of the code across active environment registries.
     */
    boolean existsByCode(String code);

    /**
     * Checks for duplicate code conflicts excluding a target primary reference row.
     */
    boolean existsByCodeAndIdNot(String code, Long id);
}