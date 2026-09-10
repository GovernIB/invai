package es.caib.invai.back.ejb.maintenance.general.systemType;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.general.systemType.DTO.SystemTypeInputDTO;
import es.caib.invai.back.interna.maintenance.general.systemType.DTO.SystemTypeOutputDTO;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.persistence.repository.maintenance.general.systemType.SystemTypeCriteria;
import es.caib.invai.back.persistence.repository.maintenance.general.systemType.SystemTypeRepository;
import es.caib.invai.back.service.mapper.maintenance.general.systemType.SystemTypeMapper;
import es.caib.invai.back.service.model.maintenance.general.systemType.SystemType;
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
 * Unit tests for {@link SystemTypeServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link SystemTypeRepository}, {@link SystemTypeMapper}, and {@link ApplicationRepository}
 * collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class SystemTypeServiceFacadeBeanTest {

    @Mock
    private SystemTypeMapper systemTypeMapper;

    @Mock
    private SystemTypeRepository systemTypeRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private SystemTypeServiceFacadeBean systemTypeServiceFacadeBean;

    private SystemType activeSystemType;

    @BeforeEach
    void setUp() {
        activeSystemType = new SystemType();
        activeSystemType.setId(1L);
        activeSystemType.setName("Microservei REST");
        activeSystemType.setNameEs("Microservicio REST");
    }

    @Test
    void getById_found_returnsMappedResponse() {
        SystemTypeOutputDTO expected = new SystemTypeOutputDTO();
        when(systemTypeRepository.findById(1L)).thenReturn(activeSystemType);
        when(systemTypeMapper.toResponse(activeSystemType)).thenReturn(expected);

        SystemTypeOutputDTO result = systemTypeServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(systemTypeRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> systemTypeServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_SYSTEM_TYPE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        SystemTypeCriteria criteria = new SystemTypeCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<SystemType> domainPage = new PageImpl<>(List.of(activeSystemType));
        SystemTypeOutputDTO mapped = new SystemTypeOutputDTO();
        when(systemTypeRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(systemTypeMapper.toResponse(activeSystemType)).thenReturn(mapped);

        Page<SystemTypeOutputDTO> result = systemTypeServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueName_persistsAndReturnsResponse() {
        SystemTypeInputDTO inputDTO = new SystemTypeInputDTO();
        inputDTO.setName("New System Type");
        inputDTO.setNameEs("Nuevo tipo de sistema");
        SystemType model = new SystemType();
        SystemType saved = new SystemType();
        SystemTypeOutputDTO response = new SystemTypeOutputDTO();
        when(systemTypeRepository.existsByNameAndDeletedAtIsNull("New System Type")).thenReturn(false);
        when(systemTypeMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(systemTypeRepository.create(model)).thenReturn(saved);
        when(systemTypeMapper.toResponse(saved)).thenReturn(response);

        SystemTypeOutputDTO result = systemTypeServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateName_throwsBusinessRuleException() {
        SystemTypeInputDTO inputDTO = new SystemTypeInputDTO();
        inputDTO.setName("Taken Type");
        inputDTO.setNameEs("Tipo ocupado");
        when(systemTypeRepository.existsByNameAndDeletedAtIsNull("Taken Type")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> systemTypeServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_SYSTEM_TYPE_DUPLICATED, ex.getMessage());
        verify(systemTypeRepository, never()).create(any());
    }

    @Test
    void create_duplicateNameEs_throwsBusinessRuleException() {
        SystemTypeInputDTO inputDTO = new SystemTypeInputDTO();
        inputDTO.setName("New Type");
        inputDTO.setNameEs("Tipo ocupado");
        when(systemTypeRepository.existsByNameAndDeletedAtIsNull("New Type")).thenReturn(false);
        when(systemTypeRepository.existsByNameEsAndDeletedAtIsNull("Tipo ocupado")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> systemTypeServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_SYSTEM_TYPE_DUPLICATED_ES, ex.getMessage());
        verify(systemTypeRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        SystemTypeInputDTO inputDTO = new SystemTypeInputDTO();
        inputDTO.setName("X");
        inputDTO.setNameEs("X");
        when(systemTypeRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> systemTypeServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_SYSTEM_TYPE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_duplicateName_throwsBusinessRuleException() {
        SystemTypeInputDTO inputDTO = new SystemTypeInputDTO();
        inputDTO.setName("Taken");
        inputDTO.setNameEs("Ocupado");
        when(systemTypeRepository.findById(1L)).thenReturn(activeSystemType);
        when(systemTypeRepository.existsByNameAndIdNotAndDeletedAtIsNull("Taken", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> systemTypeServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_SYSTEM_TYPE_DUPLICATED, ex.getMessage());
        verify(systemTypeMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_duplicateNameEs_throwsBusinessRuleException() {
        SystemTypeInputDTO inputDTO = new SystemTypeInputDTO();
        inputDTO.setName("Taken");
        inputDTO.setNameEs("Ocupado");
        when(systemTypeRepository.findById(1L)).thenReturn(activeSystemType);
        when(systemTypeRepository.existsByNameAndIdNotAndDeletedAtIsNull("Taken", 1L)).thenReturn(false);
        when(systemTypeRepository.existsByNameEsAndIdNotAndDeletedAtIsNull("Ocupado", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> systemTypeServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_SYSTEM_TYPE_DUPLICATED_ES, ex.getMessage());
        verify(systemTypeMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        SystemTypeInputDTO inputDTO = new SystemTypeInputDTO();
        inputDTO.setName("Updated");
        inputDTO.setNameEs("Actualizado");
        SystemType updated = new SystemType();
        SystemTypeOutputDTO response = new SystemTypeOutputDTO();
        when(systemTypeRepository.findById(1L)).thenReturn(activeSystemType);
        when(systemTypeRepository.existsByNameAndIdNotAndDeletedAtIsNull("Updated", 1L)).thenReturn(false);
        when(systemTypeRepository.update(activeSystemType, 1L)).thenReturn(updated);
        when(systemTypeMapper.toResponse(updated)).thenReturn(response);

        SystemTypeOutputDTO result = systemTypeServiceFacadeBean.update(1L, inputDTO);

        verify(systemTypeMapper).updateModelFromInput(inputDTO, activeSystemType);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(systemTypeRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> systemTypeServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_SYSTEM_TYPE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeSystemType.setDeletedAt(LocalDateTime.now());
        when(systemTypeRepository.findById(1L)).thenReturn(activeSystemType);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> systemTypeServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_SYSTEM_TYPE_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_hasDependencies_throwsBusinessRuleException() {
        when(systemTypeRepository.findById(1L)).thenReturn(activeSystemType);
        when(applicationRepository.existsBySystemTypeId(1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> systemTypeServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_SYSTEM_TYPE_DELETE_HAS_DEPENDENCIES, ex.getMessage());
        verify(systemTypeRepository, never()).delete(any());
    }

    @Test
    void delete_valid_softDeletesSystemType() {
        when(systemTypeRepository.findById(1L)).thenReturn(activeSystemType);
        when(applicationRepository.existsBySystemTypeId(1L)).thenReturn(false);

        systemTypeServiceFacadeBean.delete(1L);

        assertNotNull(activeSystemType.getDeletedAt());
        verify(systemTypeRepository, times(1)).delete(activeSystemType);
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(systemTypeRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> systemTypeServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_SYSTEM_TYPE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(systemTypeRepository.findById(1L)).thenReturn(activeSystemType);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> systemTypeServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_SYSTEM_TYPE_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesSystemType() {
        activeSystemType.setDeletedAt(LocalDateTime.now());
        activeSystemType.setDeletedBy("someone");
        SystemType reactivated = new SystemType();
        SystemTypeOutputDTO response = new SystemTypeOutputDTO();
        when(systemTypeRepository.findById(1L)).thenReturn(activeSystemType);
        when(systemTypeRepository.update(eq(activeSystemType), eq(1L))).thenReturn(reactivated);
        when(systemTypeMapper.toResponse(reactivated)).thenReturn(response);

        SystemTypeOutputDTO result = systemTypeServiceFacadeBean.reactivate(1L);

        assertNull(activeSystemType.getDeletedAt());
        assertNull(activeSystemType.getDeletedBy());
        assertEquals(response, result);
    }
}
