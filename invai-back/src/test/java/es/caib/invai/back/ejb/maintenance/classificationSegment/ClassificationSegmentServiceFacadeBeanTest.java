package es.caib.invai.back.ejb.maintenance.classificationSegment;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.classificationSegment.DTO.ClassificationSegmentInputDTO;
import es.caib.invai.back.interna.maintenance.classificationSegment.DTO.ClassificationSegmentOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.classificationSegment.ClassificationSegmentCriteria;
import es.caib.invai.back.persistence.repository.maintenance.classificationSegment.ClassificationSegmentRepository;
import es.caib.invai.back.service.mapper.maintenance.classificationSegment.ClassificationSegmentMapper;
import es.caib.invai.back.service.model.maintenance.classificationSegment.ClassificationSegment;
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
 * Unit tests for {@link ClassificationSegmentServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link ClassificationSegmentRepository} and {@link ClassificationSegmentMapper} collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class ClassificationSegmentServiceFacadeBeanTest {

    @Mock
    private ClassificationSegmentMapper classificationSegmentMapper;

    @Mock
    private ClassificationSegmentRepository classificationSegmentRepository;

    @InjectMocks
    private ClassificationSegmentServiceFacadeBean classificationSegmentServiceFacadeBean;

    private ClassificationSegment activeClassificationSegment;

    @BeforeEach
    void setUp() {
        activeClassificationSegment = new ClassificationSegment();
        activeClassificationSegment.setId(1L);
        activeClassificationSegment.setName("Segment I");
    }

    @Test
    void getById_found_returnsMappedResponse() {
        ClassificationSegmentOutputDTO expected = new ClassificationSegmentOutputDTO();
        when(classificationSegmentRepository.findById(1L)).thenReturn(activeClassificationSegment);
        when(classificationSegmentMapper.toResponse(activeClassificationSegment)).thenReturn(expected);

        ClassificationSegmentOutputDTO result = classificationSegmentServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(classificationSegmentRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> classificationSegmentServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_CLASSIFICATIONSEGMENT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        ClassificationSegmentCriteria criteria = new ClassificationSegmentCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<ClassificationSegment> domainPage = new PageImpl<>(List.of(activeClassificationSegment));
        ClassificationSegmentOutputDTO mapped = new ClassificationSegmentOutputDTO();
        when(classificationSegmentRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(classificationSegmentMapper.toResponse(activeClassificationSegment)).thenReturn(mapped);

        Page<ClassificationSegmentOutputDTO> result = classificationSegmentServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueName_persistsAndReturnsResponse() {
        ClassificationSegmentInputDTO inputDTO = new ClassificationSegmentInputDTO("Segment II", "Segment II");
        ClassificationSegment model = new ClassificationSegment();
        ClassificationSegment saved = new ClassificationSegment();
        ClassificationSegmentOutputDTO response = new ClassificationSegmentOutputDTO();
        when(classificationSegmentRepository.existsByNameAndDeletedAtIsNull("Segment II")).thenReturn(false);
        when(classificationSegmentMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(classificationSegmentRepository.create(model)).thenReturn(saved);
        when(classificationSegmentMapper.toResponse(saved)).thenReturn(response);

        ClassificationSegmentOutputDTO result = classificationSegmentServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateName_throwsBusinessRuleException() {
        ClassificationSegmentInputDTO inputDTO = new ClassificationSegmentInputDTO("Segment I", "Segment I");
        when(classificationSegmentRepository.existsByNameAndDeletedAtIsNull("Segment I")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> classificationSegmentServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_CLASSIFICATIONSEGMENT_DUPLICATED, ex.getMessage());
        verify(classificationSegmentRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        ClassificationSegmentInputDTO inputDTO = new ClassificationSegmentInputDTO("X", "X ES");
        when(classificationSegmentRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> classificationSegmentServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_CLASSIFICATIONSEGMENT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_duplicateName_throwsBusinessRuleException() {
        ClassificationSegmentInputDTO inputDTO = new ClassificationSegmentInputDTO("Taken", "Taken ES");
        when(classificationSegmentRepository.findById(1L)).thenReturn(activeClassificationSegment);
        when(classificationSegmentRepository.existsByNameAndIdNotAndDeletedAtIsNull("Taken", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> classificationSegmentServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_CLASSIFICATIONSEGMENT_DUPLICATED, ex.getMessage());
        verify(classificationSegmentMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        ClassificationSegmentInputDTO inputDTO = new ClassificationSegmentInputDTO("Updated", "Updated ES");
        ClassificationSegment updated = new ClassificationSegment();
        ClassificationSegmentOutputDTO response = new ClassificationSegmentOutputDTO();
        when(classificationSegmentRepository.findById(1L)).thenReturn(activeClassificationSegment);
        when(classificationSegmentRepository.existsByNameAndIdNotAndDeletedAtIsNull("Updated", 1L)).thenReturn(false);
        when(classificationSegmentRepository.update(activeClassificationSegment, 1L)).thenReturn(updated);
        when(classificationSegmentMapper.toResponse(updated)).thenReturn(response);

        ClassificationSegmentOutputDTO result = classificationSegmentServiceFacadeBean.update(1L, inputDTO);

        verify(classificationSegmentMapper).updateModelFromInput(inputDTO, activeClassificationSegment);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(classificationSegmentRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> classificationSegmentServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_CLASSIFICATIONSEGMENT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeClassificationSegment.setDeletedAt(LocalDateTime.now());
        when(classificationSegmentRepository.findById(1L)).thenReturn(activeClassificationSegment);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> classificationSegmentServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_CLASSIFICATIONSEGMENT_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesClassificationSegment() {
        when(classificationSegmentRepository.findById(1L)).thenReturn(activeClassificationSegment);

        classificationSegmentServiceFacadeBean.delete(1L);

        assertNotNull(activeClassificationSegment.getDeletedAt());
        verify(classificationSegmentRepository, times(1)).delete(activeClassificationSegment);
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(classificationSegmentRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> classificationSegmentServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_CLASSIFICATIONSEGMENT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(classificationSegmentRepository.findById(1L)).thenReturn(activeClassificationSegment);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> classificationSegmentServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_CLASSIFICATIONSEGMENT_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesClassificationSegment() {
        activeClassificationSegment.setDeletedAt(LocalDateTime.now());
        activeClassificationSegment.setDeletedBy("someone");
        ClassificationSegment reactivated = new ClassificationSegment();
        ClassificationSegmentOutputDTO response = new ClassificationSegmentOutputDTO();
        when(classificationSegmentRepository.findById(1L)).thenReturn(activeClassificationSegment);
        when(classificationSegmentRepository.update(eq(activeClassificationSegment), eq(1L))).thenReturn(reactivated);
        when(classificationSegmentMapper.toResponse(reactivated)).thenReturn(response);

        ClassificationSegmentOutputDTO result = classificationSegmentServiceFacadeBean.reactivate(1L);

        assertNull(activeClassificationSegment.getDeletedAt());
        assertNull(activeClassificationSegment.getDeletedBy());
        assertEquals(response, result);
    }
}
