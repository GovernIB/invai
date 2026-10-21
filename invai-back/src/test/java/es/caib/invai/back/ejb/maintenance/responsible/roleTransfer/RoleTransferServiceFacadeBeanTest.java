package es.caib.invai.back.ejb.maintenance.responsible.roleTransfer;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonDir3CheckOutputDTO;
import es.caib.invai.back.interna.catalog.responsibleType.DTO.ResponsibleTypeOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO.AuthorizationTypeOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO.RoleAssignmentOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO.RoleTransferInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO.RoleTransferItemDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.dir3.Dir3ValidationRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.type.AppAuthorizedTypeLinkRepository;
import es.caib.invai.back.persistence.repository.maintenance.responsible.authorizationType.AuthorizationTypeRepository;
import es.caib.invai.back.persistence.repository.maintenance.responsible.person.PersonRepository;
import es.caib.invai.back.rest.soffid.SoffidClient;
import es.caib.invai.back.rest.soffid.SoffidUser;
import es.caib.invai.back.service.facade.application.responsibleAuthorized.authorized.AppAuthorizedService;
import es.caib.invai.back.service.facade.application.responsibleAuthorized.responsible.AppResponsibleService;
import es.caib.invai.back.service.facade.maintenance.responsible.person.PersonService;
import es.caib.invai.back.service.mapper.maintenance.responsible.authorizationType.AuthorizationTypeMapper;
import es.caib.invai.back.service.mapper.maintenance.responsible.roleTransfer.RoleAssignmentMapper;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.dir3.Dir3Validation;
import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import es.caib.invai.back.service.model.maintenance.responsible.roleTransfer.RoleAssignmentType;
import es.caib.invai.back.service.model.application.responsibleAuthorized.type.AppAuthorizedTypeLink;
import es.caib.invai.back.service.model.catalog.validationStatus.ValidationStatusEnum;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
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
    private RoleAssignmentMapper roleAssignmentMapper;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private SoffidClient soffidClient;

    @Mock
    private PersonService personService;

    @Mock
    private Dir3ValidationRepository dir3ValidationRepository;

    @Mock
    private AppResponsibleService appResponsibleService;

    @Mock
    private AppAuthorizedService appAuthorizedService;

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

    private static final String ADM_UNIT = "A04027006";

    /**
     * A Personal CAIB destination person (ID 20) resolvable by {@link #TARGET_EMAIL}, whose DIR3
     * is therefore checked live against the application's administrative unit.
     */
    private Person stubPersonalCaibTargetPerson() {
        Person person = new Person();
        person.setId(20L);
        person.setEmail(TARGET_EMAIL);
        person.setPersonalCaib(true);
        when(personRepository.findByEmail(TARGET_EMAIL)).thenReturn(person);
        return person;
    }

    /** An anchor (ID 40) whose application has an administrative unit assigned. */
    private AppResponsibleAuthorized anchorWithAdmUnit() {
        Application application = new Application();
        application.setId(30L);
        application.setAdmUnitCode(ADM_UNIT);
        return AppResponsibleAuthorized.builder().id(40L).application(application).build();
    }

    private AppResponsible responsibleHeldByOrigin(ValidationStatusEnum originStatus) {
        Person origin = new Person();
        origin.setId(10L);
        Dir3Validation originValidation = Dir3Validation.builder().id(99L).dir3Status(originStatus).build();
        AppResponsible existing = AppResponsible.builder().id(1L).person(origin)
                .appResponsibleAuthorized(anchorWithAdmUnit()).dir3Validation(originValidation).build();
        when(appResponsibleRepository.findById(1L)).thenReturn(existing);
        return existing;
    }

    private RoleTransferInputDTO transferOf(Long id, RoleAssignmentType type) {
        return new RoleTransferInputDTO(List.of(new RoleTransferItemDTO(id, type)), TARGET_EMAIL, false);
    }

    // ------------------------------------------------------------------
    // getAssignmentsByPerson
    // ------------------------------------------------------------------

    @Test
    void getAssignmentsByPerson_combinesResponsibleAndAuthorizedRows() {
        AppResponsible responsible = AppResponsible.builder().id(1L).appResponsibleAuthorized(anchor).build();
        AppAuthorized authorized = AppAuthorized.builder().id(2L).appResponsibleAuthorized(anchor).build();
        when(appResponsibleRepository.findAllActiveByPersonId(10L)).thenReturn(List.of(responsible));
        when(appAuthorizedRepository.findAllActiveByPersonId(10L)).thenReturn(List.of(authorized));

        AuthorizationTypeOutputDTO typeDto = new AuthorizationTypeOutputDTO(5L, "Firmar peticiones", "Signar peticions", null);
        AppAuthorizedTypeLink link = AppAuthorizedTypeLink.builder().appAuthorizedId(2L).authorizationTypeId(5L).build();
        when(appAuthorizedTypeLinkRepository.findAllActiveByAppAuthorizedIdIn(List.of(2L))).thenReturn(List.of(link));
        AuthorizationType authorizationType = new AuthorizationType();
        authorizationType.setId(5L);
        when(authorizationTypeRepository.findAllByIdIn(List.of(5L))).thenReturn(List.of(authorizationType));
        when(authorizationTypeMapper.toResponse(authorizationType)).thenReturn(typeDto);

        RoleAssignmentOutputDTO responsibleDto = RoleAssignmentOutputDTO.builder().id(1L).build();
        RoleAssignmentOutputDTO authorizedDto = RoleAssignmentOutputDTO.builder().id(2L).build();
        when(roleAssignmentMapper.fromResponsible(responsible)).thenReturn(responsibleDto);
        when(roleAssignmentMapper.fromAuthorized(authorized, List.of(typeDto))).thenReturn(authorizedDto);

        List<RoleAssignmentOutputDTO> result = roleTransferServiceFacadeBean.getAssignmentsByPerson(10L);

        assertEquals(List.of(responsibleDto, authorizedDto), result);
    }

    @Test
    void getAssignmentsByPerson_manyAuthorized_getsTheirTypesInTwoBatchedQueries() {
        AppAuthorized first = AppAuthorized.builder().id(2L).appResponsibleAuthorized(anchor).build();
        AppAuthorized second = AppAuthorized.builder().id(3L).appResponsibleAuthorized(anchor).build();
        AppAuthorized third = AppAuthorized.builder().id(4L).appResponsibleAuthorized(anchor).build();
        when(appResponsibleRepository.findAllActiveByPersonId(10L)).thenReturn(List.of());
        when(appAuthorizedRepository.findAllActiveByPersonId(10L)).thenReturn(List.of(first, second, third));

        when(appAuthorizedTypeLinkRepository.findAllActiveByAppAuthorizedIdIn(List.of(2L, 3L, 4L))).thenReturn(List.of(
                AppAuthorizedTypeLink.builder().appAuthorizedId(2L).authorizationTypeId(5L).build(),
                AppAuthorizedTypeLink.builder().appAuthorizedId(2L).authorizationTypeId(6L).build(),
                AppAuthorizedTypeLink.builder().appAuthorizedId(3L).authorizationTypeId(5L).build()));
        AuthorizationType typeFive = new AuthorizationType();
        typeFive.setId(5L);
        AuthorizationType typeSix = new AuthorizationType();
        typeSix.setId(6L);
        when(authorizationTypeRepository.findAllByIdIn(List.of(5L, 6L))).thenReturn(List.of(typeFive, typeSix));
        AuthorizationTypeOutputDTO dtoFive = new AuthorizationTypeOutputDTO(5L, "Cinco", "Cinco", null);
        AuthorizationTypeOutputDTO dtoSix = new AuthorizationTypeOutputDTO(6L, "Seis", "Seis", null);
        when(authorizationTypeMapper.toResponse(typeFive)).thenReturn(dtoFive);
        when(authorizationTypeMapper.toResponse(typeSix)).thenReturn(dtoSix);

        roleTransferServiceFacadeBean.getAssignmentsByPerson(10L);

        verify(roleAssignmentMapper).fromAuthorized(first, List.of(dtoFive, dtoSix));
        verify(roleAssignmentMapper).fromAuthorized(second, List.of(dtoFive));
        verify(roleAssignmentMapper).fromAuthorized(third, List.of());
        verify(appAuthorizedTypeLinkRepository, never()).findAllActiveByAppAuthorizedId(any());
        verify(authorizationTypeRepository, never()).findById(any());
    }

    @Test
    void getAssignmentsByPerson_linkToMissingType_isSkipped() {
        AppAuthorized authorized = AppAuthorized.builder().id(2L).appResponsibleAuthorized(anchor).build();
        when(appResponsibleRepository.findAllActiveByPersonId(10L)).thenReturn(List.of());
        when(appAuthorizedRepository.findAllActiveByPersonId(10L)).thenReturn(List.of(authorized));
        when(appAuthorizedTypeLinkRepository.findAllActiveByAppAuthorizedIdIn(List.of(2L))).thenReturn(List.of(
                AppAuthorizedTypeLink.builder().appAuthorizedId(2L).authorizationTypeId(99L).build()));
        when(authorizationTypeRepository.findAllByIdIn(List.of(99L))).thenReturn(List.of());

        roleTransferServiceFacadeBean.getAssignmentsByPerson(10L);

        verify(roleAssignmentMapper).fromAuthorized(authorized, List.of());
    }

    @Test
    void getAssignmentsByPerson_authorizedWithoutAnyType_doesNotQueryTheCatalog() {
        AppAuthorized authorized = AppAuthorized.builder().id(2L).appResponsibleAuthorized(anchor).build();
        when(appResponsibleRepository.findAllActiveByPersonId(10L)).thenReturn(List.of());
        when(appAuthorizedRepository.findAllActiveByPersonId(10L)).thenReturn(List.of(authorized));
        when(appAuthorizedTypeLinkRepository.findAllActiveByAppAuthorizedIdIn(List.of(2L))).thenReturn(List.of());

        roleTransferServiceFacadeBean.getAssignmentsByPerson(10L);

        verify(roleAssignmentMapper).fromAuthorized(authorized, List.of());
        verify(authorizationTypeRepository, never()).findAllByIdIn(any());
    }

    @Test
    void getAssignmentsByPerson_noAssignments_returnsEmptyList() {
        when(appResponsibleRepository.findAllActiveByPersonId(99L)).thenReturn(List.of());
        when(appAuthorizedRepository.findAllActiveByPersonId(99L)).thenReturn(List.of());

        assertEquals(List.of(), roleTransferServiceFacadeBean.getAssignmentsByPerson(99L));
        verify(appAuthorizedTypeLinkRepository, never()).findAllActiveByAppAuthorizedIdIn(any());
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
    void transfer_responsibleItem_reassign_updatesPerson() {
        stubExistingTargetPerson();
        Person person = new Person();
        person.setId(10L);
        AppResponsible existing = AppResponsible.builder().id(1L).person(person).appResponsibleAuthorized(anchor).build();
        when(appResponsibleRepository.findById(1L)).thenReturn(existing);
        RoleTransferInputDTO inputDTO = new RoleTransferInputDTO(
                List.of(new RoleTransferItemDTO(1L, RoleAssignmentType.RESPONSIBLE)), TARGET_EMAIL, false);

        roleTransferServiceFacadeBean.transfer(inputDTO);

        assertEquals(20L, existing.getPerson().getId());
        verify(appResponsibleRepository).update(existing, 1L);
        verify(appResponsibleRepository, never()).delete(any());
    }

    @Test
    void transfer_responsibleItem_revoke_delegatesToRegularDelete() {
        RoleTransferInputDTO inputDTO = new RoleTransferInputDTO(
                List.of(new RoleTransferItemDTO(1L, RoleAssignmentType.RESPONSIBLE)), null, true);

        roleTransferServiceFacadeBean.transfer(inputDTO);

        verify(appResponsibleService).delete(1L, null);
        verify(appResponsibleRepository, never()).findById(any());
        verify(appResponsibleRepository, never()).update(any(), any());
        verify(appResponsibleRepository, never()).delete(any());
        verify(personRepository, never()).findByEmail(any());
        verify(soffidClient, never()).searchByEmail(any());
    }

    @Test
    void transfer_responsibleItem_revoke_propagatesFailureOfRegularDelete() {
        doThrow(new BusinessRuleException(Constants.ERR_APPRESPONSIBLE_NOT_ACTIVE))
                .when(appResponsibleService).delete(1L, null);
        RoleTransferInputDTO inputDTO = new RoleTransferInputDTO(
                List.of(new RoleTransferItemDTO(1L, RoleAssignmentType.RESPONSIBLE)), null, true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> roleTransferServiceFacadeBean.transfer(inputDTO));
        assertEquals(Constants.ERR_APPRESPONSIBLE_NOT_ACTIVE, ex.getMessage());
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
    void transfer_authorizedItem_reassign_updatesPerson() {
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
    void transfer_authorizedItem_revoke_delegatesToRegularDelete() {
        RoleTransferInputDTO inputDTO = new RoleTransferInputDTO(
                List.of(new RoleTransferItemDTO(2L, RoleAssignmentType.AUTHORIZED)), null, true);

        roleTransferServiceFacadeBean.transfer(inputDTO);

        verify(appAuthorizedService).delete(2L, null);
        verify(appAuthorizedRepository, never()).findById(any());
        verify(appAuthorizedRepository, never()).update(any(), any());
        verify(appAuthorizedRepository, never()).delete(any());
        verify(appAuthorizedRepository, never()).existsByAppResponsibleAuthorizedAndPersonAndIdNot(any(), any(), any());
    }

    @Test
    void transfer_authorizedItem_revoke_propagatesFailureOfRegularDelete() {
        doThrow(new BusinessRuleException(Constants.ERR_APPAUTHORIZED_NOT_FOUND))
                .when(appAuthorizedService).delete(2L, null);
        RoleTransferInputDTO inputDTO = new RoleTransferInputDTO(
                List.of(new RoleTransferItemDTO(2L, RoleAssignmentType.AUTHORIZED)), null, true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> roleTransferServiceFacadeBean.transfer(inputDTO));
        assertEquals(Constants.ERR_APPAUTHORIZED_NOT_FOUND, ex.getMessage());
    }

    @Test
    void transfer_mixedBatch_revoke_deletesEachItemThroughItsOwnService() {
        RoleTransferInputDTO inputDTO = new RoleTransferInputDTO(
                List.of(new RoleTransferItemDTO(1L, RoleAssignmentType.RESPONSIBLE),
                        new RoleTransferItemDTO(2L, RoleAssignmentType.AUTHORIZED)),
                null, true);

        roleTransferServiceFacadeBean.transfer(inputDTO);

        verify(appResponsibleService).delete(1L, null);
        verify(appAuthorizedService).delete(2L, null);
    }

    // ------------------------------------------------------------------
    // transfer - mixed batch
    // ------------------------------------------------------------------

    @Test
    void transfer_mixedBatch_processesEachItemByItsOwnType() {
        stubExistingTargetPerson();
        Person responsiblePerson = new Person();
        responsiblePerson.setId(10L);
        AppResponsible responsible = AppResponsible.builder().id(1L).person(responsiblePerson).appResponsibleAuthorized(anchor).build();
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
        when(soffidClient.searchByEmail(TARGET_EMAIL)).thenReturn(soffidUser);
        Person created = new Person();
        created.setId(50L);
        when(personRepository.create(any(Person.class))).thenReturn(created);

        Person existingPerson = new Person();
        existingPerson.setId(10L);
        AppResponsible existing = AppResponsible.builder().id(1L).person(existingPerson).appResponsibleAuthorized(anchor).build();
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
        when(soffidClient.searchByEmail(TARGET_EMAIL)).thenReturn(null);

        RoleTransferInputDTO inputDTO = new RoleTransferInputDTO(
                List.of(new RoleTransferItemDTO(1L, RoleAssignmentType.RESPONSIBLE)), TARGET_EMAIL, false);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> roleTransferServiceFacadeBean.transfer(inputDTO));
        assertEquals(Constants.ERR_ROLETRANSFER_TARGET_NOT_FOUND, ex.getMessage());
        verify(appResponsibleRepository, never()).findById(any());
        verify(personRepository, never()).create(any());
    }

    // ------------------------------------------------------------------
    // transfer - DIR3 validation of the destination person
    // ------------------------------------------------------------------

    @Test
    void transfer_responsibleItem_targetMatchesDir3_attachesNewValidatedValidationIgnoringOriginStatus() {
        stubPersonalCaibTargetPerson();
        AppResponsible existing = responsibleHeldByOrigin(ValidationStatusEnum.MANUAL);
        when(personService.checkDir3(TARGET_EMAIL, ADM_UNIT))
                .thenReturn(new PersonDir3CheckOutputDTO("sgaip", ADM_UNIT, ADM_UNIT, true));
        Dir3Validation created = Dir3Validation.builder().id(100L).dir3Status(ValidationStatusEnum.VALIDATED).build();
        when(dir3ValidationRepository.create(any(Dir3Validation.class))).thenReturn(created);

        roleTransferServiceFacadeBean.transfer(transferOf(1L, RoleAssignmentType.RESPONSIBLE));

        verify(dir3ValidationRepository).create(argThat(validation -> validation.getDir3Status() == ValidationStatusEnum.VALIDATED));
        assertSame(created, existing.getDir3Validation());
        assertEquals(20L, existing.getPerson().getId());
        verify(appResponsibleRepository).update(existing, 1L);
    }

    @Test
    void transfer_responsibleItem_targetMismatchesDir3_attachesNotValidatedEvenIfOriginWasValidated() {
        stubPersonalCaibTargetPerson();
        AppResponsible existing = responsibleHeldByOrigin(ValidationStatusEnum.VALIDATED);
        when(personService.checkDir3(TARGET_EMAIL, ADM_UNIT))
                .thenReturn(new PersonDir3CheckOutputDTO("other", "A99999999", ADM_UNIT, false));
        Dir3Validation created = Dir3Validation.builder().id(100L).dir3Status(ValidationStatusEnum.NOT_VALIDATED).build();
        when(dir3ValidationRepository.create(any(Dir3Validation.class))).thenReturn(created);

        roleTransferServiceFacadeBean.transfer(transferOf(1L, RoleAssignmentType.RESPONSIBLE));

        verify(dir3ValidationRepository).create(argThat(validation -> validation.getDir3Status() == ValidationStatusEnum.NOT_VALIDATED));
        assertSame(created, existing.getDir3Validation());
    }

    @Test
    void transfer_responsibleItem_dir3CheckFails_treatedAsNotValidatedAndTransferCompletes() {
        stubPersonalCaibTargetPerson();
        AppResponsible existing = responsibleHeldByOrigin(ValidationStatusEnum.VALIDATED);
        when(personService.checkDir3(TARGET_EMAIL, ADM_UNIT)).thenThrow(new RuntimeException("Soffid unavailable"));
        Dir3Validation created = Dir3Validation.builder().id(100L).dir3Status(ValidationStatusEnum.NOT_VALIDATED).build();
        when(dir3ValidationRepository.create(any(Dir3Validation.class))).thenReturn(created);

        roleTransferServiceFacadeBean.transfer(transferOf(1L, RoleAssignmentType.RESPONSIBLE));

        verify(dir3ValidationRepository).create(argThat(validation -> validation.getDir3Status() == ValidationStatusEnum.NOT_VALIDATED));
        verify(appResponsibleRepository).update(existing, 1L);
    }

    @Test
    void transfer_responsibleItem_targetNotPersonalCaib_attachesNotApplyWithoutCheckingDir3() {
        Person target = new Person();
        target.setId(20L);
        target.setEmail(TARGET_EMAIL);
        target.setPersonalCaib(false);
        when(personRepository.findByEmail(TARGET_EMAIL)).thenReturn(target);
        AppResponsible existing = responsibleHeldByOrigin(ValidationStatusEnum.VALIDATED);
        Dir3Validation created = Dir3Validation.builder().id(100L).dir3Status(ValidationStatusEnum.NOT_APPLY).build();
        when(dir3ValidationRepository.create(any(Dir3Validation.class))).thenReturn(created);

        roleTransferServiceFacadeBean.transfer(transferOf(1L, RoleAssignmentType.RESPONSIBLE));

        verify(personService, never()).checkDir3(any(), any());
        verify(dir3ValidationRepository).create(argThat(validation -> validation.getDir3Status() == ValidationStatusEnum.NOT_APPLY));
        assertSame(created, existing.getDir3Validation());
    }

    @Test
    void transfer_responsibleItem_targetAlreadyHoldsResponsibleOnAnchor_recalculatesItsSharedValidation() {
        stubPersonalCaibTargetPerson();
        AppResponsible existing = responsibleHeldByOrigin(ValidationStatusEnum.VALIDATED);
        Dir3Validation targetValidation = Dir3Validation.builder().id(77L).dir3Status(ValidationStatusEnum.VALIDATED).build();
        AppResponsible targetSibling = AppResponsible.builder().id(5L).dir3Validation(targetValidation).build();
        when(appResponsibleRepository.findAllActiveByAppResponsibleAuthorizedAndPerson(40L, 20L)).thenReturn(List.of(targetSibling));
        when(personService.checkDir3(TARGET_EMAIL, ADM_UNIT))
                .thenReturn(new PersonDir3CheckOutputDTO("other", "A99999999", ADM_UNIT, false));

        roleTransferServiceFacadeBean.transfer(transferOf(1L, RoleAssignmentType.RESPONSIBLE));

        assertSame(targetValidation, existing.getDir3Validation());
        assertEquals(ValidationStatusEnum.NOT_VALIDATED, targetValidation.getDir3Status());
        verify(dir3ValidationRepository).update(targetValidation, 77L);
        verify(dir3ValidationRepository, never()).create(any());
        verify(appResponsibleRepository).update(existing, 1L);
    }

    @Test
    void transfer_responsibleItem_targetSharedValidationIsManual_keepsItWithoutCheckingDir3() {
        stubPersonalCaibTargetPerson();
        AppResponsible existing = responsibleHeldByOrigin(ValidationStatusEnum.VALIDATED);
        Dir3Validation targetValidation = Dir3Validation.builder().id(77L).dir3Status(ValidationStatusEnum.MANUAL).build();
        AppResponsible targetSibling = AppResponsible.builder().id(5L).dir3Validation(targetValidation).build();
        when(appResponsibleRepository.findAllActiveByAppResponsibleAuthorizedAndPerson(40L, 20L)).thenReturn(List.of(targetSibling));

        roleTransferServiceFacadeBean.transfer(transferOf(1L, RoleAssignmentType.RESPONSIBLE));

        assertSame(targetValidation, existing.getDir3Validation());
        assertEquals(ValidationStatusEnum.MANUAL, targetValidation.getDir3Status());
        verify(personService, never()).checkDir3(any(), any());
        verify(dir3ValidationRepository, never()).update(any(), any());
        verify(dir3ValidationRepository, never()).create(any());
        verify(appResponsibleRepository).update(existing, 1L);
    }

    @Test
    void transfer_responsibleItem_targetHeldNoOtherAssignmentAnyMore_getsAFreshValidationEvenIfItWasManualBefore() {
        stubPersonalCaibTargetPerson();
        AppResponsible existing = responsibleHeldByOrigin(ValidationStatusEnum.MANUAL);
        when(personService.checkDir3(TARGET_EMAIL, ADM_UNIT))
                .thenReturn(new PersonDir3CheckOutputDTO("other", "A99999999", ADM_UNIT, false));
        Dir3Validation created = Dir3Validation.builder().id(100L).dir3Status(ValidationStatusEnum.NOT_VALIDATED).build();
        when(dir3ValidationRepository.create(any(Dir3Validation.class))).thenReturn(created);

        roleTransferServiceFacadeBean.transfer(transferOf(1L, RoleAssignmentType.RESPONSIBLE));

        verify(dir3ValidationRepository).create(argThat(validation -> validation.getDir3Status() == ValidationStatusEnum.NOT_VALIDATED));
        assertSame(created, existing.getDir3Validation());
    }
    @Test
    void transfer_responsibleItem_targetSharedValidationAlreadyUpToDate_isNotWrittenAgain() {
        stubPersonalCaibTargetPerson();
        AppResponsible existing = responsibleHeldByOrigin(ValidationStatusEnum.VALIDATED);
        Dir3Validation targetValidation = Dir3Validation.builder().id(77L).dir3Status(ValidationStatusEnum.VALIDATED).build();
        AppResponsible targetSibling = AppResponsible.builder().id(5L).dir3Validation(targetValidation).build();
        when(appResponsibleRepository.findAllActiveByAppResponsibleAuthorizedAndPerson(40L, 20L)).thenReturn(List.of(targetSibling));
        when(personService.checkDir3(TARGET_EMAIL, ADM_UNIT))
                .thenReturn(new PersonDir3CheckOutputDTO("sgaip", ADM_UNIT, ADM_UNIT, true));

        roleTransferServiceFacadeBean.transfer(transferOf(1L, RoleAssignmentType.RESPONSIBLE));

        assertSame(targetValidation, existing.getDir3Validation());
        verify(dir3ValidationRepository, never()).update(any(), any());
        verify(dir3ValidationRepository, never()).create(any());
    }

    @Test
    void transfer_responsibleItem_targetSharedValidationAndDir3CheckFails_keepsItsCurrentStatus() {
        stubPersonalCaibTargetPerson();
        AppResponsible existing = responsibleHeldByOrigin(ValidationStatusEnum.VALIDATED);
        Dir3Validation targetValidation = Dir3Validation.builder().id(77L).dir3Status(ValidationStatusEnum.VALIDATED).build();
        AppResponsible targetSibling = AppResponsible.builder().id(5L).dir3Validation(targetValidation).build();
        when(appResponsibleRepository.findAllActiveByAppResponsibleAuthorizedAndPerson(40L, 20L)).thenReturn(List.of(targetSibling));
        when(personService.checkDir3(TARGET_EMAIL, ADM_UNIT)).thenThrow(new RuntimeException("Soffid unavailable"));

        roleTransferServiceFacadeBean.transfer(transferOf(1L, RoleAssignmentType.RESPONSIBLE));

        assertEquals(ValidationStatusEnum.VALIDATED, targetValidation.getDir3Status());
        assertSame(targetValidation, existing.getDir3Validation());
        verify(dir3ValidationRepository, never()).update(any(), any());
        verify(appResponsibleRepository).update(existing, 1L);
    }
    @Test
    void transfer_responsibleItem_targetAlreadyAuthorizedOnAnchor_recalculatesItsSharedValidation() {
        stubPersonalCaibTargetPerson();
        AppResponsible existing = responsibleHeldByOrigin(ValidationStatusEnum.VALIDATED);
        Dir3Validation targetValidation = Dir3Validation.builder().id(78L).dir3Status(ValidationStatusEnum.VALIDATED).build();
        AppAuthorized targetAuthorized = AppAuthorized.builder().id(6L).dir3Validation(targetValidation).build();
        when(appAuthorizedRepository.findActiveByAppResponsibleAuthorizedAndPerson(40L, 20L)).thenReturn(targetAuthorized);
        when(personService.checkDir3(TARGET_EMAIL, ADM_UNIT))
                .thenReturn(new PersonDir3CheckOutputDTO("other", "A99999999", ADM_UNIT, false));

        roleTransferServiceFacadeBean.transfer(transferOf(1L, RoleAssignmentType.RESPONSIBLE));

        assertSame(targetValidation, existing.getDir3Validation());
        assertEquals(ValidationStatusEnum.NOT_VALIDATED, targetValidation.getDir3Status());
        verify(dir3ValidationRepository).update(targetValidation, 78L);
        verify(dir3ValidationRepository, never()).create(any());
    }

    @Test
    void transfer_authorizedItem_targetMismatchesDir3_attachesNewNotValidatedValidationIgnoringOriginStatus() {
        stubPersonalCaibTargetPerson();
        Person origin = new Person();
        origin.setId(11L);
        Dir3Validation originValidation = Dir3Validation.builder().id(99L).dir3Status(ValidationStatusEnum.MANUAL).build();
        AppAuthorized existing = AppAuthorized.builder().id(2L).person(origin)
                .appResponsibleAuthorized(anchorWithAdmUnit()).dir3Validation(originValidation).build();
        when(appAuthorizedRepository.findById(2L)).thenReturn(existing);
        when(personService.checkDir3(TARGET_EMAIL, ADM_UNIT))
                .thenReturn(new PersonDir3CheckOutputDTO("other", "A99999999", ADM_UNIT, false));
        Dir3Validation created = Dir3Validation.builder().id(100L).dir3Status(ValidationStatusEnum.NOT_VALIDATED).build();
        when(dir3ValidationRepository.create(any(Dir3Validation.class))).thenReturn(created);

        roleTransferServiceFacadeBean.transfer(transferOf(2L, RoleAssignmentType.AUTHORIZED));

        verify(dir3ValidationRepository).create(argThat(validation -> validation.getDir3Status() == ValidationStatusEnum.NOT_VALIDATED));
        assertSame(created, existing.getDir3Validation());
        assertEquals(20L, existing.getPerson().getId());
        verify(appAuthorizedRepository).update(existing, 2L);
    }

    @Test
    void transfer_authorizedItem_targetAlreadyHoldsResponsibleOnAnchor_recalculatesItsSharedValidation() {
        stubPersonalCaibTargetPerson();
        Person origin = new Person();
        origin.setId(11L);
        AppAuthorized existing = AppAuthorized.builder().id(2L).person(origin)
                .appResponsibleAuthorized(anchorWithAdmUnit())
                .dir3Validation(Dir3Validation.builder().id(99L).dir3Status(ValidationStatusEnum.VALIDATED).build()).build();
        when(appAuthorizedRepository.findById(2L)).thenReturn(existing);
        Dir3Validation targetValidation = Dir3Validation.builder().id(77L).dir3Status(ValidationStatusEnum.VALIDATED).build();
        AppResponsible targetSibling = AppResponsible.builder().id(5L).dir3Validation(targetValidation).build();
        when(appResponsibleRepository.findAllActiveByAppResponsibleAuthorizedAndPerson(40L, 20L)).thenReturn(List.of(targetSibling));
        when(personService.checkDir3(TARGET_EMAIL, ADM_UNIT))
                .thenReturn(new PersonDir3CheckOutputDTO("other", "A99999999", ADM_UNIT, false));

        roleTransferServiceFacadeBean.transfer(transferOf(2L, RoleAssignmentType.AUTHORIZED));

        assertSame(targetValidation, existing.getDir3Validation());
        assertEquals(ValidationStatusEnum.NOT_VALIDATED, targetValidation.getDir3Status());
        verify(dir3ValidationRepository).update(targetValidation, 77L);
        verify(dir3ValidationRepository, never()).create(any());
    }

    @Test
    void transfer_authorizedItem_targetSharedValidationIsManual_keepsItWithoutCheckingDir3() {
        stubPersonalCaibTargetPerson();
        Person origin = new Person();
        origin.setId(11L);
        AppAuthorized existing = AppAuthorized.builder().id(2L).person(origin)
                .appResponsibleAuthorized(anchorWithAdmUnit())
                .dir3Validation(Dir3Validation.builder().id(99L).dir3Status(ValidationStatusEnum.VALIDATED).build()).build();
        when(appAuthorizedRepository.findById(2L)).thenReturn(existing);
        Dir3Validation targetValidation = Dir3Validation.builder().id(77L).dir3Status(ValidationStatusEnum.MANUAL).build();
        AppResponsible targetSibling = AppResponsible.builder().id(5L).dir3Validation(targetValidation).build();
        when(appResponsibleRepository.findAllActiveByAppResponsibleAuthorizedAndPerson(40L, 20L)).thenReturn(List.of(targetSibling));

        roleTransferServiceFacadeBean.transfer(transferOf(2L, RoleAssignmentType.AUTHORIZED));

        assertSame(targetValidation, existing.getDir3Validation());
        assertEquals(ValidationStatusEnum.MANUAL, targetValidation.getDir3Status());
        verify(personService, never()).checkDir3(any(), any());
        verify(dir3ValidationRepository, never()).update(any(), any());
    }
    @Test
    void transfer_revoke_doesNotTouchDir3Validation() {
        roleTransferServiceFacadeBean.transfer(new RoleTransferInputDTO(
                List.of(new RoleTransferItemDTO(1L, RoleAssignmentType.RESPONSIBLE)), null, true));

        verify(dir3ValidationRepository, never()).create(any());
        verify(personService, never()).checkDir3(any(), any());
    }
}
