package es.caib.invai.back.ejb.application.responsibleAuthorized.responsible;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleDeleteDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonOutputDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.dir3.Dir3ValidationRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleCriteria;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleRepository;
import es.caib.invai.back.persistence.repository.catalog.responsibleType.ResponsibleTypeRepository;
import es.caib.invai.back.service.facade.maintenance.responsible.person.PersonService;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.responsible.AppResponsibleMapper;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.dir3.Dir3Validation;
import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import es.caib.invai.back.service.model.catalog.dir3Status.Dir3ValidationStatus;
import es.caib.invai.back.service.model.catalog.responsibleType.ResponsibleType;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppResponsibleServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link AppResponsibleRepository} and {@link AppResponsibleMapper} collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class AppResponsibleServiceFacadeBeanTest {

    @Mock
    private AppResponsibleMapper appResponsibleMapper;

    @Mock
    private AppResponsibleRepository appResponsibleRepository;

    @Mock
    private ResponsibleTypeRepository responsibleTypeRepository;

    @Mock
    private PersonService personService;

    @Mock
    private Dir3ValidationRepository dir3ValidationRepository;

    @Mock
    private AppAuthorizedRepository appAuthorizedRepository;

    @InjectMocks
    private AppResponsibleServiceFacadeBean appResponsibleServiceFacadeBean;

    private AppResponsible activeAppResponsible;

    @BeforeEach
    void setUp() {
        Application application = new Application();
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(40L).application(application).build();
        ResponsibleType responsibleType = new ResponsibleType();
        responsibleType.setId(20L);
        Person person = new Person();
        person.setId(10L);
        Dir3Validation dir3Validation = Dir3Validation.builder().id(500L).dir3Status(Dir3ValidationStatus.NOT_APPLY).build();

        activeAppResponsible = new AppResponsible();
        activeAppResponsible.setId(1L);
        activeAppResponsible.setAppResponsibleAuthorized(anchor);
        activeAppResponsible.setResponsibleType(responsibleType);
        activeAppResponsible.setPerson(person);
        activeAppResponsible.setDir3Validation(dir3Validation);

        lenient().when(dir3ValidationRepository.create(any())).thenAnswer(invocation -> {
            Dir3Validation created = invocation.getArgument(0);
            created.setId(600L);
            return created;
        });
        lenient().when(personService.getPersonById(10L)).thenReturn(person);
    }

    private static ResponsibleType responsibleType(Long id, String name) {
        ResponsibleType type = new ResponsibleType();
        type.setId(id);
        type.setName(name);
        return type;
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ------------------------------------------------------------------
    // getAll
    // ------------------------------------------------------------------

    @Test
    void getAll_noCriteria_returnsEveryCatalogTypeWithAssignedAndPlaceholderRows() {
        ResponsibleType assignedType = responsibleType(20L, "Responsable de la informacio");
        ResponsibleType unassignedType = responsibleType(21L, "Responsable del servei");
        Pageable pageable = PageRequest.of(0, 10);
        AppResponsibleOutputDTO mappedAssigned = new AppResponsibleOutputDTO();
        when(responsibleTypeRepository.findAll()).thenReturn(List.of(assignedType, unassignedType));
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(40L, 20L)).thenReturn(activeAppResponsible);
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(40L, 21L)).thenReturn(null);
        when(appResponsibleMapper.toResponse(activeAppResponsible)).thenReturn(mappedAssigned);

        Page<AppResponsibleOutputDTO> result = appResponsibleServiceFacadeBean.getAll(40L, null, pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(mappedAssigned, result.getContent().get(0));
        AppResponsibleOutputDTO placeholder = result.getContent().get(1);
        assertNull(placeholder.getId());
        assertNull(placeholder.getPerson());
        assertEquals(40L, placeholder.getAppResponsibleAuthorizedId());
        assertEquals(unassignedType, placeholder.getResponsibleType());
    }

    @Test
    void getAll_activeStatusCriteria_usesCatalogDrivenListing() {
        AppResponsibleCriteria criteria = AppResponsibleCriteria.builder().statusId(StatusEnum.ACTIVE.getId()).build();
        Pageable pageable = PageRequest.of(0, 10);
        ResponsibleType type = responsibleType(20L, "Responsable de la informacio");
        when(responsibleTypeRepository.findAll()).thenReturn(List.of(type));
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(40L, 20L)).thenReturn(null);

        Page<AppResponsibleOutputDTO> result = appResponsibleServiceFacadeBean.getAll(40L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        verify(appResponsibleRepository, never()).findAll(any(), any(), any());
    }

    @Test
    void getAll_inactiveStatusCriteria_fallsBackToRowBasedListing() {
        AppResponsibleCriteria criteria = AppResponsibleCriteria.builder().statusId(StatusEnum.INACTIVE.getId()).build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<AppResponsible> domainPage = new PageImpl<>(List.of(activeAppResponsible));
        AppResponsibleOutputDTO mapped = new AppResponsibleOutputDTO();
        when(appResponsibleRepository.findAll(40L, criteria, pageable)).thenReturn(domainPage);
        when(appResponsibleMapper.toResponse(activeAppResponsible)).thenReturn(mapped);

        Page<AppResponsibleOutputDTO> result = appResponsibleServiceFacadeBean.getAll(40L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
        verify(responsibleTypeRepository, never()).findAll();
    }

    @Test
    void getAll_personIdCriteria_excludesUnassignedPlaceholderRows() {
        ResponsibleType assignedType = responsibleType(20L, "Responsable de la informacio");
        ResponsibleType unassignedType = responsibleType(21L, "Responsable del servei");
        Pageable pageable = PageRequest.of(0, 10);
        AppResponsibleCriteria criteria = AppResponsibleCriteria.builder().personId(10L).build();
        AppResponsibleOutputDTO mappedAssigned = new AppResponsibleOutputDTO();
        PersonOutputDTO person = new PersonOutputDTO();
        person.setId(10L);
        mappedAssigned.setPerson(person);
        when(responsibleTypeRepository.findAll()).thenReturn(List.of(assignedType, unassignedType));
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(40L, 20L)).thenReturn(activeAppResponsible);
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(40L, 21L)).thenReturn(null);
        when(appResponsibleMapper.toResponse(activeAppResponsible)).thenReturn(mappedAssigned);

        Page<AppResponsibleOutputDTO> result = appResponsibleServiceFacadeBean.getAll(40L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mappedAssigned, result.getContent().get(0));
    }

    @Test
    void getAll_pagesInMemoryOverCatalogTypes() {
        ResponsibleType type1 = responsibleType(20L, "A");
        ResponsibleType type2 = responsibleType(21L, "B");
        ResponsibleType type3 = responsibleType(22L, "C");
        Pageable pageable = PageRequest.of(1, 2);
        when(responsibleTypeRepository.findAll()).thenReturn(List.of(type1, type2, type3));
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(eq(40L), any())).thenReturn(null);

        Page<AppResponsibleOutputDTO> result = appResponsibleServiceFacadeBean.getAll(40L, null, pageable);

        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(type3, result.getContent().get(0).getResponsibleType());
    }

    // ------------------------------------------------------------------
    // create
    // ------------------------------------------------------------------

    @Test
    void create_noExistingHolder_persistsAndReturnsResponse() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, false, null, null);
        AppResponsible model = new AppResponsible();
        AppResponsible saved = new AppResponsible();
        AppResponsibleOutputDTO response = new AppResponsibleOutputDTO();
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(40L, 20L)).thenReturn(null);
        when(appResponsibleMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appResponsibleRepository.create(model)).thenReturn(saved);
        when(appResponsibleMapper.toResponse(saved)).thenReturn(response);

        AppResponsibleOutputDTO result = appResponsibleServiceFacadeBean.create(inputDTO);

        verify(appResponsibleRepository, never()).delete(any());
        verify(personService).getPersonById(10L);
        assertEquals(response, result);
    }

    @Test
    void create_personIdGivenDoesNotExist_throwsBusinessRuleException() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, false, null, null);
        when(personService.getPersonById(10L)).thenThrow(new BusinessRuleException(Constants.ERR_PERSON_NOT_FOUND));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appResponsibleServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_PERSON_NOT_FOUND, ex.getMessage());
        verify(appResponsibleRepository, never()).create(any());
    }

    @Test
    void create_existingHolder_deactivatesOldAndCreatesNew() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, false, null, null);
        AppResponsible model = new AppResponsible();
        AppResponsible saved = new AppResponsible();
        AppResponsibleOutputDTO response = new AppResponsibleOutputDTO();
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(40L, 20L)).thenReturn(activeAppResponsible);
        when(appResponsibleMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appResponsibleRepository.create(model)).thenReturn(saved);
        when(appResponsibleMapper.toResponse(saved)).thenReturn(response);

        AppResponsibleOutputDTO result = appResponsibleServiceFacadeBean.create(inputDTO);

        assertNotNull(activeAppResponsible.getDeletedAt());
        assertNull(activeAppResponsible.getObservation());
        verify(appResponsibleRepository).delete(activeAppResponsible);
        verify(appResponsibleRepository).create(model);
        assertEquals(response, result);
    }

    @Test
    void create_persistsDir3StatusFromInputDTOAsIs() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, true, true, null);
        AppResponsible model = new AppResponsible();
        AppResponsible saved = new AppResponsible();
        AppResponsibleOutputDTO response = new AppResponsibleOutputDTO();
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(40L, 20L)).thenReturn(null);
        when(appResponsibleMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appResponsibleRepository.create(model)).thenReturn(saved);
        when(appResponsibleMapper.toResponse(saved)).thenReturn(response);

        appResponsibleServiceFacadeBean.create(inputDTO);

        ArgumentCaptor<Dir3Validation> captor = ArgumentCaptor.forClass(Dir3Validation.class);
        verify(dir3ValidationRepository).create(captor.capture());
        assertEquals(Dir3ValidationStatus.VALIDATED, captor.getValue().getDir3Status());
        assertEquals(Dir3ValidationStatus.VALIDATED, model.getDir3Validation().getDir3Status());
        verify(personService, never()).checkDir3(any(), any());
    }

    @Test
    void create_personHasAnotherActiveResponsibleAssignment_reusesSharedDir3Validation() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 21L, null, null, true, false, null);
        AppResponsible model = new AppResponsible();
        AppResponsible saved = new AppResponsible();
        AppResponsibleOutputDTO response = new AppResponsibleOutputDTO();
        Dir3Validation sharedDir3Validation = Dir3Validation.builder().id(500L).dir3Status(Dir3ValidationStatus.MANUAL).build();
        AppResponsible siblingAssignment = new AppResponsible();
        siblingAssignment.setDir3Validation(sharedDir3Validation);
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(40L, 21L)).thenReturn(null);
        when(appResponsibleRepository.findAllActiveByAppResponsibleAuthorizedAndPerson(40L, 10L)).thenReturn(List.of(siblingAssignment));
        when(appResponsibleMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appResponsibleRepository.create(model)).thenReturn(saved);
        when(appResponsibleMapper.toResponse(saved)).thenReturn(response);

        appResponsibleServiceFacadeBean.create(inputDTO);

        assertSame(sharedDir3Validation, model.getDir3Validation());
        verify(dir3ValidationRepository, never()).create(any());
        verify(appAuthorizedRepository, never()).findActiveByAppResponsibleAuthorizedAndPerson(any(), any());
    }

    @Test
    void create_personHasActiveAuthorizedAssignmentOnSameAnchor_reusesSharedDir3Validation() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, true, false, null);
        AppResponsible model = new AppResponsible();
        AppResponsible saved = new AppResponsible();
        AppResponsibleOutputDTO response = new AppResponsibleOutputDTO();
        Dir3Validation sharedDir3Validation = Dir3Validation.builder().id(500L).dir3Status(Dir3ValidationStatus.VALIDATED).build();
        AppAuthorized existingAuthorized = AppAuthorized.builder().dir3Validation(sharedDir3Validation).build();
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(40L, 20L)).thenReturn(null);
        when(appResponsibleRepository.findAllActiveByAppResponsibleAuthorizedAndPerson(40L, 10L)).thenReturn(List.of());
        when(appAuthorizedRepository.findActiveByAppResponsibleAuthorizedAndPerson(40L, 10L)).thenReturn(existingAuthorized);
        when(appResponsibleMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appResponsibleRepository.create(model)).thenReturn(saved);
        when(appResponsibleMapper.toResponse(saved)).thenReturn(response);

        appResponsibleServiceFacadeBean.create(inputDTO);

        assertSame(sharedDir3Validation, model.getDir3Validation());
        verify(dir3ValidationRepository, never()).create(any());
    }

    @Test
    void create_syncsPersonalCaibOnlyAfterPersistingTheRecord() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, false, null, null);
        AppResponsible model = new AppResponsible();
        AppResponsible saved = new AppResponsible();
        AppResponsibleOutputDTO response = new AppResponsibleOutputDTO();
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(40L, 20L)).thenReturn(null);
        when(appResponsibleMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appResponsibleRepository.create(model)).thenReturn(saved);
        when(appResponsibleMapper.toResponse(saved)).thenReturn(response);

        appResponsibleServiceFacadeBean.create(inputDTO);

        InOrder inOrder = inOrder(appResponsibleRepository, personService);
        inOrder.verify(appResponsibleRepository).create(model);
        inOrder.verify(personService).updatePersonalCaibToPerson(10L, false);
    }

    @Test
    void create_noPersonIdButNoNameOrEmail_throwsBusinessRuleException() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, null, null, null, null, null, 20L, null, null, true, null, null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appResponsibleServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_APPRESPONSIBLE_PERSON_DATA_REQUIRED, ex.getMessage());
        verify(appResponsibleRepository, never()).create(any());
    }

    @Test
    void create_noPersonIdGiven_resolvesPersonIdViaPersonService() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, null, "Joan", "Fuster", "joan@caib.es", null, 20L, null, null, true, null, null);
        Person resolved = new Person();
        resolved.setId(99L);
        AppResponsible model = new AppResponsible();
        AppResponsible saved = new AppResponsible();
        AppResponsibleOutputDTO response = new AppResponsibleOutputDTO();
        when(personService.getOrCreatePerson("Joan", "Fuster", "joan@caib.es", true, null, null)).thenReturn(resolved);
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(40L, 20L)).thenReturn(null);
        when(appResponsibleMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appResponsibleRepository.create(model)).thenReturn(saved);
        when(appResponsibleMapper.toResponse(saved)).thenReturn(response);

        appResponsibleServiceFacadeBean.create(inputDTO);

        assertEquals(99L, inputDTO.getPersonId());
    }

    @Test
    void create_personServiceResolveOrCreatePersonThrows_propagatesException() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, null, "Maria", "Puig", "maria@extern.es", null, 20L, null, null, false, null, null);
        when(personService.getOrCreatePerson("Maria", "Puig", "maria@extern.es", false, null, null))
                .thenThrow(new BusinessRuleException(Constants.ERR_PERSON_COMPANY_REQUIRED_WHEN_NOT_CAIB));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appResponsibleServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_PERSON_COMPANY_REQUIRED_WHEN_NOT_CAIB, ex.getMessage());
        verify(appResponsibleRepository, never()).create(any());
    }

    @Test
    void create_delegatesPersonalCaibSyncToPersonService() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, false, null, null);
        AppResponsible model = new AppResponsible();
        AppResponsible saved = new AppResponsible();
        AppResponsibleOutputDTO response = new AppResponsibleOutputDTO();
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(40L, 20L)).thenReturn(null);
        when(appResponsibleMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appResponsibleRepository.create(model)).thenReturn(saved);
        when(appResponsibleMapper.toResponse(saved)).thenReturn(response);

        appResponsibleServiceFacadeBean.create(inputDTO);

        verify(personService).updatePersonalCaibToPerson(10L, false);
    }

    @Test
    void create_personalCaibNull_treatedAsFalseWithoutThrowing() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, null, null, null);
        AppResponsible model = new AppResponsible();
        AppResponsible saved = new AppResponsible();
        AppResponsibleOutputDTO response = new AppResponsibleOutputDTO();
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(40L, 20L)).thenReturn(null);
        when(appResponsibleMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appResponsibleRepository.create(model)).thenReturn(saved);
        when(appResponsibleMapper.toResponse(saved)).thenReturn(response);

        appResponsibleServiceFacadeBean.create(inputDTO);

        verify(personService).updatePersonalCaibToPerson(10L, false);
    }

    @Test
    void update_personalCaibNull_treatedAsFalseWithoutThrowing() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, null, null, null);
        AppResponsibleOutputDTO response = new AppResponsibleOutputDTO();
        when(appResponsibleRepository.findById(1L)).thenReturn(activeAppResponsible);
        when(appResponsibleRepository.update(activeAppResponsible, 1L)).thenReturn(activeAppResponsible);
        when(appResponsibleMapper.toResponse(activeAppResponsible)).thenReturn(response);

        appResponsibleServiceFacadeBean.update(1L, inputDTO);

        verify(personService).updatePersonalCaibToPerson(10L, false);
    }

    @Test
    void create_personServiceSyncPersonalCaibThrows_propagatesException() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, false, null, null);
        AppResponsible model = new AppResponsible();
        AppResponsible saved = new AppResponsible();
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(40L, 20L)).thenReturn(null);
        when(appResponsibleMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appResponsibleRepository.create(model)).thenReturn(saved);
        doThrow(new BusinessRuleException(Constants.ERR_PERSON_COMPANY_REQUIRED_WHEN_NOT_CAIB))
                .when(personService).updatePersonalCaibToPerson(10L, false);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appResponsibleServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_PERSON_COMPANY_REQUIRED_WHEN_NOT_CAIB, ex.getMessage());
        verify(appResponsibleRepository).create(model);
    }

    @Test
    void create_responsibleTypeRequiresPersonalCaib_personNotCaib_throwsBusinessRuleException() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, false, null, null);
        ResponsibleType type = responsibleType(20L, "Responsable de la informacio");
        type.setRequiresPersonalCaib(true);
        when(responsibleTypeRepository.findById(20L)).thenReturn(type);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appResponsibleServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_APPRESPONSIBLE_REQUIRES_PERSONAL_CAIB, ex.getMessage());
        verify(appResponsibleRepository, never()).create(any());
        verify(personService, never()).updatePersonalCaibToPerson(any(), anyBoolean());
    }

    @Test
    void create_responsibleTypeRequiresPersonalCaib_rejectsBeforeResolvingInlinePerson() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, null, "Maria", "Puig", "maria@extern.es", null, 20L, null, null, false, null, null);
        ResponsibleType type = responsibleType(20L, "Responsable de la informacio");
        type.setRequiresPersonalCaib(true);
        when(responsibleTypeRepository.findById(20L)).thenReturn(type);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appResponsibleServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_APPRESPONSIBLE_REQUIRES_PERSONAL_CAIB, ex.getMessage());
        verify(personService, never()).getOrCreatePerson(any(), any(), any(), anyBoolean(), any(), any());
        verify(appResponsibleRepository, never()).create(any());
    }

    @Test
    void create_responsibleTypeRequiresPersonalCaib_personIsCaib_succeeds() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, true, null, null);
        ResponsibleType type = responsibleType(20L, "Responsable de la informacio");
        type.setRequiresPersonalCaib(true);
        AppResponsible model = new AppResponsible();
        AppResponsible saved = new AppResponsible();
        AppResponsibleOutputDTO response = new AppResponsibleOutputDTO();
        when(responsibleTypeRepository.findById(20L)).thenReturn(type);
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(40L, 20L)).thenReturn(null);
        when(appResponsibleMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appResponsibleRepository.create(model)).thenReturn(saved);
        when(appResponsibleMapper.toResponse(saved)).thenReturn(response);

        AppResponsibleOutputDTO result = appResponsibleServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    // ------------------------------------------------------------------
    // update
    // ------------------------------------------------------------------

    @Test
    void update_notFound_throwsBusinessRuleException() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, false, null, null);
        when(appResponsibleRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appResponsibleServiceFacadeBean.update(99L, inputDTO));

        assertEquals(Constants.ERR_APPRESPONSIBLE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_responsibleTypeRequiresPersonalCaib_personIsNotCaib_throwsBusinessRuleException() {
        ResponsibleType type = responsibleType(20L, "Responsable de la informacio");
        type.setRequiresPersonalCaib(true);
        activeAppResponsible.setResponsibleType(type);
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, false, null, null);
        when(appResponsibleRepository.findById(1L)).thenReturn(activeAppResponsible);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appResponsibleServiceFacadeBean.update(1L, inputDTO));

        assertEquals(Constants.ERR_APPRESPONSIBLE_REQUIRES_PERSONAL_CAIB, ex.getMessage());
        verify(appResponsibleRepository, never()).update(any(), any());
        verify(personService, never()).updatePersonalCaibToPerson(any(), anyBoolean());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 21L, null, null, false, null, null);
        AppResponsible updated = new AppResponsible();
        AppResponsibleOutputDTO response = new AppResponsibleOutputDTO();
        when(appResponsibleRepository.findById(1L)).thenReturn(activeAppResponsible);
        when(appResponsibleRepository.update(activeAppResponsible, 1L)).thenReturn(updated);
        when(appResponsibleMapper.toResponse(updated)).thenReturn(response);

        AppResponsibleOutputDTO result = appResponsibleServiceFacadeBean.update(1L, inputDTO);

        verify(appResponsibleMapper).updateModelFromInput(inputDTO, activeAppResponsible);
        assertEquals(response, result);
    }

    @Test
    void update_syncsPersonalCaibOnlyAfterUpdatingTheRecord() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 21L, null, null, false, null, null);
        AppResponsible updated = new AppResponsible();
        AppResponsibleOutputDTO response = new AppResponsibleOutputDTO();
        when(appResponsibleRepository.findById(1L)).thenReturn(activeAppResponsible);
        when(appResponsibleRepository.update(activeAppResponsible, 1L)).thenReturn(updated);
        when(appResponsibleMapper.toResponse(updated)).thenReturn(response);

        appResponsibleServiceFacadeBean.update(1L, inputDTO);

        InOrder inOrder = inOrder(appResponsibleRepository, personService);
        inOrder.verify(appResponsibleRepository).update(activeAppResponsible, 1L);
        inOrder.verify(personService).updatePersonalCaibToPerson(10L, false);
    }

    @Test
    void update_personChanged_throwsBusinessRuleException() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 11L, null, null, null, null, 20L, null, null, false, null, null);
        when(appResponsibleRepository.findById(1L)).thenReturn(activeAppResponsible);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appResponsibleServiceFacadeBean.update(1L, inputDTO));

        assertEquals(Constants.ERR_APPRESPONSIBLE_PERSON_IMMUTABLE, ex.getMessage());
        verify(appResponsibleRepository, never()).update(any(), any());
    }

    // ------------------------------------------------------------------
    // delete
    // ------------------------------------------------------------------

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(appResponsibleRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appResponsibleServiceFacadeBean.delete(99L, null));

        assertEquals(Constants.ERR_APPRESPONSIBLE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeAppResponsible.setDeletedAt(LocalDateTime.now());
        when(appResponsibleRepository.findById(1L)).thenReturn(activeAppResponsible);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appResponsibleServiceFacadeBean.delete(1L, null));

        assertEquals(Constants.ERR_APPRESPONSIBLE_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_validWithObservation_softDeletes() {
        AppResponsibleDeleteDTO dto = new AppResponsibleDeleteDTO("Deixa l'empresa");
        when(appResponsibleRepository.findById(1L)).thenReturn(activeAppResponsible);

        appResponsibleServiceFacadeBean.delete(1L, dto);

        assertNotNull(activeAppResponsible.getDeletedAt());
        assertEquals("Deixa l'empresa", activeAppResponsible.getObservation());
        verify(appResponsibleRepository, times(1)).delete(activeAppResponsible);
    }

    @Test
    void delete_validWithoutDto_setsNullObservation() {
        when(appResponsibleRepository.findById(1L)).thenReturn(activeAppResponsible);

        appResponsibleServiceFacadeBean.delete(1L, null);

        assertNull(activeAppResponsible.getObservation());
        assertNotNull(activeAppResponsible.getDeletedAt());
    }

    // ------------------------------------------------------------------
    // reactivate
    // ------------------------------------------------------------------

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(appResponsibleRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appResponsibleServiceFacadeBean.reactivate(99L));

        assertEquals(Constants.ERR_APPRESPONSIBLE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(appResponsibleRepository.findById(1L)).thenReturn(activeAppResponsible);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appResponsibleServiceFacadeBean.reactivate(1L));

        assertEquals(Constants.ERR_APPRESPONSIBLE_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_valid_reCheckDuplicate_throwsBusinessRuleException() {
        activeAppResponsible.setDeletedAt(LocalDateTime.now());
        activeAppResponsible.setDeletedBy("someone");
        when(appResponsibleRepository.findById(1L)).thenReturn(activeAppResponsible);
        when(appResponsibleRepository.existsByAppResponsibleAuthorizedAndResponsibleTypeAndIdNot(40L, 20L, 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appResponsibleServiceFacadeBean.reactivate(1L));

        assertEquals(Constants.ERR_APPRESPONSIBLE_DUPLICATED, ex.getMessage());
        verify(appResponsibleRepository, never()).update(any(), any());
    }

    @Test
    void reactivate_valid_reactivatesAppResponsible() {
        activeAppResponsible.setDeletedAt(LocalDateTime.now());
        activeAppResponsible.setDeletedBy("someone");
        AppResponsible reactivated = new AppResponsible();
        AppResponsibleOutputDTO response = new AppResponsibleOutputDTO();
        when(appResponsibleRepository.findById(1L)).thenReturn(activeAppResponsible);
        when(appResponsibleRepository.existsByAppResponsibleAuthorizedAndResponsibleTypeAndIdNot(40L, 20L, 1L)).thenReturn(false);
        when(appResponsibleRepository.update(eq(activeAppResponsible), eq(1L))).thenReturn(reactivated);
        when(appResponsibleMapper.toResponse(reactivated)).thenReturn(response);

        AppResponsibleOutputDTO result = appResponsibleServiceFacadeBean.reactivate(1L);

        assertNull(activeAppResponsible.getDeletedAt());
        assertNull(activeAppResponsible.getDeletedBy());
        assertEquals(response, result);
    }

}
