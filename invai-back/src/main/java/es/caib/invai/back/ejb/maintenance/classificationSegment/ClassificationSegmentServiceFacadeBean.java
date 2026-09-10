package es.caib.invai.back.ejb.maintenance.classificationSegment;

import es.caib.invai.back.interna.maintenance.classificationSegment.DTO.ClassificationSegmentInputDTO;
import es.caib.invai.back.interna.maintenance.classificationSegment.DTO.ClassificationSegmentOutputDTO;
import es.caib.invai.back.service.mapper.maintenance.classificationSegment.ClassificationSegmentMapper;
import es.caib.invai.back.persistence.repository.maintenance.classificationSegment.ClassificationSegmentCriteria;
import es.caib.invai.back.persistence.repository.maintenance.classificationSegment.ClassificationSegmentRepository;
import es.caib.invai.back.service.facade.maintenance.classificationSegment.ClassificationSegmentService;
import es.caib.invai.back.service.model.maintenance.classificationSegment.ClassificationSegment;
import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Facade service implementation for administrative ClassificationSegments.
 *
 * @since 1.0.4
 */
@Service
@Slf4j
@Transactional
public class ClassificationSegmentServiceFacadeBean implements ClassificationSegmentService {

    /** Mapper used to convert between ClassificationSegment domain models, entities, and DTOs. */
    @Autowired
    private ClassificationSegmentMapper classificationSegmentMapper;

    /** Repository port used to access ClassificationSegment persistence operations. */
    @Autowired
    private ClassificationSegmentRepository classificationSegmentRepository;

    /**
     * Retrieves a classification segment entry by its identifier.
     *
     * @param id the classification segment entry identifier
     * @return the matching classification segment entry as an output DTO
     * @throws BusinessRuleException if no classification segment entry exists with the given ID
     */
    @Override
    @Transactional(readOnly = true)
    public ClassificationSegmentOutputDTO getById(Long id) {
        log.debug("Facade: Fetching classification segment entry by ID: {}", id);
        ClassificationSegment classificationSegment = classificationSegmentRepository.findById(id);

        if (classificationSegment == null) {
            throw new BusinessRuleException(Constants.ERR_CLASSIFICATIONSEGMENT_NOT_FOUND);
        }

        return classificationSegmentMapper.toResponse(classificationSegment);
    }

    /**
     * Retrieves a paginated list of classification segment entries matching the given filter criteria.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching classification segment entries as output DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ClassificationSegmentOutputDTO> getAll(ClassificationSegmentCriteria filter, Pageable pageable) {
        log.debug("Facade: Fetching classification segment entries via pagination boundaries");
        Page<ClassificationSegment> domainPage = classificationSegmentRepository.findAll(filter, pageable);
        return domainPage.map(classificationSegmentMapper::toResponse);
    }

    /**
     * Creates a new classification segment entry after sanitizing the input and checking for name duplicates.
     *
     * @param inputDTO the data for the new classification segment entry
     * @return the created classification segment entry as an output DTO
     * @throws BusinessRuleException if an active classification segment entry with the same name already exists
     */
    @Override
    public ClassificationSegmentOutputDTO create(ClassificationSegmentInputDTO inputDTO) {
        log.info("Facade: Creating new classification segment entry record with name: {}", inputDTO.getName());

        Utils.sanitize(inputDTO);

        if (classificationSegmentRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException(Constants.ERR_CLASSIFICATIONSEGMENT_DUPLICATED);
        }

        ClassificationSegment model = classificationSegmentMapper.toModelFromInput(inputDTO);

        ClassificationSegment savedModel = classificationSegmentRepository.create(model);
        return classificationSegmentMapper.toResponse(savedModel);
    }

    /**
     * Updates an existing classification segment entry, validating existence and name uniqueness.
     *
     * @param id the identifier of the classification segment entry to update
     * @param inputDTO the new data to apply
     * @return the updated classification segment entry as an output DTO
     * @throws BusinessRuleException if the classification segment entry does not exist or the name is already used by another record
     */
    @Override
    public ClassificationSegmentOutputDTO update(Long id, ClassificationSegmentInputDTO inputDTO) {
        log.info("Facade: Updating classification segment entry with ID: {}", id);

        ClassificationSegment existing = classificationSegmentRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_CLASSIFICATIONSEGMENT_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (classificationSegmentRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException(Constants.ERR_CLASSIFICATIONSEGMENT_DUPLICATED);
        }

        classificationSegmentMapper.updateModelFromInput(inputDTO, existing);
        return classificationSegmentMapper.toResponse(classificationSegmentRepository.update(existing, id));
    }

    /**
     * Logically deletes a classification segment entry by stamping its deletion timestamp and author.
     *
     * @param id the identifier of the classification segment entry to delete
     * @throws BusinessRuleException if the classification segment entry does not exist or is already deleted
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting classification segment entry with ID: {}", id);
        ClassificationSegment existing = classificationSegmentRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_CLASSIFICATIONSEGMENT_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_CLASSIFICATIONSEGMENT_NOT_ACTIVE);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        classificationSegmentRepository.delete(existing);
    }

    /**
     * Reactivates a previously deleted classification segment entry by clearing its deletion timestamp and author.
     *
     * @param id the identifier of the classification segment entry to reactivate
     * @return the reactivated classification segment entry as an output DTO
     * @throws BusinessRuleException if the classification segment entry does not exist or is already active
     */
    @Override
    public ClassificationSegmentOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating classification segment entry with ID: {}", id);
        ClassificationSegment existing = classificationSegmentRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_CLASSIFICATIONSEGMENT_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_CLASSIFICATIONSEGMENT_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        ClassificationSegment updatedModel = classificationSegmentRepository.update(existing, id);
        return classificationSegmentMapper.toResponse(updatedModel);
    }
}
