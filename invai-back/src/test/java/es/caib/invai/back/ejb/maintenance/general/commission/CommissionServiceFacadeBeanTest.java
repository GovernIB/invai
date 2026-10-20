package es.caib.invai.back.ejb.maintenance.general.commission;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.general.commission.DTO.CommissionInputDTO;
import es.caib.invai.back.interna.maintenance.general.commission.DTO.CommissionOutputDTO;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.persistence.repository.maintenance.general.commission.CommissionCriteria;
import es.caib.invai.back.persistence.repository.maintenance.general.commission.CommissionRepository;
import es.caib.invai.back.service.mapper.maintenance.general.commission.CommissionMapper;
import es.caib.invai.back.service.model.maintenance.general.commission.Commission;
import es.caib.invai.back.service.model.maintenance.general.commission.CommissionType;
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

import java.time.LocalDate;
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
 * Unit tests for {@link CommissionServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link CommissionRepository}, {@link CommissionMapper}, and {@link ApplicationRepository}
 * collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class CommissionServiceFacadeBeanTest {

    @Mock
    private CommissionMapper commissionMapper;

    @Mock
    private CommissionRepository commissionRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private CommissionServiceFacadeBean commissionServiceFacadeBean;

    private Commission activeCommission;

    @BeforeEach
    void setUp() {
        activeCommission = new Commission();
        activeCommission.setId(1L);
        activeCommission.setName("Comissio Tecnica");
        activeCommission.setNameEs("Comision Tecnica");
        activeCommission.setExpedientNumber("EXP-2026-001");
        activeCommission.setCommissionType(CommissionType.TECNICA);
        activeCommission.setApprovalDate(LocalDate.of(2026, 1, 15));
    }

    private CommissionInputDTO buildInputDTO(String name, String nameEs, String expedientNumber) {
        CommissionInputDTO inputDTO = new CommissionInputDTO();
        inputDTO.setName(name);
        inputDTO.setNameEs(nameEs);
        inputDTO.setExpedientNumber(expedientNumber);
        inputDTO.setCommissionType(CommissionType.TECNICA);
        inputDTO.setApprovalDate(LocalDate.of(2026, 2, 1));
        return inputDTO;
    }

    @Test
    void getById_found_returnsMappedResponse() {
        CommissionOutputDTO expected = new CommissionOutputDTO();
        when(commissionRepository.findById(1L)).thenReturn(activeCommission);
        when(commissionMapper.toResponse(activeCommission)).thenReturn(expected);

        CommissionOutputDTO result = commissionServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(commissionRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> commissionServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_COMMISSION_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        CommissionCriteria criteria = new CommissionCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<Commission> domainPage = new PageImpl<>(List.of(activeCommission));
        CommissionOutputDTO mapped = new CommissionOutputDTO();
        when(commissionRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(commissionMapper.toResponse(activeCommission)).thenReturn(mapped);

        Page<CommissionOutputDTO> result = commissionServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueNameAndExpedient_persistsAndReturnsResponse() {
        CommissionInputDTO inputDTO = buildInputDTO("New Commission", "Nueva Comision", "EXP-2026-002");
        Commission model = new Commission();
        Commission saved = new Commission();
        CommissionOutputDTO response = new CommissionOutputDTO();
        when(commissionRepository.existsByNameAndDeletedAtIsNull("New Commission")).thenReturn(false);
        when(commissionRepository.existsByExpedientNumberAndDeletedAtIsNull("EXP-2026-002")).thenReturn(false);
        when(commissionMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(commissionRepository.create(model)).thenReturn(saved);
        when(commissionMapper.toResponse(saved)).thenReturn(response);

        CommissionOutputDTO result = commissionServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateName_throwsBusinessRuleException() {
        CommissionInputDTO inputDTO = buildInputDTO("Taken", "Ocupado", "EXP-2026-003");
        when(commissionRepository.existsByNameAndDeletedAtIsNull("Taken")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> commissionServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_COMMISSION_DUPLICATED, ex.getMessage());
        verify(commissionRepository, never()).create(any());
    }

    @Test
    void create_duplicateNameEs_throwsBusinessRuleException() {
        CommissionInputDTO inputDTO = buildInputDTO("Fresh Name", "Ocupado", "EXP-2026-004");
        when(commissionRepository.existsByNameAndDeletedAtIsNull("Fresh Name")).thenReturn(false);
        when(commissionRepository.existsByNameEsAndDeletedAtIsNull("Ocupado")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> commissionServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_COMMISSION_DUPLICATED_ES, ex.getMessage());
        verify(commissionRepository, never()).create(any());
    }

    @Test
    void create_duplicateExpedientNumber_throwsBusinessRuleException() {
        CommissionInputDTO inputDTO = buildInputDTO("Fresh Name", "Nombre Fresco", "EXP-DUP");
        when(commissionRepository.existsByNameAndDeletedAtIsNull("Fresh Name")).thenReturn(false);
        when(commissionRepository.existsByExpedientNumberAndDeletedAtIsNull("EXP-DUP")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> commissionServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_COMMISSION_DUPLICATED, ex.getMessage());
        verify(commissionRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        CommissionInputDTO inputDTO = buildInputDTO("X", "X", "EXP-X");
        when(commissionRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> commissionServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_COMMISSION_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_duplicateName_throwsBusinessRuleException() {
        CommissionInputDTO inputDTO = buildInputDTO("Taken", "Ocupado", "EXP-2026-001");
        when(commissionRepository.findById(1L)).thenReturn(activeCommission);
        when(commissionRepository.existsByNameAndIdNotAndDeletedAtIsNull("Taken", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> commissionServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_COMMISSION_DUPLICATED, ex.getMessage());
        verify(commissionMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_duplicateNameEs_throwsBusinessRuleException() {
        CommissionInputDTO inputDTO = buildInputDTO("Comissio Tecnica", "Ocupado", "EXP-2026-005");
        when(commissionRepository.findById(1L)).thenReturn(activeCommission);
        when(commissionRepository.existsByNameAndIdNotAndDeletedAtIsNull("Comissio Tecnica", 1L)).thenReturn(false);
        when(commissionRepository.existsByNameEsAndIdNotAndDeletedAtIsNull("Ocupado", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> commissionServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_COMMISSION_DUPLICATED_ES, ex.getMessage());
        verify(commissionMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_duplicateExpedientNumber_throwsBusinessRuleException() {
        CommissionInputDTO inputDTO = buildInputDTO("Comissio Tecnica", "Comision Tecnica", "EXP-DUP");
        when(commissionRepository.findById(1L)).thenReturn(activeCommission);
        when(commissionRepository.existsByNameAndIdNotAndDeletedAtIsNull("Comissio Tecnica", 1L)).thenReturn(false);
        when(commissionRepository.existsByExpedientNumberAndIdNotAndDeletedAtIsNull("EXP-DUP", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> commissionServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_COMMISSION_DUPLICATED, ex.getMessage());
        verify(commissionMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        CommissionInputDTO inputDTO = buildInputDTO("Updated", "Actualizado", "EXP-2026-999");
        Commission updated = new Commission();
        CommissionOutputDTO response = new CommissionOutputDTO();
        when(commissionRepository.findById(1L)).thenReturn(activeCommission);
        when(commissionRepository.existsByNameAndIdNotAndDeletedAtIsNull("Updated", 1L)).thenReturn(false);
        when(commissionRepository.existsByExpedientNumberAndIdNotAndDeletedAtIsNull("EXP-2026-999", 1L)).thenReturn(false);
        when(commissionRepository.update(activeCommission, 1L)).thenReturn(updated);
        when(commissionMapper.toResponse(updated)).thenReturn(response);

        CommissionOutputDTO result = commissionServiceFacadeBean.update(1L, inputDTO);

        verify(commissionMapper).updateModelFromInput(inputDTO, activeCommission);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(commissionRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> commissionServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_COMMISSION_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeCommission.setDeletedAt(LocalDateTime.now());
        when(commissionRepository.findById(1L)).thenReturn(activeCommission);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> commissionServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_COMMISSION_NOT_ACTIVE, ex.getMessage());
        verify(commissionRepository, never()).delete(any());
        verify(applicationRepository, never()).existsByCommissionId(any());
    }

    @Test
    void delete_hasDependencies_throwsBusinessRuleException() {
        when(commissionRepository.findById(1L)).thenReturn(activeCommission);
        when(applicationRepository.existsByCommissionId(1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> commissionServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_COMMISSION_DELETE_HAS_DEPENDENCIES, ex.getMessage());
        verify(commissionRepository, never()).delete(any());
    }

    @Test
    void delete_valid_softDeletesCommission() {
        when(commissionRepository.findById(1L)).thenReturn(activeCommission);
        when(applicationRepository.existsByCommissionId(1L)).thenReturn(false);

        commissionServiceFacadeBean.delete(1L);

        assertNotNull(activeCommission.getDeletedAt());
        verify(commissionRepository, times(1)).delete(activeCommission);
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(commissionRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> commissionServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_COMMISSION_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(commissionRepository.findById(1L)).thenReturn(activeCommission);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> commissionServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_COMMISSION_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesCommission() {
        activeCommission.setDeletedAt(LocalDateTime.now());
        activeCommission.setDeletedBy("someone");
        Commission reactivated = new Commission();
        CommissionOutputDTO response = new CommissionOutputDTO();
        when(commissionRepository.findById(1L)).thenReturn(activeCommission);
        when(commissionRepository.update(eq(activeCommission), eq(1L))).thenReturn(reactivated);
        when(commissionMapper.toResponse(reactivated)).thenReturn(response);

        CommissionOutputDTO result = commissionServiceFacadeBean.reactivate(1L);

        assertNull(activeCommission.getDeletedAt());
        assertNull(activeCommission.getDeletedBy());
        assertEquals(response, result);
    }
}
