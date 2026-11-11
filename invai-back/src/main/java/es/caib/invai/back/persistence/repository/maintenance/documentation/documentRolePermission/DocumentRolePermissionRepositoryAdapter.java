package es.caib.invai.back.persistence.repository.maintenance.documentation.documentRolePermission;

import es.caib.invai.back.persistence.model.maintenance.documentation.documentRolePermission.DocumentRolePermissionAudEntity;
import es.caib.invai.back.persistence.model.maintenance.documentation.documentRolePermission.DocumentRolePermissionEntity;
import es.caib.invai.back.service.mapper.maintenance.documentation.documentRolePermission.DocumentRolePermissionMapper;
import es.caib.invai.back.service.mapper.maintenance.role.RoleMapper;
import es.caib.invai.back.service.model.maintenance.documentation.documentRolePermission.DocumentRolePermission;
import es.caib.invai.back.service.model.maintenance.role.Role;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary
 * {@link DocumentRolePermissionRepository}.
 *
 * @since 1.0.7
 */
@Repository
@Slf4j
public class DocumentRolePermissionRepositoryAdapter implements DocumentRolePermissionRepository {

    /** Spring Data JPA repository used to access live {@link DocumentRolePermissionEntity} records. */
    @Autowired
    private DocumentRolePermissionJPARepository documentRolePermissionJPARepository;

    /** Spring Data JPA repository used to persist {@link DocumentRolePermissionAudEntity} audit snapshots. */
    @Autowired
    private DocumentRolePermissionAudJPARepository documentRolePermissionAudJPARepository;

    /** Mapper used to convert between documentation permission domain models and JPA entities. */
    @Autowired
    private DocumentRolePermissionMapper documentRolePermissionMapper;

    /** Mapper used to convert the roles that have permissions into domain models. */
    @Autowired
    private RoleMapper roleMapper;

    @Override
    public List<DocumentRolePermission> findByCategoryId(Long categoryId) {
        return documentRolePermissionJPARepository.findByCategoryIdOrderByRoleNameAsc(categoryId).stream()
                .map(documentRolePermissionMapper::toModel)
                .toList();
    }

    @Override
    public List<DocumentRolePermission> findByRoleId(Long roleId) {
        return documentRolePermissionJPARepository.findByRoleId(roleId).stream()
                .map(documentRolePermissionMapper::toModel)
                .toList();
    }

    @Override
    public DocumentRolePermission findByRoleIdAndCategoryId(Long roleId, Long categoryId) {
        return documentRolePermissionJPARepository.findByRoleIdAndCategoryId(roleId, categoryId)
                .map(documentRolePermissionMapper::toModel)
                .orElse(null);
    }

    @Override
    public boolean existsByRoleName(String roleName) {
        return documentRolePermissionJPARepository.existsByRoleName(roleName);
    }

    @Override
    public List<DocumentRolePermission> findByRoleNames(Collection<String> roleNames) {
        return documentRolePermissionJPARepository.findByRoleNameIn(roleNames).stream()
                .map(documentRolePermissionMapper::toModel)
                .toList();
    }

    @Override
    public List<Role> findRoles() {
        return documentRolePermissionJPARepository.findDistinctRoles().stream()
                .map(roleMapper::toModel)
                .toList();
    }

    @Override
    public DocumentRolePermission create(DocumentRolePermission documentRolePermission) {
        log.debug("Repository: Persisting new documentation permissions of role ID: {} in category ID: {}",
                documentRolePermission.getRoleId(), documentRolePermission.getCategoryId());
        DocumentRolePermissionEntity entity = documentRolePermissionMapper.toEntity(documentRolePermission);
        entity = documentRolePermissionJPARepository.save(entity);
        saveAuditRecord(entity, "INSERT");
        return documentRolePermissionMapper.toModel(entity);
    }

    @Override
    public DocumentRolePermission update(DocumentRolePermission documentRolePermission, Long id) {
        log.debug("Repository: Merging changes into the documentation permissions record ID: {}", id);
        DocumentRolePermissionEntity entity = documentRolePermissionMapper.toEntity(documentRolePermission);
        entity.setId(id);
        entity = documentRolePermissionJPARepository.save(entity);
        saveAuditRecord(entity, "UPDATE");
        return documentRolePermissionMapper.toModel(entity);
    }

    @Override
    public void delete(DocumentRolePermission documentRolePermission) {
        log.debug("Repository: Deleting the documentation permissions record ID: {}", documentRolePermission.getId());
        DocumentRolePermissionEntity entity = documentRolePermissionMapper.toEntity(documentRolePermission);
        entity.setId(documentRolePermission.getId());
        saveAuditRecord(entity, "DELETE");
        documentRolePermissionJPARepository.deleteById(documentRolePermission.getId());
    }

    /**
     * Builds and persists an audit snapshot capturing the current state of the given entity.
     *
     * @param entity the documentation permissions entity state to snapshot
     * @param action the type of mutation being audited ({@code INSERT}, {@code UPDATE} or {@code DELETE})
     */
    private void saveAuditRecord(DocumentRolePermissionEntity entity, String action) {
        DocumentRolePermissionAudEntity aud = new DocumentRolePermissionAudEntity();
        aud.setDocRolePermId(entity.getId());
        aud.setRoleId(entity.getRole().getId());
        aud.setCategoryId(entity.getCategory().getId());
        aud.setCanRead(entity.isCanRead());
        aud.setCanUpload(entity.isCanUpload());
        aud.setCanEdit(entity.isCanEdit());
        aud.setCanDelete(entity.isCanDelete());
        aud.setCreatedAt(Objects.nonNull(entity.getCreatedAt()) ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(Objects.nonNull(entity.getCreatedBy()) ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        if ("DELETE".equals(action)) {
            aud.setDeletedAt(LocalDateTime.now());
            aud.setDeletedBy(Utils.resolveCurrentUsername());
        }
        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());
        documentRolePermissionAudJPARepository.save(aud);
    }
}
