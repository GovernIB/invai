package es.caib.invai.back.ejb.maintenance.responsible.roleTransfer;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.catalog.responsibleType.DTO.ResponsibleTypeOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO.AuthorizationTypeOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO.RoleAssignmentOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO.RoleTransferInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO.RoleTransferItemDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.type.AppAuthorizedTypeLinkRepository;
import es.caib.invai.back.persistence.repository.maintenance.responsible.authorizationType.AuthorizationTypeRepository;
import es.caib.invai.back.persistence.repository.maintenance.responsible.person.PersonRepository;
import es.caib.invai.back.rest.soffid.SoffidClient;
import es.caib.invai.back.rest.soffid.SoffidUser;
import es.caib.invai.back.service.mapper.catalog.responsibleType.ResponsibleTypeMapper;
import es.caib.invai.back.service.mapper.maintenance.responsible.authorizationType.AuthorizationTypeMapper;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import es.caib.invai.back.service.model.maintenance.responsible.roleTransfer.RoleAssignmentType;
import es.caib.invai.back.service.model.application.responsibleAuthorized.type.AppAuthorizedTypeLink;
import es.caib.invai.back.service.model.catalog.responsibleType.ResponsibleType;
import es.caib.invai.back.service.model.maintenance.responsible.authorizationType.AuthorizationType;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RoleTransferServiceFacadeBean}, covering the person-scoped assignment
 * listing and the transfer/revoke fan-out across {@link AppResponsibleRepository} and
 * {@link AppAuthorizedRepository}, with all collaborators mocked.
 */
@ExtendWith(MockitoExtension.class)
class RoleTransferServiceFacadeBeanTest {

    @Mock
    private AppResponsibleRepository appResponsibleRepository;

    @Mock
    private AppAuthorizedRepository appAuthorizedRepository;

    @Mock
    private AppAuthorizedTypeLinkRepository appAuthorizedTypeLinkRepository;

    @Mock
    private AuthorizationTypeRepository authorizationTypeRepository;

    @Mock
    private AuthorizationTypeMapper authorizationTypeMapper;

    @Mock
    private ResponsibleTypeMapper responsibleTypeMapper;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private SoffidClient soffidClient;

    @InjectMocks
    private RoleTransferServiceFacadeBean roleTransferServiceFacadeBean;

    private AppResponsibleAuthorized anchor;

    private static final String TARGET_EMAIL = "target@example.com";

    @BeforeEach
    void setUp() {
        Application application = new Application();
        application.setId(30L);
        application.setName("Application One");
        anchor = AppResponsibleAuthorized.builder().id(40L).application(application).build();
    }

    /**
     * Stubs {@code personRepository.findByEmail(TARGET_EMAIL)} to resolve to an existing local
     * person with the given ID, so the transfer's target-resolution step succeeds without touching
     * Soffid.
     */
    private void stubExistingTargetPerson() {
        Person person = new Person();
        person.setId(20L);
        when(personRepository.findByEmail(TARGET_EMAIL)).thenReturn(person);
    }

    // ------------------------------------------------------------------
    // getAssignmentsByPerson
    // ------------------------------------------------------------------

    @Test
    void getAssignmentsByPerson_combinesResponsibleAndAuthorizedRows() {
        ResponsibleType responsibleType = new ResponsibleType();
        responsibleType.setName("Responsable de Servei");
        AppResponsible responsible = AppResponsible.builder()
                .id(1L)
                .appResponsibleAuthorized(anchor)
                .responsibleType(responsibleType)
                .build();
        ResponsibleTypeOutputDTO responsibleTypeOutputDTO =
                new ResponsibleTypeOutputDTO(6L, "Responsable de Servei", "Responsable de Servicio", false);
        when(responsibleTypeMapper.toResponse(responsibleType)).thenReturn(responsibleTypeOutputDTO);

        AppAuthorized authorized = AppAuthorized.builder()
                .id(2L)
                .appResponsibleAuthorized(anchor)
                .build();

        when(appResponsibleRepository.findAllActiveByPersonId(10L)).thenReturn(List.of(responsible));
        when(appAuthorizedRepository.findAllActiveByPersonId(10L)).thenReturn(List.of(authorized));

        AppAuthorizedTypeLink link = AppAuthorizedTypeLink.builder().appAuthorizedId(2L).authorizationTypeId(5L).build();
        when(appAuthorizedTypeLinkRepository.findAllByAppAuthorizedId(2L)).thenReturn(List.of(link));
        AuthorizationType authorizationType = new AuthorizationType();
        authorizationType.setName("Firmar peticiones");
        when(authorizationTypeRepository.findById(5L)).thenReturn(authorizationType);
        AuthorizationTypeOutputDTO authorizationTypeOutputDTO =
                new AuthorizationTypeOutputDTO(5L, "Firmar peticiones", "Signar peticions", null);
        when(authorizationTypeMapper.toResponse(authorizationType)).thenReturn(authorizationTypeOutputDTO);

        List<RoleAssignmentOutputDTO> result = roleTransferServiceFacadeBean.getAssignmentsByPerson(10L);

        assertEquals(2, result.size());

        RoleAssignmentOutputDTO responsibleDto = result.get(0);
        assertEquals(1L, responsibleDto.getId());
        assertEquals(RoleAssignmentType.RESPONSIBLE, responsibleDto.getType());
        assertEquals(30L, responsibleDto.getApplicationId());
        assertEquals("Application One", responsibleDto.getApplicationName());
        assertEquals(responsibleTypeOutputDTO, responsibleDto.getResponsibleType());

        RoleAssignmentOutputDTO authorizedDto = result.get(1);
        assertEquals(2L, authorizedDto.getId());
        assertEquals(RoleAssignmentType.AUTHORIZED, authorizedDto.getType());
        assertEquals(30L, authorizedDto.getApplicationId());
        assertEquals("Application One", authorizedDto.getApplicationName());
        assertEquals(List.of(authorizationTypeOutputDTO), authorizedDto.getAuthorizationTypes());
    }

    @Test
    void getAssignmentsByPerson_noAssignments_returnsEmptyList() {
        when(appResponsibleRepository.findAllActiveByPersonId(99L)).thenReturn(List.of());
        when(appAuthorizedRepository.findAllActiveByPersonId(99L)).thenReturn(List.of());

        assertEquals(List.of(), roleTransferServiceFacadeBean.getAssignmentsByPerson(99L));
    }

    // ------------------------------------------------------------------
    // transfer - validation
    // ------------------------------------------------------------------

    @Test
    void transfer_noTargetPersonAndNotRevoking_throwsBusinessRuleException() {
        RoleTransferInputDTO inputDTO = new RoleTransferInputDTO(
                List.of(new RoleTransferItemDTO(1L, RoleAssignmentType.RESPONSIBLE)), null, false);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> roleTransferServiceFacadeBean.transfer(inputDTO));
        assertEquals(Constants.ERR_ROLETRANSFER_TARGET_REQUIRED, ex.getMessage());
        verify(appResponsibleRepository, never()).findById(any());
    }

    // ------------------------------------------------------------------
    // transfer - RESPONSIBLE items
    // ------------------------------------------------------------------

    @Test
    void transfer_responsibleItem_notFound_throwsBusinessRuleException() {
        stubExistingTargetPerson();
        when(appResponsibleRepository.findById(99L)).thenReturn(null);
        RoleTransferInputDTO inputDTO = new RoleTransferInputDTO(
                List.of(new RoleTransferItemDTO(99L, RoleAssignmentType.RESPONSIBLE)), TARGET_EMAIL, false);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> roleTransferServiceFacadeBean.transfer(inputDTO));
        assertEquals(Constants.ERR_APPRESPONSIBLE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void transfer_responsibleItem_alreadyInactive_throwsBusinessRuleException() {
        stubExistingTargetPerson();
        AppResponsible existing = AppResponsible.builder().id(1L).deletedAt(LocalDateTime.now()).build();
        when(appResponsibleRepository.findById(1L)).thenReturn(existing);
        RoleTransferInputDTO inputDTO = new RoleTransferInputDTO(
                List.of(new RoleTransferItemDTO(1L, RoleAssignmentType.RESPONSIBLE)), TARGET_EMAIL, false);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> roleTransferServiceFacadeBean.transfer(inputDTO));
        assertEquals(Constants.ERR_APPRESPONSIBLE_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void transfer_responsibleItem_reassign_updatesPersonIdOnly() {
        stubExistingTargetPerson();
        Person person = new Person();
        person.setId(10L);
        AppResponsible existing = AppResponsible.builder().id(1L).person(person).build();
        when(appResponsibleRepository.findById(1L)).thenReturn(existing);
        RoleTransferInputDTO inputDTO = new RoleTransferInputDTO(
                List.of(new RoleTransferItemDTO(1L, RoleAssignmentType.RESPONSIBLE)), TARGET_EMAIL, false);

        roleTransferServiceFacadeBean.transfer(inputDTO);

        assertEquals(20L, existing.getPerson().getId());
        verify(appResponsibleRepository).update(existing, 1L);
        verify(appResponsibleRepository, never()).delete(any());
    }

    @Test
    void transfer_responsibleItem_revoke_softDeletes() {
        AppResponsible existing = AppResponsible.builder().id(1L).build();
        when(appResponsibleRepository.findById(1L)).thenReturn(existing);
        RoleTransferInputDTO inputDTO = new RoleTransferInputDTO(
                List.of(new RoleTransferItemDTO(1L, RoleAssignmentType.RESPONSIBLE)), null, true);

        roleTransferServiceFacadeBean.transfer(inputDTO);

        assertNotNull(existing.getDeletedAt());
        assertNotNull(existing.getDeletedBy());
        verify(appResponsibleRepository).delete(existing);
        verify(appResponsibleRepository, never()).update(any(), any());
        verify(personRepository, never()).findByEmail(any());
        verify(soffidClient, never()).findByEmail(any());
    }

    // ------------------------------------------------------------------
    // transfer - AUTHORIZED items
    // ------------------------------------------------------------------

    @Test
    void transfer_authorizedItem_notFound_throwsBusinessRuleException() {
        stubExistingTargetPerson();
        when(appAuthorizedRepository.findById(99L)).thenReturn(null);
        RoleTransferInputDTO inputDTO = new RoleTransferInputDTO(
                List.of(new RoleTransferItemDTO(99L, RoleAssignmentType.AUTHORIZED)), TARGET_EMAIL, false);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> roleTransferServiceFacadeBean.transfer(inputDTO));
        assertEquals(Constants.ERR_APPAUTHORIZED_NOT_FOUND, ex.getMessage());
    }

    @Test
    void transfer_authorizedItem_alreadyInactive_throwsBusinessRuleException() {
        stubExistingTargetPerson();
        AppAuthorized existing = AppAuthorized.builder().id(2L).deletedAt(LocalDateTime.now()).build();
        when(appAuthorizedRepository.findById(2L)).thenReturn(existing);
        RoleTransferInputDTO inputDTO = new RoleTransferInputDTO(
                List.of(new RoleTransferItemDTO(2L, RoleAssignmentType.AUTHORIZED)), TARGET_EMAIL, false);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> roleTransferServiceFacadeBean.transfer(inputDTO));
        assertEquals(Constants.ERR_APPAUTHORIZED_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void transfer_authorizedItem_targetAlreadyHoldsAnchor_throwsBusinessRuleException() {
        stubExistingTargetPerson();
        AppAuthorized existing = AppAuthorized.builder().id(2L).appResponsibleAuthorized(anchor).build();
        when(appAuthorizedRepository.findById(2L)).thenReturn(existing);
        when(appAuthorizedRepository.existsByAppResponsibleAuthorizedAndPersonAndIdNot(40L, 20L, 2L)).thenReturn(true);
        RoleTransferInputDTO inputDTO = new RoleTransferInputDTO(
                List.of(new RoleTransferItemDTO(2L, RoleAssignmentType.AUTHORIZED)), TARGET_EMAIL, false);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> roleTransferServiceFacadeBean.transfer(inputDTO));
        assertEquals(Constants.ERR_APPAUTHORIZED_DUPLICATED, ex.getMessage());
        verify(appAuthorizedRepository, never()).update(any(), any());
    }

    @Test
    void transfer_authorizedItem_reassign_updatesPersonIdOnly() {
        stubExistingTargetPerson();
        Person person = new Person();
        person.setId(11L);
        AppAuthorized existing = AppAuthorized.builder().id(2L).appResponsibleAuthorized(anchor).person(person).build();
        when(appAuthorizedRepository.findById(2L)).thenReturn(existing);
        when(appAuthorizedRepository.existsByAppResponsibleAuthorizedAndPersonAndIdNot(40L, 20L, 2L)).thenReturn(false);
        RoleTransferInputDTO inputDTO = new RoleTransferInputDTO(
                List.of(new RoleTransferItemDTO(2L, RoleAssignmentType.AUTHORIZED)), TARGET_EMAIL, false);

        roleTransferServiceFacadeBean.transfer(inputDTO);

        assertEquals(20L, existing.getPerson().getId());
        verify(appAuthorizedRepository).update(existing, 2L);
    }

    @Test
    void transfer_authorizedItem_revoke_softDeletes() {
        AppAuthorized existing = AppAuthorized.builder().id(2L).build();
        when(appAuthorizedRepository.findById(2L)).thenReturn(existing);
        RoleTransferInputDTO inputDTO = new RoleTransferInputDTO(
                List.of(new RoleTransferItemDTO(2L, RoleAssignmentType.AUTHORIZED)), null, true);

        roleTransferServiceFacadeBean.transfer(inputDTO);

        assertNotNull(existing.getDeletedAt());
        assertNotNull(existing.getDeletedBy());
        verify(appAuthorizedRepository).delete(existing);
        verify(appAuthorizedRepository, never()).update(any(), any());
        verify(appAuthorizedRepository, never()).existsByAppResponsibleAuthorizedAndPersonAndIdNot(any(), any(), any());
    }

    // ------------------------------------------------------------------
    // transfer - mixed batch
    // ------------------------------------------------------------------

    @Test
    void transfer_mixedBatch_processesEachItemByItsOwnType() {
        stubExistingTargetPerson();
        Person responsiblePerson = new Person();
        responsiblePerson.setId(10L);
        AppResponsible responsible = AppResponsible.builder().id(1L).person(responsiblePerson).build();
        when(appResponsibleRepository.findById(1L)).thenReturn(responsible);

        Person authorizedPerson = new Person();
        authorizedPerson.setId(11L);
        AppAuthorized authorized = AppAuthorized.builder().id(2L).appResponsibleAuthorized(anchor).person(authorizedPerson).build();
        when(appAuthorizedRepository.findById(2L)).thenReturn(authorized);
        when(appAuthorizedRepository.existsByAppResponsibleAuthorizedAndPersonAndIdNot(40L, 20L, 2L)).thenReturn(false);

        RoleTransferInputDTO inputDTO = new RoleTransferInputDTO(
                List.of(new RoleTransferItemDTO(1L, RoleAssignmentType.RESPONSIBLE),
                        new RoleTransferItemDTO(2L, RoleAssignmentType.AUTHORIZED)),
                TARGET_EMAIL, false);

        roleTransferServiceFacadeBean.transfer(inputDTO);

        assertEquals(20L, responsible.getPerson().getId());
        assertEquals(20L, authorized.getPerson().getId());
        verify(appResponsibleRepository).update(responsible, 1L);
        verify(appAuthorizedRepository).update(authorized, 2L);
    }

    // ------------------------------------------------------------------
    // transfer - target person resolution by e-mail
    // ------------------------------------------------------------------

    @Test
    void transfer_targetNotFoundLocallyButFoundInSoffid_createsLocalPersonAndTransfers() {
        when(personRepository.findByEmail(TARGET_EMAIL)).thenReturn(null);
        SoffidUser soffidUser = new SoffidUser();
        soffidUser.setFirstName("Joan");
        soffidUser.setLastName("Fuster");
        soffidUser.setEmailAddress(TARGET_EMAIL);
        soffidUser.setActive(true);
        when(soffidClient.findByEmail(TARGET_EMAIL)).thenReturn(soffidUser);
        Person created = new Person();
        created.setId(50L);
        when(personRepository.create(any(Person.class))).thenReturn(created);

        Person existingPerson = new Person();
        existingPerson.setId(10L);
        AppResponsible existing = AppResponsible.builder().id(1L).person(existingPerson).build();
        when(appResponsibleRepository.findById(1L)).thenReturn(existing);

        RoleTransferInputDTO inputDTO = new RoleTransferInputDTO(
                List.of(new RoleTransferItemDTO(1L, RoleAssignmentType.RESPONSIBLE)), TARGET_EMAIL, false);

        roleTransferServiceFacadeBean.transfer(inputDTO);

        assertEquals(50L, existing.getPerson().getId());
        verify(personRepository).create(argThat(person ->
                "Joan".equals(person.getFirstName())
                        && "Fuster".equals(person.getLastName())
                        && TARGET_EMAIL.equals(person.getEmail())
                        && person.isPersonalCaib()));
    }

    @Test
    void transfer_targetNotFoundLocallyNorInSoffid_throwsBusinessRuleException() {
        when(personRepository.findByEmail(TARGET_EMAIL)).thenReturn(null);
        when(soffidClient.findByEmail(TARGET_EMAIL)).thenReturn(null);

        RoleTransferInputDTO inputDTO = new RoleTransferInputDTO(
                List.of(new RoleTransferItemDTO(1L, RoleAssignmentType.RESPONSIBLE)), TARGET_EMAIL, false);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> roleTransferServiceFacadeBean.transfer(inputDTO));
        assertEquals(Constants.ERR_ROLETRANSFER_TARGET_NOT_FOUND, ex.getMessage());
        verify(appResponsibleRepository, never()).findById(any());
        verify(personRepository, never()).create(any());
    }
}
