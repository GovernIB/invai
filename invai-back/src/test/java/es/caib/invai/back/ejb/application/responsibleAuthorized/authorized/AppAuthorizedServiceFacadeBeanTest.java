package es.caib.invai.back.ejb.application.responsibleAuthorized.authorized;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedDeleteDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedOutputDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedCriteria;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.dir3.Dir3ValidationRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.type.AppAuthorizedTypeLinkRepository;
import es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO.AuthorizationTypeOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.responsible.authorizationType.AuthorizationTypeRepository;
import es.caib.invai.back.service.facade.maintenance.responsible.person.PersonService;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.authorized.AppAuthorizedMapper;
import es.caib.invai.back.service.mapper.maintenance.responsible.authorizationType.AuthorizationTypeMapper;
import es.caib.invai.back.service.model.maintenance.responsible.authorizationType.AuthorizationType;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.dir3.Dir3Validation;
import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import es.caib.invai.back.service.model.application.responsibleAuthorized.type.AppAuthorizedTypeLink;
import es.caib.invai.back.service.model.catalog.dir3Status.Dir3ValidationStatus;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppAuthorizedServiceFacadeBean}, with particular focus on the multi-select
 * authorization type reconciliation logic performed on create/update (attach-only vs diff-based
 * attach/detach) and the person-keyed swap-out/lifecycle business rules, with all collaborators
 * mocked.
 */
@ExtendWith(MockitoExtension.class)
class AppAuthorizedServiceFacadeBeanTest {

    @Mock
    private AppAuthorizedMapper appAuthorizedMapper;

    @Mock
    private AppAuthorizedRepository appAuthorizedRepository;

    @Mock
    private AppAuthorizedTypeLinkRepository appAuthorizedTypeLinkRepository;

    @Mock
    private AuthorizationTypeRepository authorizationTypeRepository;

    @Mock
    private AuthorizationTypeMapper authorizationTypeMapper;

    @Mock
    private PersonService personService;

    @Mock
    private Dir3ValidationRepository dir3ValidationRepository;

    @Mock
    private AppResponsibleRepository appResponsibleRepository;

    @InjectMocks
    private AppAuthorizedServiceFacadeBean appAuthorizedServiceFacadeBean;

    private AppAuthorized activeModel;

    @BeforeEach
    void setUp() {
        Application application = new Application();
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(10L).application(application).build();
        Person person = new Person();
        person.setId(7L);
        Dir3Validation dir3Validation = Dir3Validation.builder().id(500L).dir3Status(Dir3ValidationStatus.NOT_APPLY).build();
        activeModel = AppAuthorized.builder().id(1L).appResponsibleAuthorized(anchor).person(person).dir3Validation(dir3Validation).build();

        lenient().when(dir3ValidationRepository.create(any())).thenAnswer(invocation -> {
            Dir3Validation created = invocation.getArgument(0);
            created.setId(600L);
            return created;
        });
        lenient().when(personService.getPersonById(7L)).thenReturn(person);
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPageWithAuthorizationTypes() {
        AppAuthorizedCriteria criteria = new AppAuthorizedCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppAuthorized> domainPage = new PageImpl<>(List.of(activeModel));
        AppAuthorizedOutputDTO mapped = AppAuthorizedOutputDTO.builder().id(1L).build();
        when(appAuthorizedRepository.findAll(10L, criteria, pageable)).thenReturn(domainPage);
        when(appAuthorizedMapper.toResponse(activeModel)).thenReturn(mapped);
        when(appAuthorizedTypeLinkRepository.findAllActiveByAppAuthorizedId(1L)).thenReturn(List.of());

        Page<AppAuthorizedOutputDTO> result = appAuthorizedServiceFacadeBean.getAll(10L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
        assertEquals(List.of(), mapped.getAuthorizationTypes());
    }

    @Test
    void getAll_multipleAttachedTypes_resolvesThemInOneBatchedCallNotOnePerType() {
        AppAuthorizedCriteria criteria = new AppAuthorizedCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppAuthorized> domainPage = new PageImpl<>(List.of(activeModel));
        AppAuthorizedOutputDTO mapped = AppAuthorizedOutputDTO.builder().id(1L).build();
        AppAuthorizedTypeLink link2 = AppAuthorizedTypeLink.builder().appAuthorizedId(1L).authorizationTypeId(2L).build();
        AppAuthorizedTypeLink link3 = AppAuthorizedTypeLink.builder().appAuthorizedId(1L).authorizationTypeId(3L).build();
        AuthorizationType type2 = new AuthorizationType();
        type2.setId(2L);
        AuthorizationType type3 = new AuthorizationType();
        type3.setId(3L);
        AuthorizationTypeOutputDTO typeDto2 = new AuthorizationTypeOutputDTO();
        AuthorizationTypeOutputDTO typeDto3 = new AuthorizationTypeOutputDTO();
        when(appAuthorizedRepository.findAll(10L, criteria, pageable)).thenReturn(domainPage);
        when(appAuthorizedMapper.toResponse(activeModel)).thenReturn(mapped);
        when(appAuthorizedTypeLinkRepository.findAllActiveByAppAuthorizedId(1L)).thenReturn(List.of(link2, link3));
        when(authorizationTypeRepository.findAllByIdIn(List.of(2L, 3L))).thenReturn(List.of(type2, type3));
        when(authorizationTypeMapper.toResponse(type2)).thenReturn(typeDto2);
        when(authorizationTypeMapper.toResponse(type3)).thenReturn(typeDto3);

        appAuthorizedServiceFacadeBean.getAll(10L, criteria, pageable);

        assertEquals(List.of(typeDto2, typeDto3), mapped.getAuthorizationTypes());
        verify(authorizationTypeRepository).findAllByIdIn(List.of(2L, 3L));
        verify(authorizationTypeRepository, never()).findById(any());
    }

    // ------------------------------------------------------------------
    // create
    // ------------------------------------------------------------------

    @Test
    void create_noExistingHolder_persistsAttachesTypesAndReturnsResponse() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, 7L, null, null, null, null, List.of(2L, 3L), null, false, null, null);
        AppAuthorized model = new AppAuthorized();
        AppAuthorized saved = AppAuthorized.builder().id(1L).build();
        AppAuthorizedOutputDTO response = AppAuthorizedOutputDTO.builder().id(1L).build();
        when(appAuthorizedRepository.findActiveByAppResponsibleAuthorizedAndPerson(10L, 7L)).thenReturn(null);
        when(appAuthorizedMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appAuthorizedRepository.create(model)).thenReturn(saved);
        when(appAuthorizedTypeLinkRepository.findAllActiveByAppAuthorizedId(1L)).thenReturn(List.of());
        when(appAuthorizedMapper.toResponse(saved)).thenReturn(response);

        AppAuthorizedOutputDTO result = appAuthorizedServiceFacadeBean.create(inputDTO);

        verify(appAuthorizedRepository, never()).delete(any());
        ArgumentCaptor<AppAuthorizedTypeLink> captor = ArgumentCaptor.forClass(AppAuthorizedTypeLink.class);
        verify(appAuthorizedTypeLinkRepository, times(2)).create(captor.capture());
        List<Long> attachedTypeIds = captor.getAllValues().stream()
                .map(AppAuthorizedTypeLink::getAuthorizationTypeId)
                .sorted()
                .toList();
        assertEquals(List.of(2L, 3L), attachedTypeIds);
        assertEquals(response, result);
        verify(personService).getPersonById(7L);
    }

    @Test
    void create_personIdGivenDoesNotExist_throwsBusinessRuleException() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, 7L, null, null, null, null, List.of(2L), null, false, null, null);
        when(personService.getPersonById(7L)).thenThrow(new BusinessRuleException(Constants.ERR_PERSON_NOT_FOUND));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appAuthorizedServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_PERSON_NOT_FOUND, ex.getMessage());
        verify(appAuthorizedRepository, never()).create(any());
    }

    @Test
    void create_existingAuthorizedHolder_deactivatesOldAndCreatesNew() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, 7L, null, null, null, null, List.of(2L), null, false, null, null);
        Person oldAuthorizedPerson = new Person();
        oldAuthorizedPerson.setId(7L);
        AppAuthorized existingAuthorizedHolder = AppAuthorized.builder().id(2L).person(oldAuthorizedPerson).build();
        AppAuthorized model = new AppAuthorized();
        AppAuthorized saved = AppAuthorized.builder().id(1L).build();
        AppAuthorizedOutputDTO response = AppAuthorizedOutputDTO.builder().id(1L).build();
        when(appAuthorizedRepository.findActiveByAppResponsibleAuthorizedAndPerson(10L, 7L)).thenReturn(existingAuthorizedHolder);
        when(appAuthorizedMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appAuthorizedRepository.create(model)).thenReturn(saved);
        when(appAuthorizedTypeLinkRepository.findAllActiveByAppAuthorizedId(1L)).thenReturn(List.of());
        when(appAuthorizedMapper.toResponse(saved)).thenReturn(response);

        AppAuthorizedOutputDTO result = appAuthorizedServiceFacadeBean.create(inputDTO);

        assertNotNull(existingAuthorizedHolder.getDeletedAt());
        assertNull(existingAuthorizedHolder.getObservation());
        verify(appAuthorizedRepository).delete(existingAuthorizedHolder);
        verify(appAuthorizedRepository).create(model);
        assertEquals(response, result);
    }

    @Test
    void create_noPersonIdButNoNameOrEmail_throwsBusinessRuleException() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, null, null, null, null, null, List.of(2L), null, true, null, null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appAuthorizedServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_APPAUTHORIZED_PERSON_DATA_REQUIRED, ex.getMessage());
        verify(appAuthorizedRepository, never()).create(any());
    }

    @Test
    void create_noPersonIdGiven_resolvesPersonIdViaPersonService() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, null, "Joan", "Fuster", "joan@caib.es", null, List.of(2L), null, true, null, null);
        Person resolved = new Person();
        resolved.setId(99L);
        AppAuthorized model = new AppAuthorized();
        AppAuthorized saved = AppAuthorized.builder().id(1L).build();
        AppAuthorizedOutputDTO response = AppAuthorizedOutputDTO.builder().id(1L).build();
        when(personService.getOrCreatePerson("Joan", "Fuster", "joan@caib.es", true, null, null)).thenReturn(resolved);
        when(appAuthorizedRepository.findActiveByAppResponsibleAuthorizedAndPerson(10L, 99L)).thenReturn(null);
        when(appAuthorizedMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appAuthorizedRepository.create(model)).thenReturn(saved);
        when(appAuthorizedTypeLinkRepository.findAllActiveByAppAuthorizedId(1L)).thenReturn(List.of());
        when(appAuthorizedMapper.toResponse(saved)).thenReturn(response);

        appAuthorizedServiceFacadeBean.create(inputDTO);

        assertEquals(99L, inputDTO.getPersonId());
    }

    @Test
    void create_personServiceResolveOrCreatePersonThrows_propagatesException() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, null, "Maria", "Puig", "maria@extern.es", null, List.of(2L), null, false, null, null);
        when(personService.getOrCreatePerson("Maria", "Puig", "maria@extern.es", false, null, null))
                .thenThrow(new BusinessRuleException(Constants.ERR_PERSON_COMPANY_REQUIRED_WHEN_NOT_CAIB));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appAuthorizedServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_PERSON_COMPANY_REQUIRED_WHEN_NOT_CAIB, ex.getMessage());
        verify(appAuthorizedRepository, never()).create(any());
    }

    @Test
    void create_delegatesPersonalCaibSyncToPersonService() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, 7L, null, null, null, null, List.of(2L), null, false, null, null);
        AppAuthorized model = new AppAuthorized();
        AppAuthorized saved = AppAuthorized.builder().id(1L).build();
        AppAuthorizedOutputDTO response = AppAuthorizedOutputDTO.builder().id(1L).build();
        when(appAuthorizedRepository.findActiveByAppResponsibleAuthorizedAndPerson(10L, 7L)).thenReturn(null);
        when(appAuthorizedMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appAuthorizedRepository.create(model)).thenReturn(saved);
        when(appAuthorizedTypeLinkRepository.findAllActiveByAppAuthorizedId(1L)).thenReturn(List.of());
        when(appAuthorizedMapper.toResponse(saved)).thenReturn(response);

        appAuthorizedServiceFacadeBean.create(inputDTO);

        verify(personService).updatePersonalCaibToPerson(7L, false);
    }

    @Test
    void create_personalCaibNull_treatedAsFalseWithoutThrowing() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, 7L, null, null, null, null, List.of(2L), null, null, null, null);
        AppAuthorized model = new AppAuthorized();
        AppAuthorized saved = AppAuthorized.builder().id(1L).build();
        AppAuthorizedOutputDTO response = AppAuthorizedOutputDTO.builder().id(1L).build();
        when(appAuthorizedRepository.findActiveByAppResponsibleAuthorizedAndPerson(10L, 7L)).thenReturn(null);
        when(appAuthorizedMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appAuthorizedRepository.create(model)).thenReturn(saved);
        when(appAuthorizedTypeLinkRepository.findAllActiveByAppAuthorizedId(1L)).thenReturn(List.of());
        when(appAuthorizedMapper.toResponse(saved)).thenReturn(response);

        appAuthorizedServiceFacadeBean.create(inputDTO);

        verify(personService).updatePersonalCaibToPerson(7L, false);
    }

    @Test
    void create_persistsDir3StatusFromInputDTOAsIs() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, 7L, null, null, null, null, List.of(2L), null, true, true, null);
        AppAuthorized model = new AppAuthorized();
        AppAuthorized saved = AppAuthorized.builder().id(1L).build();
        AppAuthorizedOutputDTO response = AppAuthorizedOutputDTO.builder().id(1L).build();
        when(appAuthorizedRepository.findActiveByAppResponsibleAuthorizedAndPerson(10L, 7L)).thenReturn(null);
        when(appAuthorizedMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appAuthorizedRepository.create(model)).thenReturn(saved);
        when(appAuthorizedTypeLinkRepository.findAllActiveByAppAuthorizedId(1L)).thenReturn(List.of());
        when(appAuthorizedMapper.toResponse(saved)).thenReturn(response);

        appAuthorizedServiceFacadeBean.create(inputDTO);

        ArgumentCaptor<Dir3Validation> captor = ArgumentCaptor.forClass(Dir3Validation.class);
        verify(dir3ValidationRepository).create(captor.capture());
        assertEquals(Dir3ValidationStatus.VALIDATED, captor.getValue().getDir3Status());
        assertEquals(Dir3ValidationStatus.VALIDATED, model.getDir3Validation().getDir3Status());
        verify(personService, never()).checkDir3(any(), any());
    }

    @Test
    void create_personHasActiveResponsibleAssignmentOnSameAnchor_reusesSharedDir3Validation() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, 7L, null, null, null, null, List.of(2L), null, true, false, null);
        AppAuthorized model = new AppAuthorized();
        AppAuthorized saved = AppAuthorized.builder().id(1L).build();
        AppAuthorizedOutputDTO response = AppAuthorizedOutputDTO.builder().id(1L).build();
        Dir3Validation sharedDir3Validation = Dir3Validation.builder().id(500L).dir3Status(Dir3ValidationStatus.MANUAL).build();
        AppResponsible existingResponsibleAssignment = new AppResponsible();
        existingResponsibleAssignment.setDir3Validation(sharedDir3Validation);
        when(appAuthorizedRepository.findActiveByAppResponsibleAuthorizedAndPerson(10L, 7L)).thenReturn(null);
        when(appResponsibleRepository.findAllActiveByAppResponsibleAuthorizedAndPerson(10L, 7L)).thenReturn(List.of(existingResponsibleAssignment));
        when(appAuthorizedMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appAuthorizedRepository.create(model)).thenReturn(saved);
        when(appAuthorizedTypeLinkRepository.findAllActiveByAppAuthorizedId(1L)).thenReturn(List.of());
        when(appAuthorizedMapper.toResponse(saved)).thenReturn(response);

        appAuthorizedServiceFacadeBean.create(inputDTO);

        assertSame(sharedDir3Validation, model.getDir3Validation());
        verify(dir3ValidationRepository, never()).create(any());
    }

    @Test
    void create_syncsPersonalCaibOnlyAfterPersistingTheRecord() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, 7L, null, null, null, null, List.of(2L), null, false, null, null);
        AppAuthorized model = new AppAuthorized();
        AppAuthorized saved = AppAuthorized.builder().id(1L).build();
        AppAuthorizedOutputDTO response = AppAuthorizedOutputDTO.builder().id(1L).build();
        when(appAuthorizedRepository.findActiveByAppResponsibleAuthorizedAndPerson(10L, 7L)).thenReturn(null);
        when(appAuthorizedMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appAuthorizedRepository.create(model)).thenReturn(saved);
        when(appAuthorizedTypeLinkRepository.findAllActiveByAppAuthorizedId(1L)).thenReturn(List.of());
        when(appAuthorizedMapper.toResponse(saved)).thenReturn(response);

        appAuthorizedServiceFacadeBean.create(inputDTO);

        InOrder inOrder = inOrder(appAuthorizedRepository, personService);
        inOrder.verify(appAuthorizedRepository).create(model);
        inOrder.verify(personService).updatePersonalCaibToPerson(7L, false);
    }

    @Test
    void create_personServiceSyncPersonalCaibThrows_propagatesException() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, 7L, null, null, null, null, List.of(2L), null, false, null, null);
        AppAuthorized model = new AppAuthorized();
        AppAuthorized saved = AppAuthorized.builder().id(1L).build();
        when(appAuthorizedRepository.findActiveByAppResponsibleAuthorizedAndPerson(10L, 7L)).thenReturn(null);
        when(appAuthorizedMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appAuthorizedRepository.create(model)).thenReturn(saved);
        doThrow(new BusinessRuleException(Constants.ERR_PERSON_COMPANY_REQUIRED_WHEN_NOT_CAIB))
                .when(personService).updatePersonalCaibToPerson(7L, false);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appAuthorizedServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_PERSON_COMPANY_REQUIRED_WHEN_NOT_CAIB, ex.getMessage());
        verify(appAuthorizedRepository).create(model);
    }

    // ------------------------------------------------------------------
    // update
    // ------------------------------------------------------------------

    @Test
    void update_notFound_throwsBusinessRuleException() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, 7L, null, null, null, null, List.of(2L), null, false, null, null);
        when(appAuthorizedRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appAuthorizedServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_APPAUTHORIZED_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_valid_reconcilesAuthorizationTypesAddingAndRemoving() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, 7L, null, null, null, null, List.of(2L, 3L), "Nova observacio", false, null, null);
        AppAuthorized updated = AppAuthorized.builder().id(1L).build();
        AppAuthorizedOutputDTO response = AppAuthorizedOutputDTO.builder().id(1L).build();
        AppAuthorizedTypeLink existingLink1 = AppAuthorizedTypeLink.builder().id(100L).appAuthorizedId(1L).authorizationTypeId(1L).build();
        AppAuthorizedTypeLink existingLink2 = AppAuthorizedTypeLink.builder().id(101L).appAuthorizedId(1L).authorizationTypeId(2L).build();
        when(appAuthorizedRepository.findById(1L)).thenReturn(activeModel);
        when(appAuthorizedRepository.update(activeModel, 1L)).thenReturn(updated);
        when(appAuthorizedTypeLinkRepository.findAllActiveByAppAuthorizedId(1L)).thenReturn(List.of(existingLink1, existingLink2));
        when(appAuthorizedMapper.toResponse(updated)).thenReturn(response);

        AppAuthorizedOutputDTO result = appAuthorizedServiceFacadeBean.update(1L, inputDTO);

        verify(appAuthorizedMapper).updateModelFromInput(inputDTO, activeModel);
        verify(appAuthorizedRepository, never()).existsByAppResponsibleAuthorizedAndPersonAndIdNot(any(), any(), any());
        verifyNoInteractions(personService);
        verify(appAuthorizedTypeLinkRepository).delete(existingLink1);
        verify(appAuthorizedTypeLinkRepository, never()).delete(existingLink2);
        assertNotNull(existingLink1.getDeletedAt());
        assertNotNull(existingLink1.getDeletedBy());
        ArgumentCaptor<AppAuthorizedTypeLink> captor = ArgumentCaptor.forClass(AppAuthorizedTypeLink.class);
        verify(appAuthorizedTypeLinkRepository).create(captor.capture());
        assertEquals(3L, captor.getValue().getAuthorizationTypeId());
        assertEquals(response, result);
    }

    @Test
    void update_sameAuthorizationTypeIdSet_touchesNothing() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, 7L, null, null, null, null, List.of(1L, 2L), "Nova observacio", false, null, null);
        AppAuthorized updated = AppAuthorized.builder().id(1L).build();
        AppAuthorizedOutputDTO response = AppAuthorizedOutputDTO.builder().id(1L).build();
        AppAuthorizedTypeLink existingLink1 = AppAuthorizedTypeLink.builder().id(100L).appAuthorizedId(1L).authorizationTypeId(1L).build();
        AppAuthorizedTypeLink existingLink2 = AppAuthorizedTypeLink.builder().id(101L).appAuthorizedId(1L).authorizationTypeId(2L).build();
        when(appAuthorizedRepository.findById(1L)).thenReturn(activeModel);
        when(appAuthorizedRepository.update(activeModel, 1L)).thenReturn(updated);
        when(appAuthorizedTypeLinkRepository.findAllActiveByAppAuthorizedId(1L)).thenReturn(List.of(existingLink1, existingLink2));
        when(appAuthorizedMapper.toResponse(updated)).thenReturn(response);

        AppAuthorizedOutputDTO result = appAuthorizedServiceFacadeBean.update(1L, inputDTO);

        verify(appAuthorizedTypeLinkRepository, never()).delete(any());
        verify(appAuthorizedTypeLinkRepository, never()).create(any());
        assertEquals(response, result);
    }

    @Test
    void update_personChanged_throwsBusinessRuleException() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, 8L, null, null, null, null, List.of(2L), null, false, null, null);
        when(appAuthorizedRepository.findById(1L)).thenReturn(activeModel);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appAuthorizedServiceFacadeBean.update(1L, inputDTO));

        assertEquals(Constants.ERR_APPAUTHORIZED_PERSON_IMMUTABLE, ex.getMessage());
        verify(appAuthorizedRepository, never()).update(any(), any());
    }

    // ------------------------------------------------------------------
    // delete
    // ------------------------------------------------------------------

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(appAuthorizedRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appAuthorizedServiceFacadeBean.delete(99L, null));
        assertEquals(Constants.ERR_APPAUTHORIZED_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeModel.setDeletedAt(LocalDateTime.now());
        when(appAuthorizedRepository.findById(1L)).thenReturn(activeModel);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appAuthorizedServiceFacadeBean.delete(1L, null));
        assertEquals(Constants.ERR_APPAUTHORIZED_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_setsObservationAndSoftDeletes() {
        AppAuthorizedDeleteDTO dto = new AppAuthorizedDeleteDTO("no longer needed");
        when(appAuthorizedRepository.findById(1L)).thenReturn(activeModel);

        appAuthorizedServiceFacadeBean.delete(1L, dto);

        assertNotNull(activeModel.getDeletedAt());
        assertEquals("no longer needed", activeModel.getObservation());
        verify(appAuthorizedRepository).delete(activeModel);
    }

    @Test
    void delete_valid_withoutDto_leavesObservationNull() {
        when(appAuthorizedRepository.findById(1L)).thenReturn(activeModel);

        appAuthorizedServiceFacadeBean.delete(1L, null);

        assertNull(activeModel.getObservation());
        verify(appAuthorizedRepository).delete(activeModel);
    }

    // ------------------------------------------------------------------
    // reactivate
    // ------------------------------------------------------------------

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(appAuthorizedRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appAuthorizedServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_APPAUTHORIZED_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(appAuthorizedRepository.findById(1L)).thenReturn(activeModel);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appAuthorizedServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_APPAUTHORIZED_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_duplicateAssignmentNowExists_throwsBusinessRuleException() {
        activeModel.setDeletedAt(LocalDateTime.now());
        when(appAuthorizedRepository.findById(1L)).thenReturn(activeModel);
        when(appAuthorizedRepository.existsByAppResponsibleAuthorizedAndPersonAndIdNot(10L, 7L, 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appAuthorizedServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_APPAUTHORIZED_DUPLICATED, ex.getMessage());
        verify(appAuthorizedRepository, never()).update(any(), any());
    }

    @Test
    void reactivate_valid_clearsDeletedFieldsAndReturnsResponse() {
        activeModel.setDeletedAt(LocalDateTime.now());
        activeModel.setDeletedBy("someone");
        AppAuthorized updated = AppAuthorized.builder().id(1L).build();
        AppAuthorizedOutputDTO response = AppAuthorizedOutputDTO.builder().id(1L).build();
        when(appAuthorizedRepository.findById(1L)).thenReturn(activeModel);
        when(appAuthorizedRepository.existsByAppResponsibleAuthorizedAndPersonAndIdNot(10L, 7L, 1L)).thenReturn(false);
        when(appAuthorizedRepository.update(activeModel, 1L)).thenReturn(updated);
        when(appAuthorizedTypeLinkRepository.findAllActiveByAppAuthorizedId(1L)).thenReturn(List.of());
        when(appAuthorizedMapper.toResponse(updated)).thenReturn(response);

        AppAuthorizedOutputDTO result = appAuthorizedServiceFacadeBean.reactivate(1L);

        assertNull(activeModel.getDeletedAt());
        assertNull(activeModel.getDeletedBy());
        assertEquals(response, result);
    }

}
