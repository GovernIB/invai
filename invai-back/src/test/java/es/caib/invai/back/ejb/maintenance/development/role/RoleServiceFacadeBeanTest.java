package es.caib.invai.back.ejb.maintenance.development.role;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.development.role.DTO.RoleInputDTO;
import es.caib.invai.back.interna.maintenance.development.role.DTO.RoleOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.provider.AppProviderRepository;
import es.caib.invai.back.persistence.repository.maintenance.development.role.RoleCriteria;
import es.caib.invai.back.persistence.repository.maintenance.development.role.RoleRepository;
import es.caib.invai.back.service.mapper.maintenance.development.role.RoleMapper;
import es.caib.invai.back.service.model.maintenance.development.role.Role;
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
 * Unit tests for {@link RoleServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link RoleRepository}, {@link RoleMapper}, and {@link AppProviderRepository}
 * collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceFacadeBeanTest {

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AppProviderRepository appProviderRepository;

    @InjectMocks
    private RoleServiceFacadeBean roleServiceFacadeBean;

    private Role activeRole;

    @BeforeEach
    void setUp() {
        activeRole = new Role();
        activeRole.setId(1L);
        activeRole.setName("DevOps Lead");
        activeRole.setNameEs("Responsable DevOps");
    }

    @Test
    void getById_found_returnsMappedResponse() {
        RoleOutputDTO expected = new RoleOutputDTO();
        when(roleRepository.findById(1L)).thenReturn(activeRole);
        when(roleMapper.toResponse(activeRole)).thenReturn(expected);

        RoleOutputDTO result = roleServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(roleRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> roleServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_ROLE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        RoleCriteria criteria = new RoleCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<Role> domainPage = new PageImpl<>(List.of(activeRole));
        RoleOutputDTO mapped = new RoleOutputDTO();
        when(roleRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(roleMapper.toResponse(activeRole)).thenReturn(mapped);

        Page<RoleOutputDTO> result = roleServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueName_persistsAndReturnsResponse() {
        RoleInputDTO inputDTO = new RoleInputDTO("New Role", "Rol nuevo");
        Role model = new Role();
        Role saved = new Role();
        RoleOutputDTO response = new RoleOutputDTO();
        when(roleRepository.existsByNameAndDeletedAtIsNull("New Role")).thenReturn(false);
        when(roleMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(roleRepository.create(model)).thenReturn(saved);
        when(roleMapper.toResponse(saved)).thenReturn(response);

        RoleOutputDTO result = roleServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateName_throwsBusinessRuleException() {
        RoleInputDTO inputDTO = new RoleInputDTO("Proveïdor principal", null);
        when(roleRepository.existsByNameAndDeletedAtIsNull("Proveïdor principal")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> roleServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_ROLE_DUPLICATED, ex.getMessage());
        verify(roleRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        RoleInputDTO inputDTO = new RoleInputDTO("X", null);
        when(roleRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> roleServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_ROLE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_duplicateName_throwsBusinessRuleException() {
        RoleInputDTO inputDTO = new RoleInputDTO("Taken", null);
        when(roleRepository.findById(1L)).thenReturn(activeRole);
        when(roleRepository.existsByNameAndIdNotAndDeletedAtIsNull("Taken", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> roleServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_ROLE_DUPLICATED, ex.getMessage());
        verify(roleMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        RoleInputDTO inputDTO = new RoleInputDTO("Updated", "Actualizado");
        Role updated = new Role();
        RoleOutputDTO response = new RoleOutputDTO();
        when(roleRepository.findById(1L)).thenReturn(activeRole);
        when(roleRepository.existsByNameAndIdNotAndDeletedAtIsNull("Updated", 1L)).thenReturn(false);
        when(roleRepository.update(activeRole, 1L)).thenReturn(updated);
        when(roleMapper.toResponse(updated)).thenReturn(response);

        RoleOutputDTO result = roleServiceFacadeBean.update(1L, inputDTO);

        verify(roleMapper).updateModelFromInput(inputDTO, activeRole);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(roleRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> roleServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_ROLE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeRole.setDeletedAt(LocalDateTime.now());
        when(roleRepository.findById(1L)).thenReturn(activeRole);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> roleServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_ROLE_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_hasDependencies_throwsBusinessRuleException() {
        when(roleRepository.findById(1L)).thenReturn(activeRole);
        when(appProviderRepository.existsByRoleId(1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> roleServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_ROLE_DELETE_HAS_DEPENDENCIES, ex.getMessage());
        verify(roleRepository, never()).delete(any());
    }

    @Test
    void delete_valid_softDeletesRole() {
        when(roleRepository.findById(1L)).thenReturn(activeRole);
        when(appProviderRepository.existsByRoleId(1L)).thenReturn(false);

        roleServiceFacadeBean.delete(1L);

        assertNotNull(activeRole.getDeletedAt());
        verify(roleRepository, times(1)).delete(activeRole);
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(roleRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> roleServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_ROLE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(roleRepository.findById(1L)).thenReturn(activeRole);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> roleServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_ROLE_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesRole() {
        activeRole.setDeletedAt(LocalDateTime.now());
        activeRole.setDeletedBy("someone");
        Role reactivated = new Role();
        RoleOutputDTO response = new RoleOutputDTO();
        when(roleRepository.findById(1L)).thenReturn(activeRole);
        when(roleRepository.update(eq(activeRole), eq(1L))).thenReturn(reactivated);
        when(roleMapper.toResponse(reactivated)).thenReturn(response);

        RoleOutputDTO result = roleServiceFacadeBean.reactivate(1L);

        assertNull(activeRole.getDeletedAt());
        assertNull(activeRole.getDeletedBy());
        assertEquals(response, result);
    }
}
