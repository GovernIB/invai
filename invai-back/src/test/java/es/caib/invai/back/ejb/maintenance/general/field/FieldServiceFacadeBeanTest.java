package es.caib.invai.back.ejb.maintenance.general.field;

import es.caib.invai.back.ejb.maintenance.general.field.FieldServiceFacadeBean;
import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.general.field.DTO.FieldInputDTO;
import es.caib.invai.back.interna.maintenance.general.field.DTO.FieldOutputDTO;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.persistence.repository.maintenance.general.field.FieldCriteria;
import es.caib.invai.back.persistence.repository.maintenance.general.field.FieldRepository;
import es.caib.invai.back.service.mapper.maintenance.general.field.FieldMapper;
import es.caib.invai.back.service.model.maintenance.general.field.Field;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link FieldServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link FieldRepository}, {@link FieldMapper}, and {@link ApplicationRepository}
 * collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class FieldServiceFacadeBeanTest {

    @Mock
    private FieldMapper fieldMapper;

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private FieldServiceFacadeBean fieldServiceFacadeBean;

    private Field activeField;

    @BeforeEach
    void setUp() {
        activeField = new Field();
        activeField.setId(1L);
        activeField.setName("Cybersecurity");
        activeField.setNameEs("Ciberseguridad");
    }

    @Test
    void getById_found_returnsMappedResponse() {
        FieldOutputDTO expected = new FieldOutputDTO();
        when(fieldRepository.findById(1L)).thenReturn(activeField);
        when(fieldMapper.toResponse(activeField)).thenReturn(expected);

        FieldOutputDTO result = fieldServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(fieldRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> fieldServiceFacadeBean.getById(99L));
        assertEquals(Constants.FIELD_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        FieldCriteria criteria = new FieldCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<Field> domainPage = new PageImpl<>(List.of(activeField));
        FieldOutputDTO mapped = new FieldOutputDTO();
        when(fieldRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(fieldMapper.toResponse(activeField)).thenReturn(mapped);

        Page<FieldOutputDTO> result = fieldServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueName_persistsAndReturnsResponse() {
        FieldInputDTO inputDTO = new FieldInputDTO("New Field", "Campo nuevo");
        Field model = new Field();
        Field saved = new Field();
        FieldOutputDTO response = new FieldOutputDTO();
        when(fieldRepository.existsByNameAndDeletedAtIsNull("New Field")).thenReturn(false);
        when(fieldMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(fieldRepository.create(model)).thenReturn(saved);
        when(fieldMapper.toResponse(saved)).thenReturn(response);

        FieldOutputDTO result = fieldServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateName_throwsBusinessRuleException() {
        FieldInputDTO inputDTO = new FieldInputDTO("Cybersecurity", "Ciberseguridad");
        when(fieldRepository.existsByNameAndDeletedAtIsNull("Cybersecurity")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> fieldServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.FIELD_DUPLICATED, ex.getMessage());
        verify(fieldRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        FieldInputDTO inputDTO = new FieldInputDTO("X", "Y");
        when(fieldRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> fieldServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.FIELD_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_duplicateName_throwsBusinessRuleException() {
        FieldInputDTO inputDTO = new FieldInputDTO("Taken", "Ocupado");
        when(fieldRepository.findById(1L)).thenReturn(activeField);
        when(fieldRepository.existsByNameAndIdNotAndDeletedAtIsNull("Taken", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> fieldServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.FIELD_DUPLICATED, ex.getMessage());
        verify(fieldMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        FieldInputDTO inputDTO = new FieldInputDTO("Updated", "Actualizado");
        Field updated = new Field();
        FieldOutputDTO response = new FieldOutputDTO();
        when(fieldRepository.findById(1L)).thenReturn(activeField);
        when(fieldRepository.existsByNameAndIdNotAndDeletedAtIsNull("Updated", 1L)).thenReturn(false);
        when(fieldRepository.update(activeField, 1L)).thenReturn(updated);
        when(fieldMapper.toResponse(updated)).thenReturn(response);

        FieldOutputDTO result = fieldServiceFacadeBean.update(1L, inputDTO);

        verify(fieldMapper).updateModelFromInput(inputDTO, activeField);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(fieldRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> fieldServiceFacadeBean.delete(99L));
        assertEquals(Constants.FIELD_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeField.setDeletedAt(LocalDateTime.now());
        when(fieldRepository.findById(1L)).thenReturn(activeField);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> fieldServiceFacadeBean.delete(1L));
        assertEquals(Constants.FIELD_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_hasDependencies_throwsBusinessRuleException() {
        when(fieldRepository.findById(1L)).thenReturn(activeField);
        when(applicationRepository.existsByFieldId(1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> fieldServiceFacadeBean.delete(1L));
        assertEquals(Constants.FIELD_DELETE_HAS_DEPENDENCIES, ex.getMessage());
        verify(fieldRepository, never()).delete(any());
    }

    @Test
    void delete_valid_softDeletesField() {
        when(fieldRepository.findById(1L)).thenReturn(activeField);
        when(applicationRepository.existsByFieldId(1L)).thenReturn(false);

        fieldServiceFacadeBean.delete(1L);

        assertEquals(activeField.getDeletedAt() != null, true);
        verify(fieldRepository, times(1)).delete(activeField);
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(fieldRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> fieldServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.FIELD_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(fieldRepository.findById(1L)).thenReturn(activeField);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> fieldServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.FIELD_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesField() {
        activeField.setDeletedAt(LocalDateTime.now());
        activeField.setDeletedBy("someone");
        Field reactivated = new Field();
        FieldOutputDTO response = new FieldOutputDTO();
        when(fieldRepository.findById(1L)).thenReturn(activeField);
        when(fieldRepository.update(eq(activeField), eq(1L))).thenReturn(reactivated);
        when(fieldMapper.toResponse(reactivated)).thenReturn(response);

        FieldOutputDTO result = fieldServiceFacadeBean.reactivate(1L);

        assertNull(activeField.getDeletedAt());
        assertNull(activeField.getDeletedBy());
        assertEquals(response, result);
    }
}
