package es.caib.invai.back.ejb.maintenance.security.securityMeasureType;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.security.securityMeasureType.DTO.SecurityMeasureTypeInputDTO;
import es.caib.invai.back.interna.maintenance.security.securityMeasureType.DTO.SecurityMeasureTypeOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.security.securityMeasureType.SecurityMeasureTypeCriteria;
import es.caib.invai.back.persistence.repository.maintenance.security.securityMeasureType.SecurityMeasureTypeRepository;
import es.caib.invai.back.service.mapper.maintenance.security.securityMeasureType.SecurityMeasureTypeMapper;
import es.caib.invai.back.service.model.maintenance.security.securityMeasureType.SecurityMeasureType;
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
 * Unit tests for {@link SecurityMeasureTypeServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link SecurityMeasureTypeRepository} and {@link SecurityMeasureTypeMapper} collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class SecurityMeasureTypeServiceFacadeBeanTest {

    @Mock
    private SecurityMeasureTypeMapper securityMeasureTypeMapper;

    @Mock
    private SecurityMeasureTypeRepository securityMeasureTypeRepository;

    @InjectMocks
    private SecurityMeasureTypeServiceFacadeBean securityMeasureTypeServiceFacadeBean;

    private SecurityMeasureType activeSecurityMeasureType;

    @BeforeEach
    void setUp() {
        activeSecurityMeasureType = new SecurityMeasureType();
        activeSecurityMeasureType.setId(1L);
        activeSecurityMeasureType.setName("Organitzativa");
    }

    @Test
    void getById_found_returnsMappedResponse() {
        SecurityMeasureTypeOutputDTO expected = new SecurityMeasureTypeOutputDTO();
        when(securityMeasureTypeRepository.findById(1L)).thenReturn(activeSecurityMeasureType);
        when(securityMeasureTypeMapper.toResponse(activeSecurityMeasureType)).thenReturn(expected);

        SecurityMeasureTypeOutputDTO result = securityMeasureTypeServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(securityMeasureTypeRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> securityMeasureTypeServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_SECURITYMEASURETYPE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        SecurityMeasureTypeCriteria criteria = new SecurityMeasureTypeCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<SecurityMeasureType> domainPage = new PageImpl<>(List.of(activeSecurityMeasureType));
        SecurityMeasureTypeOutputDTO mapped = new SecurityMeasureTypeOutputDTO();
        when(securityMeasureTypeRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(securityMeasureTypeMapper.toResponse(activeSecurityMeasureType)).thenReturn(mapped);

        Page<SecurityMeasureTypeOutputDTO> result = securityMeasureTypeServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueName_persistsAndReturnsResponse() {
        SecurityMeasureTypeInputDTO inputDTO = new SecurityMeasureTypeInputDTO("Tecnica", "Tecnica");
        SecurityMeasureType model = new SecurityMeasureType();
        SecurityMeasureType saved = new SecurityMeasureType();
        SecurityMeasureTypeOutputDTO response = new SecurityMeasureTypeOutputDTO();
        when(securityMeasureTypeRepository.existsByNameAndDeletedAtIsNull("Tecnica")).thenReturn(false);
        when(securityMeasureTypeMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(securityMeasureTypeRepository.create(model)).thenReturn(saved);
        when(securityMeasureTypeMapper.toResponse(saved)).thenReturn(response);

        SecurityMeasureTypeOutputDTO result = securityMeasureTypeServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateName_throwsBusinessRuleException() {
        SecurityMeasureTypeInputDTO inputDTO = new SecurityMeasureTypeInputDTO("Organitzativa", "Organitzativa");
        when(securityMeasureTypeRepository.existsByNameAndDeletedAtIsNull("Organitzativa")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> securityMeasureTypeServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_SECURITYMEASURETYPE_DUPLICATED, ex.getMessage());
        verify(securityMeasureTypeRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        SecurityMeasureTypeInputDTO inputDTO = new SecurityMeasureTypeInputDTO("X", "X ES");
        when(securityMeasureTypeRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> securityMeasureTypeServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_SECURITYMEASURETYPE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_duplicateName_throwsBusinessRuleException() {
        SecurityMeasureTypeInputDTO inputDTO = new SecurityMeasureTypeInputDTO("Taken", "Taken ES");
        when(securityMeasureTypeRepository.findById(1L)).thenReturn(activeSecurityMeasureType);
        when(securityMeasureTypeRepository.existsByNameAndIdNotAndDeletedAtIsNull("Taken", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> securityMeasureTypeServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_SECURITYMEASURETYPE_DUPLICATED, ex.getMessage());
        verify(securityMeasureTypeMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        SecurityMeasureTypeInputDTO inputDTO = new SecurityMeasureTypeInputDTO("Updated", "Updated ES");
        SecurityMeasureType updated = new SecurityMeasureType();
        SecurityMeasureTypeOutputDTO response = new SecurityMeasureTypeOutputDTO();
        when(securityMeasureTypeRepository.findById(1L)).thenReturn(activeSecurityMeasureType);
        when(securityMeasureTypeRepository.existsByNameAndIdNotAndDeletedAtIsNull("Updated", 1L)).thenReturn(false);
        when(securityMeasureTypeRepository.update(activeSecurityMeasureType, 1L)).thenReturn(updated);
        when(securityMeasureTypeMapper.toResponse(updated)).thenReturn(response);

        SecurityMeasureTypeOutputDTO result = securityMeasureTypeServiceFacadeBean.update(1L, inputDTO);

        verify(securityMeasureTypeMapper).updateModelFromInput(inputDTO, activeSecurityMeasureType);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(securityMeasureTypeRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> securityMeasureTypeServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_SECURITYMEASURETYPE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeSecurityMeasureType.setDeletedAt(LocalDateTime.now());
        when(securityMeasureTypeRepository.findById(1L)).thenReturn(activeSecurityMeasureType);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> securityMeasureTypeServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_SECURITYMEASURETYPE_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesSecurityMeasureType() {
        when(securityMeasureTypeRepository.findById(1L)).thenReturn(activeSecurityMeasureType);

        securityMeasureTypeServiceFacadeBean.delete(1L);

        assertNotNull(activeSecurityMeasureType.getDeletedAt());
        verify(securityMeasureTypeRepository, times(1)).delete(activeSecurityMeasureType);
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(securityMeasureTypeRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> securityMeasureTypeServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_SECURITYMEASURETYPE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(securityMeasureTypeRepository.findById(1L)).thenReturn(activeSecurityMeasureType);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> securityMeasureTypeServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_SECURITYMEASURETYPE_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesSecurityMeasureType() {
        activeSecurityMeasureType.setDeletedAt(LocalDateTime.now());
        activeSecurityMeasureType.setDeletedBy("someone");
        SecurityMeasureType reactivated = new SecurityMeasureType();
        SecurityMeasureTypeOutputDTO response = new SecurityMeasureTypeOutputDTO();
        when(securityMeasureTypeRepository.findById(1L)).thenReturn(activeSecurityMeasureType);
        when(securityMeasureTypeRepository.update(eq(activeSecurityMeasureType), eq(1L))).thenReturn(reactivated);
        when(securityMeasureTypeMapper.toResponse(reactivated)).thenReturn(response);

        SecurityMeasureTypeOutputDTO result = securityMeasureTypeServiceFacadeBean.reactivate(1L);

        assertNull(activeSecurityMeasureType.getDeletedAt());
        assertNull(activeSecurityMeasureType.getDeletedBy());
        assertEquals(response, result);
    }
}
