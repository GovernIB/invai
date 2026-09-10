package es.caib.invai.back.persistence.repository.maintenance.classificationSegment;

import es.caib.invai.back.service.model.maintenance.classificationSegment.ClassificationSegment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Core business domain outbound Port boundary interface declaring relational persistence mechanisms
 * for the ClassificationSegment catalog layer.
 *
 * @since 1.0.4
 */
public interface ClassificationSegmentRepository {

    /**
     * Finds a classification segment entry by its identifier.
     *
     * @param id the classification segment entry identifier
     * @return the matching domain model, or {@code null} if not found
     */
    ClassificationSegment findById(Long id);

    /**
     * Finds classification segment entries matching the given filter criteria, paginated.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching classification segment entries
     */
    Page<ClassificationSegment> findAll(ClassificationSegmentCriteria filter, Pageable pageable);

    /**
     * Persists a new classification segment entry.
     *
     * @param classificationSegment the domain model to persist
     * @return the persisted domain model
     */
    ClassificationSegment create(ClassificationSegment classificationSegment);

    /**
     * Persists changes to an existing classification segment entry.
     *
     * @param classificationSegment the domain model containing the updated data
     * @param id the identifier of the classification segment entry to update
     * @return the updated domain model
     */
    ClassificationSegment update(ClassificationSegment classificationSegment, Long id);

    /**
     * Deletes (logically) the given classification segment entry.
     *
     * @param classificationSegment the domain model to delete
     */
    void delete(ClassificationSegment classificationSegment);

    /**
     * Checks whether an active (not logically deleted) classification segment entry with the given name exists.
     *
     * @param name the name to check
     * @return {@code true} if a matching active record exists
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Checks whether an active (not logically deleted) classification segment entry with the given name exists,
     * excluding the record with the given ID.
     *
     * @param name the name to check
     * @param id the identifier to exclude from the check
     * @return {@code true} if a matching active record exists other than the one with the given ID
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}
