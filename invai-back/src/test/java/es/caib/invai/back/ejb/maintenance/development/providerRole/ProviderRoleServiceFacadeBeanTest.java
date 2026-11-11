package es.caib.invai.back.ejb.maintenance.development.providerRole;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.development.providerRole.DTO.ProviderRoleInputDTO;
import es.caib.invai.back.interna.maintenance.development.providerRole.DTO.ProviderRoleOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.provider.AppProviderRepository;
import es.caib.invai.back.persistence.repository.maintenance.development.providerRole.ProviderRoleCriteria;
import es.caib.invai.back.persistence.repository.maintenance.development.providerRole.ProviderRoleRepository;
import es.caib.invai.back.service.mapper.maintenance.development.providerRole.ProviderRoleMapper;
import es.caib.invai.back.service.model.maintenance.development.providerRole.ProviderRole;
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
 * Unit tests for {@link ProviderRoleServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link ProviderRoleRepository}, {@link ProviderRoleMapper}, and {@link AppProviderRepository}
 * collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class ProviderRoleServiceFacadeBeanTest {

    @Mock
    private ProviderRoleMapper providerRoleMapper;

    @Mock
    private ProviderRoleRepository providerRoleRepository;

    @Mock
    private AppProviderRepository appProviderRepository;

    @InjectMocks
    private ProviderRoleServiceFacadeBean providerRoleServiceFacadeBean;

    private ProviderRole activeRole;

    @BeforeEach
    void setUp() {
        activeRole = new ProviderRole();
        activeRole.setId(1L);
        activeRole.setName("DevOps Lead");
        activeRole.setNameEs("Responsable DevOps");
    }

    @Test
    void getById_found_returnsMappedResponse() {
        ProviderRoleOutputDTO expected = new ProviderRoleOutputDTO();
        when(providerRoleRepository.findById(1L)).thenReturn(activeRole);
        when(providerRoleMapper.toResponse(activeRole)).thenReturn(expected);

        ProviderRoleOutputDTO result = providerRoleServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(providerRoleRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> providerRoleServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_ROLE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        ProviderRoleCriteria criteria = new ProviderRoleCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<ProviderRole> domainPage = new PageImpl<>(List.of(activeRole));
        ProviderRoleOutputDTO mapped = new ProviderRoleOutputDTO();
        when(providerRoleRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(providerRoleMapper.toResponse(activeRole)).thenReturn(mapped);

        Page<ProviderRoleOutputDTO> result = providerRoleServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueName_persistsAndReturnsResponse() {
        ProviderRoleInputDTO inputDTO = new ProviderRoleInputDTO("New ProviderRole", "Rol nuevo");
        ProviderRole model = new ProviderRole();
        ProviderRole saved = new ProviderRole();
        ProviderRoleOutputDTO response = new ProviderRoleOutputDTO();
        when(providerRoleRepository.existsByNameAndDeletedAtIsNull("New ProviderRole")).thenReturn(false);
        when(providerRoleMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(providerRoleRepository.create(model)).thenReturn(saved);
        when(providerRoleMapper.toResponse(saved)).thenReturn(response);

        ProviderRoleOutputDTO result = providerRoleServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateName_throwsBusinessRuleException() {
        ProviderRoleInputDTO inputDTO = new ProviderRoleInputDTO("Proveïdor principal", null);
        when(providerRoleRepository.existsByNameAndDeletedAtIsNull("Proveïdor principal")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> providerRoleServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_ROLE_DUPLICATED, ex.getMessage());
        verify(providerRoleRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        ProviderRoleInputDTO inputDTO = new ProviderRoleInputDTO("X", null);
        when(providerRoleRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> providerRoleServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_ROLE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_duplicateName_throwsBusinessRuleException() {
        ProviderRoleInputDTO inputDTO = new ProviderRoleInputDTO("Taken", null);
        when(providerRoleRepository.findById(1L)).thenReturn(activeRole);
        when(providerRoleRepository.existsByNameAndIdNotAndDeletedAtIsNull("Taken", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> providerRoleServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_ROLE_DUPLICATED, ex.getMessage());
        verify(providerRoleMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        ProviderRoleInputDTO inputDTO = new ProviderRoleInputDTO("Updated", "Actualizado");
        ProviderRole updated = new ProviderRole();
        ProviderRoleOutputDTO response = new ProviderRoleOutputDTO();
        when(providerRoleRepository.findById(1L)).thenReturn(activeRole);
        when(providerRoleRepository.existsByNameAndIdNotAndDeletedAtIsNull("Updated", 1L)).thenReturn(false);
        when(providerRoleRepository.update(activeRole, 1L)).thenReturn(updated);
        when(providerRoleMapper.toResponse(updated)).thenReturn(response);

        ProviderRoleOutputDTO result = providerRoleServiceFacadeBean.update(1L, inputDTO);

        verify(providerRoleMapper).updateModelFromInput(inputDTO, activeRole);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(providerRoleRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> providerRoleServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_ROLE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeRole.setDeletedAt(LocalDateTime.now());
        when(providerRoleRepository.findById(1L)).thenReturn(activeRole);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> providerRoleServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_ROLE_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_hasDependencies_throwsBusinessRuleException() {
        when(providerRoleRepository.findById(1L)).thenReturn(activeRole);
        when(appProviderRepository.existsByRoleId(1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> providerRoleServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_ROLE_DELETE_HAS_DEPENDENCIES, ex.getMessage());
        verify(providerRoleRepository, never()).delete(any());
    }

    @Test
    void delete_valid_softDeletesRole() {
        when(providerRoleRepository.findById(1L)).thenReturn(activeRole);
        when(appProviderRepository.existsByRoleId(1L)).thenReturn(false);

        providerRoleServiceFacadeBean.delete(1L);

        assertNotNull(activeRole.getDeletedAt());
        verify(providerRoleRepository, times(1)).delete(activeRole);
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(providerRoleRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> providerRoleServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_ROLE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(providerRoleRepository.findById(1L)).thenReturn(activeRole);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> providerRoleServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_ROLE_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesRole() {
        activeRole.setDeletedAt(LocalDateTime.now());
        activeRole.setDeletedBy("someone");
        ProviderRole reactivated = new ProviderRole();
        ProviderRoleOutputDTO response = new ProviderRoleOutputDTO();
        when(providerRoleRepository.findById(1L)).thenReturn(activeRole);
        when(providerRoleRepository.update(eq(activeRole), eq(1L))).thenReturn(reactivated);
        when(providerRoleMapper.toResponse(reactivated)).thenReturn(response);

        ProviderRoleOutputDTO result = providerRoleServiceFacadeBean.reactivate(1L);

        assertNull(activeRole.getDeletedAt());
        assertNull(activeRole.getDeletedBy());
        assertEquals(response, result);
    }
}
