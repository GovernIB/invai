package es.caib.invai.back.ejb.application.security.role;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.security.role.DTO.AppRoleInputDTO;
import es.caib.invai.back.interna.application.security.role.DTO.AppRoleOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.role.AppRoleCriteria;
import es.caib.invai.back.persistence.repository.application.security.role.AppRoleRepository;
import es.caib.invai.back.service.mapper.application.security.role.AppRoleMapper;
import es.caib.invai.back.service.model.application.security.role.AppRole;
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
 * Unit tests for {@link AppRoleServiceFacadeBean}, exercising every branch of its business
 * rules with the {@link AppRoleRepository} and {@link AppRoleMapper} collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class AppRoleServiceFacadeBeanTest {

    @Mock
    private AppRoleMapper appRoleMapper;

    @Mock
    private AppRoleRepository appRoleRepository;

    @InjectMocks
    private AppRoleServiceFacadeBean appRoleServiceFacadeBean;

    private AppRole activeModel;

    @BeforeEach
    void setUp() {
        activeModel = new AppRole();
        activeModel.setId(1L);
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        AppRoleCriteria criteria = new AppRoleCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppRole> domainPage = new PageImpl<>(List.of(activeModel));
        AppRoleOutputDTO mapped = new AppRoleOutputDTO();
        when(appRoleRepository.findAll(10L, criteria, pageable)).thenReturn(domainPage);
        when(appRoleMapper.toResponse(activeModel)).thenReturn(mapped);

        Page<AppRoleOutputDTO> result = appRoleServiceFacadeBean.getAll(10L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_valid_persistsAndReturnsResponse() {
        AppRoleInputDTO inputDTO = new AppRoleInputDTO(5L, 7L);
        AppRole model = new AppRole();
        AppRole saved = new AppRole();
        AppRoleOutputDTO response = new AppRoleOutputDTO();
        when(appRoleMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appRoleRepository.create(model)).thenReturn(saved);
        when(appRoleMapper.toResponse(saved)).thenReturn(response);

        AppRoleOutputDTO result = appRoleServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        AppRoleInputDTO inputDTO = new AppRoleInputDTO(5L, 7L);
        when(appRoleRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appRoleServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_APP_ROLE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        AppRoleInputDTO inputDTO = new AppRoleInputDTO(5L, 7L);
        AppRole updated = new AppRole();
        AppRoleOutputDTO response = new AppRoleOutputDTO();
        when(appRoleRepository.findById(1L)).thenReturn(activeModel);
        when(appRoleRepository.update(activeModel, 1L)).thenReturn(updated);
        when(appRoleMapper.toResponse(updated)).thenReturn(response);

        AppRoleOutputDTO result = appRoleServiceFacadeBean.update(1L, inputDTO);

        verify(appRoleMapper).updateModelFromInput(inputDTO, activeModel);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(appRoleRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appRoleServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_APP_ROLE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeModel.setDeletedAt(LocalDateTime.now());
        when(appRoleRepository.findById(1L)).thenReturn(activeModel);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appRoleServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_APP_ROLE_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesModel() {
        when(appRoleRepository.findById(1L)).thenReturn(activeModel);

        appRoleServiceFacadeBean.delete(1L);

        assertNotNull(activeModel.getDeletedAt());
        verify(appRoleRepository).delete(activeModel);
    }
}
