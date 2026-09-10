package es.caib.invai.back.persistence.repository.maintenance.classificationSegment;

import es.caib.invai.back.service.model.maintenance.classificationSegment.ClassificationSegment;
import es.caib.invai.back.persistence.model.maintenance.classificationSegment.ClassificationSegmentEntity;
import es.caib.invai.back.persistence.model.maintenance.classificationSegment.ClassificationSegmentAudEntity;
import es.caib.invai.back.service.mapper.maintenance.classificationSegment.ClassificationSegmentMapper;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link ClassificationSegmentRepository}.
 *
 * @since 1.0.4
 */
@Repository
@Slf4j
public class ClassificationSegmentRepositoryAdapter implements ClassificationSegmentRepository {

    /** JPA repository used for CRUD operations on live classification segment entry entities. */
    @Autowired
    private ClassificationSegmentJPARepository classificationSegmentJPARepository;

    /** JPA repository used to persist audit snapshots of classification segment entry changes. */
    @Autowired
    private ClassificationSegmentAudJPARepository classificationSegmentAudJPARepository;

    /** Mapper used to convert between ClassificationSegment domain models and entities. */
    @Autowired
    private ClassificationSegmentMapper classificationSegmentMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassificationSegment findById(Long id) {
        return classificationSegmentJPARepository.findById(id)
                .map(classificationSegmentMapper::toModel)
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<ClassificationSegment> findAll(ClassificationSegmentCriteria filter, Pageable pageable) {
        Specification<ClassificationSegmentEntity> spec = ClassificationSegmentSpecification.filterByCriteria(filter);
        return classificationSegmentJPARepository.findAll(spec, pageable).map(classificationSegmentMapper::toModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByNameAndDeletedAtIsNull(String name) {
        return classificationSegmentJPARepository.existsByNameAndDeletedAtIsNull(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id) {
        return classificationSegmentJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassificationSegment create(ClassificationSegment classificationSegment) {
        log.info("Repository: Persisting new classification segment entry entity into database");
        ClassificationSegmentEntity entity = classificationSegmentMapper.toEntity(classificationSegment);
        entity = classificationSegmentJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");

        return classificationSegmentMapper.toModel(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassificationSegment update(ClassificationSegment classificationSegment, Long id) {
        log.info("Repository: Merging changes into existing classification segment entry record for ID: {}", id);
        ClassificationSegmentEntity entity = classificationSegmentMapper.toEntity(classificationSegment);
        entity.setId(id);
        entity = classificationSegmentJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");

        return classificationSegmentMapper.toModel(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(ClassificationSegment classificationSegment) {
        log.info("Repository: Soft deleting classification segment entry entity with ID: {}", classificationSegment.getId());
        ClassificationSegmentEntity entity = classificationSegmentMapper.toEntity(classificationSegment);
        entity.setId(classificationSegment.getId());
        entity = classificationSegmentJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Builds and persists an audit snapshot capturing the current state of the given entity
     * along with the operation that triggered it.
     *
     * @param entity the classification segment entry entity whose state is being audited
     * @param action the type of operation performed (e.g. {@code INSERT}, {@code UPDATE}, {@code DELETE})
     */
    private void saveAuditRecord(ClassificationSegmentEntity entity, String action) {
        ClassificationSegmentAudEntity aud = new ClassificationSegmentAudEntity();

        aud.setClassificationSegmentId(entity.getId());
        aud.setName(entity.getName());
        aud.setNameEs(entity.getNameEs());

        aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());

        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());

        classificationSegmentAudJPARepository.save(aud);
    }
}
