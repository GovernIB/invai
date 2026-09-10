package es.caib.invai.back.ejb.application.security.core;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.security.core.DTO.AppSecurityInputDTO;
import es.caib.invai.back.interna.application.security.core.DTO.AppSecurityOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.core.AppSecurityRepository;
import es.caib.invai.back.service.mapper.application.security.core.AppSecurityMapper;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppSecurityServiceFacadeBean}, exercising every branch of its
 * business rules with the {@link AppSecurityRepository} and
 * {@link AppSecurityMapper} collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class AppSecurityServiceFacadeBeanTest {

    @Mock
    private AppSecurityMapper appSecurityMapper;

    @Mock
    private AppSecurityRepository appSecurityRepository;

    @InjectMocks
    private AppSecurityServiceFacadeBean appSecurityServiceFacadeBean;

    private AppSecurity activeModel;

    @BeforeEach
    void setUp() {
        activeModel = new AppSecurity();
        activeModel.setId(1L);
        activeModel.setObservation("Core grouping");
    }

    @Test
    void getById_delegatesToRepositoryAndMapsResponse() {
        AppSecurityOutputDTO mapped = new AppSecurityOutputDTO();
        when(appSecurityRepository.findById(1L)).thenReturn(activeModel);
        when(appSecurityMapper.toResponse(activeModel)).thenReturn(mapped);

        AppSecurityOutputDTO result = appSecurityServiceFacadeBean.getById(1L);

        assertEquals(mapped, result);
    }

    @Test
    void create_valid_persistsAndReturnsResponse() {
        AppSecurityInputDTO inputDTO = new AppSecurityInputDTO(10L, "Notes");
        AppSecurity model = new AppSecurity();
        AppSecurity saved = new AppSecurity();
        AppSecurityOutputDTO response = new AppSecurityOutputDTO();
        when(appSecurityMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appSecurityRepository.create(model)).thenReturn(saved);
        when(appSecurityMapper.toResponse(saved)).thenReturn(response);

        AppSecurityOutputDTO result = appSecurityServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
        assertEquals("Notes", inputDTO.getObservation());
    }

    @Test
    void create_sanitizesInputBeforeMapping() {
        AppSecurityInputDTO inputDTO = new AppSecurityInputDTO(10L, "  Notes  ");
        when(appSecurityMapper.toModelFromInput(inputDTO)).thenReturn(new AppSecurity());
        when(appSecurityRepository.create(org.mockito.ArgumentMatchers.any())).thenReturn(new AppSecurity());
        when(appSecurityMapper.toResponse(org.mockito.ArgumentMatchers.any())).thenReturn(new AppSecurityOutputDTO());

        appSecurityServiceFacadeBean.create(inputDTO);

        assertEquals("Notes", inputDTO.getObservation());
    }

    @Test
    void create_applicationAlreadyHasActiveSecurityAnchor_throwsBusinessRuleException() {
        AppSecurityInputDTO inputDTO = new AppSecurityInputDTO(10L, "Notes");
        when(appSecurityRepository.findByApplicationId(10L)).thenReturn(activeModel);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appSecurityServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_APP_SECURITY_ALREADY_EXISTS, ex.getMessage());
        verify(appSecurityRepository, org.mockito.Mockito.never()).create(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void create_applicationHasOnlySoftDeletedSecurityAnchor_persistsNewRecord() {
        activeModel.setDeletedAt(LocalDateTime.now());
        AppSecurityInputDTO inputDTO = new AppSecurityInputDTO(10L, "Notes");
        AppSecurity model = new AppSecurity();
        AppSecurity saved = new AppSecurity();
        AppSecurityOutputDTO response = new AppSecurityOutputDTO();
        when(appSecurityRepository.findByApplicationId(10L)).thenReturn(activeModel);
        when(appSecurityMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appSecurityRepository.create(model)).thenReturn(saved);
        when(appSecurityMapper.toResponse(saved)).thenReturn(response);

        AppSecurityOutputDTO result = appSecurityServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        AppSecurityInputDTO inputDTO = new AppSecurityInputDTO(10L, "Notes");
        when(appSecurityRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appSecurityServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_APP_SECURITY_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        AppSecurityInputDTO inputDTO = new AppSecurityInputDTO(10L, "Updated");
        AppSecurity updated = new AppSecurity();
        AppSecurityOutputDTO response = new AppSecurityOutputDTO();
        when(appSecurityRepository.findById(1L)).thenReturn(activeModel);
        when(appSecurityRepository.update(activeModel, 1L)).thenReturn(updated);
        when(appSecurityMapper.toResponse(updated)).thenReturn(response);

        AppSecurityOutputDTO result = appSecurityServiceFacadeBean.update(1L, inputDTO);

        verify(appSecurityMapper).updateModelFromInput(inputDTO, activeModel);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(appSecurityRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appSecurityServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_APP_SECURITY_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeModel.setDeletedAt(LocalDateTime.now());
        when(appSecurityRepository.findById(1L)).thenReturn(activeModel);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appSecurityServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_APP_SECURITY_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesModel() {
        when(appSecurityRepository.findById(1L)).thenReturn(activeModel);

        appSecurityServiceFacadeBean.delete(1L);

        assertNotNull(activeModel.getDeletedAt());
        verify(appSecurityRepository).delete(activeModel);
    }
}
