package es.caib.invai.back.ejb.maintenance.general.category;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.general.category.DTO.CategoryInputDTO;
import es.caib.invai.back.interna.maintenance.general.category.DTO.CategoryOutputDTO;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.persistence.repository.maintenance.general.category.CategoryCriteria;
import es.caib.invai.back.persistence.repository.maintenance.general.category.CategoryRepository;
import es.caib.invai.back.service.mapper.maintenance.general.category.CategoryMapper;
import es.caib.invai.back.service.model.maintenance.general.category.Category;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CategoryServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link CategoryRepository}, {@link CategoryMapper}, and {@link ApplicationRepository}
 * collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceFacadeBeanTest {

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private CategoryServiceFacadeBean categoryServiceFacadeBean;

    private Category activeCategory;

    @BeforeEach
    void setUp() {
        activeCategory = new Category();
        activeCategory.setId(1L);
        activeCategory.setName("Infrastructure");
        activeCategory.setNameEs("Infraestructura");
    }

    @Test
    void getById_found_returnsMappedResponse() {
        CategoryOutputDTO expected = new CategoryOutputDTO();
        when(categoryRepository.findById(1L)).thenReturn(activeCategory);
        when(categoryMapper.toResponse(activeCategory)).thenReturn(expected);

        CategoryOutputDTO result = categoryServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(categoryRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> categoryServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_CATEGORY_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        CategoryCriteria criteria = new CategoryCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<Category> domainPage = new PageImpl<>(List.of(activeCategory));
        CategoryOutputDTO mapped = new CategoryOutputDTO();
        when(categoryRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(categoryMapper.toResponse(activeCategory)).thenReturn(mapped);

        Page<CategoryOutputDTO> result = categoryServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueName_persistsAndReturnsResponse() {
        CategoryInputDTO inputDTO = new CategoryInputDTO("New Category", "Categoria nueva");
        Category model = new Category();
        Category saved = new Category();
        CategoryOutputDTO response = new CategoryOutputDTO();
        when(categoryRepository.existsByNameAndDeletedAtIsNull("New Category")).thenReturn(false);
        when(categoryMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(categoryRepository.create(model)).thenReturn(saved);
        when(categoryMapper.toResponse(saved)).thenReturn(response);

        CategoryOutputDTO result = categoryServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateName_throwsBusinessRuleException() {
        CategoryInputDTO inputDTO = new CategoryInputDTO("Infrastructure", "Infraestructura");
        when(categoryRepository.existsByNameAndDeletedAtIsNull("Infrastructure")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> categoryServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_CATEGORY_DUPLICATED, ex.getMessage());
        verify(categoryRepository, never()).create(any());
    }

    @Test
    void create_duplicateNameEs_throwsBusinessRuleException() {
        CategoryInputDTO inputDTO = new CategoryInputDTO("New Category", "Infraestructura");
        when(categoryRepository.existsByNameEsAndDeletedAtIsNull("Infraestructura")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> categoryServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_CATEGORY_DUPLICATED_ES, ex.getMessage());
        verify(categoryRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        CategoryInputDTO inputDTO = new CategoryInputDTO("X", "Y");
        when(categoryRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> categoryServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_CATEGORY_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_duplicateName_throwsBusinessRuleException() {
        CategoryInputDTO inputDTO = new CategoryInputDTO("Taken", "Ocupado");
        when(categoryRepository.findById(1L)).thenReturn(activeCategory);
        when(categoryRepository.existsByNameAndIdNotAndDeletedAtIsNull("Taken", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> categoryServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_CATEGORY_DUPLICATED, ex.getMessage());
        verify(categoryMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_duplicateNameEs_throwsBusinessRuleException() {
        CategoryInputDTO inputDTO = new CategoryInputDTO("Taken", "Ocupado");
        when(categoryRepository.findById(1L)).thenReturn(activeCategory);
        when(categoryRepository.existsByNameEsAndIdNotAndDeletedAtIsNull("Ocupado", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> categoryServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_CATEGORY_DUPLICATED_ES, ex.getMessage());
        verify(categoryMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        CategoryInputDTO inputDTO = new CategoryInputDTO("Updated", "Actualizado");
        Category updated = new Category();
        CategoryOutputDTO response = new CategoryOutputDTO();
        when(categoryRepository.findById(1L)).thenReturn(activeCategory);
        when(categoryRepository.existsByNameAndIdNotAndDeletedAtIsNull("Updated", 1L)).thenReturn(false);
        when(categoryRepository.update(activeCategory, 1L)).thenReturn(updated);
        when(categoryMapper.toResponse(updated)).thenReturn(response);

        CategoryOutputDTO result = categoryServiceFacadeBean.update(1L, inputDTO);

        verify(categoryMapper).updateModelFromInput(inputDTO, activeCategory);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(categoryRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> categoryServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_CATEGORY_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeCategory.setDeletedAt(LocalDateTime.now());
        when(categoryRepository.findById(1L)).thenReturn(activeCategory);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> categoryServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_CATEGORY_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_hasDependencies_throwsBusinessRuleException() {
        when(categoryRepository.findById(1L)).thenReturn(activeCategory);
        when(applicationRepository.existsByCategoryId(1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> categoryServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_CATEGORY_DELETE_HAS_DEPENDENCIES, ex.getMessage());
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void delete_valid_softDeletesCategory() {
        when(categoryRepository.findById(1L)).thenReturn(activeCategory);
        when(applicationRepository.existsByCategoryId(1L)).thenReturn(false);

        categoryServiceFacadeBean.delete(1L);

        assertNotNull(activeCategory.getDeletedAt());
        verify(categoryRepository, times(1)).delete(activeCategory);
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(categoryRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> categoryServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_CATEGORY_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(categoryRepository.findById(1L)).thenReturn(activeCategory);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> categoryServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_CATEGORY_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesCategory() {
        activeCategory.setDeletedAt(LocalDateTime.now());
        activeCategory.setDeletedBy("someone");
        Category reactivated = new Category();
        CategoryOutputDTO response = new CategoryOutputDTO();
        when(categoryRepository.findById(1L)).thenReturn(activeCategory);
        when(categoryRepository.update(eq(activeCategory), eq(1L))).thenReturn(reactivated);
        when(categoryMapper.toResponse(reactivated)).thenReturn(response);

        CategoryOutputDTO result = categoryServiceFacadeBean.reactivate(1L);

        assertNull(activeCategory.getDeletedAt());
        assertNull(activeCategory.getDeletedBy());
        assertEquals(response, result);
    }
}
