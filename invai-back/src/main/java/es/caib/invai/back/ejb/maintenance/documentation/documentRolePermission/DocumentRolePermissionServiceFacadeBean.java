package es.caib.invai.back.ejb.maintenance.documentation.documentRolePermission;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentRoleCategoryOutputDTO;
import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentRolePermissionInputDTO;
import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentRolePermissionOutputDTO;
import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentRolePermissionUpdateInputDTO;
import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentUserPermissionOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.documentation.documentCategory.DocumentCategoryRepository;
import es.caib.invai.back.persistence.repository.maintenance.documentation.documentRolePermission.DocumentRolePermissionRepository;
import es.caib.invai.back.service.facade.maintenance.documentation.documentRolePermission.DocumentRolePermissionService;
import es.caib.invai.back.service.mapper.maintenance.documentation.documentCategory.DocumentCategoryMapper;
import es.caib.invai.back.service.mapper.maintenance.documentation.documentRolePermission.DocumentRolePermissionMapper;
import es.caib.invai.back.service.model.maintenance.documentation.documentCategory.DocumentCategory;
import es.caib.invai.back.service.model.maintenance.documentation.documentRolePermission.DocumentPermission;
import es.caib.invai.back.service.model.maintenance.documentation.documentRolePermission.DocumentRolePermission;
import es.caib.invai.back.service.model.maintenance.role.Role;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Facade service implementation for the documentation permissions of the roles of the INVAI
 * application. Each role has one row per category with a flag per permission; a role added to the
 * application gets its rows, with read permission only, for every active category, and a category
 * created later gets its rows for every role. The rows of a role are deleted when the role is
 * taken off the application.
 *
 * @since 1.0.7
 */
@Service
@Slf4j
@Transactional
public class DocumentRolePermissionServiceFacadeBean implements DocumentRolePermissionService {

    /** Mapper used to convert between documentation permission domain models, entities, and DTOs. */
    @Autowired
    private DocumentRolePermissionMapper documentRolePermissionMapper;

    /** Repository port used to persist and query the documentation permissions of the roles. */
    @Autowired
    private DocumentRolePermissionRepository documentRolePermissionRepository;

    /** Repository port used to check the categories the permissions are set on. */
    @Autowired
    private DocumentCategoryRepository documentCategoryRepository;

    /** Mapper used to convert the documentation categories to their response DTOs. */
    @Autowired
    private DocumentCategoryMapper documentCategoryMapper;

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<DocumentRolePermissionOutputDTO> getByCategory(Long categoryId) {
        log.debug("Facade: Fetching the documentation permissions of every role in category ID: {}", categoryId);
        validateCategory(categoryId);
        List<DocumentRolePermission> existing = documentRolePermissionRepository.findByCategoryId(categoryId);
        Set<Long> rolesWithRow = existing.stream().map(DocumentRolePermission::getRoleId).collect(Collectors.toSet());
        List<DocumentRolePermissionOutputDTO> permissions = new ArrayList<>(existing.stream()
                .map(documentRolePermissionMapper::toResponse)
                .toList());
        documentRolePermissionRepository.findRoles().stream()
                .filter(role -> !rolesWithRow.contains(role.getId()))
                .map(role -> new DocumentRolePermissionOutputDTO(role.getId(), role.getName(), role.getDescription(), false, false, false, false))
                .forEach(permissions::add);
        permissions.sort(Comparator.comparing(DocumentRolePermissionOutputDTO::getName));
        return permissions;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<DocumentRoleCategoryOutputDTO> getByRole(String roleName) {
        log.debug("Facade: Fetching the categories assigned to role '{}' with its documentation permissions", roleName);
        if (StringUtils.isBlank(roleName) || !documentRolePermissionRepository.existsByRoleName(roleName)) {
            throw new BusinessRuleException(Constants.ERR_DOCROLEPERM_UNKNOWN_ROLE);
        }
        Set<Long> activeCategoryIds = new HashSet<>(documentCategoryRepository.findActiveCategoryIds());
        return documentRolePermissionRepository.findByRoleNames(Set.of(roleName)).stream()
                .filter(row -> activeCategoryIds.contains(row.getCategoryId()))
                .filter(row -> row.isCanRead() || row.isCanUpload() || row.isCanEdit() || row.isCanDelete())
                .sorted(Comparator.comparing(DocumentRolePermission::getCategoryId))
                .map(row -> new DocumentRoleCategoryOutputDTO(
                        documentCategoryMapper.toResponse(documentCategoryRepository.findById(row.getCategoryId())),
                        row.isCanRead(), row.isCanUpload(), row.isCanEdit(), row.isCanDelete()))
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public List<DocumentRolePermissionOutputDTO> update(Long categoryId, DocumentRolePermissionUpdateInputDTO inputDTO) {
        log.info("Facade: Setting the documentation permissions of {} role(s) in category ID: {}", inputDTO.getPermissions().size(), categoryId);
        validateCategory(categoryId);

        Set<Long> roleIds = new HashSet<>();
        for (DocumentRolePermissionInputDTO item : inputDTO.getPermissions()) {
            if (!roleIds.add(item.getRoleId())) {
                throw new BusinessRuleException(Constants.ERR_DOCROLEPERM_DUPLICATED_ROLE);
            }
        }

        Set<Long> knownRoleIds = documentRolePermissionRepository.findRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet());
        for (DocumentRolePermissionInputDTO item : inputDTO.getPermissions()) {
            DocumentRolePermission existing = documentRolePermissionRepository.findByRoleIdAndCategoryId(item.getRoleId(), categoryId);
            if (Objects.isNull(existing)) {
                if (knownRoleIds.contains(item.getRoleId())) {
                    createPermission(item, categoryId);
                    continue;
                }
                throw new BusinessRuleException(Constants.ERR_DOCROLEPERM_ROLE_NOT_FOUND);
            }
            if (hasChanged(existing, item)) {
                documentRolePermissionMapper.updateModelFromInput(item, existing);
                documentRolePermissionRepository.update(existing, existing.getId());
            }
        }
        return getByCategory(categoryId);
    }

    /** {@inheritDoc} */
    @Override
    public void assignDefaultPermissions(Long roleId, String roleName) {
        if (documentRolePermissionRepository.existsByRoleName(roleName)) {
            log.debug("Facade: Role '{}' already has its documentation permissions configured", roleName);
            return;
        }
        log.info("Facade: Giving the permission to read the documents to the new role '{}'", roleName);
        documentCategoryRepository.findActiveCategoryIds()
                .forEach(categoryId -> createReadPermission(roleId, categoryId));
    }

    /** {@inheritDoc} */
    @Override
    public void assignDefaultPermissionsToCategory(Long categoryId) {
        Set<Long> rolesWithPermissions = documentRolePermissionRepository.findByCategoryId(categoryId).stream()
                .map(DocumentRolePermission::getRoleId)
                .collect(Collectors.toSet());
        List<Long> roleIds = documentRolePermissionRepository.findRoles().stream()
                .map(Role::getId)
                .filter(roleId -> !rolesWithPermissions.contains(roleId))
                .toList();
        if (roleIds.isEmpty()) {
            return;
        }
        log.info("Facade: Giving the permission to read the documents of category ID: {} to {} role(s)", categoryId, roleIds.size());
        roleIds.forEach(roleId -> createReadPermission(roleId, categoryId));
    }

    /** {@inheritDoc} */
    @Override
    public void removePermissions(Long roleId) {
        List<DocumentRolePermission> existing = documentRolePermissionRepository.findByRoleId(roleId);
        if (existing.isEmpty()) {
            return;
        }
        log.info("Facade: Removing the documentation permissions of role ID: {}", roleId);
        existing.forEach(documentRolePermissionRepository::delete);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<DocumentUserPermissionOutputDTO> getCurrentUserPermissions() {
        if (isSuper()) {
            return documentCategoryRepository.findActiveCategoryIds().stream()
                    .sorted()
                    .map(categoryId -> new DocumentUserPermissionOutputDTO(categoryId, true, true, true, true))
                    .toList();
        }
        Map<Long, List<DocumentRolePermission>> byCategory = findCurrentUserPermissions().stream()
                .collect(Collectors.groupingBy(DocumentRolePermission::getCategoryId, TreeMap::new, Collectors.toList()));
        return byCategory.entrySet().stream()
                .map(entry -> new DocumentUserPermissionOutputDTO(
                        entry.getKey(),
                        entry.getValue().stream().anyMatch(DocumentRolePermission::isCanRead),
                        entry.getValue().stream().anyMatch(DocumentRolePermission::isCanUpload),
                        entry.getValue().stream().anyMatch(DocumentRolePermission::isCanEdit),
                        entry.getValue().stream().anyMatch(DocumentRolePermission::isCanDelete)))
                .filter(permissions -> permissions.isCanRead() || permissions.isCanUpload() || permissions.isCanEdit() || permissions.isCanDelete())
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public Set<Long> getReadableCategoryIds() {
        if (isSuper()) {
            return null;
        }
        return findCurrentUserPermissions().stream()
                .filter(DocumentRolePermission::isCanRead)
                .map(DocumentRolePermission::getCategoryId)
                .collect(Collectors.toSet());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public void requirePermission(DocumentPermission permission, Long categoryId) {
        boolean granted = isSuper() || findCurrentUserPermissions().stream()
                .filter(row -> Objects.equals(row.getCategoryId(), categoryId))
                .anyMatch(row -> switch (permission) {
                    case READ -> row.isCanRead();
                    case UPLOAD -> row.isCanUpload();
                    case EDIT -> row.isCanEdit();
                    case DELETE -> row.isCanDelete();
                });
        if (!granted) {
            log.warn("Facade: The current user lacks the documentation permission {} in category ID: {}", permission, categoryId);
            throw new AccessDeniedException("Access denied: the user can't " + permission + " documents of the category " + categoryId);
        }
    }

    /**
     * Retrieves the permissions of every role of the logged user, in every category.
     *
     * @return the permissions, empty if none of their roles has any configured
     */
    private List<DocumentRolePermission> findCurrentUserPermissions() {
        Set<String> roleNames = SecurityUtils.getCurrentAuthorities().stream()
                .filter(authority -> StringUtils.startsWith(authority, Constants.ROLE_PREFIX))
                .map(authority -> StringUtils.removeStart(authority, Constants.ROLE_PREFIX))
                .collect(Collectors.toSet());
        if (roleNames.isEmpty()) {
            return List.of();
        }
        return documentRolePermissionRepository.findByRoleNames(roleNames);
    }

    /**
     * Gives a role the permission to read the documents of a category, and no other.
     *
     * @param roleId     identifier of the role
     * @param categoryId identifier of the category
     */
    private void createReadPermission(Long roleId, Long categoryId) {
        DocumentRolePermission documentRolePermission = new DocumentRolePermission();
        documentRolePermission.setRoleId(roleId);
        documentRolePermission.setCategoryId(categoryId);
        documentRolePermission.setCanRead(true);
        documentRolePermissionRepository.create(documentRolePermission);
    }

    /**
     * Persists the permissions the input sets for a role that had none configured in the category,
     * as the role is listed in it with all of them set to {@code false}.
     *
     * @param item       the permissions to set
     * @param categoryId identifier of the category
     */
    private void createPermission(DocumentRolePermissionInputDTO item, Long categoryId) {
        DocumentRolePermission documentRolePermission = new DocumentRolePermission();
        documentRolePermission.setRoleId(item.getRoleId());
        documentRolePermission.setCategoryId(categoryId);
        documentRolePermissionMapper.updateModelFromInput(item, documentRolePermission);
        documentRolePermissionRepository.create(documentRolePermission);
    }

    /**
     * Checks that the category exists, is active and is a category, not a subcategory: the
     * permissions are only set on categories.
     *
     * @param categoryId identifier of the category
     * @throws BusinessRuleException if any of those conditions fails
     */
    private void validateCategory(Long categoryId) {
        DocumentCategory category = documentCategoryRepository.findById(categoryId);
        if (Objects.isNull(category)) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTCATEGORY_NOT_FOUND);
        }
        if (Objects.nonNull(category.getDeletedAt())) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTCATEGORY_NOT_ACTIVE);
        }
        if (!category.isCategory()) {
            throw new BusinessRuleException(Constants.ERR_DOCUMENTCATEGORY_NOT_CATEGORY);
        }
    }

    /**
     * Tells whether the permissions of the input differ from the ones the role has now.
     */
    private static boolean hasChanged(DocumentRolePermission existing, DocumentRolePermissionInputDTO input) {
        return existing.isCanRead() != input.getCanRead()
                || existing.isCanUpload() != input.getCanUpload()
                || existing.isCanEdit() != input.getCanEdit()
                || existing.isCanDelete() != input.getCanDelete();
    }

    /**
     * Tells whether the user bound to the current security context is {@code INV_SUPER}.
     *
     * @return {@code true} if the user holds the {@link Constants#SUPER_AUTHORITY} authority
     */
    private static boolean isSuper() {
        return SecurityUtils.getCurrentAuthorities().contains(Constants.SUPER_AUTHORITY);
    }
}
