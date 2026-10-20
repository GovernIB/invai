package es.caib.invai.back.ejb.application.development.webContext;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.development.webContext.DTO.AppWebContextInputDTO;
import es.caib.invai.back.interna.application.development.webContext.DTO.AppWebContextOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.core.AppDevelopmentRepository;
import es.caib.invai.back.persistence.repository.application.security.core.AppSecurityRepository;
import es.caib.invai.back.persistence.repository.application.security.webContext.AppWebContextCriteria;
import es.caib.invai.back.persistence.repository.application.security.webContext.AppWebContextRepository;
import es.caib.invai.back.service.mapper.application.security.webContext.AppWebContextMapper;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
import es.caib.invai.back.service.model.application.security.webContext.AppWebContext;
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
 * Unit tests for {@link AppWebContextServiceFacadeBean} (Development's view): full CRUD, with
 * {@code create}'s dual-anchor resolution exercised against fully mocked
 * {@link AppDevelopmentRepository}/{@link AppSecurityRepository}/{@link AppWebContextRepository}/
 * {@link AppWebContextMapper} collaborators.
 */
@ExtendWith(MockitoExtension.class)
class AppWebContextServiceFacadeBeanTest {

    @Mock
    private AppWebContextMapper appWebContextMapper;

    @Mock
    private AppWebContextRepository appWebContextRepository;

    @Mock
    private AppDevelopmentRepository appDevelopmentRepository;

    @Mock
    private AppSecurityRepository appSecurityRepository;

    @InjectMocks
    private AppWebContextServiceFacadeBean appWebContextServiceFacadeBean;

    private AppWebContext activeModel;

    @BeforeEach
    void setUp() {
        activeModel = new AppWebContext();
        activeModel.setId(1L);
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        AppWebContextCriteria criteria = new AppWebContextCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppWebContext> domainPage = new PageImpl<>(List.of(activeModel));
        AppWebContextOutputDTO mapped = new AppWebContextOutputDTO();
        when(appWebContextRepository.findAllByAppDevelopmentId(15L, criteria, pageable)).thenReturn(domainPage);
        when(appWebContextMapper.toResponse(activeModel)).thenReturn(mapped);

        Page<AppWebContextOutputDTO> result = appWebContextServiceFacadeBean.getAll(15L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_developmentNotFound_throwsBusinessRuleException() {
        AppWebContextInputDTO inputDTO = new AppWebContextInputDTO(15L, 7L, 9L, "obs", null);
        when(appDevelopmentRepository.findById(15L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appWebContextServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_DEVELOPMENT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void create_developmentApplicationHasNoSecurityAnchor_throwsBusinessRuleException() {
        AppWebContextInputDTO inputDTO = new AppWebContextInputDTO(15L, 7L, 9L, "obs", null);
        Application application = new Application();
        application.setId(200L);
        AppDevelopment development = AppDevelopment.builder().id(15L).application(application).build();
        when(appDevelopmentRepository.findById(15L)).thenReturn(development);
        when(appSecurityRepository.findByApplicationId(200L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appWebContextServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_APP_WEB_CONTEXT_NO_SECURITY_ANCHOR, ex.getMessage());
    }

    @Test
    void create_valid_resolvesBothAnchorsPersistsAndReturnsResponse() {
        AppWebContextInputDTO inputDTO = new AppWebContextInputDTO(15L, 7L, 9L, "obs", null);
        Application application = new Application();
        application.setId(200L);
        AppDevelopment development = AppDevelopment.builder().id(15L).application(application).build();
        AppSecurity security = new AppSecurity();
        security.setId(10L);
        AppWebContext model = new AppWebContext();
        AppWebContext saved = new AppWebContext();
        AppWebContextOutputDTO response = new AppWebContextOutputDTO();
        when(appDevelopmentRepository.findById(15L)).thenReturn(development);
        when(appSecurityRepository.findByApplicationId(200L)).thenReturn(security);
        when(appWebContextMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appWebContextRepository.create(model)).thenReturn(saved);
        when(appWebContextMapper.toResponse(saved)).thenReturn(response);

        AppWebContextOutputDTO result = appWebContextServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
        assertEquals(security, model.getAppSecurity());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        AppWebContextInputDTO inputDTO = new AppWebContextInputDTO(15L, 7L, 9L, "obs", null);
        when(appWebContextRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appWebContextServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_APP_WEB_CONTEXT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        AppWebContextInputDTO inputDTO = new AppWebContextInputDTO(15L, 7L, 9L, "obs", null);
        AppWebContext updated = new AppWebContext();
        AppWebContextOutputDTO response = new AppWebContextOutputDTO();
        when(appWebContextRepository.findById(1L)).thenReturn(activeModel);
        when(appWebContextRepository.update(activeModel, 1L)).thenReturn(updated);
        when(appWebContextMapper.toResponse(updated)).thenReturn(response);

        AppWebContextOutputDTO result = appWebContextServiceFacadeBean.update(1L, inputDTO);

        verify(appWebContextMapper).updateModelFromInput(inputDTO, activeModel);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(appWebContextRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appWebContextServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_APP_WEB_CONTEXT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeModel.setDeletedAt(LocalDateTime.now());
        when(appWebContextRepository.findById(1L)).thenReturn(activeModel);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appWebContextServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_APP_WEB_CONTEXT_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesModel() {
        when(appWebContextRepository.findById(1L)).thenReturn(activeModel);

        appWebContextServiceFacadeBean.delete(1L);

        assertNotNull(activeModel.getDeletedAt());
        verify(appWebContextRepository).delete(activeModel);
    }
}
