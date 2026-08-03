package es.caib.invai.api.persistence.repository.role;

import es.caib.invai.api.service.model.Role;
import es.caib.invai.api.persistence.model.RoleEntity;
import es.caib.invai.api.persistence.model.RoleAudEntity;
import es.caib.invai.api.service.mapper.RoleMapper;
import es.caib.invai.api.utils.Utils;
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

    @Autowired
    private RoleJPARepository roleJPARepository;

    @Autowired
    private RoleAudJPARepository roleAudJPARepository;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public Role findById(Long id) {
        return roleJPARepository.findById(id)
                .map(roleMapper::toModel)
                .orElse(null);
    }

    @Override
    public Page<Role> findAll(RoleCriteria filter, Pageable pageable) {
        Specification<RoleEntity> spec = RoleSpecification.filterByCriteria(filter);
        return roleJPARepository.findAll(spec, pageable).map(roleMapper::toModel);
    }

    @Override
    public boolean existsByNameAndDeletedAtIsNull(String name) {
        return roleJPARepository.existsByNameAndDeletedAtIsNull(name);
    }

    @Override
    public boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id) {
        return roleJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
    }

    @Override
    public Role create(Role role) {
        log.info("Repository: Persisting new role entity into database");
        RoleEntity entity = roleMapper.toEntity(role);
        entity = roleJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");

        return roleMapper.toModel(entity);
    }

    @Override
    public Role update(Role role, Long id) {
        log.info("Repository: Merging changes into existing role record for ID: {}", id);
        RoleEntity entity = roleMapper.toEntity(role);
        entity.setId(id);
        entity = roleJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");

        return roleMapper.toModel(entity);
    }

    @Override
    public void delete(Role role) {
        log.info("Repository: Soft deleting role entity with ID: {}", role.getId());
        RoleEntity entity = roleMapper.toEntity(role);
        entity.setId(role.getId());
        entity = roleJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    private void saveAuditRecord(RoleEntity entity, String action) {
        RoleAudEntity aud = new RoleAudEntity();

        aud.setRoleId(entity.getId());
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

        roleAudJPARepository.save(aud);
    }
}
