package es.caib.invai.back.ejb.application.security.risk;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.security.risk.DTO.AppSecurityRiskInputDTO;
import es.caib.invai.back.interna.application.security.risk.DTO.AppSecurityRiskOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.risk.AppSecurityRiskCriteria;
import es.caib.invai.back.persistence.repository.application.security.risk.AppSecurityRiskRepository;
import es.caib.invai.back.service.mapper.application.security.risk.AppSecurityRiskMapper;
import es.caib.invai.back.service.model.application.security.risk.AppSecurityRisk;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppSecurityRiskServiceFacadeBean}, exercising every branch of its business
 * rules with the {@link AppSecurityRiskRepository} and {@link AppSecurityRiskMapper} collaborators
 * fully mocked. There is no uniqueness/duplicate-check business rule for this entity.
 */
@ExtendWith(MockitoExtension.class)
class AppSecurityRiskServiceFacadeBeanTest {

    @Mock
    private AppSecurityRiskMapper appSecurityRiskMapper;

    @Mock
    private AppSecurityRiskRepository appSecurityRiskRepository;

    @InjectMocks
    private AppSecurityRiskServiceFacadeBean appSecurityRiskServiceFacadeBean;

    private AppSecurityRisk activeModel;

    @BeforeEach
    void setUp() {
        activeModel = new AppSecurityRisk();
        activeModel.setId(1L);
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        AppSecurityRiskCriteria criteria = new AppSecurityRiskCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppSecurityRisk> domainPage = new PageImpl<>(List.of(activeModel));
        AppSecurityRiskOutputDTO mapped = new AppSecurityRiskOutputDTO();
        when(appSecurityRiskRepository.findAll(10L, criteria, pageable)).thenReturn(domainPage);
        when(appSecurityRiskMapper.toResponse(activeModel)).thenReturn(mapped);

        Page<AppSecurityRiskOutputDTO> result = appSecurityRiskServiceFacadeBean.getAll(10L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_valid_persistsAndReturnsResponse() {
        AppSecurityRiskInputDTO inputDTO = new AppSecurityRiskInputDTO(5L, 7L, "Description", 9L);
        AppSecurityRisk model = new AppSecurityRisk();
        AppSecurityRisk saved = new AppSecurityRisk();
        AppSecurityRiskOutputDTO response = new AppSecurityRiskOutputDTO();
        when(appSecurityRiskMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appSecurityRiskRepository.create(model)).thenReturn(saved);
        when(appSecurityRiskMapper.toResponse(saved)).thenReturn(response);

        AppSecurityRiskOutputDTO result = appSecurityRiskServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        AppSecurityRiskInputDTO inputDTO = new AppSecurityRiskInputDTO(5L, 7L, "Description", 9L);
        when(appSecurityRiskRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appSecurityRiskServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_APP_SECURITY_RISK_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        AppSecurityRiskInputDTO inputDTO = new AppSecurityRiskInputDTO(5L, 7L, "Description", 9L);
        AppSecurityRisk updated = new AppSecurityRisk();
        AppSecurityRiskOutputDTO response = new AppSecurityRiskOutputDTO();
        when(appSecurityRiskRepository.findById(1L)).thenReturn(activeModel);
        when(appSecurityRiskRepository.update(activeModel, 1L)).thenReturn(updated);
        when(appSecurityRiskMapper.toResponse(updated)).thenReturn(response);

        AppSecurityRiskOutputDTO result = appSecurityRiskServiceFacadeBean.update(1L, inputDTO);

        verify(appSecurityRiskMapper).updateModelFromInput(inputDTO, activeModel);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(appSecurityRiskRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appSecurityRiskServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_APP_SECURITY_RISK_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeModel.setDeletedAt(LocalDateTime.now());
        when(appSecurityRiskRepository.findById(1L)).thenReturn(activeModel);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appSecurityRiskServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_APP_SECURITY_RISK_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesModel() {
        when(appSecurityRiskRepository.findById(1L)).thenReturn(activeModel);

        appSecurityRiskServiceFacadeBean.delete(1L);

        assertNotNull(activeModel.getDeletedAt());
        verify(appSecurityRiskRepository).delete(activeModel);
    }
}
