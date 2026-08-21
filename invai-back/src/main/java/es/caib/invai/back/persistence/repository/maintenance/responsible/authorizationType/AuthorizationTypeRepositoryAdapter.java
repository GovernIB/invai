package es.caib.invai.back.persistence.repository.maintenance.responsible.authorizationType;

import es.caib.invai.back.service.model.maintenance.responsible.authorizationType.AuthorizationType;
import es.caib.invai.back.persistence.model.maintenance.responsible.authorizationType.AuthorizationTypeEntity;
import es.caib.invai.back.persistence.model.maintenance.responsible.authorizationType.AuthorizationTypeAudEntity;
import es.caib.invai.back.service.mapper.maintenance.responsible.authorizationType.AuthorizationTypeMapper;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link AuthorizationTypeRepository}.
 *
 * @since 1.0.3
 */
@Repository
@Slf4j
public class AuthorizationTypeRepositoryAdapter implements AuthorizationTypeRepository {

    /** JPA repository used for CRUD operations on live authorization type entities. */
    @Autowired
    private AuthorizationTypeJPARepository authorizationTypeJPARepository;

    /** JPA repository used to persist audit snapshots of authorization type changes. */
    @Autowired
    private AuthorizationTypeAudJPARepository authorizationTypeAudJPARepository;

    /** Mapper used to convert between AuthorizationType domain models and entities. */
    @Autowired
    private AuthorizationTypeMapper authorizationTypeMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizationType findById(Long id) {
        return authorizationTypeJPARepository.findById(id)
                .map(authorizationTypeMapper::toModel)
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<AuthorizationType> findAll(AuthorizationTypeCriteria filter, Pageable pageable) {
        Specification<AuthorizationTypeEntity> spec = AuthorizationTypeSpecification.filterByCriteria(filter);
        return authorizationTypeJPARepository.findAll(spec, pageable).map(authorizationTypeMapper::toModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByNameAndDeletedAtIsNull(String name) {
        return authorizationTypeJPARepository.existsByNameAndDeletedAtIsNull(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id) {
        return authorizationTypeJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizationType create(AuthorizationType authorizationType) {
        log.info("Repository: Persisting new authorization type entity into database");
        AuthorizationTypeEntity entity = authorizationTypeMapper.toEntity(authorizationType);
        entity = authorizationTypeJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");

        return authorizationTypeMapper.toModel(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizationType update(AuthorizationType authorizationType, Long id) {
        log.info("Repository: Merging changes into existing authorization type record for ID: {}", id);
        AuthorizationTypeEntity entity = authorizationTypeMapper.toEntity(authorizationType);
        entity.setId(id);
        entity = authorizationTypeJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");

        return authorizationTypeMapper.toModel(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(AuthorizationType authorizationType) {
        log.info("Repository: Soft deleting authorization type entity with ID: {}", authorizationType.getId());
        AuthorizationTypeEntity entity = authorizationTypeMapper.toEntity(authorizationType);
        entity.setId(authorizationType.getId());
        entity = authorizationTypeJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Builds and persists an audit snapshot capturing the current state of the given entity
     * along with the operation that triggered it.
     *
     * @param entity the authorization type entity whose state is being audited
     * @param action the type of operation performed (e.g. {@code INSERT}, {@code UPDATE}, {@code DELETE})
     */
    private void saveAuditRecord(AuthorizationTypeEntity entity, String action) {
        AuthorizationTypeAudEntity aud = new AuthorizationTypeAudEntity();

        aud.setAuthorizationTypeId(entity.getId());
        aud.setName(entity.getName());
        aud.setNameEs(entity.getNameEs());

        aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : LocalDateTime.now());
        aud.setUpdatedBy(entity.getUpdatedBy() != null ? entity.getUpdatedBy() : Utils.resolveCurrentUsername());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());

        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());

        authorizationTypeAudJPARepository.save(aud);
    }
}
