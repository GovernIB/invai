package es.caib.invai.back.persistence.repository.maintenance.development.role;

import es.caib.invai.back.service.model.maintenance.development.role.Role;
import es.caib.invai.back.persistence.model.maintenance.development.role.RoleEntity;
import es.caib.invai.back.persistence.model.maintenance.development.role.RoleAudEntity;
import es.caib.invai.back.service.mapper.maintenance.development.role.RoleMapper;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link RoleRepository}.
 *
 * @since 1.0.2
 */
@Repository
@Slf4j
public class RoleRepositoryAdapter implements RoleRepository {

    /** Spring Data JPA repository used for CRUD and query operations on {@link RoleEntity}. */
    @Autowired
    private RoleJPARepository roleJPARepository;

    /** Spring Data JPA repository used to persist historical audit snapshots of role records. */
    @Autowired
    private RoleAudJPARepository roleAudJPARepository;

    /** Mapper used to convert between Role domain models and JPA entities. */
    @Autowired
    private RoleMapper roleMapper;

    /**
     * Resolves an active role record matching an identification primary index.
     *
     * @param id the unique role identifier
     * @return the matching {@link Role} domain model, or {@code null} if not found
     */
    @Override
    public Role findById(Long id) {
        return roleJPARepository.findById(id)
                .map(roleMapper::toModel)
                .orElse(null);
    }

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries.
     *
     * @param filter   the search criteria used to narrow the results
     * @param pageable the pagination and sorting parameters
     * @return a page of matching {@link Role} domain models
     */
    @Override
    public Page<Role> findAll(RoleCriteria filter, Pageable pageable) {
        Specification<RoleEntity> spec = RoleSpecification.filterByCriteria(filter);
        return roleJPARepository.findAll(spec, pageable).map(roleMapper::toModel);
    }

    /**
     * Confirms uniqueness of the name across active role registries.
     *
     * @param name the role name to check
     * @return {@code true} if an active role with that name exists
     */
    @Override
    public boolean existsByNameAndDeletedAtIsNull(String name) {
        return roleJPARepository.existsByNameAndDeletedAtIsNull(name);
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
        return roleJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
    }

    /**
     * Registers a new role profile structure inside relational tracking systems, mirroring an audit snapshot.
     *
     * @param role the role domain model to persist
     * @return the persisted role, including its generated identifier
     */
    @Override
    public Role create(Role role) {
        log.info("Repository: Persisting new role entity into database");
        RoleEntity entity = roleMapper.toEntity(role);
        entity = roleJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");

        return roleMapper.toModel(entity);
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
    public Role update(Role role, Long id) {
        log.info("Repository: Merging changes into existing role record for ID: {}", id);
        RoleEntity entity = roleMapper.toEntity(role);
        entity.setId(id);
        entity = roleJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");

        return roleMapper.toModel(entity);
    }

    /**
     * Flags a tracking profile context record as soft-deleted inside persistence layers,
     * mirroring an audit snapshot.
     *
     * @param role the role domain model to soft-delete
     */
    @Override
    public void delete(Role role) {
        log.info("Repository: Soft deleting role entity with ID: {}", role.getId());
        RoleEntity entity = roleMapper.toEntity(role);
        entity.setId(role.getId());
        entity = roleJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Commits a flat audit record mirror tracing the current transactional context phase status.
     *
     * @param entity the role entity whose current state should be captured
     * @param action the type of action being audited (e.g. INSERT, UPDATE, DELETE)
     */
    private void saveAuditRecord(RoleEntity entity, String action) {
        RoleAudEntity aud = new RoleAudEntity();

        aud.setRoleId(entity.getId());
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

        roleAudJPARepository.save(aud);
    }
}
