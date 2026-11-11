package es.caib.invai.back.ejb.maintenance.documentation.documentRolePermission;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.documentation.category.DTO.DocumentCategoryOutputDTO;
import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentRoleCategoryOutputDTO;
import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentRolePermissionInputDTO;
import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentRolePermissionOutputDTO;
import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentRolePermissionUpdateInputDTO;
import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentUserPermissionOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.documentation.documentCategory.DocumentCategoryRepository;
import es.caib.invai.back.persistence.repository.maintenance.documentation.documentRolePermission.DocumentRolePermissionRepository;
import es.caib.invai.back.service.mapper.maintenance.documentation.documentCategory.DocumentCategoryMapper;
import es.caib.invai.back.service.mapper.maintenance.documentation.documentRolePermission.DocumentRolePermissionMapper;
import es.caib.invai.back.service.mapper.maintenance.documentation.documentRolePermission.DocumentRolePermissionMapperImpl;
import es.caib.invai.back.service.model.maintenance.documentation.documentCategory.DocumentCategory;
import es.caib.invai.back.service.model.maintenance.documentation.documentRolePermission.DocumentPermission;
import es.caib.invai.back.service.model.maintenance.documentation.documentRolePermission.DocumentRolePermission;
import es.caib.invai.back.service.model.maintenance.role.Role;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DocumentRolePermissionServiceFacadeBean}, exercising every branch of its
 * business rules with the {@link DocumentRolePermissionRepository} and
 * {@link DocumentCategoryRepository} ports mocked.
 */
@ExtendWith(MockitoExtension.class)
class DocumentRolePermissionServiceFacadeBeanTest {

    private static final Long CATEGORY_ID = 20L;

    @Spy
    private DocumentRolePermissionMapper documentRolePermissionMapper = new DocumentRolePermissionMapperImpl();

    @Mock
    private DocumentRolePermissionRepository documentRolePermissionRepository;

    @Mock
    private DocumentCategoryRepository documentCategoryRepository;

    @Mock
    private DocumentCategoryMapper documentCategoryMapper;

    @InjectMocks
    private DocumentRolePermissionServiceFacadeBean facade;

    private DocumentRolePermission sistemes;

    @BeforeEach
    void setUp() {
        sistemes = permission(1L, 10L, CATEGORY_ID, "INV_SISTEMES", true, true, true, true);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static DocumentRolePermission permission(Long id, Long roleId, Long categoryId, String name,
                                                      boolean read, boolean upload, boolean edit, boolean delete) {
        DocumentRolePermission model = new DocumentRolePermission();
        model.setId(id);
        model.setRoleId(roleId);
        model.setCategoryId(categoryId);
        model.setRoleName(name);
        model.setCanRead(read);
        model.setCanUpload(upload);
        model.setCanEdit(edit);
        model.setCanDelete(delete);
        return model;
    }

    private static Role role(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        role.setDescription("description of " + name);
        return role;
    }

    private static DocumentCategory category(Long id, Long parentId, LocalDateTime deletedAt) {
        DocumentCategory category = new DocumentCategory();
        category.setId(id);
        category.setParentId(parentId);
        category.setDeletedAt(deletedAt);
        return category;
    }

    private void givenActiveCategory() {
        when(documentCategoryRepository.findById(CATEGORY_ID)).thenReturn(category(CATEGORY_ID, null, null));
    }

    private static DocumentRolePermissionUpdateInputDTO update(DocumentRolePermissionInputDTO... items) {
        return new DocumentRolePermissionUpdateInputDTO(List.of(items));
    }

    // -- getByCategory

    @Test
    void getByCategory_returnsEveryRoleMapped() {
        givenActiveCategory();
        when(documentRolePermissionRepository.findByCategoryId(CATEGORY_ID)).thenReturn(List.of(sistemes));

        List<DocumentRolePermissionOutputDTO> result = facade.getByCategory(CATEGORY_ID);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getRoleId());
        assertEquals("INV_SISTEMES", result.get(0).getName());
        assertTrue(result.get(0).isCanDelete());
    }

    @Test
    void getByCategory_listsTheRolesWithoutRowInTheCategoryWithEveryPermissionAtFalse() {
        givenActiveCategory();
        when(documentRolePermissionRepository.findByCategoryId(CATEGORY_ID)).thenReturn(List.of(sistemes));
        when(documentRolePermissionRepository.findRoles())
                .thenReturn(List.of(role(10L, "INV_SISTEMES"), role(12L, "INV_DADES"), role(11L, "INV_DESENVO")));

        List<DocumentRolePermissionOutputDTO> result = facade.getByCategory(CATEGORY_ID);

        assertEquals(List.of("INV_DADES", "INV_DESENVO", "INV_SISTEMES"),
                result.stream().map(DocumentRolePermissionOutputDTO::getName).toList());
        DocumentRolePermissionOutputDTO dades = result.get(0);
        assertEquals(12L, dades.getRoleId());
        assertEquals("description of INV_DADES", dades.getDescription());
        assertFalse(dades.isCanRead());
        assertFalse(dades.isCanUpload());
        assertFalse(dades.isCanEdit());
        assertFalse(dades.isCanDelete());
        assertTrue(result.get(2).isCanDelete());
    }

    @Test
    void getByCategory_roleWithEveryPermissionAtFalse_isListed() {
        givenActiveCategory();
        DocumentRolePermission none = permission(2L, 11L, CATEGORY_ID, "INV_DESENVO", false, false, false, false);
        when(documentRolePermissionRepository.findByCategoryId(CATEGORY_ID)).thenReturn(List.of(none));
        when(documentRolePermissionRepository.findRoles()).thenReturn(List.of(role(11L, "INV_DESENVO")));

        List<DocumentRolePermissionOutputDTO> result = facade.getByCategory(CATEGORY_ID);

        assertEquals(1, result.size());
        assertEquals(11L, result.get(0).getRoleId());
    }

    @Test
    void getByCategory_categoryNotFound_throws() {
        when(documentCategoryRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.getByCategory(99L));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_NOT_FOUND, ex.getMessage());
        verifyNoInteractions(documentRolePermissionRepository);
    }

    @Test
    void getByCategory_deletedCategory_throws() {
        when(documentCategoryRepository.findById(CATEGORY_ID))
                .thenReturn(category(CATEGORY_ID, null, LocalDateTime.of(2026, 10, 27, 9, 0)));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.getByCategory(CATEGORY_ID));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_NOT_ACTIVE, ex.getMessage());
        verifyNoInteractions(documentRolePermissionRepository);
    }

    @Test
    void getByCategory_subcategory_throws() {
        when(documentCategoryRepository.findById(21L)).thenReturn(category(21L, CATEGORY_ID, null));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.getByCategory(21L));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_NOT_CATEGORY, ex.getMessage());
        verifyNoInteractions(documentRolePermissionRepository);
    }

    // -- getByRole

    @Test
    void getByRole_returnsTheActiveCategoriesWithSomePermission() {
        when(documentRolePermissionRepository.existsByRoleName("INV_SISTEMES")).thenReturn(true);
        when(documentCategoryRepository.findActiveCategoryIds()).thenReturn(List.of(20L, 21L, 22L));
        when(documentRolePermissionRepository.findByRoleNames(Set.of("INV_SISTEMES"))).thenReturn(List.of(
                permission(3L, 10L, 22L, "INV_SISTEMES", true, false, false, false),
                permission(1L, 10L, 20L, "INV_SISTEMES", true, true, true, true),
                permission(2L, 10L, 21L, "INV_SISTEMES", false, false, false, false),
                permission(4L, 10L, 23L, "INV_SISTEMES", true, true, true, true)));

        stubCategory(20L);
        stubCategory(22L);

        List<DocumentRoleCategoryOutputDTO> result = facade.getByRole("INV_SISTEMES");

        assertEquals(List.of(20L, 22L), result.stream().map(entry -> entry.getCategory().getId()).toList());
        assertEquals("Categoria 20", result.get(0).getCategory().getName());
        assertTrue(result.get(0).isCanDelete());
        assertTrue(result.get(1).isCanRead());
        assertFalse(result.get(1).isCanUpload());
    }

    private void stubCategory(Long id) {
        DocumentCategory category = new DocumentCategory();
        category.setId(id);
        DocumentCategoryOutputDTO dto = new DocumentCategoryOutputDTO();
        dto.setId(id);
        dto.setName("Categoria " + id);
        when(documentCategoryRepository.findById(id)).thenReturn(category);
        when(documentCategoryMapper.toResponse(category)).thenReturn(dto);
    }

    @Test
    void getByRole_roleWithoutPermissionsInAnyCategory_returnsAnEmptyList() {
        when(documentRolePermissionRepository.existsByRoleName("INV_SISTEMES")).thenReturn(true);
        when(documentCategoryRepository.findActiveCategoryIds()).thenReturn(List.of(20L));
        when(documentRolePermissionRepository.findByRoleNames(Set.of("INV_SISTEMES"))).thenReturn(List.of(
                permission(1L, 10L, 20L, "INV_SISTEMES", false, false, false, false)));

        assertTrue(facade.getByRole("INV_SISTEMES").isEmpty());
    }

    @Test
    void getByRole_unknownRole_throws() {
        when(documentRolePermissionRepository.existsByRoleName("INV_NOPE")).thenReturn(false);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.getByRole("INV_NOPE"));

        assertEquals(Constants.ERR_DOCROLEPERM_UNKNOWN_ROLE, ex.getMessage());
        verify(documentRolePermissionRepository, never()).findByRoleNames(any());
    }

    @Test
    void getByRole_blankRole_throwsWithoutQuerying() {
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.getByRole(" "));

        assertEquals(Constants.ERR_DOCROLEPERM_UNKNOWN_ROLE, ex.getMessage());
        verifyNoInteractions(documentRolePermissionRepository);
    }

    // -- update

    @Test
    void update_roleListedInTheCategoryWithoutRow_createsItsRow() {
        givenActiveCategory();
        when(documentRolePermissionRepository.findByRoleIdAndCategoryId(12L, CATEGORY_ID)).thenReturn(null);
        when(documentRolePermissionRepository.findRoles()).thenReturn(List.of(role(12L, "INV_DADES")));

        facade.update(CATEGORY_ID, update(new DocumentRolePermissionInputDTO(12L, true, false, true, false)));

        ArgumentCaptor<DocumentRolePermission> captor = ArgumentCaptor.forClass(DocumentRolePermission.class);
        verify(documentRolePermissionRepository).create(captor.capture());
        assertEquals(12L, captor.getValue().getRoleId());
        assertEquals(CATEGORY_ID, captor.getValue().getCategoryId());
        assertTrue(captor.getValue().isCanRead());
        assertFalse(captor.getValue().isCanUpload());
        assertTrue(captor.getValue().isCanEdit());
        assertFalse(captor.getValue().isCanDelete());
        verify(documentRolePermissionRepository, never()).update(any(), anyLong());
    }

    @Test
    void update_changedPermissions_savesThemAndReturnsEveryRoleOfTheCategory() {
        givenActiveCategory();
        when(documentRolePermissionRepository.findByRoleIdAndCategoryId(10L, CATEGORY_ID)).thenReturn(sistemes);
        when(documentRolePermissionRepository.findByCategoryId(CATEGORY_ID)).thenReturn(List.of(sistemes));

        List<DocumentRolePermissionOutputDTO> result = facade.update(CATEGORY_ID,
                update(new DocumentRolePermissionInputDTO(10L, true, false, false, false)));

        ArgumentCaptor<DocumentRolePermission> captor = ArgumentCaptor.forClass(DocumentRolePermission.class);
        verify(documentRolePermissionRepository).update(captor.capture(), any());
        assertTrue(captor.getValue().isCanRead());
        assertFalse(captor.getValue().isCanUpload());
        assertFalse(captor.getValue().isCanEdit());
        assertFalse(captor.getValue().isCanDelete());
        assertEquals(1, result.size());
    }

    @Test
    void update_severalRoles_savesOnlyTheOnesThatChanged() {
        givenActiveCategory();
        DocumentRolePermission lector = permission(2L, 11L, CATEGORY_ID, "INV_LECTOR", true, false, false, false);
        when(documentRolePermissionRepository.findByRoleIdAndCategoryId(10L, CATEGORY_ID)).thenReturn(sistemes);
        when(documentRolePermissionRepository.findByRoleIdAndCategoryId(11L, CATEGORY_ID)).thenReturn(lector);

        facade.update(CATEGORY_ID, update(
                new DocumentRolePermissionInputDTO(10L, true, true, true, true),
                new DocumentRolePermissionInputDTO(11L, true, true, false, false)));

        verify(documentRolePermissionRepository, never()).update(eq(sistemes), any());
        verify(documentRolePermissionRepository).update(lector, 2L);
        assertTrue(lector.isCanUpload());
    }

    @Test
    void update_samePermissions_savesNothing() {
        givenActiveCategory();
        when(documentRolePermissionRepository.findByRoleIdAndCategoryId(10L, CATEGORY_ID)).thenReturn(sistemes);

        facade.update(CATEGORY_ID, update(new DocumentRolePermissionInputDTO(10L, true, true, true, true)));

        verify(documentRolePermissionRepository, never()).update(any(), anyLong());
    }

    @Test
    void update_eachPermissionChangedAlone_isDetected() {
        givenActiveCategory();
        when(documentRolePermissionRepository.findByRoleIdAndCategoryId(10L, CATEGORY_ID)).thenReturn(sistemes);

        facade.update(CATEGORY_ID, update(new DocumentRolePermissionInputDTO(10L, false, true, true, true)));
        sistemes.setCanRead(true);
        facade.update(CATEGORY_ID, update(new DocumentRolePermissionInputDTO(10L, true, false, true, true)));
        sistemes.setCanUpload(true);
        facade.update(CATEGORY_ID, update(new DocumentRolePermissionInputDTO(10L, true, true, false, true)));
        sistemes.setCanEdit(true);
        facade.update(CATEGORY_ID, update(new DocumentRolePermissionInputDTO(10L, true, true, true, false)));

        verify(documentRolePermissionRepository, times(4)).update(any(), any());
    }

    @Test
    void update_roleListedTwice_throwsBeforeSavingAnything() {
        givenActiveCategory();

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.update(CATEGORY_ID, update(
                new DocumentRolePermissionInputDTO(10L, true, true, true, true),
                new DocumentRolePermissionInputDTO(10L, false, false, false, false))));

        assertEquals(Constants.ERR_DOCROLEPERM_DUPLICATED_ROLE, ex.getMessage());
        verifyNoInteractions(documentRolePermissionRepository);
    }

    @Test
    void update_unknownRole_throws() {
        givenActiveCategory();
        when(documentRolePermissionRepository.findByRoleIdAndCategoryId(99L, CATEGORY_ID)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.update(CATEGORY_ID,
                update(new DocumentRolePermissionInputDTO(99L, true, true, true, true))));

        assertEquals(Constants.ERR_DOCROLEPERM_ROLE_NOT_FOUND, ex.getMessage());
        verify(documentRolePermissionRepository, never()).update(any(), anyLong());
    }

    @Test
    void update_categoryNotFound_throwsBeforeTouchingThePermissions() {
        when(documentCategoryRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.update(99L,
                update(new DocumentRolePermissionInputDTO(10L, true, true, true, true))));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_NOT_FOUND, ex.getMessage());
        verifyNoInteractions(documentRolePermissionRepository);
    }

    @Test
    void update_deletedCategory_throwsBeforeTouchingThePermissions() {
        when(documentCategoryRepository.findById(CATEGORY_ID))
                .thenReturn(category(CATEGORY_ID, null, LocalDateTime.of(2026, 10, 27, 9, 0)));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.update(CATEGORY_ID,
                update(new DocumentRolePermissionInputDTO(10L, true, true, true, true))));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_NOT_ACTIVE, ex.getMessage());
        verifyNoInteractions(documentRolePermissionRepository);
    }

    @Test
    void update_subcategory_throwsBeforeTouchingThePermissions() {
        when(documentCategoryRepository.findById(21L)).thenReturn(category(21L, CATEGORY_ID, null));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.update(21L,
                update(new DocumentRolePermissionInputDTO(10L, true, true, true, true))));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_NOT_CATEGORY, ex.getMessage());
        verifyNoInteractions(documentRolePermissionRepository);
    }

    // -- assignDefaultPermissions

    @Test
    void assignDefaultPermissions_newRole_createsAReadOnlyRowInEveryActiveCategory() {
        when(documentRolePermissionRepository.existsByRoleName("INV_ADMIN")).thenReturn(false);
        when(documentCategoryRepository.findActiveCategoryIds()).thenReturn(List.of(20L, 21L));

        facade.assignDefaultPermissions(5L, "INV_ADMIN");

        ArgumentCaptor<DocumentRolePermission> captor = ArgumentCaptor.forClass(DocumentRolePermission.class);
        verify(documentRolePermissionRepository, times(2)).create(captor.capture());
        List<DocumentRolePermission> created = captor.getAllValues();
        assertEquals(List.of(20L, 21L), created.stream().map(DocumentRolePermission::getCategoryId).toList());
        for (DocumentRolePermission row : created) {
            assertEquals(5L, row.getRoleId());
            assertTrue(row.isCanRead());
            assertFalse(row.isCanUpload());
            assertFalse(row.isCanEdit());
            assertFalse(row.isCanDelete());
        }
    }

    @Test
    void assignDefaultPermissions_noActiveCategories_createsNothing() {
        when(documentRolePermissionRepository.existsByRoleName("INV_ADMIN")).thenReturn(false);
        when(documentCategoryRepository.findActiveCategoryIds()).thenReturn(List.of());

        facade.assignDefaultPermissions(5L, "INV_ADMIN");

        verify(documentRolePermissionRepository, never()).create(any());
    }

    @Test
    void assignDefaultPermissions_roleNameAlreadyConfigured_doesNothing() {
        when(documentRolePermissionRepository.existsByRoleName("INV_ADMIN")).thenReturn(true);

        facade.assignDefaultPermissions(5L, "INV_ADMIN");

        verify(documentRolePermissionRepository, never()).create(any());
        verifyNoInteractions(documentCategoryRepository);
    }

    // -- assignDefaultPermissionsToCategory

    @Test
    void assignDefaultPermissionsToCategory_createsAReadOnlyRowForEveryRole() {
        when(documentRolePermissionRepository.findByCategoryId(30L)).thenReturn(List.of());
        when(documentRolePermissionRepository.findRoles()).thenReturn(List.of(role(10L, "INV_SISTEMES"), role(11L, "INV_DESENVO")));

        facade.assignDefaultPermissionsToCategory(30L);

        ArgumentCaptor<DocumentRolePermission> captor = ArgumentCaptor.forClass(DocumentRolePermission.class);
        verify(documentRolePermissionRepository, times(2)).create(captor.capture());
        List<DocumentRolePermission> created = captor.getAllValues();
        assertEquals(List.of(10L, 11L), created.stream().map(DocumentRolePermission::getRoleId).toList());
        for (DocumentRolePermission row : created) {
            assertEquals(30L, row.getCategoryId());
            assertTrue(row.isCanRead());
            assertFalse(row.isCanUpload());
            assertFalse(row.isCanEdit());
            assertFalse(row.isCanDelete());
        }
    }

    @Test
    void assignDefaultPermissionsToCategory_skipsTheRolesThatAlreadyHaveARow() {
        when(documentRolePermissionRepository.findByCategoryId(CATEGORY_ID)).thenReturn(List.of(sistemes));
        when(documentRolePermissionRepository.findRoles()).thenReturn(List.of(role(10L, "INV_SISTEMES"), role(11L, "INV_DESENVO")));

        facade.assignDefaultPermissionsToCategory(CATEGORY_ID);

        ArgumentCaptor<DocumentRolePermission> captor = ArgumentCaptor.forClass(DocumentRolePermission.class);
        verify(documentRolePermissionRepository).create(captor.capture());
        assertEquals(11L, captor.getValue().getRoleId());
        assertEquals(CATEGORY_ID, captor.getValue().getCategoryId());
    }

    @Test
    void assignDefaultPermissionsToCategory_everyRoleAlreadyHasARow_isIdempotent() {
        when(documentRolePermissionRepository.findByCategoryId(CATEGORY_ID)).thenReturn(List.of(sistemes));
        when(documentRolePermissionRepository.findRoles()).thenReturn(List.of(role(10L, "INV_SISTEMES")));

        facade.assignDefaultPermissionsToCategory(CATEGORY_ID);

        verify(documentRolePermissionRepository, never()).create(any());
    }

    @Test
    void assignDefaultPermissionsToCategory_noRolesWithPermissions_createsNothing() {
        when(documentRolePermissionRepository.findByCategoryId(30L)).thenReturn(List.of());
        when(documentRolePermissionRepository.findRoles()).thenReturn(List.of());

        facade.assignDefaultPermissionsToCategory(30L);

        verify(documentRolePermissionRepository, never()).create(any());
    }

    // -- removePermissions

    @Test
    void removePermissions_roleWithPermissions_deletesEveryRow() {
        DocumentRolePermission otherCategory = permission(2L, 10L, 21L, "INV_SISTEMES", true, false, false, false);
        when(documentRolePermissionRepository.findByRoleId(10L)).thenReturn(List.of(sistemes, otherCategory));

        facade.removePermissions(10L);

        verify(documentRolePermissionRepository).delete(sistemes);
        verify(documentRolePermissionRepository).delete(otherCategory);
    }

    @Test
    void removePermissions_roleWithoutPermissions_doesNothing() {
        when(documentRolePermissionRepository.findByRoleId(10L)).thenReturn(List.of());

        facade.removePermissions(10L);

        verify(documentRolePermissionRepository, never()).delete(any());
    }

    // -- Permissions of the logged user

    private static void loginWithRoles(String... roles) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("jdoe", null, roles));
    }

    @Test
    void getCurrentUserPermissions_superUser_canDoEverythingInEveryActiveCategoryWithoutLookingAtTheTable() {
        loginWithRoles("ROLE_INV_SUPER");
        when(documentCategoryRepository.findActiveCategoryIds()).thenReturn(List.of(21L, 20L));

        List<DocumentUserPermissionOutputDTO> result = facade.getCurrentUserPermissions();

        assertEquals(List.of(20L, 21L), result.stream().map(DocumentUserPermissionOutputDTO::getCategoryId).toList());
        for (DocumentUserPermissionOutputDTO permissions : result) {
            assertTrue(permissions.isCanRead() && permissions.isCanUpload() && permissions.isCanEdit() && permissions.isCanDelete());
        }
        verifyNoInteractions(documentRolePermissionRepository);
    }

    @Test
    void getCurrentUserPermissions_addsUpThePermissionsOfEveryRoleOfTheUserInTheSameCategory() {
        loginWithRoles("ROLE_INV_SISTEMES", "ROLE_INV_LECTOR");
        when(documentRolePermissionRepository.findByRoleNames(Set.of("INV_SISTEMES", "INV_LECTOR"))).thenReturn(List.of(
                permission(1L, 10L, 20L, "INV_SISTEMES", false, true, false, false),
                permission(2L, 11L, 20L, "INV_LECTOR", true, false, false, false)));

        List<DocumentUserPermissionOutputDTO> result = facade.getCurrentUserPermissions();

        assertEquals(1, result.size());
        assertEquals(20L, result.get(0).getCategoryId());
        assertTrue(result.get(0).isCanRead());
        assertTrue(result.get(0).isCanUpload());
        assertFalse(result.get(0).isCanEdit());
        assertFalse(result.get(0).isCanDelete());
    }

    @Test
    void getCurrentUserPermissions_doesNotMixTheCategories_andSortsThemById() {
        loginWithRoles("ROLE_INV_SISTEMES");
        when(documentRolePermissionRepository.findByRoleNames(Set.of("INV_SISTEMES"))).thenReturn(List.of(
                permission(1L, 10L, 22L, "INV_SISTEMES", false, false, true, false),
                permission(2L, 10L, 20L, "INV_SISTEMES", true, false, false, false)));

        List<DocumentUserPermissionOutputDTO> result = facade.getCurrentUserPermissions();

        assertEquals(2, result.size());
        assertEquals(20L, result.get(0).getCategoryId());
        assertTrue(result.get(0).isCanRead());
        assertFalse(result.get(0).isCanEdit());
        assertEquals(22L, result.get(1).getCategoryId());
        assertFalse(result.get(1).isCanRead());
        assertTrue(result.get(1).isCanEdit());
    }

    @Test
    void getCurrentUserPermissions_categoriesWithoutAnyPermission_areLeftOut() {
        loginWithRoles("ROLE_INV_SISTEMES");
        when(documentRolePermissionRepository.findByRoleNames(Set.of("INV_SISTEMES"))).thenReturn(List.of(
                permission(1L, 10L, 20L, "INV_SISTEMES", false, false, false, false),
                permission(2L, 10L, 21L, "INV_SISTEMES", true, false, false, false)));

        List<DocumentUserPermissionOutputDTO> result = facade.getCurrentUserPermissions();

        assertEquals(1, result.size());
        assertEquals(21L, result.get(0).getCategoryId());
    }

    @Test
    void getCurrentUserPermissions_aRoleWithoutRow_hasNoPermissions() {
        loginWithRoles("ROLE_INV_NEW");
        when(documentRolePermissionRepository.findByRoleNames(Set.of("INV_NEW"))).thenReturn(List.of());

        assertTrue(facade.getCurrentUserPermissions().isEmpty());
    }

    @Test
    void getCurrentUserPermissions_authoritiesThatAreNotRoles_areIgnored() {
        loginWithRoles("SCOPE_openid", "ROLE_INV_LECTOR");
        when(documentRolePermissionRepository.findByRoleNames(Set.of("INV_LECTOR"))).thenReturn(List.of());

        facade.getCurrentUserPermissions();

        verify(documentRolePermissionRepository).findByRoleNames(Set.of("INV_LECTOR"));
    }

    @Test
    void getCurrentUserPermissions_withoutRoles_hasNoPermissionsAndDoesNotQuery() {
        loginWithRoles();

        assertTrue(facade.getCurrentUserPermissions().isEmpty());
        verifyNoInteractions(documentRolePermissionRepository);
    }

    @Test
    void getCurrentUserPermissions_nobodyLoggedIn_hasNoPermissions() {
        SecurityContextHolder.clearContext();

        assertTrue(facade.getCurrentUserPermissions().isEmpty());
        verifyNoInteractions(documentRolePermissionRepository);
    }

    // -- getReadableCategoryIds

    @Test
    void getReadableCategoryIds_superUser_returnsNullMeaningEveryCategory() {
        loginWithRoles("ROLE_INV_SUPER");

        assertNull(facade.getReadableCategoryIds());
        verifyNoInteractions(documentRolePermissionRepository);
    }

    @Test
    void getReadableCategoryIds_returnsTheCategoriesWhereSomeRoleCanRead() {
        loginWithRoles("ROLE_INV_SISTEMES", "ROLE_INV_LECTOR");
        when(documentRolePermissionRepository.findByRoleNames(Set.of("INV_SISTEMES", "INV_LECTOR"))).thenReturn(List.of(
                permission(1L, 10L, 20L, "INV_SISTEMES", false, true, false, false),
                permission(2L, 11L, 20L, "INV_LECTOR", true, false, false, false),
                permission(3L, 10L, 21L, "INV_SISTEMES", true, true, true, true),
                permission(4L, 11L, 22L, "INV_LECTOR", false, true, false, false)));

        assertEquals(Set.of(20L, 21L), facade.getReadableCategoryIds());
    }

    @Test
    void getReadableCategoryIds_withoutRoles_returnsAnEmptySetAndDoesNotQuery() {
        loginWithRoles();

        Set<Long> result = facade.getReadableCategoryIds();

        assertTrue(result.isEmpty());
        verifyNoInteractions(documentRolePermissionRepository);
    }

    // -- requirePermission

    @Test
    void requirePermission_grantedByARoleOfTheUser_doesNotThrow() {
        loginWithRoles("ROLE_INV_SISTEMES");
        when(documentRolePermissionRepository.findByRoleNames(Set.of("INV_SISTEMES"))).thenReturn(List.of(
                permission(1L, 10L, 20L, "INV_SISTEMES", true, false, true, false)));

        assertDoesNotThrow(() -> facade.requirePermission(DocumentPermission.READ, 20L));
        assertDoesNotThrow(() -> facade.requirePermission(DocumentPermission.EDIT, 20L));
    }

    @Test
    void requirePermission_grantedByAnyOfTheRolesOfTheUser_doesNotThrow() {
        loginWithRoles("ROLE_INV_SISTEMES", "ROLE_INV_LECTOR");
        when(documentRolePermissionRepository.findByRoleNames(Set.of("INV_SISTEMES", "INV_LECTOR"))).thenReturn(List.of(
                permission(1L, 10L, 20L, "INV_SISTEMES", false, true, false, false),
                permission(2L, 11L, 20L, "INV_LECTOR", false, false, false, true)));

        assertDoesNotThrow(() -> facade.requirePermission(DocumentPermission.UPLOAD, 20L));
        assertDoesNotThrow(() -> facade.requirePermission(DocumentPermission.DELETE, 20L));
    }

    @Test
    void requirePermission_notGrantedByAnyRoleOfTheUser_throwsAccessDenied() {
        loginWithRoles("ROLE_INV_SISTEMES");
        when(documentRolePermissionRepository.findByRoleNames(Set.of("INV_SISTEMES"))).thenReturn(List.of(
                permission(1L, 10L, 20L, "INV_SISTEMES", true, false, true, false)));

        assertThrows(AccessDeniedException.class, () -> facade.requirePermission(DocumentPermission.UPLOAD, 20L));
        assertThrows(AccessDeniedException.class, () -> facade.requirePermission(DocumentPermission.DELETE, 20L));
    }

    @Test
    void requirePermission_grantedInAnotherCategoryOnly_throwsAccessDenied() {
        loginWithRoles("ROLE_INV_SISTEMES");
        when(documentRolePermissionRepository.findByRoleNames(Set.of("INV_SISTEMES"))).thenReturn(List.of(
                permission(1L, 10L, 20L, "INV_SISTEMES", true, true, true, true)));

        assertThrows(AccessDeniedException.class, () -> facade.requirePermission(DocumentPermission.READ, 21L));
    }

    @Test
    void requirePermission_userWithoutRoles_throwsAccessDeniedWithoutQuerying() {
        loginWithRoles();

        assertThrows(AccessDeniedException.class, () -> facade.requirePermission(DocumentPermission.READ, 20L));
        verifyNoInteractions(documentRolePermissionRepository);
    }

    @Test
    void requirePermission_superUser_neverThrows() {
        loginWithRoles("ROLE_INV_SUPER");

        for (DocumentPermission permission : DocumentPermission.values()) {
            assertDoesNotThrow(() -> facade.requirePermission(permission, 20L));
        }
        verifyNoInteractions(documentRolePermissionRepository);
    }
}
