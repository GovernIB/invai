package es.caib.invai.back.persistence.repository.maintenance.development.providerRole;

import es.caib.invai.back.service.model.maintenance.development.providerRole.ProviderRole;
import es.caib.invai.back.persistence.model.maintenance.development.providerRole.ProviderRoleEntity;
import es.caib.invai.back.persistence.model.maintenance.development.providerRole.ProviderRoleAudEntity;
import es.caib.invai.back.service.mapper.maintenance.development.providerRole.ProviderRoleMapper;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link ProviderRoleRepository}.
 *
 * @since 1.0.2
 */
@Repository
@Slf4j
public class ProviderRoleRepositoryAdapter implements ProviderRoleRepository {

    /** Spring Data JPA repository used for CRUD and query operations on {@link ProviderRoleEntity}. */
    @Autowired
    private ProviderRoleJPARepository providerRoleJPARepository;

    /** Spring Data JPA repository used to persist historical audit snapshots of role records. */
    @Autowired
    private ProviderRoleAudJPARepository providerRoleAudJPARepository;

    /** Mapper used to convert between Role domain models and JPA entities. */
    @Autowired
    private ProviderRoleMapper providerRoleMapper;

    /**
     * Resolves an active role record matching an identification primary index.
     *
     * @param id the unique role identifier
     * @return the matching {@link ProviderRole} domain model, or {@code null} if not found
     */
    @Override
    public ProviderRole findById(Long id) {
        return providerRoleJPARepository.findById(id)
                .map(providerRoleMapper::toModel)
                .orElse(null);
    }

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries.
     *
     * @param filter   the search criteria used to narrow the results
     * @param pageable the pagination and sorting parameters
     * @return a page of matching {@link ProviderRole} domain models
     */
    @Override
    public Page<ProviderRole> findAll(ProviderRoleCriteria filter, Pageable pageable) {
        Specification<ProviderRoleEntity> spec = ProviderRoleSpecification.filterByCriteria(filter);
        return providerRoleJPARepository.findAll(spec, pageable).map(providerRoleMapper::toModel);
    }

    /**
     * Confirms uniqueness of the name across active role registries.
     *
     * @param name the role name to check
     * @return {@code true} if an active role with that name exists
     */
    @Override
    public boolean existsByNameAndDeletedAtIsNull(String name) {
        return providerRoleJPARepository.existsByNameAndDeletedAtIsNull(name);
    }

    /**
     * Checks for duplicate name conflicts excluding a target primary reference row.
     *
     * @param name the role name to check
     * @param id   the identifier to exclude from the check
     * @return {@code true} if an active role other than the given identifier owns that name
     */
    @Override
    public boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id) {
        return providerRoleJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
    }

    /**
     * Registers a new role profile structure inside relational tracking systems, mirroring an audit snapshot.
     *
     * @param role the role domain model to persist
     * @return the persisted role, including its generated identifier
     */
    @Override
    public ProviderRole create(ProviderRole role) {
        log.info("Repository: Persisting new role entity into database");
        ProviderRoleEntity entity = providerRoleMapper.toEntity(role);
        entity = providerRoleJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");

        return providerRoleMapper.toModel(entity);
    }

    /**
     * Commits changes onto an active domain record identified by a target primary sequence index,
     * mirroring an audit snapshot.
     *
     * @param role the role domain model holding the updated data
     * @param id   the identifier of the role record to update
     * @return the updated role domain model
     */
    @Override
    public ProviderRole update(ProviderRole role, Long id) {
        log.info("Repository: Merging changes into existing role record for ID: {}", id);
        ProviderRoleEntity entity = providerRoleMapper.toEntity(role);
        entity.setId(id);
        entity = providerRoleJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");

        return providerRoleMapper.toModel(entity);
    }

    /**
     * Flags a tracking profile context record as soft-deleted inside persistence layers,
     * mirroring an audit snapshot.
     *
     * @param role the role domain model to soft-delete
     */
    @Override
    public void delete(ProviderRole role) {
        log.info("Repository: Soft deleting role entity with ID: {}", role.getId());
        ProviderRoleEntity entity = providerRoleMapper.toEntity(role);
        entity.setId(role.getId());
        entity = providerRoleJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Commits a flat audit record mirror tracing the current transactional context phase status.
     *
     * @param entity the role entity whose current state should be captured
     * @param action the type of action being audited (e.g. INSERT, UPDATE, DELETE)
     */
    private void saveAuditRecord(ProviderRoleEntity entity, String action) {
        ProviderRoleAudEntity aud = new ProviderRoleAudEntity();

        aud.setRoleId(entity.getId());
        aud.setName(entity.getName());
        aud.setNameEs(entity.getNameEs());

        aud.setCreatedAt(Objects.nonNull(entity.getCreatedAt()) ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(Objects.nonNull(entity.getCreatedBy()) ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());

        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());

        providerRoleAudJPARepository.save(aud);
    }
}
