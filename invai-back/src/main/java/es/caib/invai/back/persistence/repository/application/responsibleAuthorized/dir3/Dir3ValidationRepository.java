package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.dir3;

import es.caib.invai.back.service.model.application.responsibleAuthorized.dir3.Dir3Validation;

/**
 * Persistence port abstraction decoupling the service layer from the JPA infrastructure used to
 * store {@code Dir3Validation} records.
 *
 * @since 1.0.5
 */
public interface Dir3ValidationRepository {

    /**
     * Persists a new DIR3 validation record and writes the corresponding audit trail entry.
     *
     * @param dir3Validation the record to create
     * @return the persisted record, including its generated identifier
     */
    Dir3Validation create(Dir3Validation dir3Validation);

    /**
     * Merges changes into an existing DIR3 validation record and writes the corresponding audit
     * trail entry.
     *
     * @param dir3Validation the record data to merge
     * @param id identifier of the record to update
     * @return the updated record
     */
    Dir3Validation update(Dir3Validation dir3Validation, Long id);

    /**
     * Resolves a DIR3 validation record by its identifier.
     *
     * @param id the record identifier
     * @return the matching record, or {@code null} if not found
     */
    Dir3Validation findById(Long id);
}
