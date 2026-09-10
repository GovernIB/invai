package es.caib.invai.back.persistence.repository.maintenance.security.identityProvider;

import es.caib.invai.back.service.model.maintenance.security.identityProvider.IdentityProvider;
import es.caib.invai.back.persistence.model.maintenance.security.identityProvider.IdentityProviderEntity;
import es.caib.invai.back.persistence.model.maintenance.security.identityProvider.IdentityProviderAudEntity;
import es.caib.invai.back.service.mapper.maintenance.security.identityProvider.IdentityProviderMapper;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link IdentityProviderRepository}.
 *
 * @since 1.0.4
 */
@Repository
@Slf4j
public class IdentityProviderRepositoryAdapter implements IdentityProviderRepository {

    /** JPA repository used for CRUD operations on live identity provider entities. */
    @Autowired
    private IdentityProviderJPARepository identityProviderJPARepository;

    /** JPA repository used to persist audit snapshots of identity provider changes. */
    @Autowired
    private IdentityProviderAudJPARepository identityProviderAudJPARepository;

    /** Mapper used to convert between IdentityProvider domain models and entities. */
    @Autowired
    private IdentityProviderMapper identityProviderMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public IdentityProvider findById(Long id) {
        return identityProviderJPARepository.findById(id)
                .map(identityProviderMapper::toModel)
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<IdentityProvider> findAll(IdentityProviderCriteria filter, Pageable pageable) {
        Specification<IdentityProviderEntity> spec = IdentityProviderSpecification.filterByCriteria(filter);
        return identityProviderJPARepository.findAll(spec, pageable).map(identityProviderMapper::toModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByNameAndDeletedAtIsNull(String name) {
        return identityProviderJPARepository.existsByNameAndDeletedAtIsNull(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id) {
        return identityProviderJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IdentityProvider create(IdentityProvider identityProvider) {
        log.info("Repository: Persisting new identity provider entity into database");
        IdentityProviderEntity entity = identityProviderMapper.toEntity(identityProvider);
        entity = identityProviderJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");

        return identityProviderMapper.toModel(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IdentityProvider update(IdentityProvider identityProvider, Long id) {
        log.info("Repository: Merging changes into existing identity provider record for ID: {}", id);
        IdentityProviderEntity entity = identityProviderMapper.toEntity(identityProvider);
        entity.setId(id);
        entity = identityProviderJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");

        return identityProviderMapper.toModel(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(IdentityProvider identityProvider) {
        log.info("Repository: Soft deleting identity provider entity with ID: {}", identityProvider.getId());
        IdentityProviderEntity entity = identityProviderMapper.toEntity(identityProvider);
        entity.setId(identityProvider.getId());
        entity = identityProviderJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Builds and persists an audit snapshot capturing the current state of the given entity
     * along with the operation that triggered it.
     *
     * @param entity the identity provider entity whose state is being audited
     * @param action the type of operation performed (e.g. {@code INSERT}, {@code UPDATE}, {@code DELETE})
     */
    private void saveAuditRecord(IdentityProviderEntity entity, String action) {
        IdentityProviderAudEntity aud = new IdentityProviderAudEntity();

        aud.setIdentityProviderId(entity.getId());
        aud.setName(entity.getName());

        aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());

        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());

        identityProviderAudJPARepository.save(aud);
    }
}
