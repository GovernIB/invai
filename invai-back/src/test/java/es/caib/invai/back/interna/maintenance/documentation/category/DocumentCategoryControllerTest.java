package es.caib.invai.back.interna.maintenance.documentation.category;

import es.caib.invai.back.interna.maintenance.documentation.category.DTO.DocumentCategoryInputDTO;
import es.caib.invai.back.interna.maintenance.documentation.category.DTO.DocumentCategoryOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.documentation.documentCategory.DocumentCategoryCriteria;
import es.caib.invai.back.service.facade.maintenance.documentation.documentCategory.DocumentCategoryService;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DocumentCategoryController}, verifying that every endpoint delegates to
 * {@link DocumentCategoryService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class DocumentCategoryControllerTest {

    @Mock
    private DocumentCategoryService documentCategoryService;

    private DocumentCategoryController controller;

    @BeforeEach
    void setUp() {
        controller = new DocumentCategoryController(documentCategoryService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        DocumentCategoryOutputDTO dto = new DocumentCategoryOutputDTO();
        when(documentCategoryService.getById(1L)).thenReturn(dto);

        ResponseEntity<DocumentCategoryOutputDTO> response = controller.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        DocumentCategoryCriteria filter = new DocumentCategoryCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<DocumentCategoryOutputDTO> page = new PageImpl<>(List.of(new DocumentCategoryOutputDTO()));
        when(documentCategoryService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<DocumentCategoryOutputDTO>> response = controller.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        DocumentCategoryInputDTO input = new DocumentCategoryInputDTO("Tècnica", "Técnica", null);
        DocumentCategoryOutputDTO dto = new DocumentCategoryOutputDTO();
        when(documentCategoryService.create(input)).thenReturn(dto);

        ResponseEntity<DocumentCategoryOutputDTO> response = controller.create(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        DocumentCategoryInputDTO input = new DocumentCategoryInputDTO("API", "API", 1L);
        DocumentCategoryOutputDTO dto = new DocumentCategoryOutputDTO();
        when(documentCategoryService.update(2L, input)).thenReturn(dto);

        ResponseEntity<DocumentCategoryOutputDTO> response = controller.update(2L, input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = controller.delete(3L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(documentCategoryService).delete(3L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        DocumentCategoryOutputDTO dto = new DocumentCategoryOutputDTO();
        when(documentCategoryService.reactivate(4L)).thenReturn(dto);

        ResponseEntity<DocumentCategoryOutputDTO> response = controller.reactivate(4L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
