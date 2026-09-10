package es.caib.invai.back.interna.maintenance.classificationSegment;

import es.caib.invai.back.interna.maintenance.classificationSegment.DTO.ClassificationSegmentInputDTO;
import es.caib.invai.back.interna.maintenance.classificationSegment.DTO.ClassificationSegmentOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.classificationSegment.ClassificationSegmentCriteria;
import es.caib.invai.back.service.facade.maintenance.classificationSegment.ClassificationSegmentService;
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
 * Unit tests for {@link ClassificationSegmentController}, verifying that every endpoint delegates to
 * {@link ClassificationSegmentService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class ClassificationSegmentControllerTest {

    @Mock
    private ClassificationSegmentService classificationSegmentService;

    private ClassificationSegmentController classificationSegmentController;

    @BeforeEach
    void setUp() {
        classificationSegmentController = new ClassificationSegmentController(classificationSegmentService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        ClassificationSegmentOutputDTO dto = new ClassificationSegmentOutputDTO();
        when(classificationSegmentService.getById(1L)).thenReturn(dto);

        ResponseEntity<ClassificationSegmentOutputDTO> response = classificationSegmentController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        ClassificationSegmentCriteria filter = new ClassificationSegmentCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<ClassificationSegmentOutputDTO> page = new PageImpl<>(List.of(new ClassificationSegmentOutputDTO()));
        when(classificationSegmentService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<ClassificationSegmentOutputDTO>> response = classificationSegmentController.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        ClassificationSegmentInputDTO inputDTO = new ClassificationSegmentInputDTO("Name", "Name ES");
        ClassificationSegmentOutputDTO dto = new ClassificationSegmentOutputDTO();
        when(classificationSegmentService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<ClassificationSegmentOutputDTO> response = classificationSegmentController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        ClassificationSegmentInputDTO inputDTO = new ClassificationSegmentInputDTO("Name", "Name ES");
        ClassificationSegmentOutputDTO dto = new ClassificationSegmentOutputDTO();
        when(classificationSegmentService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<ClassificationSegmentOutputDTO> response = classificationSegmentController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = classificationSegmentController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(classificationSegmentService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        ClassificationSegmentOutputDTO dto = new ClassificationSegmentOutputDTO();
        when(classificationSegmentService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<ClassificationSegmentOutputDTO> response = classificationSegmentController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}
