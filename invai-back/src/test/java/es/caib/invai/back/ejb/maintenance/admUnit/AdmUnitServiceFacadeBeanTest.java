package es.caib.invai.back.ejb.maintenance.admUnit;

import es.caib.invai.back.ejb.maintenance.admUnit.AdmUnitServiceFacadeBean;
import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.admUnit.DTO.AdmUnitInputDTO;
import es.caib.invai.back.interna.maintenance.admUnit.DTO.AdmUnitOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.admUnit.AdmUnitCriteria;
import es.caib.invai.back.persistence.repository.maintenance.admUnit.AdmUnitRepository;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.service.mapper.maintenance.admUnit.AdmUnitMapper;
import es.caib.invai.back.service.model.maintenance.admUnit.AdmUnit;
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
 * Unit tests for {@link AdmUnitServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link AdmUnitRepository}, {@link AdmUnitMapper}, and {@link ApplicationRepository}
 * collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class AdmUnitServiceFacadeBeanTest {

    @Mock
    private AdmUnitMapper admUnitMapper;

    @Mock
    private AdmUnitRepository admUnitRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private AdmUnitServiceFacadeBean admUnitServiceFacadeBean;

    private AdmUnit activeAdmUnit;

    @BeforeEach
    void setUp() {
        activeAdmUnit = new AdmUnit();
        activeAdmUnit.setId(1L);
        activeAdmUnit.setCode("PRO");
        activeAdmUnit.setName("Production Unit");
        activeAdmUnit.setNameEs("Unidad de Produccion");
    }

    @Test
    void getById_found_returnsMappedResponse() {
        AdmUnitOutputDTO expected = new AdmUnitOutputDTO();
        when(admUnitRepository.findById(1L)).thenReturn(activeAdmUnit);
        when(admUnitMapper.toResponse(activeAdmUnit)).thenReturn(expected);

        AdmUnitOutputDTO result = admUnitServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(admUnitRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> admUnitServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_ADMUNIT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        AdmUnitCriteria criteria = new AdmUnitCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AdmUnit> domainPage = new PageImpl<>(List.of(activeAdmUnit));
        AdmUnitOutputDTO mapped = new AdmUnitOutputDTO();
        when(admUnitRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(admUnitMapper.toResponse(activeAdmUnit)).thenReturn(mapped);

        Page<AdmUnitOutputDTO> result = admUnitServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueCodeAndName_persistsAndReturnsResponse() {
        AdmUnitInputDTO inputDTO = new AdmUnitInputDTO("DEV", "Development Unit", "Unidad de Desarrollo");
        AdmUnit model = new AdmUnit();
        AdmUnit saved = new AdmUnit();
        AdmUnitOutputDTO response = new AdmUnitOutputDTO();
        when(admUnitRepository.existsByCodeAndDeletedAtIsNull("DEV")).thenReturn(false);
        when(admUnitRepository.existsByNameAndDeletedAtIsNull("Development Unit")).thenReturn(false);
        when(admUnitMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(admUnitRepository.create(model)).thenReturn(saved);
        when(admUnitMapper.toResponse(saved)).thenReturn(response);

        AdmUnitOutputDTO result = admUnitServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateCode_throwsBusinessRuleException() {
        AdmUnitInputDTO inputDTO = new AdmUnitInputDTO("PRO", "Another Unit", "Otra Unidad");
        when(admUnitRepository.existsByCodeAndDeletedAtIsNull("PRO")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> admUnitServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_ADMUNIT_CODE_DUPLICATED, ex.getMessage());
        verify(admUnitRepository, never()).create(any());
        verify(admUnitRepository, never()).existsByNameAndDeletedAtIsNull(any());
    }

    @Test
    void create_duplicateName_throwsBusinessRuleException() {
        AdmUnitInputDTO inputDTO = new AdmUnitInputDTO("DEV", "Production Unit", "Unidad de Produccion");
        when(admUnitRepository.existsByCodeAndDeletedAtIsNull("DEV")).thenReturn(false);
        when(admUnitRepository.existsByNameAndDeletedAtIsNull("Production Unit")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> admUnitServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_ADMUNIT_DUPLICATED, ex.getMessage());
        verify(admUnitRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        AdmUnitInputDTO inputDTO = new AdmUnitInputDTO("X", "Y", "Z");
        when(admUnitRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> admUnitServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_ADMUNIT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_duplicateCode_throwsBusinessRuleException() {
        AdmUnitInputDTO inputDTO = new AdmUnitInputDTO("TAKEN", "New Name", "Nuevo Nombre");
        when(admUnitRepository.findById(1L)).thenReturn(activeAdmUnit);
        when(admUnitRepository.existsByCodeAndIdNotAndDeletedAtIsNull("TAKEN", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> admUnitServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_ADMUNIT_CODE_DUPLICATED, ex.getMessage());
        verify(admUnitMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_duplicateName_throwsBusinessRuleException() {
        AdmUnitInputDTO inputDTO = new AdmUnitInputDTO("PRO", "Taken Name", "Nombre Ocupado");
        when(admUnitRepository.findById(1L)).thenReturn(activeAdmUnit);
        when(admUnitRepository.existsByCodeAndIdNotAndDeletedAtIsNull("PRO", 1L)).thenReturn(false);
        when(admUnitRepository.existsByNameAndIdNotAndDeletedAtIsNull("Taken Name", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> admUnitServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_ADMUNIT_DUPLICATED, ex.getMessage());
        verify(admUnitMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        AdmUnitInputDTO inputDTO = new AdmUnitInputDTO("PRO", "Updated Name", "Nombre Actualizado");
        AdmUnit updated = new AdmUnit();
        AdmUnitOutputDTO response = new AdmUnitOutputDTO();
        when(admUnitRepository.findById(1L)).thenReturn(activeAdmUnit);
        when(admUnitRepository.existsByCodeAndIdNotAndDeletedAtIsNull("PRO", 1L)).thenReturn(false);
        when(admUnitRepository.existsByNameAndIdNotAndDeletedAtIsNull("Updated Name", 1L)).thenReturn(false);
        when(admUnitRepository.update(activeAdmUnit, 1L)).thenReturn(updated);
        when(admUnitMapper.toResponse(updated)).thenReturn(response);

        AdmUnitOutputDTO result = admUnitServiceFacadeBean.update(1L, inputDTO);

        verify(admUnitMapper).updateModelFromInput(inputDTO, activeAdmUnit);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(admUnitRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> admUnitServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_ADMUNIT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeAdmUnit.setDeletedAt(LocalDateTime.now());
        when(admUnitRepository.findById(1L)).thenReturn(activeAdmUnit);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> admUnitServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_ADMUNIT_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_hasDependencies_throwsBusinessRuleException() {
        when(admUnitRepository.findById(1L)).thenReturn(activeAdmUnit);
        when(applicationRepository.existsByAdmUnitId(1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> admUnitServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_ADMUNIT_DELETE_HAS_DEPENDENCIES, ex.getMessage());
        verify(admUnitRepository, never()).delete(any());
    }

    @Test
    void delete_valid_softDeletesAdmUnit() {
        when(admUnitRepository.findById(1L)).thenReturn(activeAdmUnit);
        when(applicationRepository.existsByAdmUnitId(1L)).thenReturn(false);

        admUnitServiceFacadeBean.delete(1L);

        assertEquals(activeAdmUnit.getDeletedAt() != null, true);
        verify(admUnitRepository, times(1)).delete(activeAdmUnit);
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(admUnitRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> admUnitServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_ADMUNIT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(admUnitRepository.findById(1L)).thenReturn(activeAdmUnit);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> admUnitServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_ADMUNIT_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesAdmUnit() {
        activeAdmUnit.setDeletedAt(LocalDateTime.now());
        activeAdmUnit.setDeletedBy("someone");
        AdmUnit reactivated = new AdmUnit();
        AdmUnitOutputDTO response = new AdmUnitOutputDTO();
        when(admUnitRepository.findById(1L)).thenReturn(activeAdmUnit);
        when(admUnitRepository.update(eq(activeAdmUnit), eq(1L))).thenReturn(reactivated);
        when(admUnitMapper.toResponse(reactivated)).thenReturn(response);

        AdmUnitOutputDTO result = admUnitServiceFacadeBean.reactivate(1L);

        assertNull(activeAdmUnit.getDeletedAt());
        assertNull(activeAdmUnit.getDeletedBy());
        assertEquals(response, result);
    }
}
