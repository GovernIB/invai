package es.caib.invai.back.ejb.application.documentation;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkInputDTO;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkOutputDTO;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkUpdateInputDTO;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.persistence.repository.application.documentation.link.AppDocumentationLinkCriteria;
import es.caib.invai.back.persistence.repository.application.documentation.link.AppLinkRepository;
import es.caib.invai.back.persistence.repository.maintenance.documentation.documentCategory.DocumentCategoryRepository;
import es.caib.invai.back.service.facade.maintenance.documentation.documentRolePermission.DocumentRolePermissionService;
import es.caib.invai.back.service.mapper.application.documentation.link.AppLinkMapper;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.documentation.link.AppLink;
import es.caib.invai.back.service.model.maintenance.documentation.documentCategory.DocumentCategory;
import es.caib.invai.back.service.model.maintenance.documentation.documentRolePermission.DocumentPermission;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppDocumentationLinkServiceFacadeBean}, with every collaborator mocked.
 */
@ExtendWith(MockitoExtension.class)
class AppDocumentationLinkServiceFacadeBeanTest {

    private static final String URL = "https://intranet.caib.es/manual";

    @Mock
    private AppLinkMapper appLinkMapper;
    @Mock
    private AppLinkRepository appLinkRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private DocumentCategoryRepository documentCategoryRepository;
    @Mock
    private DocumentRolePermissionService documentRolePermissionService;

    @InjectMocks
    private AppDocumentationLinkServiceFacadeBean facade;

    private AppLink stored;

    @BeforeEach
    void setUp() {
        stored = new AppLink();
        stored.setId(10L);
        stored.setApplicationId(3L);
        stored.setTitle("Manual");
        stored.setCategory(category(5L, null));
        stored.setUrl(URL);
    }

    private static DocumentCategory category(Long id, Long parentId) {
        DocumentCategory category = new DocumentCategory();
        category.setId(id);
        category.setParentId(parentId);
        return category;
    }

    private static AppDocumentationLinkInputDTO createInput() {
        return new AppDocumentationLinkInputDTO(3L, "  Manual  ", "desc", 5L, "  " + URL + "  ");
    }

    /** Update that moves the link from its current category 5 to the category 6. */
    private static AppDocumentationLinkUpdateInputDTO updateInput() {
        return new AppDocumentationLinkUpdateInputDTO("  Manual v2  ", null, 6L, URL);
    }

    /** Update that keeps the link in its current category 5. */
    private static AppDocumentationLinkUpdateInputDTO updateInputSameCategory() {
        return new AppDocumentationLinkUpdateInputDTO("  Manual v2  ", null, 5L, URL);
    }

    // ------------------------------------------------------------------
    // getAll
    // ------------------------------------------------------------------

    @Test
    void getAll_mapsEveryLinkOfThePage() {
        AppDocumentationLinkCriteria filter = new AppDocumentationLinkCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        AppDocumentationLinkOutputDTO dto = new AppDocumentationLinkOutputDTO();
        when(appLinkRepository.findAll(eq(filter), any(), eq(pageable))).thenReturn(new PageImpl<>(List.of(stored)));
        when(appLinkMapper.toResponse(stored)).thenReturn(dto);

        Page<AppDocumentationLinkOutputDTO> result = facade.getAll(filter, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(dto, result.getContent().get(0));
    }

    @Test
    void getAll_restrictsTheListingToTheCategoriesTheUserCanRead() {
        AppDocumentationLinkCriteria filter = new AppDocumentationLinkCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Set<Long> readable = Set.of(5L, 7L);
        when(documentRolePermissionService.getReadableCategoryIds()).thenReturn(readable);
        when(appLinkRepository.findAll(eq(filter), any(), eq(pageable))).thenReturn(new PageImpl<>(List.of()));

        facade.getAll(filter, pageable);

        verify(appLinkRepository).findAll(filter, readable, pageable);
        verify(documentRolePermissionService, never()).requirePermission(any(), anyLong());
    }

    @Test
    void getAll_leavesTheListingUnrestrictedWhenTheUserCanReadEverything() {
        AppDocumentationLinkCriteria filter = new AppDocumentationLinkCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        when(documentRolePermissionService.getReadableCategoryIds()).thenReturn(null);
        when(appLinkRepository.findAll(eq(filter), any(), eq(pageable))).thenReturn(new PageImpl<>(List.of()));

        facade.getAll(filter, pageable);

        verify(appLinkRepository).findAll(filter, null, pageable);
    }

    @Test
    void getAll_withACategoryFilter_requiresReadOnThatCategory() {
        AppDocumentationLinkCriteria filter = new AppDocumentationLinkCriteria();
        filter.setCategoryId(5L);
        Pageable pageable = PageRequest.of(0, 10);
        when(appLinkRepository.findAll(eq(filter), any(), eq(pageable))).thenReturn(new PageImpl<>(List.of()));

        facade.getAll(filter, pageable);

        verify(documentRolePermissionService).requirePermission(DocumentPermission.READ, 5L);
        verify(documentRolePermissionService).getReadableCategoryIds();
    }

    // ------------------------------------------------------------------
    // create
    // ------------------------------------------------------------------

    @Test
    void create_storesTheTrimmedLink() {
        AppDocumentationLinkInputDTO inputDTO = createInput();
        AppLink toSave = new AppLink();
        AppLink saved = new AppLink();
        AppDocumentationLinkOutputDTO dto = new AppDocumentationLinkOutputDTO();
        when(applicationRepository.findById(3L)).thenReturn(new Application());
        when(documentCategoryRepository.findById(5L)).thenReturn(category(5L, null));
        when(appLinkRepository.existsActiveDuplicate(3L, "Manual", 5L, URL)).thenReturn(false);
        when(appLinkMapper.toModelFromInput(inputDTO)).thenReturn(toSave);
        when(appLinkRepository.create(toSave)).thenReturn(saved);
        when(appLinkMapper.toResponse(saved)).thenReturn(dto);

        AppDocumentationLinkOutputDTO result = facade.create(inputDTO);

        assertSame(dto, result);
        assertEquals("Manual", inputDTO.getTitle());
        assertEquals(URL, inputDTO.getUrl());
        verify(documentRolePermissionService).requirePermission(DocumentPermission.UPLOAD, 5L);
    }

    @Test
    void create_applicationNotFound_throwsAndStoresNothing() {
        when(applicationRepository.findById(3L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.create(createInput()));

        assertEquals(Constants.ERR_APP_NOT_FOUND, ex.getMessage());
        verify(appLinkRepository, never()).create(any());
    }

    @Test
    void create_categoryNotFound_throwsAndStoresNothing() {
        when(applicationRepository.findById(3L)).thenReturn(new Application());
        when(documentCategoryRepository.findById(5L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.create(createInput()));

        assertEquals(Constants.ERR_LINK_CATEGORY_NOT_FOUND, ex.getMessage());
        verify(appLinkRepository, never()).create(any());
    }

    @Test
    void create_deletedCategory_throwsAndStoresNothing() {
        DocumentCategory deleted = category(5L, null);
        deleted.setDeletedAt(LocalDateTime.now());
        when(applicationRepository.findById(3L)).thenReturn(new Application());
        when(documentCategoryRepository.findById(5L)).thenReturn(deleted);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.create(createInput()));

        assertEquals(Constants.ERR_LINK_CATEGORY_NOT_ACTIVE, ex.getMessage());
        verify(appLinkRepository, never()).create(any());
    }

    @Test
    void create_aSubcategory_throwsAndStoresNothing() {
        when(applicationRepository.findById(3L)).thenReturn(new Application());
        when(documentCategoryRepository.findById(5L)).thenReturn(category(5L, 2L));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.create(createInput()));

        assertEquals(Constants.ERR_LINK_CATEGORY_NOT_ACTIVE, ex.getMessage());
        verify(appLinkRepository, never()).create(any());
    }

    @Test
    void create_aLinkWithTheSameTitleCategoryAndUrl_throwsAndStoresNothing() {
        when(applicationRepository.findById(3L)).thenReturn(new Application());
        when(documentCategoryRepository.findById(5L)).thenReturn(category(5L, null));
        when(appLinkRepository.existsActiveDuplicate(3L, "Manual", 5L, URL)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.create(createInput()));

        assertEquals(Constants.ERR_LINK_DUPLICATED, ex.getMessage());
        verify(appLinkRepository, never()).create(any());
    }

    // ------------------------------------------------------------------
    // update
    // ------------------------------------------------------------------

    @Test
    void update_appliesTheTrimmedDataAndSavesIt() {
        AppDocumentationLinkUpdateInputDTO inputDTO = updateInput();
        AppDocumentationLinkOutputDTO dto = new AppDocumentationLinkOutputDTO();
        when(appLinkRepository.findById(10L)).thenReturn(stored);
        when(documentCategoryRepository.findById(6L)).thenReturn(category(6L, null));
        when(appLinkRepository.existsActiveDuplicateExcluding(3L, "Manual v2", 6L, URL, 10L)).thenReturn(false);
        when(appLinkRepository.update(stored, 10L)).thenReturn(stored);
        when(appLinkMapper.toResponse(stored)).thenReturn(dto);

        AppDocumentationLinkOutputDTO result = facade.update(10L, inputDTO);

        assertSame(dto, result);
        assertEquals("Manual v2", inputDTO.getTitle());
        verify(appLinkMapper).updateModelFromInput(inputDTO, stored);
    }

    @Test
    void update_notFound_throwsWithoutCheckingAnyPermission() {
        when(appLinkRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.update(99L, updateInput()));

        assertEquals(Constants.ERR_LINK_NOT_FOUND, ex.getMessage());
        verify(appLinkRepository, never()).update(any(), anyLong());
        verifyNoInteractions(documentRolePermissionService);
    }

    @Test
    void update_deletedLink_throwsWithoutCheckingAnyPermission() {
        stored.setDeletedAt(LocalDateTime.now());
        when(appLinkRepository.findById(10L)).thenReturn(stored);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.update(10L, updateInput()));

        assertEquals(Constants.ERR_LINK_NOT_ACTIVE, ex.getMessage());
        verify(appLinkRepository, never()).update(any(), anyLong());
        verifyNoInteractions(documentRolePermissionService);
    }

    @Test
    void update_categoryNotFound_throws() {
        when(appLinkRepository.findById(10L)).thenReturn(stored);
        when(documentCategoryRepository.findById(6L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.update(10L, updateInput()));

        assertEquals(Constants.ERR_LINK_CATEGORY_NOT_FOUND, ex.getMessage());
        verify(appLinkRepository, never()).update(any(), anyLong());
    }

    @Test
    void update_deletedNewCategory_throws() {
        DocumentCategory deleted = category(6L, null);
        deleted.setDeletedAt(LocalDateTime.now());
        when(appLinkRepository.findById(10L)).thenReturn(stored);
        when(documentCategoryRepository.findById(6L)).thenReturn(deleted);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.update(10L, updateInput()));

        assertEquals(Constants.ERR_LINK_CATEGORY_NOT_ACTIVE, ex.getMessage());
        verify(appLinkRepository, never()).update(any(), anyLong());
    }

    @Test
    void update_aSubcategoryAsNewCategory_throws() {
        when(appLinkRepository.findById(10L)).thenReturn(stored);
        when(documentCategoryRepository.findById(6L)).thenReturn(category(6L, 2L));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.update(10L, updateInput()));

        assertEquals(Constants.ERR_LINK_CATEGORY_NOT_ACTIVE, ex.getMessage());
        verify(appLinkRepository, never()).update(any(), anyLong());
    }

    @Test
    void update_unchangedCategory_isNotCheckedAgain() {
        // The current category may have been deleted since the link was filed: the link must stay editable
        AppDocumentationLinkUpdateInputDTO inputDTO = updateInputSameCategory();
        when(appLinkRepository.findById(10L)).thenReturn(stored);
        when(appLinkRepository.update(stored, 10L)).thenReturn(stored);

        facade.update(10L, inputDTO);

        verifyNoInteractions(documentCategoryRepository);
        verify(appLinkMapper).updateModelFromInput(inputDTO, stored);
        verify(appLinkRepository).update(stored, 10L);
    }

    @Test
    void update_requiresEditOnTheCurrentAndOnTheNewCategory() {
        when(appLinkRepository.findById(10L)).thenReturn(stored);
        when(documentCategoryRepository.findById(6L)).thenReturn(category(6L, null));
        when(appLinkRepository.update(any(), anyLong())).thenReturn(stored);

        facade.update(10L, updateInput());

        InOrder order = inOrder(documentRolePermissionService);
        order.verify(documentRolePermissionService).requirePermission(DocumentPermission.EDIT, 5L);
        order.verify(documentRolePermissionService).requirePermission(DocumentPermission.EDIT, 6L);
    }

    @Test
    void update_unchangedCategory_requiresEditOnlyOnceOnTheCurrentCategory() {
        when(appLinkRepository.findById(10L)).thenReturn(stored);
        when(appLinkRepository.update(any(), anyLong())).thenReturn(stored);

        facade.update(10L, updateInputSameCategory());

        verify(documentRolePermissionService, times(1)).requirePermission(DocumentPermission.EDIT, 5L);
        verify(documentRolePermissionService, never()).requirePermission(DocumentPermission.EDIT, 6L);
    }

    @Test
    void update_anotherLinkWithTheSameTitleCategoryAndUrl_throwsAndChangesNothing() {
        when(appLinkRepository.findById(10L)).thenReturn(stored);
        when(documentCategoryRepository.findById(6L)).thenReturn(category(6L, null));
        when(appLinkRepository.existsActiveDuplicateExcluding(3L, "Manual v2", 6L, URL, 10L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.update(10L, updateInput()));

        assertEquals(Constants.ERR_LINK_DUPLICATED, ex.getMessage());
        verify(appLinkRepository, never()).update(any(), anyLong());
    }

    @Test
    void update_neverLooksForDuplicatesAmongTheDeletedOrOfOtherApplications() {
        when(appLinkRepository.findById(10L)).thenReturn(stored);
        when(documentCategoryRepository.findById(6L)).thenReturn(category(6L, null));
        when(appLinkRepository.update(any(), anyLong())).thenReturn(stored);

        facade.update(10L, updateInput());

        verify(appLinkRepository).existsActiveDuplicateExcluding(3L, "Manual v2", 6L, URL, 10L);
        verify(appLinkRepository, never()).existsActiveDuplicate(anyLong(), anyString(), anyLong(), anyString());
    }

    // ------------------------------------------------------------------
    // delete
    // ------------------------------------------------------------------

    @Test
    void delete_marksTheLinkAsDeleted() {
        when(appLinkRepository.findById(10L)).thenReturn(stored);

        facade.delete(10L);

        assertNotNull(stored.getDeletedAt());
        assertNotNull(stored.getDeletedBy());
        verify(appLinkRepository).delete(stored);
    }

    @Test
    void delete_requiresDeleteOnTheCategoryOfTheLink() {
        when(appLinkRepository.findById(10L)).thenReturn(stored);

        facade.delete(10L);

        verify(documentRolePermissionService).requirePermission(DocumentPermission.DELETE, 5L);
    }

    @Test
    void delete_notFound_throwsWithoutCheckingAnyPermission() {
        when(appLinkRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.delete(99L));

        assertEquals(Constants.ERR_LINK_NOT_FOUND, ex.getMessage());
        verify(appLinkRepository, never()).delete(any());
        verifyNoInteractions(documentRolePermissionService);
    }

    @Test
    void delete_alreadyDeleted_throwsWithoutCheckingAnyPermission() {
        stored.setDeletedAt(LocalDateTime.now());
        when(appLinkRepository.findById(10L)).thenReturn(stored);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.delete(10L));

        assertEquals(Constants.ERR_LINK_NOT_ACTIVE, ex.getMessage());
        verify(appLinkRepository, never()).delete(any());
        verifyNoInteractions(documentRolePermissionService);
    }

    // -- Permissions of the role of the logged user on the category: each action needs its own one, and is rejected before changing anything

    /** Makes the permission check fail for the given permission and category; lenient because other checks of the same method may run first. */
    private void denyOn(DocumentPermission permission, Long categoryId) {
        lenient().doThrow(new AccessDeniedException("denied")).when(documentRolePermissionService).requirePermission(permission, categoryId);
    }

    @Test
    void getAll_withoutReadPermissionOnTheFilteredCategory_isDenied() {
        AppDocumentationLinkCriteria filter = new AppDocumentationLinkCriteria();
        filter.setCategoryId(5L);
        denyOn(DocumentPermission.READ, 5L);

        assertThrows(AccessDeniedException.class, () -> facade.getAll(filter, Pageable.unpaged()));

        verifyNoInteractions(appLinkRepository);
    }

    @Test
    void create_withoutUploadPermissionOnTheCategory_isDeniedBeforeAnythingElse() {
        AppDocumentationLinkInputDTO inputDTO = createInput();
        denyOn(DocumentPermission.UPLOAD, 5L);

        assertThrows(AccessDeniedException.class, () -> facade.create(inputDTO));

        verifyNoInteractions(appLinkRepository, applicationRepository, documentCategoryRepository);
        // Not even sanitized: the permission is the first thing checked
        assertEquals("  Manual  ", inputDTO.getTitle());
    }

    @Test
    void update_withoutEditOnTheCurrentCategory_isDeniedAndChangesNothing() {
        when(appLinkRepository.findById(10L)).thenReturn(stored);
        denyOn(DocumentPermission.EDIT, 5L);

        assertThrows(AccessDeniedException.class, () -> facade.update(10L, updateInput()));

        verify(documentRolePermissionService, never()).requirePermission(DocumentPermission.EDIT, 6L);
        verifyNoInteractions(documentCategoryRepository);
        verify(appLinkRepository, never()).update(any(), anyLong());
    }

    @Test
    void update_withoutEditOnTheNewCategory_isDeniedAndChangesNothing() {
        when(appLinkRepository.findById(10L)).thenReturn(stored);
        denyOn(DocumentPermission.EDIT, 6L);

        assertThrows(AccessDeniedException.class, () -> facade.update(10L, updateInput()));

        verify(documentRolePermissionService).requirePermission(DocumentPermission.EDIT, 5L);
        verifyNoInteractions(documentCategoryRepository);
        verify(appLinkRepository, never()).update(any(), anyLong());
    }

    @Test
    void delete_withoutDeletePermissionOnTheCategoryOfTheLink_isDenied() {
        when(appLinkRepository.findById(10L)).thenReturn(stored);
        denyOn(DocumentPermission.DELETE, 5L);

        assertThrows(AccessDeniedException.class, () -> facade.delete(10L));

        verify(appLinkRepository, never()).delete(any());
        assertNull(stored.getDeletedAt());
    }
}
