package es.caib.invai.back.ejb.application.security.role;

import static org.junit.jupiter.api.Assertions.assertNull;
import es.caib.invai.back.exception.SoffidTimeoutException;
import es.caib.invai.back.rest.soffid.SoffidClient;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.lenient;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertTrue;
import es.caib.invai.back.exception.SoffidClientException;
import es.caib.invai.back.interna.maintenance.security.securityRole.DTO.SecurityRoleOutputDTO;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import org.mockito.ArgumentCaptor;
import es.caib.invai.back.rest.soffid.SoffidRole;
import es.caib.invai.back.service.model.maintenance.security.securityRole.SecurityRole;
import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.security.role.DTO.AppRoleInputDTO;
import es.caib.invai.back.interna.application.security.role.DTO.AppRoleOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.role.AppRoleCriteria;
import es.caib.invai.back.persistence.repository.application.security.role.AppRoleRepository;
import es.caib.invai.back.service.mapper.application.security.role.AppRoleMapper;
import es.caib.invai.back.service.model.application.security.role.AppRole;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.persistence.repository.application.security.core.AppSecurityRepository;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
import es.caib.invai.back.service.model.catalog.validationStatus.ValidationStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import es.caib.invai.back.service.mapper.maintenance.security.securityRole.SecurityRoleMapper;
import es.caib.invai.back.service.mapper.maintenance.security.securityRole.SecurityRoleMapperImpl;
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

    @Mock
    private SoffidClient soffidClient;

    @Mock
    private AppSecurityRepository appSecurityRepository;

    @Spy
    private SecurityRoleMapper securityRoleMapper = new SecurityRoleMapperImpl();

    @InjectMocks
    private AppRoleServiceFacadeBean appRoleServiceFacadeBean;

    private AppRole activeModel;

    @BeforeEach
    void setUp() {
        activeModel = new AppRole();
        activeModel.setId(1L);
        activeModel.setAppSecurity(appSecurityOf("AP2"));
        lenient().when(appSecurityRepository.findById(any())).thenReturn(activeModel.getAppSecurity());
    }

    /** A security anchor whose application has the given prefix. */
    private static AppSecurity appSecurityOf(String prefix) {
        Application application = new Application();
        application.setPrefix(prefix);
        AppSecurity appSecurity = new AppSecurity();
        appSecurity.setId(5L);
        appSecurity.setApplication(application);
        return appSecurity;
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

    private static SoffidRole soffidRole(Long id, String name, String system, String description) {
        SoffidRole role = new SoffidRole();
        role.setId(id);
        role.setName(name);
        role.setSystem(system);
        role.setDescription(description);
        return role;
    }

    @Test
    void create_valid_getsTheRoleFromSoffidAndSavesItsDataWithTheAssignment() {
        AppRoleInputDTO inputDTO = new AppRoleInputDTO(5L, 7L);
        AppRole saved = new AppRole();
        AppRoleOutputDTO response = new AppRoleOutputDTO();
        when(soffidClient.getRolesByIds(List.of(7L))).thenReturn(List.of(soffidRole(7L, "AP2_ADMIN", "soffid", "Administrador")));
        when(appRoleRepository.create(any(AppRole.class))).thenReturn(saved);
        when(appRoleMapper.toResponse(saved)).thenReturn(response);

        AppRoleOutputDTO result = appRoleServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
        ArgumentCaptor<AppRole> persisted = ArgumentCaptor.forClass(AppRole.class);
        verify(appRoleRepository).create(persisted.capture());
        assertEquals(5L, persisted.getValue().getAppSecurity().getId());
        assertNull(persisted.getValue().getSecurityRole().getId());
        assertEquals(7L, persisted.getValue().getSecurityRole().getRoleId());
        assertEquals("AP2_ADMIN", persisted.getValue().getSecurityRole().getName());
        assertEquals("soffid", persisted.getValue().getSecurityRole().getSystem());
        assertEquals("Administrador", persisted.getValue().getSecurityRole().getDescription());
    }

    @Test
    void create_roleUnknownInSoffid_throwsAndPersistsNothing() {
        AppRoleInputDTO inputDTO = new AppRoleInputDTO(5L, 999L);
        when(soffidClient.getRolesByIds(List.of(999L))).thenReturn(List.of());

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appRoleServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_SOFFID_ROLE_NOT_FOUND, ex.getMessage());
        verify(appRoleRepository, never()).create(any());
    }

    @Test
    void create_soffidAnswersWithAnotherRole_isNotTrusted() {
        AppRoleInputDTO inputDTO = new AppRoleInputDTO(5L, 7L);
        when(soffidClient.getRolesByIds(List.of(7L))).thenReturn(List.of(soffidRole(8L, "OTHER_ROLE", "soffid", "x")));

        assertThrows(BusinessRuleException.class, () -> appRoleServiceFacadeBean.create(inputDTO));
        verify(appRoleRepository, never()).create(any());
    }

    @Test
    void create_soffidTimesOut_propagatesAndPersistsNothing() {
        AppRoleInputDTO inputDTO = new AppRoleInputDTO(5L, 7L);
        when(soffidClient.getRolesByIds(List.of(7L))).thenThrow(new SoffidTimeoutException());

        assertThrows(SoffidTimeoutException.class, () -> appRoleServiceFacadeBean.create(inputDTO));
        verify(appRoleRepository, never()).create(any());
    }

    @Test
    void assign_savesTheDataOfTheRoleAsFetchedWithoutCallingSoffid() {
        SoffidRole soffidRole = soffidRole(100L, "AP2_ADMIN", "soffid", "Administrador");
        AppRole saved = new AppRole();
        AppRoleOutputDTO response = new AppRoleOutputDTO();
        when(appRoleRepository.create(any(AppRole.class))).thenReturn(saved);
        when(appRoleMapper.toResponse(saved)).thenReturn(response);

        AppRoleOutputDTO result = appRoleServiceFacadeBean.assign(55L, soffidRole);

        assertEquals(response, result);
        ArgumentCaptor<AppRole> persisted = ArgumentCaptor.forClass(AppRole.class);
        verify(appRoleRepository).create(persisted.capture());
        assertEquals(55L, persisted.getValue().getAppSecurity().getId());
        assertNull(persisted.getValue().getSecurityRole().getId());
        assertEquals(100L, persisted.getValue().getSecurityRole().getRoleId());
        assertEquals("AP2_ADMIN", persisted.getValue().getSecurityRole().getName());
        assertEquals("soffid", persisted.getValue().getSecurityRole().getSystem());
        assertEquals("Administrador", persisted.getValue().getSecurityRole().getDescription());
        verify(soffidClient, never()).getRolesByIds(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        AppRoleInputDTO inputDTO = new AppRoleInputDTO(5L, 7L);
        when(appRoleRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appRoleServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_APP_ROLE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_valid_rewritesTheDataOfTheSameSecurityRoleRow() {
        AppRoleInputDTO inputDTO = new AppRoleInputDTO(5L, 7L);
        AppRole updated = new AppRole();
        AppRoleOutputDTO response = new AppRoleOutputDTO();
        SecurityRole currentRole = new SecurityRole();
        currentRole.setId(3L);
        currentRole.setRoleId(6L);
        currentRole.setName("AP2_OLD");
        activeModel.setSecurityRole(currentRole);
        when(appRoleRepository.findById(1L)).thenReturn(activeModel);
        when(soffidClient.getRolesByIds(List.of(7L))).thenReturn(List.of(soffidRole(7L, "AP2_NEW", "soffid", "Nova")));
        when(appRoleRepository.update(activeModel, 1L)).thenReturn(updated);
        when(appRoleMapper.toResponse(updated)).thenReturn(response);

        AppRoleOutputDTO result = appRoleServiceFacadeBean.update(1L, inputDTO);

        verify(appRoleMapper).updateModelFromInput(inputDTO, activeModel);
        assertEquals(3L, activeModel.getSecurityRole().getId());
        assertEquals(7L, activeModel.getSecurityRole().getRoleId());
        assertEquals("AP2_NEW", activeModel.getSecurityRole().getName());
        assertEquals("soffid", activeModel.getSecurityRole().getSystem());
        assertEquals("Nova", activeModel.getSecurityRole().getDescription());
        assertEquals(response, result);
    }

    @Test
    void update_assignmentWithoutARole_savesANewRow() {
        AppRoleInputDTO inputDTO = new AppRoleInputDTO(5L, 7L);
        when(appRoleRepository.findById(1L)).thenReturn(activeModel);
        when(soffidClient.getRolesByIds(List.of(7L))).thenReturn(List.of(soffidRole(7L, "AP2_NEW", "soffid", "Nova")));
        when(appRoleRepository.update(activeModel, 1L)).thenReturn(new AppRole());
        when(appRoleMapper.toResponse(any(AppRole.class))).thenReturn(new AppRoleOutputDTO());

        appRoleServiceFacadeBean.update(1L, inputDTO);

        assertNull(activeModel.getSecurityRole().getId());
        assertEquals(7L, activeModel.getSecurityRole().getRoleId());
    }

    @Test
    void update_roleUnknownInSoffid_throwsAndPersistsNothing() {
        AppRoleInputDTO inputDTO = new AppRoleInputDTO(5L, 999L);
        when(appRoleRepository.findById(1L)).thenReturn(activeModel);
        when(soffidClient.getRolesByIds(List.of(999L))).thenReturn(List.of());

        assertThrows(BusinessRuleException.class, () -> appRoleServiceFacadeBean.update(1L, inputDTO));
        verify(appRoleRepository, never()).update(any(), any());
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

    // ------------------------------------------------------------------
    // assignByPrefix - the roles Soffid has for an application
    // ------------------------------------------------------------------

    private static SoffidRole prefixRole(Long id, String name) {
        return soffidRole(id, name, "soffid", "Descripcio " + name);
    }

    /** Soffid answers the role search with the given roles (in one page). */
    private void stubSoffidRoles(SoffidRole... roles) {
        when(soffidClient.searchRoles(any(), any())).thenReturn(new PageImpl<>(List.of(roles)));
    }

    /** The security anchor 55 already has active assignments to the given Soffid roles. */
    private void stubAssignedRoles(Long... soffidRoleIds) {
        List<AppRole> assigned = java.util.Arrays.stream(soffidRoleIds).map(id -> {
            SecurityRole securityRole = new SecurityRole();
            securityRole.setRoleId(id);
            AppRole appRole = new AppRole();
            appRole.setSecurityRole(securityRole);
            return appRole;
        }).toList();
        lenient().when(appRoleRepository.findAll(eq(55L), any(AppRoleCriteria.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(assigned));
    }

    /** Saving an assignment returns it as is, and its output carries the Soffid id of its role. */
    private void stubAssignments() {
        lenient().when(appRoleRepository.create(any(AppRole.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(appRoleMapper.toResponse(any(AppRole.class))).thenAnswer(invocation -> {
            AppRole appRole = invocation.getArgument(0);
            return AppRoleOutputDTO.builder().id(7L)
                    .securityRole(new SecurityRoleOutputDTO(null, appRole.getSecurityRole().getRoleId(), appRole.getSecurityRole().getName(), "soffid", null))
                    .validationStatus(appRole.getValidationStatus())
                    .build();
        });
    }

    private static List<Long> soffidIdsOf(List<AppRoleOutputDTO> assignments) {
        return assignments.stream().map(assignment -> assignment.getSecurityRole().getRoleId()).toList();
    }

    @Test
    void assignByPrefix_soffidHasRolesNamedAfterThePrefix_assignsEachOneAsSoffidReportedIt() {
        stubAssignedRoles();
        stubAssignments();
        stubSoffidRoles(prefixRole(100L, "AP2_ADMIN"), prefixRole(200L, "AP2_USER"));

        List<AppRoleOutputDTO> result = appRoleServiceFacadeBean.assignByPrefix(55L, "AP2");

        assertEquals(List.of(100L, 200L), soffidIdsOf(result));
        ArgumentCaptor<AppRole> persisted = ArgumentCaptor.forClass(AppRole.class);
        verify(appRoleRepository, times(2)).create(persisted.capture());
        assertEquals(List.of(55L, 55L), persisted.getAllValues().stream().map(appRole -> appRole.getAppSecurity().getId()).toList());
        assertEquals(List.of("AP2_ADMIN", "AP2_USER"), persisted.getAllValues().stream().map(appRole -> appRole.getSecurityRole().getName()).toList());
    }

    @Test
    void assignByPrefix_asksSoffidForTheRolesStartingWithThePrefixAndAnUnderscore() {
        stubAssignedRoles();
        stubSoffidRoles();

        appRoleServiceFacadeBean.assignByPrefix(55L, "AP2");

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(soffidClient).searchRoles(eq("AP2_"), pageable.capture());
        assertTrue(pageable.getValue().isPaged());
    }

    @Test
    void assignByPrefix_looksOnlyAtTheActiveAssignmentsOfTheSecurityAnchor() {
        stubAssignedRoles();
        stubSoffidRoles();

        appRoleServiceFacadeBean.assignByPrefix(55L, "AP2");

        ArgumentCaptor<AppRoleCriteria> criteria = ArgumentCaptor.forClass(AppRoleCriteria.class);
        verify(appRoleRepository).findAll(eq(55L), criteria.capture(), any(Pageable.class));
        assertEquals(StatusEnum.ACTIVE.getId(), criteria.getValue().getStatusId());
    }

    @Test
    void assignByPrefix_soffidMatchesTheTextAnywhere_onlyTheNamesStartingWithItAreAssigned() {
        stubAssignedRoles();
        stubAssignments();
        stubSoffidRoles(prefixRole(100L, "AP2_ADMIN"), prefixRole(200L, "XAP2_ADMIN"), prefixRole(300L, "OTRO_AP2_USER"),
                prefixRole(350L, "ap2_lectura"), prefixRole(400L, "AP2_USER"), prefixRole(500L, null));

        List<AppRoleOutputDTO> result = appRoleServiceFacadeBean.assignByPrefix(55L, "AP2");

        assertEquals(List.of(100L, 400L), soffidIdsOf(result));
    }

    @Test
    void assignByPrefix_aFullPageOfRoles_isFollowedByTheNextOne() {
        stubAssignedRoles();
        stubAssignments();
        List<SoffidRole> firstPage = java.util.stream.IntStream.rangeClosed(1, 100)
                .mapToObj(i -> prefixRole((long) i, "AP2_R" + i)).toList();
        when(soffidClient.searchRoles(any(), any()))
                .thenReturn(new PageImpl<>(firstPage), new PageImpl<>(List.of(prefixRole(101L, "AP2_R101"))));

        List<AppRoleOutputDTO> result = appRoleServiceFacadeBean.assignByPrefix(55L, "AP2");

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(soffidClient, times(2)).searchRoles(eq("AP2_"), pageable.capture());
        assertEquals(List.of(0, 1), pageable.getAllValues().stream().map(Pageable::getPageNumber).toList());
        assertEquals(101, result.size());
    }

    @Test
    void assignByPrefix_aRoleRepeatedInSoffidsAnswer_isAssignedOnce() {
        stubAssignedRoles();
        stubAssignments();
        stubSoffidRoles(prefixRole(100L, "AP2_ADMIN"), prefixRole(100L, "AP2_ADMIN"));

        List<AppRoleOutputDTO> result = appRoleServiceFacadeBean.assignByPrefix(55L, "AP2");

        assertEquals(List.of(100L), soffidIdsOf(result));
    }

    @Test
    void assignByPrefix_someRolesAreAlreadyAssigned_onlyTheNewOnesAreAssigned() {
        stubAssignedRoles(100L);
        stubAssignments();
        stubSoffidRoles(prefixRole(100L, "AP2_ADMIN"), prefixRole(200L, "AP2_USER"));

        List<AppRoleOutputDTO> result = appRoleServiceFacadeBean.assignByPrefix(55L, "AP2");

        assertEquals(List.of(200L), soffidIdsOf(result));
        verify(appRoleRepository, times(1)).create(any(AppRole.class));
    }

    @Test
    void assignByPrefix_calledAgainWithEveryRoleAlreadyAssigned_assignsNothing() {
        stubAssignedRoles(100L, 200L);
        stubAssignments();
        stubSoffidRoles(prefixRole(100L, "AP2_ADMIN"), prefixRole(200L, "AP2_USER"));

        List<AppRoleOutputDTO> result = appRoleServiceFacadeBean.assignByPrefix(55L, "AP2");

        assertTrue(result.isEmpty());
        verify(appRoleRepository, never()).create(any());
    }

    @Test
    void assignByPrefix_soffidHasNoRolesForThePrefix_assignsNothing() {
        stubAssignedRoles();
        stubSoffidRoles();

        List<AppRoleOutputDTO> result = appRoleServiceFacadeBean.assignByPrefix(55L, "AP2");

        assertTrue(result.isEmpty());
        verify(appRoleRepository, never()).create(any());
    }

    @Test
    void assignByPrefix_soffidTimesOut_theTimeoutPropagatesAndNothingIsAssigned() {
        when(soffidClient.searchRoles(any(), any())).thenThrow(new SoffidTimeoutException());

        assertThrows(SoffidTimeoutException.class, () -> appRoleServiceFacadeBean.assignByPrefix(55L, "AP2"));

        verify(appRoleRepository, never()).create(any());
    }

    @Test
    void assignByPrefix_soffidFails_throwsTheRolesUnavailableErrorAndNothingIsAssigned() {
        when(soffidClient.searchRoles(any(), any())).thenThrow(new SoffidClientException("502"));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appRoleServiceFacadeBean.assignByPrefix(55L, "AP2"));

        assertEquals(Constants.ERR_APP_SOFFID_ROLES_UNAVAILABLE, ex.getMessage());
        verify(appRoleRepository, never()).create(any());
    }

    @Test
    void assignByPrefix_soffidFailsOnALaterPage_nothingIsAssigned() {
        List<SoffidRole> firstPage = java.util.stream.IntStream.rangeClosed(1, 100)
                .mapToObj(i -> prefixRole((long) i, "AP2_R" + i)).toList();
        when(soffidClient.searchRoles(any(), any()))
                .thenReturn(new PageImpl<>(firstPage)).thenThrow(new SoffidClientException("502"));

        assertThrows(BusinessRuleException.class, () -> appRoleServiceFacadeBean.assignByPrefix(55L, "AP2"));

        verify(appRoleRepository, never()).create(any());
    }

    @Test
    void assignByPrefix_aFailureWhileAssigningARole_isNotSwallowed() {
        stubAssignedRoles();
        stubSoffidRoles(prefixRole(100L, "AP2_ADMIN"));
        when(appRoleRepository.create(any(AppRole.class))).thenThrow(new IllegalStateException("database down"));

        assertThrows(IllegalStateException.class, () -> appRoleServiceFacadeBean.assignByPrefix(55L, "AP2"));
    }


    // ------------------------------------------------------------------
    // Validation state against the application's prefix
    // ------------------------------------------------------------------

    private AppRole createdWith(String soffidRoleName) {
        when(soffidClient.getRolesByIds(List.of(7L))).thenReturn(List.of(soffidRole(7L, soffidRoleName, "soffid", "Rol")));
        when(appRoleRepository.create(any(AppRole.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(appRoleMapper.toResponse(any(AppRole.class))).thenReturn(new AppRoleOutputDTO());

        appRoleServiceFacadeBean.create(new AppRoleInputDTO(5L, 7L));

        ArgumentCaptor<AppRole> persisted = ArgumentCaptor.forClass(AppRole.class);
        verify(appRoleRepository).create(persisted.capture());
        return persisted.getValue();
    }

    @Test
    void create_roleMatchingThePrefix_isSavedValidated() {
        AppRole persisted = createdWith("AP2_ADMIN");

        assertEquals(ValidationStatusEnum.VALIDATED, persisted.getValidationStatus());
        assertNull(persisted.getManualValidatedAt());
        assertNull(persisted.getManualValidatedBy());
    }

    @Test
    void create_roleNotMatchingThePrefix_addedByHandIsSavedManual() {
        AppRole persisted = createdWith("OTHER_ADMIN");

        assertEquals(ValidationStatusEnum.MANUAL, persisted.getValidationStatus());
        assertNotNull(persisted.getManualValidatedAt());
        assertNotNull(persisted.getManualValidatedBy());
    }

    @Test
    void create_prefixMatchingIsCaseSensitive() {
        assertEquals(ValidationStatusEnum.MANUAL, createdWith("ap2_admin").getValidationStatus());
    }

    @Test
    void create_nameOnlyContainingThePrefixInTheMiddle_doesNotMatch() {
        assertEquals(ValidationStatusEnum.MANUAL, createdWith("XAP2_ADMIN").getValidationStatus());
    }

    @Test
    void assign_applicationWithoutPrefix_isSavedNotApply() {
        when(appSecurityRepository.findById(55L)).thenReturn(appSecurityOf(null));
        when(appRoleRepository.create(any(AppRole.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(appRoleMapper.toResponse(any(AppRole.class))).thenReturn(new AppRoleOutputDTO());

        appRoleServiceFacadeBean.assign(55L, soffidRole(100L, "AP2_ADMIN", "soffid", "Administrador"));

        ArgumentCaptor<AppRole> persisted = ArgumentCaptor.forClass(AppRole.class);
        verify(appRoleRepository).create(persisted.capture());
        assertEquals(ValidationStatusEnum.NOT_APPLY, persisted.getValue().getValidationStatus());
    }

    @Test
    void assign_automaticAssignmentOfARoleNotMatchingThePrefix_isSavedNotValidatedNeverManual() {
        when(appSecurityRepository.findById(55L)).thenReturn(appSecurityOf("ZZZ"));
        when(appRoleRepository.create(any(AppRole.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(appRoleMapper.toResponse(any(AppRole.class))).thenReturn(new AppRoleOutputDTO());

        appRoleServiceFacadeBean.assign(55L, soffidRole(100L, "AP2_ADMIN", "soffid", "Administrador"));

        ArgumentCaptor<AppRole> persisted = ArgumentCaptor.forClass(AppRole.class);
        verify(appRoleRepository).create(persisted.capture());
        assertEquals(ValidationStatusEnum.NOT_VALIDATED, persisted.getValue().getValidationStatus());
        assertNull(persisted.getValue().getManualValidatedAt());
    }

    @Test
    void assignByPrefix_assignedRolesAreSavedValidatedWithoutLookingTheAnchorUp() {
        stubAssignedRoles();
        stubAssignments();
        stubSoffidRoles(prefixRole(100L, "AP2_ADMIN"));

        appRoleServiceFacadeBean.assignByPrefix(55L, "AP2");

        ArgumentCaptor<AppRole> persisted = ArgumentCaptor.forClass(AppRole.class);
        verify(appRoleRepository).create(persisted.capture());
        assertEquals(ValidationStatusEnum.VALIDATED, persisted.getValue().getValidationStatus());
        verify(appSecurityRepository, never()).findById(any());
    }

    @Test
    void update_roleNotMatchingThePrefix_becomesManualDroppingTheEarlierValidation() {
        activeModel.setValidationStatus(ValidationStatusEnum.NOT_VALIDATED);
        when(appRoleRepository.findById(1L)).thenReturn(activeModel);
        when(soffidClient.getRolesByIds(List.of(7L))).thenReturn(List.of(soffidRole(7L, "OTHER_NEW", "soffid", "Nova")));
        when(appRoleRepository.update(activeModel, 1L)).thenReturn(new AppRole());
        when(appRoleMapper.toResponse(any(AppRole.class))).thenReturn(new AppRoleOutputDTO());

        appRoleServiceFacadeBean.update(1L, new AppRoleInputDTO(5L, 7L));

        assertEquals(ValidationStatusEnum.MANUAL, activeModel.getValidationStatus());
        assertNotNull(activeModel.getManualValidatedAt());
    }

    @Test
    void update_roleMatchingThePrefix_clearsAnEarlierManualValidation() {
        activeModel.setValidationStatus(ValidationStatusEnum.MANUAL);
        activeModel.setManualValidatedAt(LocalDateTime.now());
        activeModel.setManualValidatedBy("jdoe");
        when(appRoleRepository.findById(1L)).thenReturn(activeModel);
        when(soffidClient.getRolesByIds(List.of(7L))).thenReturn(List.of(soffidRole(7L, "AP2_NEW", "soffid", "Nova")));
        when(appRoleRepository.update(activeModel, 1L)).thenReturn(new AppRole());
        when(appRoleMapper.toResponse(any(AppRole.class))).thenReturn(new AppRoleOutputDTO());

        appRoleServiceFacadeBean.update(1L, new AppRoleInputDTO(5L, 7L));

        assertEquals(ValidationStatusEnum.VALIDATED, activeModel.getValidationStatus());
        assertNull(activeModel.getManualValidatedAt());
        assertNull(activeModel.getManualValidatedBy());
    }

    @Test
    void validate_notFound_throwsBusinessRuleException() {
        when(appRoleRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appRoleServiceFacadeBean.validate(99L));

        assertEquals(Constants.ERR_APP_ROLE_NOT_FOUND, ex.getMessage());
        verify(appRoleRepository, never()).update(any(), any());
    }

    @Test
    void validate_inactive_throwsBusinessRuleException() {
        activeModel.setValidationStatus(ValidationStatusEnum.NOT_VALIDATED);
        activeModel.setDeletedAt(LocalDateTime.now());
        when(appRoleRepository.findById(1L)).thenReturn(activeModel);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appRoleServiceFacadeBean.validate(1L));

        assertEquals(Constants.ERR_APP_ROLE_NOT_ACTIVE, ex.getMessage());
        verify(appRoleRepository, never()).update(any(), any());
    }

    @Test
    void validate_anyStateButNotValidated_isRefused() {
        for (ValidationStatusEnum status : List.of(ValidationStatusEnum.VALIDATED, ValidationStatusEnum.MANUAL, ValidationStatusEnum.NOT_APPLY)) {
            activeModel.setValidationStatus(status);
            when(appRoleRepository.findById(1L)).thenReturn(activeModel);

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> appRoleServiceFacadeBean.validate(1L), status.name());

            assertEquals(Constants.ERR_APP_ROLE_MANUAL_VALIDATION_NOT_ALLOWED, ex.getMessage());
        }
        verify(appRoleRepository, never()).update(any(), any());
    }

    @Test
    void validate_notValidated_becomesManualStamped() {
        activeModel.setValidationStatus(ValidationStatusEnum.NOT_VALIDATED);
        when(appRoleRepository.findById(1L)).thenReturn(activeModel);

        appRoleServiceFacadeBean.validate(1L);

        assertEquals(ValidationStatusEnum.MANUAL, activeModel.getValidationStatus());
        assertNotNull(activeModel.getManualValidatedAt());
        assertNotNull(activeModel.getManualValidatedBy());
        verify(appRoleRepository).update(activeModel, 1L);
    }

    // ------------------------------------------------------------------
    // Recalculation when the application's prefix changes
    // ------------------------------------------------------------------

    private static AppRole assignedRole(Long id, String roleName, ValidationStatusEnum status) {
        SecurityRole securityRole = new SecurityRole();
        securityRole.setName(roleName);
        AppRole appRole = new AppRole();
        appRole.setId(id);
        appRole.setSecurityRole(securityRole);
        appRole.setValidationStatus(status);
        return appRole;
    }

    private void stubActiveRoles(AppRole... roles) {
        when(appRoleRepository.findAll(eq(55L), any(AppRoleCriteria.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(roles)));
    }

    @Test
    void recalculateValidation_roleNowMatchingThePrefix_becomesValidated() {
        AppRole role = assignedRole(1L, "NEW_ADMIN", ValidationStatusEnum.NOT_VALIDATED);
        stubActiveRoles(role);

        appRoleServiceFacadeBean.recalculateValidation(55L, "NEW");

        assertEquals(ValidationStatusEnum.VALIDATED, role.getValidationStatus());
        verify(appRoleRepository).update(role, 1L);
    }

    @Test
    void recalculateValidation_roleNoLongerMatchingThePrefix_becomesNotValidated() {
        AppRole role = assignedRole(1L, "OLD_ADMIN", ValidationStatusEnum.VALIDATED);
        stubActiveRoles(role);

        appRoleServiceFacadeBean.recalculateValidation(55L, "NEW");

        assertEquals(ValidationStatusEnum.NOT_VALIDATED, role.getValidationStatus());
        verify(appRoleRepository).update(role, 1L);
    }

    @Test
    void recalculateValidation_manualRole_isLeftAsItIs() {
        AppRole role = assignedRole(1L, "OLD_ADMIN", ValidationStatusEnum.MANUAL);
        stubActiveRoles(role);

        appRoleServiceFacadeBean.recalculateValidation(55L, "NEW");

        assertEquals(ValidationStatusEnum.MANUAL, role.getValidationStatus());
        verify(appRoleRepository, never()).update(any(), any());
    }

    @Test
    void recalculateValidation_stateUnchanged_isNotPersistedAgain() {
        stubActiveRoles(assignedRole(1L, "NEW_ADMIN", ValidationStatusEnum.VALIDATED),
                assignedRole(2L, "OLD_ADMIN", ValidationStatusEnum.NOT_VALIDATED));

        appRoleServiceFacadeBean.recalculateValidation(55L, "NEW");

        verify(appRoleRepository, never()).update(any(), any());
    }

    @Test
    void recalculateValidation_looksOnlyAtTheActiveAssignmentsAndNeverCallsSoffid() {
        stubActiveRoles();

        appRoleServiceFacadeBean.recalculateValidation(55L, "NEW");

        ArgumentCaptor<AppRoleCriteria> criteria = ArgumentCaptor.forClass(AppRoleCriteria.class);
        verify(appRoleRepository).findAll(eq(55L), criteria.capture(), any(Pageable.class));
        assertEquals(1L, criteria.getValue().getStatusId());
        verify(soffidClient, never()).searchRoles(any(), any());
        verify(soffidClient, never()).getRolesByIds(any());
    }
}
