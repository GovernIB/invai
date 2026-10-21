package es.caib.invai.back.ejb.maintenance.documentation.documentCategory;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.documentation.category.DTO.DocumentCategoryInputDTO;
import es.caib.invai.back.interna.maintenance.documentation.category.DTO.DocumentCategoryOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.documentation.documentCategory.DocumentCategoryCriteria;
import es.caib.invai.back.persistence.repository.maintenance.documentation.documentCategory.DocumentCategoryRepository;
import es.caib.invai.back.service.mapper.maintenance.documentation.documentCategory.DocumentCategoryMapper;
import es.caib.invai.back.service.model.maintenance.documentation.documentCategory.DocumentCategory;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DocumentCategoryServiceFacadeBean}, exercising every branch of its
 * business rules with the {@link DocumentCategoryRepository} and {@link DocumentCategoryMapper}
 * collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class DocumentCategoryServiceFacadeBeanTest {

    @Mock
    private DocumentCategoryMapper documentCategoryMapper;

    @Mock
    private DocumentCategoryRepository documentCategoryRepository;

    @InjectMocks
    private DocumentCategoryServiceFacadeBean facade;

    private DocumentCategory category;
    private DocumentCategory subcategory;

    @BeforeEach
    void setUp() {
        category = model(1L, "Tècnica", null);
        subcategory = model(2L, "API", 1L);
    }

    private static DocumentCategory model(Long id, String name, Long parentId) {
        DocumentCategory model = new DocumentCategory();
        model.setId(id);
        model.setName(name);
        model.setNameEs(name);
        model.setParentId(parentId);
        return model;
    }

    private static DocumentCategoryInputDTO input(String name, Long parentId) {
        return new DocumentCategoryInputDTO(name, name, parentId);
    }

    private static BusinessRuleException rule(org.junit.jupiter.api.function.Executable executable) {
        return assertThrows(BusinessRuleException.class, executable);
    }

    // ------------------------------------------------------------------
    // getById / getAll
    // ------------------------------------------------------------------

    @Test
    void getById_found_returnsMappedResponse() {
        DocumentCategoryOutputDTO expected = new DocumentCategoryOutputDTO();
        when(documentCategoryRepository.findById(1L)).thenReturn(category);
        when(documentCategoryMapper.toResponse(category)).thenReturn(expected);

        assertSame(expected, facade.getById(1L));
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(documentCategoryRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = rule(() -> facade.getById(99L));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_mapsEveryCategoryOfThePage() {
        DocumentCategoryCriteria filter = new DocumentCategoryCriteria();
        Pageable pageable = Pageable.unpaged();
        DocumentCategoryOutputDTO dto = new DocumentCategoryOutputDTO();
        when(documentCategoryRepository.findAll(filter, pageable)).thenReturn(new PageImpl<>(List.of(category)));
        when(documentCategoryMapper.toResponse(category)).thenReturn(dto);

        Page<DocumentCategoryOutputDTO> result = facade.getAll(filter, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(dto, result.getContent().get(0));
    }

    // ------------------------------------------------------------------
    // create
    // ------------------------------------------------------------------

    @Test
    void create_category_isPersistedWithoutLookingForAParent() {
        DocumentCategoryInputDTO inputDTO = input("  Tècnica  ", null);
        DocumentCategory toSave = new DocumentCategory();
        DocumentCategory saved = new DocumentCategory();
        DocumentCategoryOutputDTO dto = new DocumentCategoryOutputDTO();
        when(documentCategoryRepository.existsActiveWithName("Tècnica", null, null)).thenReturn(false);
        when(documentCategoryMapper.toModelFromInput(inputDTO)).thenReturn(toSave);
        when(documentCategoryRepository.create(toSave)).thenReturn(saved);
        when(documentCategoryMapper.toResponse(saved)).thenReturn(dto);

        DocumentCategoryOutputDTO result = facade.create(inputDTO);

        assertSame(dto, result);
        assertEquals("Tècnica", inputDTO.getName());
        verify(documentCategoryRepository, never()).findById(any());
    }

    @Test
    void create_subcategory_ofAnActiveCategory_isPersisted() {
        DocumentCategoryInputDTO inputDTO = input("API", 1L);
        DocumentCategory toSave = new DocumentCategory();
        DocumentCategory saved = new DocumentCategory();
        when(documentCategoryRepository.findById(1L)).thenReturn(category);
        when(documentCategoryRepository.existsActiveWithName("API", 1L, null)).thenReturn(false);
        when(documentCategoryMapper.toModelFromInput(inputDTO)).thenReturn(toSave);
        when(documentCategoryRepository.create(toSave)).thenReturn(saved);
        when(documentCategoryMapper.toResponse(saved)).thenReturn(new DocumentCategoryOutputDTO());

        facade.create(inputDTO);

        verify(documentCategoryRepository).create(toSave);
    }

    @Test
    void create_parentNotFound_throwsBusinessRuleException() {
        when(documentCategoryRepository.findById(9L)).thenReturn(null);

        BusinessRuleException ex = rule(() -> facade.create(input("API", 9L)));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_PARENT_NOT_FOUND, ex.getMessage());
        verify(documentCategoryRepository, never()).create(any());
    }

    @Test
    void create_parentDeleted_throwsBusinessRuleException() {
        category.setDeletedAt(LocalDateTime.now());
        when(documentCategoryRepository.findById(1L)).thenReturn(category);

        BusinessRuleException ex = rule(() -> facade.create(input("API", 1L)));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_PARENT_NOT_ACTIVE, ex.getMessage());
        verify(documentCategoryRepository, never()).create(any());
    }

    @Test
    void create_parentIsASubcategory_throwsBusinessRuleException() {
        when(documentCategoryRepository.findById(2L)).thenReturn(subcategory);

        BusinessRuleException ex = rule(() -> facade.create(input("Deep", 2L)));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_PARENT_NOT_CATEGORY, ex.getMessage());
        verify(documentCategoryRepository, never()).create(any());
    }

    @Test
    void create_duplicatedNameInTheSameLevel_throwsBusinessRuleException() {
        when(documentCategoryRepository.findById(1L)).thenReturn(category);
        when(documentCategoryRepository.existsActiveWithName("API", 1L, null)).thenReturn(true);

        BusinessRuleException ex = rule(() -> facade.create(input("API", 1L)));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_DUPLICATED, ex.getMessage());
        verify(documentCategoryRepository, never()).create(any());
    }

    @Test
    void create_duplicatedSpanishNameInTheSameLevel_throwsBusinessRuleException() {
        when(documentCategoryRepository.findById(1L)).thenReturn(category);
        when(documentCategoryRepository.existsActiveWithNameEs("API", 1L, null)).thenReturn(true);

        BusinessRuleException ex = rule(() -> facade.create(input("API", 1L)));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_NAME_ES_DUPLICATED, ex.getMessage());
        verify(documentCategoryRepository, never()).create(any());
    }

    @Test
    void create_subcategoryWithItsOwnSpanishName_isPersisted() {
        DocumentCategoryInputDTO inputDTO = new DocumentCategoryInputDTO("Manuals", "Manuales", 1L);
        DocumentCategory toSave = new DocumentCategory();
        DocumentCategory saved = new DocumentCategory();
        when(documentCategoryRepository.findById(1L)).thenReturn(category);
        when(documentCategoryMapper.toModelFromInput(inputDTO)).thenReturn(toSave);
        when(documentCategoryRepository.create(toSave)).thenReturn(saved);
        when(documentCategoryMapper.toResponse(saved)).thenReturn(new DocumentCategoryOutputDTO());

        facade.create(inputDTO);

        verify(documentCategoryRepository).existsActiveWithName("Manuals", 1L, null);
        verify(documentCategoryRepository).existsActiveWithNameEs("Manuales", 1L, null);
        verify(documentCategoryRepository).create(toSave);
    }

    // ------------------------------------------------------------------
    // update
    // ------------------------------------------------------------------

    @Test
    void update_category_isPersisted() {
        DocumentCategoryInputDTO inputDTO = input("Tècnica 2", null);
        DocumentCategory updated = new DocumentCategory();
        DocumentCategoryOutputDTO dto = new DocumentCategoryOutputDTO();
        when(documentCategoryRepository.findById(1L)).thenReturn(category);
        when(documentCategoryRepository.existsActiveWithName("Tècnica 2", null, 1L)).thenReturn(false);
        when(documentCategoryRepository.update(category, 1L)).thenReturn(updated);
        when(documentCategoryMapper.toResponse(updated)).thenReturn(dto);

        DocumentCategoryOutputDTO result = facade.update(1L, inputDTO);

        assertSame(dto, result);
        verify(documentCategoryMapper).updateModelFromInput(inputDTO, category);
    }

    @Test
    void update_subcategoryMovedToAnotherActiveCategory_validatesTheNewParent() {
        DocumentCategory otherCategory = model(5L, "Seguretat", null);
        DocumentCategoryInputDTO inputDTO = input("API", 5L);
        when(documentCategoryRepository.findById(2L)).thenReturn(subcategory);
        when(documentCategoryRepository.findById(5L)).thenReturn(otherCategory);
        when(documentCategoryRepository.existsActiveWithName("API", 5L, 2L)).thenReturn(false);
        when(documentCategoryRepository.update(subcategory, 2L)).thenReturn(new DocumentCategory());
        when(documentCategoryMapper.toResponse(any())).thenReturn(new DocumentCategoryOutputDTO());

        facade.update(2L, inputDTO);

        verify(documentCategoryRepository).findById(5L);
        verify(documentCategoryRepository).update(subcategory, 2L);
    }

    @Test
    void update_subcategoryMovedToADeletedCategory_throwsBusinessRuleException() {
        DocumentCategory deletedCategory = model(5L, "Seguretat", null);
        deletedCategory.setDeletedAt(LocalDateTime.now());
        when(documentCategoryRepository.findById(2L)).thenReturn(subcategory);
        when(documentCategoryRepository.findById(5L)).thenReturn(deletedCategory);

        BusinessRuleException ex = rule(() -> facade.update(2L, input("API", 5L)));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_PARENT_NOT_ACTIVE, ex.getMessage());
        verify(documentCategoryRepository, never()).update(any(), any());
    }

    @Test
    void update_subcategoryKeepingItsCategory_doesNotLookTheParentUpAgain() {
        when(documentCategoryRepository.findById(2L)).thenReturn(subcategory);
        when(documentCategoryRepository.existsActiveWithName("API v2", 1L, 2L)).thenReturn(false);
        when(documentCategoryRepository.update(subcategory, 2L)).thenReturn(new DocumentCategory());
        when(documentCategoryMapper.toResponse(any())).thenReturn(new DocumentCategoryOutputDTO());

        facade.update(2L, input("API v2", 1L));

        verify(documentCategoryRepository, never()).findById(1L);
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        when(documentCategoryRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = rule(() -> facade.update(99L, input("X", null)));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_deleted_throwsBusinessRuleException() {
        category.setDeletedAt(LocalDateTime.now());
        when(documentCategoryRepository.findById(1L)).thenReturn(category);

        BusinessRuleException ex = rule(() -> facade.update(1L, input("X", null)));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_NOT_ACTIVE, ex.getMessage());
        verify(documentCategoryRepository, never()).update(any(), any());
    }

    @Test
    void update_categoryTurnedIntoSubcategory_throwsBusinessRuleException() {
        when(documentCategoryRepository.findById(1L)).thenReturn(category);

        BusinessRuleException ex = rule(() -> facade.update(1L, input("Tècnica", 5L)));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_TYPE_CHANGE_NOT_ALLOWED, ex.getMessage());
        verify(documentCategoryRepository, never()).update(any(), any());
    }

    @Test
    void update_subcategoryTurnedIntoCategory_throwsBusinessRuleException() {
        when(documentCategoryRepository.findById(2L)).thenReturn(subcategory);

        BusinessRuleException ex = rule(() -> facade.update(2L, input("API", null)));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_TYPE_CHANGE_NOT_ALLOWED, ex.getMessage());
        verify(documentCategoryRepository, never()).update(any(), any());
    }

    @Test
    void update_duplicatedNameInTheSameLevel_throwsBusinessRuleException() {
        when(documentCategoryRepository.findById(1L)).thenReturn(category);
        when(documentCategoryRepository.existsActiveWithName("Seguretat", null, 1L)).thenReturn(true);

        BusinessRuleException ex = rule(() -> facade.update(1L, input("Seguretat", null)));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_DUPLICATED, ex.getMessage());
        verify(documentCategoryRepository, never()).update(any(), any());
    }

    @Test
    void update_duplicatedSpanishNameInTheSameLevel_throwsBusinessRuleException() {
        when(documentCategoryRepository.findById(1L)).thenReturn(category);
        when(documentCategoryRepository.existsActiveWithNameEs("Seguretat", null, 1L)).thenReturn(true);

        BusinessRuleException ex = rule(() -> facade.update(1L, input("Seguretat", null)));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_NAME_ES_DUPLICATED, ex.getMessage());
        verify(documentCategoryRepository, never()).update(any(), any());
    }

    // ------------------------------------------------------------------
    // delete
    // ------------------------------------------------------------------

    @Test
    void delete_categoryWithoutActiveSubcategories_isSoftDeleted() {
        when(documentCategoryRepository.findById(1L)).thenReturn(category);
        when(documentCategoryRepository.hasActiveSubcategories(1L)).thenReturn(false);

        facade.delete(1L);

        assertNotNull(category.getDeletedAt());
        assertNotNull(category.getDeletedBy());
        verify(documentCategoryRepository).delete(category);
    }

    @Test
    void delete_categoryWithActiveSubcategories_throwsBusinessRuleException() {
        when(documentCategoryRepository.findById(1L)).thenReturn(category);
        when(documentCategoryRepository.hasActiveSubcategories(1L)).thenReturn(true);

        BusinessRuleException ex = rule(() -> facade.delete(1L));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_HAS_ACTIVE_SUBCATEGORIES, ex.getMessage());
        assertNull(category.getDeletedAt());
        verify(documentCategoryRepository, never()).delete(any());
    }

    @Test
    void delete_subcategory_isSoftDeletedWithoutCheckingSubcategories() {
        when(documentCategoryRepository.findById(2L)).thenReturn(subcategory);

        facade.delete(2L);

        assertNotNull(subcategory.getDeletedAt());
        verify(documentCategoryRepository, never()).hasActiveSubcategories(any());
        verify(documentCategoryRepository).delete(subcategory);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(documentCategoryRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = rule(() -> facade.delete(99L));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyDeleted_throwsBusinessRuleException() {
        category.setDeletedAt(LocalDateTime.now());
        when(documentCategoryRepository.findById(1L)).thenReturn(category);

        BusinessRuleException ex = rule(() -> facade.delete(1L));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_NOT_ACTIVE, ex.getMessage());
        verify(documentCategoryRepository, never()).delete(any());
    }

    // ------------------------------------------------------------------
    // reactivate
    // ------------------------------------------------------------------

    @Test
    void reactivate_category_clearsTheDeletion() {
        category.setDeletedAt(LocalDateTime.now());
        category.setDeletedBy("jdoe");
        DocumentCategory updated = new DocumentCategory();
        DocumentCategoryOutputDTO dto = new DocumentCategoryOutputDTO();
        when(documentCategoryRepository.findById(1L)).thenReturn(category);
        when(documentCategoryRepository.existsActiveWithName("Tècnica", null, 1L)).thenReturn(false);
        when(documentCategoryRepository.update(category, 1L)).thenReturn(updated);
        when(documentCategoryMapper.toResponse(updated)).thenReturn(dto);

        DocumentCategoryOutputDTO result = facade.reactivate(1L);

        assertSame(dto, result);
        assertNull(category.getDeletedAt());
        assertNull(category.getDeletedBy());
    }

    @Test
    void reactivate_subcategoryOfAnActiveCategory_clearsTheDeletion() {
        subcategory.setDeletedAt(LocalDateTime.now());
        when(documentCategoryRepository.findById(2L)).thenReturn(subcategory);
        when(documentCategoryRepository.findById(1L)).thenReturn(category);
        when(documentCategoryRepository.existsActiveWithName("API", 1L, 2L)).thenReturn(false);
        when(documentCategoryRepository.update(subcategory, 2L)).thenReturn(new DocumentCategory());
        when(documentCategoryMapper.toResponse(any())).thenReturn(new DocumentCategoryOutputDTO());

        facade.reactivate(2L);

        assertNull(subcategory.getDeletedAt());
        verify(documentCategoryRepository).update(subcategory, 2L);
    }

    @Test
    void reactivate_subcategoryOfADeletedCategory_throwsBusinessRuleException() {
        subcategory.setDeletedAt(LocalDateTime.now());
        category.setDeletedAt(LocalDateTime.now());
        when(documentCategoryRepository.findById(2L)).thenReturn(subcategory);
        when(documentCategoryRepository.findById(1L)).thenReturn(category);

        BusinessRuleException ex = rule(() -> facade.reactivate(2L));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_PARENT_NOT_ACTIVE, ex.getMessage());
        verify(documentCategoryRepository, never()).update(any(), any());
    }

    @Test
    void reactivate_subcategoryWhoseCategoryIsGone_throwsBusinessRuleException() {
        subcategory.setDeletedAt(LocalDateTime.now());
        when(documentCategoryRepository.findById(2L)).thenReturn(subcategory);
        when(documentCategoryRepository.findById(1L)).thenReturn(null);

        BusinessRuleException ex = rule(() -> facade.reactivate(2L));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_PARENT_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_nameTakenByAnActiveRecordMeanwhile_throwsBusinessRuleException() {
        category.setDeletedAt(LocalDateTime.now());
        when(documentCategoryRepository.findById(1L)).thenReturn(category);
        when(documentCategoryRepository.existsActiveWithName("Tècnica", null, 1L)).thenReturn(true);

        BusinessRuleException ex = rule(() -> facade.reactivate(1L));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_DUPLICATED, ex.getMessage());
        verify(documentCategoryRepository, never()).update(any(), any());
    }

    @Test
    void reactivate_spanishNameTakenByAnActiveRecordMeanwhile_throwsBusinessRuleException() {
        category.setDeletedAt(LocalDateTime.now());
        when(documentCategoryRepository.findById(1L)).thenReturn(category);
        when(documentCategoryRepository.existsActiveWithNameEs("Tècnica", null, 1L)).thenReturn(true);

        BusinessRuleException ex = rule(() -> facade.reactivate(1L));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_NAME_ES_DUPLICATED, ex.getMessage());
        verify(documentCategoryRepository, never()).update(any(), any());
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(documentCategoryRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = rule(() -> facade.reactivate(99L));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(documentCategoryRepository.findById(1L)).thenReturn(category);

        BusinessRuleException ex = rule(() -> facade.reactivate(1L));

        assertEquals(Constants.ERR_DOCUMENTCATEGORY_ACTIVE, ex.getMessage());
        verify(documentCategoryRepository, never()).update(any(), any());
    }
}
