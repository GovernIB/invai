package es.caib.invai.back.ejb.maintenance.security.ensRequirement;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.security.ensRequirement.DTO.EnsRequirementInputDTO;
import es.caib.invai.back.interna.maintenance.security.ensRequirement.DTO.EnsRequirementOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.security.ensRequirement.EnsRequirementCriteria;
import es.caib.invai.back.persistence.repository.maintenance.security.ensRequirement.EnsRequirementRepository;
import es.caib.invai.back.service.mapper.maintenance.security.ensRequirement.EnsRequirementMapper;
import es.caib.invai.back.service.model.maintenance.security.ensRequirement.EnsRequirement;
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
 * Unit tests for {@link EnsRequirementServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link EnsRequirementRepository} and {@link EnsRequirementMapper} collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class EnsRequirementServiceFacadeBeanTest {

    @Mock
    private EnsRequirementMapper ensRequirementMapper;

    @Mock
    private EnsRequirementRepository ensRequirementRepository;

    @InjectMocks
    private EnsRequirementServiceFacadeBean ensRequirementServiceFacadeBean;

    private EnsRequirement activeEnsRequirement;

    @BeforeEach
    void setUp() {
        activeEnsRequirement = new EnsRequirement();
        activeEnsRequirement.setId(1L);
        activeEnsRequirement.setName("Control d'accessos");
    }

    @Test
    void getById_found_returnsMappedResponse() {
        EnsRequirementOutputDTO expected = new EnsRequirementOutputDTO();
        when(ensRequirementRepository.findById(1L)).thenReturn(activeEnsRequirement);
        when(ensRequirementMapper.toResponse(activeEnsRequirement)).thenReturn(expected);

        EnsRequirementOutputDTO result = ensRequirementServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(ensRequirementRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> ensRequirementServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_ENSREQUIREMENT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        EnsRequirementCriteria criteria = new EnsRequirementCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<EnsRequirement> domainPage = new PageImpl<>(List.of(activeEnsRequirement));
        EnsRequirementOutputDTO mapped = new EnsRequirementOutputDTO();
        when(ensRequirementRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(ensRequirementMapper.toResponse(activeEnsRequirement)).thenReturn(mapped);

        Page<EnsRequirementOutputDTO> result = ensRequirementServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueName_persistsAndReturnsResponse() {
        EnsRequirementInputDTO inputDTO = new EnsRequirementInputDTO("Gestio d'incidents", "Gestion de incidentes");
        EnsRequirement model = new EnsRequirement();
        EnsRequirement saved = new EnsRequirement();
        EnsRequirementOutputDTO response = new EnsRequirementOutputDTO();
        when(ensRequirementRepository.existsByNameAndDeletedAtIsNull("Gestio d'incidents")).thenReturn(false);
        when(ensRequirementMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(ensRequirementRepository.create(model)).thenReturn(saved);
        when(ensRequirementMapper.toResponse(saved)).thenReturn(response);

        EnsRequirementOutputDTO result = ensRequirementServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateName_throwsBusinessRuleException() {
        EnsRequirementInputDTO inputDTO = new EnsRequirementInputDTO("Control d'accessos", "Control de accesos");
        when(ensRequirementRepository.existsByNameAndDeletedAtIsNull("Control d'accessos")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> ensRequirementServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_ENSREQUIREMENT_DUPLICATED, ex.getMessage());
        verify(ensRequirementRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        EnsRequirementInputDTO inputDTO = new EnsRequirementInputDTO("X", "X ES");
        when(ensRequirementRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> ensRequirementServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_ENSREQUIREMENT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_duplicateName_throwsBusinessRuleException() {
        EnsRequirementInputDTO inputDTO = new EnsRequirementInputDTO("Taken", "Taken ES");
        when(ensRequirementRepository.findById(1L)).thenReturn(activeEnsRequirement);
        when(ensRequirementRepository.existsByNameAndIdNotAndDeletedAtIsNull("Taken", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> ensRequirementServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_ENSREQUIREMENT_DUPLICATED, ex.getMessage());
        verify(ensRequirementMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        EnsRequirementInputDTO inputDTO = new EnsRequirementInputDTO("Updated", "Updated ES");
        EnsRequirement updated = new EnsRequirement();
        EnsRequirementOutputDTO response = new EnsRequirementOutputDTO();
        when(ensRequirementRepository.findById(1L)).thenReturn(activeEnsRequirement);
        when(ensRequirementRepository.existsByNameAndIdNotAndDeletedAtIsNull("Updated", 1L)).thenReturn(false);
        when(ensRequirementRepository.update(activeEnsRequirement, 1L)).thenReturn(updated);
        when(ensRequirementMapper.toResponse(updated)).thenReturn(response);

        EnsRequirementOutputDTO result = ensRequirementServiceFacadeBean.update(1L, inputDTO);

        verify(ensRequirementMapper).updateModelFromInput(inputDTO, activeEnsRequirement);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(ensRequirementRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> ensRequirementServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_ENSREQUIREMENT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeEnsRequirement.setDeletedAt(LocalDateTime.now());
        when(ensRequirementRepository.findById(1L)).thenReturn(activeEnsRequirement);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> ensRequirementServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_ENSREQUIREMENT_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesEnsRequirement() {
        when(ensRequirementRepository.findById(1L)).thenReturn(activeEnsRequirement);

        ensRequirementServiceFacadeBean.delete(1L);

        assertNotNull(activeEnsRequirement.getDeletedAt());
        verify(ensRequirementRepository, times(1)).delete(activeEnsRequirement);
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(ensRequirementRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> ensRequirementServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_ENSREQUIREMENT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(ensRequirementRepository.findById(1L)).thenReturn(activeEnsRequirement);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> ensRequirementServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_ENSREQUIREMENT_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesEnsRequirement() {
        activeEnsRequirement.setDeletedAt(LocalDateTime.now());
        activeEnsRequirement.setDeletedBy("someone");
        EnsRequirement reactivated = new EnsRequirement();
        EnsRequirementOutputDTO response = new EnsRequirementOutputDTO();
        when(ensRequirementRepository.findById(1L)).thenReturn(activeEnsRequirement);
        when(ensRequirementRepository.update(eq(activeEnsRequirement), eq(1L))).thenReturn(reactivated);
        when(ensRequirementMapper.toResponse(reactivated)).thenReturn(response);

        EnsRequirementOutputDTO result = ensRequirementServiceFacadeBean.reactivate(1L);

        assertNull(activeEnsRequirement.getDeletedAt());
        assertNull(activeEnsRequirement.getDeletedBy());
        assertEquals(response, result);
    }
}
