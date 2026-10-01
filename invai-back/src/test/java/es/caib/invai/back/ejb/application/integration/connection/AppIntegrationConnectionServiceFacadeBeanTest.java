package es.caib.invai.back.ejb.application.integration.connection;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.exception.SoffidTimeoutException;
import es.caib.invai.back.interna.application.integration.connection.DTO.AppIntegrationConnectionInputDTO;
import es.caib.invai.back.interna.application.integration.connection.DTO.AppIntegrationConnectionOutputDTO;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.persistence.repository.application.integration.core.AppIntegrationRepository;
import es.caib.invai.back.persistence.repository.application.integration.connection.AppIntegrationConnectionCriteria;
import es.caib.invai.back.persistence.repository.application.integration.connection.AppIntegrationConnectionRepository;
import es.caib.invai.back.persistence.repository.application.integration.requiredRole.AppIntegrationRequiredRoleRepository;
import es.caib.invai.back.persistence.repository.maintenance.development.technology.TechnologyRepository;
import es.caib.invai.back.persistence.repository.maintenance.integration.externalSystem.ExternalSystemRepository;
import es.caib.invai.back.rest.soffid.SoffidClient;
import es.caib.invai.back.rest.soffid.SoffidRole;
import es.caib.invai.back.service.mapper.application.integration.connection.AppIntegrationConnectionMapper;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.integration.core.AppIntegration;
import es.caib.invai.back.service.model.application.integration.connection.AppIntegrationConnection;
import es.caib.invai.back.service.model.application.integration.requiredRole.AppIntegrationRequiredRole;
import es.caib.invai.back.service.model.maintenance.development.technology.Technology;
import es.caib.invai.back.service.model.maintenance.integration.externalSystem.ExternalSystem;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppIntegrationConnectionServiceFacadeBean}, focused on the "exactly one
 * system" business rule, the required-roles-in-bloc reconciliation by id (create/update/delete),
 * and the live required/granted roles resolution and warning computation.
 */
@ExtendWith(MockitoExtension.class)
class AppIntegrationConnectionServiceFacadeBeanTest {

    @Mock
    private AppIntegrationConnectionMapper appIntegrationConnectionMapper;

    @Mock
    private AppIntegrationConnectionRepository appIntegrationConnectionRepository;

    @Mock
    private AppIntegrationRequiredRoleRepository appIntegrationRequiredRoleRepository;

    @Mock
    private SoffidClient soffidClient;

    @Mock
    private AppIntegrationRepository appIntegrationRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ExternalSystemRepository externalSystemRepository;

    @Mock
    private TechnologyRepository technologyRepository;

    @InjectMocks
    private AppIntegrationConnectionServiceFacadeBean appIntegrationConnectionServiceFacadeBean;

    private AppIntegrationConnection activeModel;

    @BeforeEach
    void setUp() {
        activeModel = new AppIntegrationConnection();
        activeModel.setId(1L);
        activeModel.setUsername("u00629");

        lenient().when(appIntegrationConnectionMapper.toResponse(any())).thenReturn(new AppIntegrationConnectionOutputDTO());
        lenient().when(appIntegrationRequiredRoleRepository.findAllActiveByAppIntegrationConnectionId(any()))
                .thenReturn(List.of());
        lenient().when(soffidClient.getGrantedRoles(any())).thenReturn(List.of());
        lenient().when(soffidClient.getRolesByIds(any())).thenAnswer(invocation -> {
            List<Long> ids = invocation.getArgument(0);
            return ids.stream().map(AppIntegrationConnectionServiceFacadeBeanTest::soffidRole).toList();
        });
        lenient().when(appIntegrationRepository.findById(any())).thenReturn(new AppIntegration());
        lenient().when(applicationRepository.findById(any())).thenReturn(new Application());
        lenient().when(externalSystemRepository.findById(any())).thenReturn(new ExternalSystem());
        lenient().when(technologyRepository.findById(any())).thenReturn(new Technology());
    }

    private static SoffidRole soffidRole(Long roleId) {
        SoffidRole role = new SoffidRole();
        role.setId(roleId);
        role.setName("role-" + roleId);
        return role;
    }

    private static AppIntegrationRequiredRole existingRequiredRole(Long id, Long roleId) {
        AppIntegrationRequiredRole role = new AppIntegrationRequiredRole();
        role.setId(id);
        role.setRoleId(roleId);
        return role;
    }

    private AppIntegrationConnectionOutputDTO getAllSingleResult() {
        Page<AppIntegrationConnection> page = new PageImpl<>(List.of(activeModel));
        when(appIntegrationConnectionRepository.findAll(any(), any(), any())).thenReturn(page);
        return appIntegrationConnectionServiceFacadeBean
                .getAll(10L, new AppIntegrationConnectionCriteria(), Pageable.unpaged())
                .getContent().get(0);
    }

    @Test
    void create_neitherApplicationNorExternalSystem_throwsSystemRequired() {
        AppIntegrationConnectionInputDTO inputDTO = new AppIntegrationConnectionInputDTO(10L, null, null, 5L, "u00629", List.of(100L));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appIntegrationConnectionServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_APP_INTEGRATION_CONNECTION_SYSTEM_REQUIRED, ex.getMessage());
    }

    @Test
    void create_bothApplicationAndExternalSystem_throwsSystemAmbiguous() {
        AppIntegrationConnectionInputDTO inputDTO = new AppIntegrationConnectionInputDTO(10L, 20L, 30L, 5L, "u00629", List.of(100L));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appIntegrationConnectionServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_APP_INTEGRATION_CONNECTION_SYSTEM_AMBIGUOUS, ex.getMessage());
    }

    @Test
    void create_appIntegrationNotFound_throwsBusinessRuleException() {
        when(appIntegrationRepository.findById(10L)).thenReturn(null);
        AppIntegrationConnectionInputDTO inputDTO = new AppIntegrationConnectionInputDTO(10L, 20L, null, 5L, "u00629", List.of(100L));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appIntegrationConnectionServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_APP_INTEGRATION_NOT_FOUND, ex.getMessage());
    }

    @Test
    void create_applicationNotFound_throwsBusinessRuleException() {
        when(applicationRepository.findById(20L)).thenReturn(null);
        AppIntegrationConnectionInputDTO inputDTO = new AppIntegrationConnectionInputDTO(10L, 20L, null, 5L, "u00629", List.of(100L));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appIntegrationConnectionServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_APP_NOT_FOUND, ex.getMessage());
    }

    @Test
    void create_externalSystemNotFound_throwsBusinessRuleException() {
        when(externalSystemRepository.findById(30L)).thenReturn(null);
        AppIntegrationConnectionInputDTO inputDTO = new AppIntegrationConnectionInputDTO(10L, null, 30L, 5L, "u00629", List.of(100L));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appIntegrationConnectionServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_EXTERNALSYSTEM_NOT_FOUND, ex.getMessage());
    }

    @Test
    void create_technologyNotFound_throwsBusinessRuleException() {
        when(technologyRepository.findById(5L)).thenReturn(null);
        AppIntegrationConnectionInputDTO inputDTO = new AppIntegrationConnectionInputDTO(10L, 20L, null, 5L, "u00629", List.of(100L));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appIntegrationConnectionServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_TECHNOLOGY_NOT_FOUND, ex.getMessage());
    }

    @Test
    void create_persistsEntryAndEachRequiredRoleIdAsChildOfIt() {
        AppIntegrationConnectionInputDTO inputDTO = new AppIntegrationConnectionInputDTO(10L, 20L, null, 5L, "u00629",
                List.of(100L, 200L));
        AppIntegrationConnection created = new AppIntegrationConnection();
        created.setId(1L);
        created.setUsername("u00629");
        when(appIntegrationConnectionMapper.toModelFromInput(inputDTO)).thenReturn(created);
        when(appIntegrationConnectionRepository.create(created)).thenReturn(created);

        appIntegrationConnectionServiceFacadeBean.create(inputDTO);

        ArgumentCaptor<AppIntegrationRequiredRole> captor = ArgumentCaptor.forClass(AppIntegrationRequiredRole.class);
        verify(appIntegrationRequiredRoleRepository, times(2)).create(captor.capture());
        List<Long> persistedRoleIds = captor.getAllValues().stream().map(AppIntegrationRequiredRole::getRoleId).toList();
        assertTrue(persistedRoleIds.containsAll(List.of(100L, 200L)));
        captor.getAllValues().forEach(role -> assertEquals(1L, role.getAppIntegrationConnection().getId()));
    }

    @Test
    void update_missingRoleFromRequest_softDeletesIt() {
        when(appIntegrationConnectionRepository.findById(1L)).thenReturn(activeModel);
        when(appIntegrationConnectionRepository.update(activeModel, 1L)).thenReturn(activeModel);
        AppIntegrationRequiredRole current = existingRequiredRole(500L, 100L);
        when(appIntegrationRequiredRoleRepository.findAllActiveByAppIntegrationConnectionId(1L)).thenReturn(List.of(current));

        AppIntegrationConnectionInputDTO inputDTO = new AppIntegrationConnectionInputDTO(10L, 20L, null, 5L, "u00629",
                List.of(200L));

        appIntegrationConnectionServiceFacadeBean.update(1L, inputDTO);

        verify(appIntegrationRequiredRoleRepository).delete(current);
        assertTrue(current.getDeletedAt() != null);
    }

    @Test
    void update_newRoleInRequest_createsIt() {
        when(appIntegrationConnectionRepository.findById(1L)).thenReturn(activeModel);
        when(appIntegrationConnectionRepository.update(activeModel, 1L)).thenReturn(activeModel);

        AppIntegrationConnectionInputDTO inputDTO = new AppIntegrationConnectionInputDTO(10L, 20L, null, 5L, "u00629",
                List.of(300L));

        appIntegrationConnectionServiceFacadeBean.update(1L, inputDTO);

        ArgumentCaptor<AppIntegrationRequiredRole> captor = ArgumentCaptor.forClass(AppIntegrationRequiredRole.class);
        verify(appIntegrationRequiredRoleRepository).create(captor.capture());
        assertEquals(300L, captor.getValue().getRoleId());
        assertEquals(1L, captor.getValue().getAppIntegrationConnection().getId());
    }

    @Test
    void update_sameRoleIdSetRequested_touchesNothing() {
        when(appIntegrationConnectionRepository.findById(1L)).thenReturn(activeModel);
        when(appIntegrationConnectionRepository.update(activeModel, 1L)).thenReturn(activeModel);
        AppIntegrationRequiredRole current = existingRequiredRole(500L, 100L);
        when(appIntegrationRequiredRoleRepository.findAllActiveByAppIntegrationConnectionId(1L)).thenReturn(List.of(current));

        AppIntegrationConnectionInputDTO inputDTO = new AppIntegrationConnectionInputDTO(10L, 20L, null, 5L, "u00629",
                List.of(100L));

        appIntegrationConnectionServiceFacadeBean.update(1L, inputDTO);

        verify(appIntegrationRequiredRoleRepository, never()).create(any());
        verify(appIntegrationRequiredRoleRepository, never()).delete(any());
    }

    @Test
    void delete_cascadesToActiveRequiredRoles() {
        when(appIntegrationConnectionRepository.findById(1L)).thenReturn(activeModel);
        AppIntegrationRequiredRole child = existingRequiredRole(500L, 100L);
        when(appIntegrationRequiredRoleRepository.findAllActiveByAppIntegrationConnectionId(1L)).thenReturn(List.of(child));

        appIntegrationConnectionServiceFacadeBean.delete(1L);

        verify(appIntegrationConnectionRepository).delete(activeModel);
        verify(appIntegrationRequiredRoleRepository).delete(child);
        assertTrue(child.getDeletedAt() != null);
    }

    @Test
    void getAll_resolvesRequiredRoleDetailsLiveFromSoffid() {
        AppIntegrationRequiredRole required = existingRequiredRole(500L, 100L);
        when(appIntegrationRequiredRoleRepository.findAllActiveByAppIntegrationConnectionId(1L)).thenReturn(List.of(required));

        AppIntegrationConnectionOutputDTO result = getAllSingleResult();

        verify(soffidClient).getRolesByIds(List.of(100L));
        assertEquals(1, result.getRequiredRoles().size());
        assertEquals(100L, result.getRequiredRoles().get(0).getId());
    }

    @Test
    void getAll_noRequiredRoles_skipsSoffidLookup() {
        AppIntegrationConnectionOutputDTO result = getAllSingleResult();

        verify(soffidClient, never()).getRolesByIds(any());
        assertTrue(result.getRequiredRoles().isEmpty());
    }

    @Test
    void getAll_requiredRoleLookupFails_throwsBusinessRuleException() {
        AppIntegrationRequiredRole required = existingRequiredRole(500L, 100L);
        when(appIntegrationRequiredRoleRepository.findAllActiveByAppIntegrationConnectionId(1L)).thenReturn(List.of(required));
        when(soffidClient.getRolesByIds(List.of(100L))).thenThrow(new SoffidTimeoutException());

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, this::getAllSingleResult);

        assertEquals(Constants.ERR_REQUIREDROLE_UNAVAILABLE, ex.getMessage());
    }

    @Test
    void getAll_grantedRolesMatchRequiredRoles_noRolesMismatch() {
        AppIntegrationRequiredRole required = existingRequiredRole(500L, 100L);
        when(appIntegrationRequiredRoleRepository.findAllActiveByAppIntegrationConnectionId(1L)).thenReturn(List.of(required));
        when(soffidClient.getGrantedRoles("u00629")).thenReturn(List.of(soffidRole(100L)));

        AppIntegrationConnectionOutputDTO result = getAllSingleResult();

        assertFalse(result.getRolesMismatch());
    }

    @Test
    void getAll_missingRequiredRole_flagsRolesMismatch() {
        AppIntegrationRequiredRole required = existingRequiredRole(500L, 100L);
        when(appIntegrationRequiredRoleRepository.findAllActiveByAppIntegrationConnectionId(1L)).thenReturn(List.of(required));
        when(soffidClient.getGrantedRoles("u00629")).thenReturn(List.of());

        AppIntegrationConnectionOutputDTO result = getAllSingleResult();

        assertTrue(result.getRolesMismatch());
    }

    @Test
    void getAll_extraGrantedRole_flagsRolesMismatch() {
        when(soffidClient.getGrantedRoles("u00629")).thenReturn(List.of(soffidRole(100L)));

        AppIntegrationConnectionOutputDTO result = getAllSingleResult();

        assertTrue(result.getRolesMismatch());
    }

    @Test
    void getAll_grantedRoleLookupFails_throwsBusinessRuleException() {
        when(soffidClient.getGrantedRoles("u00629")).thenThrow(new SoffidTimeoutException());

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, this::getAllSingleResult);

        assertEquals(Constants.ERR_GRANTEDROLE_UNAVAILABLE, ex.getMessage());
    }
}
