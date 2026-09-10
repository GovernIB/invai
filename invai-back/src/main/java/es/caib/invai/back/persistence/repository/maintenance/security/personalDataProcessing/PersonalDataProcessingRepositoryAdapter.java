package es.caib.invai.back.persistence.repository.maintenance.security.personalDataProcessing;

import es.caib.invai.back.service.model.maintenance.security.personalDataProcessing.PersonalDataProcessing;
import es.caib.invai.back.persistence.model.maintenance.security.personalDataProcessing.PersonalDataProcessingEntity;
import es.caib.invai.back.persistence.model.maintenance.security.personalDataProcessing.PersonalDataProcessingAudEntity;
import es.caib.invai.back.service.mapper.maintenance.security.personalDataProcessing.PersonalDataProcessingMapper;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link PersonalDataProcessingRepository}.
 *
 * @since 1.0.4
 */
@Repository
@Slf4j
public class PersonalDataProcessingRepositoryAdapter implements PersonalDataProcessingRepository {

    /** JPA repository used for CRUD operations on live personal data processing entry entities. */
    @Autowired
    private PersonalDataProcessingJPARepository personalDataProcessingJPARepository;

    /** JPA repository used to persist audit snapshots of personal data processing entry changes. */
    @Autowired
    private PersonalDataProcessingAudJPARepository personalDataProcessingAudJPARepository;

    /** Mapper used to convert between PersonalDataProcessing domain models and entities. */
    @Autowired
    private PersonalDataProcessingMapper personalDataProcessingMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public PersonalDataProcessing findById(Long id) {
        return personalDataProcessingJPARepository.findById(id)
                .map(personalDataProcessingMapper::toModel)
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<PersonalDataProcessing> findAll(PersonalDataProcessingCriteria filter, Pageable pageable) {
        Specification<PersonalDataProcessingEntity> spec = PersonalDataProcessingSpecification.filterByCriteria(filter);
        return personalDataProcessingJPARepository.findAll(spec, pageable).map(personalDataProcessingMapper::toModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByNameAndDeletedAtIsNull(String name) {
        return personalDataProcessingJPARepository.existsByNameAndDeletedAtIsNull(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id) {
        return personalDataProcessingJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PersonalDataProcessing create(PersonalDataProcessing personalDataProcessing) {
        log.info("Repository: Persisting new personal data processing entry entity into database");
        PersonalDataProcessingEntity entity = personalDataProcessingMapper.toEntity(personalDataProcessing);
        entity = personalDataProcessingJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");

        return personalDataProcessingMapper.toModel(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PersonalDataProcessing update(PersonalDataProcessing personalDataProcessing, Long id) {
        log.info("Repository: Merging changes into existing personal data processing entry record for ID: {}", id);
        PersonalDataProcessingEntity entity = personalDataProcessingMapper.toEntity(personalDataProcessing);
        entity.setId(id);
        entity = personalDataProcessingJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");

        return personalDataProcessingMapper.toModel(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(PersonalDataProcessing personalDataProcessing) {
        log.info("Repository: Soft deleting personal data processing entry entity with ID: {}", personalDataProcessing.getId());
        PersonalDataProcessingEntity entity = personalDataProcessingMapper.toEntity(personalDataProcessing);
        entity.setId(personalDataProcessing.getId());
        entity = personalDataProcessingJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Builds and persists an audit snapshot capturing the current state of the given entity
     * along with the operation that triggered it.
     *
     * @param entity the personal data processing entry entity whose state is being audited
     * @param action the type of operation performed (e.g. {@code INSERT}, {@code UPDATE}, {@code DELETE})
     */
    private void saveAuditRecord(PersonalDataProcessingEntity entity, String action) {
        PersonalDataProcessingAudEntity aud = new PersonalDataProcessingAudEntity();

        aud.setPersonalDataProcessingId(entity.getId());
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

        personalDataProcessingAudJPARepository.save(aud);
    }
}
