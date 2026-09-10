package es.caib.invai.back.persistence.repository.maintenance.security.securityMeasureType;

import es.caib.invai.back.service.model.maintenance.security.securityMeasureType.SecurityMeasureType;
import es.caib.invai.back.persistence.model.maintenance.security.securityMeasureType.SecurityMeasureTypeEntity;
import es.caib.invai.back.persistence.model.maintenance.security.securityMeasureType.SecurityMeasureTypeAudEntity;
import es.caib.invai.back.service.mapper.maintenance.security.securityMeasureType.SecurityMeasureTypeMapper;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link SecurityMeasureTypeRepository}.
 *
 * @since 1.0.4
 */
@Repository
@Slf4j
public class SecurityMeasureTypeRepositoryAdapter implements SecurityMeasureTypeRepository {

    /** JPA repository used for CRUD operations on live security measure type entry entities. */
    @Autowired
    private SecurityMeasureTypeJPARepository securityMeasureTypeJPARepository;

    /** JPA repository used to persist audit snapshots of security measure type entry changes. */
    @Autowired
    private SecurityMeasureTypeAudJPARepository securityMeasureTypeAudJPARepository;

    /** Mapper used to convert between SecurityMeasureType domain models and entities. */
    @Autowired
    private SecurityMeasureTypeMapper securityMeasureTypeMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public SecurityMeasureType findById(Long id) {
        return securityMeasureTypeJPARepository.findById(id)
                .map(securityMeasureTypeMapper::toModel)
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<SecurityMeasureType> findAll(SecurityMeasureTypeCriteria filter, Pageable pageable) {
        Specification<SecurityMeasureTypeEntity> spec = SecurityMeasureTypeSpecification.filterByCriteria(filter);
        return securityMeasureTypeJPARepository.findAll(spec, pageable).map(securityMeasureTypeMapper::toModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByNameAndDeletedAtIsNull(String name) {
        return securityMeasureTypeJPARepository.existsByNameAndDeletedAtIsNull(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id) {
        return securityMeasureTypeJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SecurityMeasureType create(SecurityMeasureType securityMeasureType) {
        log.info("Repository: Persisting new security measure type entry entity into database");
        SecurityMeasureTypeEntity entity = securityMeasureTypeMapper.toEntity(securityMeasureType);
        entity = securityMeasureTypeJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");

        return securityMeasureTypeMapper.toModel(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SecurityMeasureType update(SecurityMeasureType securityMeasureType, Long id) {
        log.info("Repository: Merging changes into existing security measure type entry record for ID: {}", id);
        SecurityMeasureTypeEntity entity = securityMeasureTypeMapper.toEntity(securityMeasureType);
        entity.setId(id);
        entity = securityMeasureTypeJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");

        return securityMeasureTypeMapper.toModel(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(SecurityMeasureType securityMeasureType) {
        log.info("Repository: Soft deleting security measure type entry entity with ID: {}", securityMeasureType.getId());
        SecurityMeasureTypeEntity entity = securityMeasureTypeMapper.toEntity(securityMeasureType);
        entity.setId(securityMeasureType.getId());
        entity = securityMeasureTypeJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Builds and persists an audit snapshot capturing the current state of the given entity
     * along with the operation that triggered it.
     *
     * @param entity the security measure type entry entity whose state is being audited
     * @param action the type of operation performed (e.g. {@code INSERT}, {@code UPDATE}, {@code DELETE})
     */
    private void saveAuditRecord(SecurityMeasureTypeEntity entity, String action) {
        SecurityMeasureTypeAudEntity aud = new SecurityMeasureTypeAudEntity();

        aud.setSecurityMeasureTypeId(entity.getId());
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

        securityMeasureTypeAudJPARepository.save(aud);
    }
}
