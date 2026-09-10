package es.caib.invai.back.ejb.maintenance.complianceSituation;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.complianceSituation.DTO.ComplianceSituationInputDTO;
import es.caib.invai.back.interna.maintenance.complianceSituation.DTO.ComplianceSituationOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.complianceSituation.ComplianceSituationCriteria;
import es.caib.invai.back.persistence.repository.maintenance.complianceSituation.ComplianceSituationRepository;
import es.caib.invai.back.service.mapper.maintenance.complianceSituation.ComplianceSituationMapper;
import es.caib.invai.back.service.model.maintenance.complianceSituation.ComplianceSituation;
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
 * Unit tests for {@link ComplianceSituationServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link ComplianceSituationRepository} and {@link ComplianceSituationMapper} collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class ComplianceSituationServiceFacadeBeanTest {

    @Mock
    private ComplianceSituationMapper complianceSituationMapper;

    @Mock
    private ComplianceSituationRepository complianceSituationRepository;

    @InjectMocks
    private ComplianceSituationServiceFacadeBean complianceSituationServiceFacadeBean;

    private ComplianceSituation activeComplianceSituation;

    @BeforeEach
    void setUp() {
        activeComplianceSituation = new ComplianceSituation();
        activeComplianceSituation.setId(1L);
        activeComplianceSituation.setName("Conforme");
    }

    @Test
    void getById_found_returnsMappedResponse() {
        ComplianceSituationOutputDTO expected = new ComplianceSituationOutputDTO();
        when(complianceSituationRepository.findById(1L)).thenReturn(activeComplianceSituation);
        when(complianceSituationMapper.toResponse(activeComplianceSituation)).thenReturn(expected);

        ComplianceSituationOutputDTO result = complianceSituationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(complianceSituationRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> complianceSituationServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_COMPLIANCESITUATION_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        ComplianceSituationCriteria criteria = new ComplianceSituationCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<ComplianceSituation> domainPage = new PageImpl<>(List.of(activeComplianceSituation));
        ComplianceSituationOutputDTO mapped = new ComplianceSituationOutputDTO();
        when(complianceSituationRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(complianceSituationMapper.toResponse(activeComplianceSituation)).thenReturn(mapped);

        Page<ComplianceSituationOutputDTO> result = complianceSituationServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueName_persistsAndReturnsResponse() {
        ComplianceSituationInputDTO inputDTO = new ComplianceSituationInputDTO("Parcialment", "Parcialment");
        ComplianceSituation model = new ComplianceSituation();
        ComplianceSituation saved = new ComplianceSituation();
        ComplianceSituationOutputDTO response = new ComplianceSituationOutputDTO();
        when(complianceSituationRepository.existsByNameAndDeletedAtIsNull("Parcialment")).thenReturn(false);
        when(complianceSituationMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(complianceSituationRepository.create(model)).thenReturn(saved);
        when(complianceSituationMapper.toResponse(saved)).thenReturn(response);

        ComplianceSituationOutputDTO result = complianceSituationServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateName_throwsBusinessRuleException() {
        ComplianceSituationInputDTO inputDTO = new ComplianceSituationInputDTO("Conforme", "Conforme");
        when(complianceSituationRepository.existsByNameAndDeletedAtIsNull("Conforme")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> complianceSituationServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_COMPLIANCESITUATION_DUPLICATED, ex.getMessage());
        verify(complianceSituationRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        ComplianceSituationInputDTO inputDTO = new ComplianceSituationInputDTO("X", "X ES");
        when(complianceSituationRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> complianceSituationServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_COMPLIANCESITUATION_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_duplicateName_throwsBusinessRuleException() {
        ComplianceSituationInputDTO inputDTO = new ComplianceSituationInputDTO("Taken", "Taken ES");
        when(complianceSituationRepository.findById(1L)).thenReturn(activeComplianceSituation);
        when(complianceSituationRepository.existsByNameAndIdNotAndDeletedAtIsNull("Taken", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> complianceSituationServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_COMPLIANCESITUATION_DUPLICATED, ex.getMessage());
        verify(complianceSituationMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        ComplianceSituationInputDTO inputDTO = new ComplianceSituationInputDTO("Updated", "Updated ES");
        ComplianceSituation updated = new ComplianceSituation();
        ComplianceSituationOutputDTO response = new ComplianceSituationOutputDTO();
        when(complianceSituationRepository.findById(1L)).thenReturn(activeComplianceSituation);
        when(complianceSituationRepository.existsByNameAndIdNotAndDeletedAtIsNull("Updated", 1L)).thenReturn(false);
        when(complianceSituationRepository.update(activeComplianceSituation, 1L)).thenReturn(updated);
        when(complianceSituationMapper.toResponse(updated)).thenReturn(response);

        ComplianceSituationOutputDTO result = complianceSituationServiceFacadeBean.update(1L, inputDTO);

        verify(complianceSituationMapper).updateModelFromInput(inputDTO, activeComplianceSituation);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(complianceSituationRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> complianceSituationServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_COMPLIANCESITUATION_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeComplianceSituation.setDeletedAt(LocalDateTime.now());
        when(complianceSituationRepository.findById(1L)).thenReturn(activeComplianceSituation);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> complianceSituationServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_COMPLIANCESITUATION_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesComplianceSituation() {
        when(complianceSituationRepository.findById(1L)).thenReturn(activeComplianceSituation);

        complianceSituationServiceFacadeBean.delete(1L);

        assertNotNull(activeComplianceSituation.getDeletedAt());
        verify(complianceSituationRepository, times(1)).delete(activeComplianceSituation);
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(complianceSituationRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> complianceSituationServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_COMPLIANCESITUATION_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(complianceSituationRepository.findById(1L)).thenReturn(activeComplianceSituation);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> complianceSituationServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_COMPLIANCESITUATION_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesComplianceSituation() {
        activeComplianceSituation.setDeletedAt(LocalDateTime.now());
        activeComplianceSituation.setDeletedBy("someone");
        ComplianceSituation reactivated = new ComplianceSituation();
        ComplianceSituationOutputDTO response = new ComplianceSituationOutputDTO();
        when(complianceSituationRepository.findById(1L)).thenReturn(activeComplianceSituation);
        when(complianceSituationRepository.update(eq(activeComplianceSituation), eq(1L))).thenReturn(reactivated);
        when(complianceSituationMapper.toResponse(reactivated)).thenReturn(response);

        ComplianceSituationOutputDTO result = complianceSituationServiceFacadeBean.reactivate(1L);

        assertNull(activeComplianceSituation.getDeletedAt());
        assertNull(activeComplianceSituation.getDeletedBy());
        assertEquals(response, result);
    }
}
