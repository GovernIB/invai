package es.caib.invai.back.service.facade.maintenance.classificationSegment;

import es.caib.invai.back.interna.maintenance.classificationSegment.DTO.ClassificationSegmentInputDTO;
import es.caib.invai.back.interna.maintenance.classificationSegment.DTO.ClassificationSegmentOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.classificationSegment.ClassificationSegmentCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting ClassificationSegments.
 *
 * @since 1.0.4
 */
public interface ClassificationSegmentService {

    /**
     * Retrieves a classification segment entry by its identifier.
     *
     * @param id the classification segment entry identifier
     * @return the matching classification segment entry as an output DTO
     */
    ClassificationSegmentOutputDTO getById(Long id);

    /**
     * Retrieves a paginated list of classification segment entries matching the given filter criteria.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching classification segment entries as output DTOs
     */
    Page<ClassificationSegmentOutputDTO> getAll(ClassificationSegmentCriteria filter, Pageable pageable);

    /**
     * Creates a new classification segment entry.
     *
     * @param inputDTO the data for the new classification segment entry
     * @return the created classification segment entry as an output DTO
     */
    ClassificationSegmentOutputDTO create(ClassificationSegmentInputDTO inputDTO);

    /**
     * Updates an existing classification segment entry.
     *
     * @param id the identifier of the classification segment entry to update
     * @param inputDTO the new data to apply
     * @return the updated classification segment entry as an output DTO
     */
    ClassificationSegmentOutputDTO update(Long id, ClassificationSegmentInputDTO inputDTO);

    /**
     * Logically deletes a classification segment entry by its identifier.
     *
     * @param id the identifier of the classification segment entry to delete
     */
    void delete(Long id);

    /**
     * Reactivates a previously deleted classification segment entry.
     *
     * @param id the identifier of the classification segment entry to reactivate
     * @return the reactivated classification segment entry as an output DTO
     */
    ClassificationSegmentOutputDTO reactivate(Long id);
}
