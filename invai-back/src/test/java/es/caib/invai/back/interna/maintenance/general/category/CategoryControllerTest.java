package es.caib.invai.back.interna.maintenance.general.category;

import es.caib.invai.back.interna.maintenance.general.category.DTO.CategoryInputDTO;
import es.caib.invai.back.interna.maintenance.general.category.DTO.CategoryOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.general.category.CategoryCriteria;
import es.caib.invai.back.service.facade.maintenance.general.category.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CategoryController}, verifying that every endpoint delegates to
 * {@link CategoryService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    private CategoryController categoryController;

    @BeforeEach
    void setUp() {
        categoryController = new CategoryController(categoryService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        CategoryOutputDTO dto = new CategoryOutputDTO();
        when(categoryService.getById(1L)).thenReturn(dto);

        ResponseEntity<CategoryOutputDTO> response = categoryController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        CategoryCriteria filter = new CategoryCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<CategoryOutputDTO> page = new PageImpl<>(List.of(new CategoryOutputDTO()));
        when(categoryService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<CategoryOutputDTO>> response = categoryController.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        CategoryInputDTO inputDTO = new CategoryInputDTO("Name", "Nombre");
        CategoryOutputDTO dto = new CategoryOutputDTO();
        when(categoryService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<CategoryOutputDTO> response = categoryController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        CategoryInputDTO inputDTO = new CategoryInputDTO("Name", "Nombre");
        CategoryOutputDTO dto = new CategoryOutputDTO();
        when(categoryService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<CategoryOutputDTO> response = categoryController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = categoryController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(categoryService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        CategoryOutputDTO dto = new CategoryOutputDTO();
        when(categoryService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<CategoryOutputDTO> response = categoryController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
