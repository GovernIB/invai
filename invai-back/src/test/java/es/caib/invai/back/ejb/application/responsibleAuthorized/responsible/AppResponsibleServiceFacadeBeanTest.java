package es.caib.invai.back.ejb.application.responsibleAuthorized.responsible;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleDeleteDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonOutputDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleCriteria;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleRepository;
import es.caib.invai.back.persistence.repository.catalog.responsibleType.ResponsibleTypeRepository;
import es.caib.invai.back.persistence.repository.maintenance.responsible.person.PersonRepository;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.responsible.AppResponsibleMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import es.caib.invai.back.service.model.catalog.responsibleType.ResponsibleType;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import es.caib.invai.back.service.model.maintenance.responsible.company.Company;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.eq;
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
    private PersonRepository personRepository;

    @InjectMocks
    private AppResponsibleServiceFacadeBean appResponsibleServiceFacadeBean;

    private AppResponsible activeAppResponsible;

    @BeforeEach
    void setUp() {
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(40L).build();
        ResponsibleType responsibleType = new ResponsibleType();
        responsibleType.setId(20L);

        activeAppResponsible = new AppResponsible();
        activeAppResponsible.setId(1L);
        activeAppResponsible.setAppResponsibleAuthorized(anchor);
        activeAppResponsible.setResponsibleType(responsibleType);
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
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, false);
        AppResponsible model = new AppResponsible();
        AppResponsible saved = new AppResponsible();
        AppResponsibleOutputDTO response = new AppResponsibleOutputDTO();
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(40L, 20L)).thenReturn(null);
        when(appResponsibleMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appResponsibleRepository.create(model)).thenReturn(saved);
        when(appResponsibleMapper.toResponse(saved)).thenReturn(response);

        AppResponsibleOutputDTO result = appResponsibleServiceFacadeBean.create(inputDTO);

        verify(appResponsibleRepository, never()).delete(any());
        assertEquals(response, result);
    }

    @Test
    void create_existingHolder_deactivatesOldAndCreatesNew() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, false);
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
    void create_noPersonIdButNoNameOrEmail_throwsBusinessRuleException() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, null, null, null, null, null, 20L, null, null, true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appResponsibleServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_APPRESPONSIBLE_PERSON_DATA_REQUIRED, ex.getMessage());
        verify(appResponsibleRepository, never()).create(any());
    }

    @Test
    void create_noPersonIdWithExistingEmail_reusesExistingPersonAndSkipsCreation() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, null, "Joan", "Fuster", "joan@caib.es", null, 20L, null, null, true);
        Person existingPerson = new Person();
        existingPerson.setId(99L);
        existingPerson.setPersonalCaib(true);
        AppResponsible model = new AppResponsible();
        AppResponsible saved = new AppResponsible();
        AppResponsibleOutputDTO response = new AppResponsibleOutputDTO();
        when(personRepository.findByEmail("joan@caib.es")).thenReturn(existingPerson);
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(40L, 20L)).thenReturn(null);
        when(appResponsibleMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appResponsibleRepository.create(model)).thenReturn(saved);
        when(appResponsibleMapper.toResponse(saved)).thenReturn(response);

        appResponsibleServiceFacadeBean.create(inputDTO);

        assertEquals(99L, inputDTO.getPersonId());
        verify(personRepository, never()).create(any());
    }

    @Test
    void create_noPersonIdNoExistingEmailPersonalCaib_createsNewCaibPerson() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, null, "Joan", "Fuster", "joan@caib.es", null, 20L, null, null, true);
        AppResponsible model = new AppResponsible();
        AppResponsible saved = new AppResponsible();
        AppResponsibleOutputDTO response = new AppResponsibleOutputDTO();
        Person createdPerson = new Person();
        createdPerson.setId(55L);
        when(personRepository.findByEmail("joan@caib.es")).thenReturn(null);
        when(personRepository.create(any())).thenReturn(createdPerson);
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(40L, 20L)).thenReturn(null);
        when(appResponsibleMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appResponsibleRepository.create(model)).thenReturn(saved);
        when(appResponsibleMapper.toResponse(saved)).thenReturn(response);

        appResponsibleServiceFacadeBean.create(inputDTO);

        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).create(captor.capture());
        assertEquals("Joan", captor.getValue().getFirstName());
        assertEquals("Fuster", captor.getValue().getLastName());
        assertEquals("joan@caib.es", captor.getValue().getEmail());
        assertTrue(captor.getValue().isPersonalCaib());
        assertNull(captor.getValue().getCompany());
        assertEquals(55L, inputDTO.getPersonId());
    }

    @Test
    void create_noPersonIdExternalWithoutCompany_throwsBusinessRuleException() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, null, "Maria", "Puig", "maria@extern.es", null, 20L, null, null, false);
        when(personRepository.findByEmail("maria@extern.es")).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appResponsibleServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_PERSON_COMPANY_REQUIRED_WHEN_NOT_CAIB, ex.getMessage());
        verify(personRepository, never()).create(any());
    }

    @Test
    void create_noPersonIdExternalWithCompany_createsNewExternalPersonWithCompany() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, null, "Maria", "Puig", "maria@extern.es", 5L, 20L, null, null, false);
        AppResponsible model = new AppResponsible();
        AppResponsible saved = new AppResponsible();
        AppResponsibleOutputDTO response = new AppResponsibleOutputDTO();
        Person createdPerson = new Person();
        createdPerson.setId(56L);
        when(personRepository.findByEmail("maria@extern.es")).thenReturn(null);
        when(personRepository.create(any())).thenReturn(createdPerson);
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(40L, 20L)).thenReturn(null);
        when(appResponsibleMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appResponsibleRepository.create(model)).thenReturn(saved);
        when(appResponsibleMapper.toResponse(saved)).thenReturn(response);

        appResponsibleServiceFacadeBean.create(inputDTO);

        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).create(captor.capture());
        assertFalse(captor.getValue().isPersonalCaib());
        assertEquals(5L, captor.getValue().getCompany().getId());
        assertEquals(56L, inputDTO.getPersonId());
    }

    @Test
    void create_personalCaibDiffersFromStoredValue_updatesPerson() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, true);
        Person person = new Person();
        person.setId(10L);
        person.setPersonalCaib(false);
        AppResponsible model = new AppResponsible();
        AppResponsible saved = new AppResponsible();
        AppResponsibleOutputDTO response = new AppResponsibleOutputDTO();
        when(personRepository.findById(10L)).thenReturn(person);
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(40L, 20L)).thenReturn(null);
        when(appResponsibleMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appResponsibleRepository.create(model)).thenReturn(saved);
        when(appResponsibleMapper.toResponse(saved)).thenReturn(response);

        appResponsibleServiceFacadeBean.create(inputDTO);

        assertTrue(person.isPersonalCaib());
        verify(personRepository).update(person, 10L);
    }

    @Test
    void create_personalCaibMatchesStoredValue_doesNotTouchPerson() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, true);
        Person person = new Person();
        person.setId(10L);
        person.setPersonalCaib(true);
        AppResponsible model = new AppResponsible();
        AppResponsible saved = new AppResponsible();
        AppResponsibleOutputDTO response = new AppResponsibleOutputDTO();
        when(personRepository.findById(10L)).thenReturn(person);
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(40L, 20L)).thenReturn(null);
        when(appResponsibleMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appResponsibleRepository.create(model)).thenReturn(saved);
        when(appResponsibleMapper.toResponse(saved)).thenReturn(response);

        appResponsibleServiceFacadeBean.create(inputDTO);

        verify(personRepository, never()).update(any(), any());
    }

    @Test
    void create_settingNonCaibWithoutCompany_throwsBusinessRuleException() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, false);
        Person person = new Person();
        person.setId(10L);
        person.setPersonalCaib(true);
        person.setCompany(null);
        when(personRepository.findById(10L)).thenReturn(person);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appResponsibleServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_PERSON_COMPANY_REQUIRED_WHEN_NOT_CAIB, ex.getMessage());
        verify(appResponsibleRepository, never()).create(any());
    }

    @Test
    void create_settingNonCaibWithCompany_updatesPerson() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, false);
        Person person = new Person();
        person.setId(10L);
        person.setPersonalCaib(true);
        person.setCompany(new Company());
        AppResponsible model = new AppResponsible();
        AppResponsible saved = new AppResponsible();
        AppResponsibleOutputDTO response = new AppResponsibleOutputDTO();
        when(personRepository.findById(10L)).thenReturn(person);
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(40L, 20L)).thenReturn(null);
        when(appResponsibleMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appResponsibleRepository.create(model)).thenReturn(saved);
        when(appResponsibleMapper.toResponse(saved)).thenReturn(response);

        appResponsibleServiceFacadeBean.create(inputDTO);

        assertFalse(person.isPersonalCaib());
        verify(personRepository).update(person, 10L);
    }

    @Test
    void create_responsibleTypeRequiresPersonalCaib_personNotCaib_throwsBusinessRuleException() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, false);
        ResponsibleType type = responsibleType(20L, "Responsable de la informacio");
        type.setRequiresPersonalCaib(true);
        Person person = new Person();
        person.setId(10L);
        person.setPersonalCaib(false);
        person.setCompany(new Company());
        when(personRepository.findById(10L)).thenReturn(person);
        when(responsibleTypeRepository.findById(20L)).thenReturn(type);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appResponsibleServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_APPRESPONSIBLE_REQUIRES_PERSONAL_CAIB, ex.getMessage());
        verify(appResponsibleRepository, never()).create(any());
    }

    @Test
    void create_responsibleTypeRequiresPersonalCaib_personIsCaib_succeeds() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, true);
        ResponsibleType type = responsibleType(20L, "Responsable de la informacio");
        type.setRequiresPersonalCaib(true);
        Person person = new Person();
        person.setId(10L);
        person.setPersonalCaib(true);
        AppResponsible model = new AppResponsible();
        AppResponsible saved = new AppResponsible();
        AppResponsibleOutputDTO response = new AppResponsibleOutputDTO();
        when(personRepository.findById(10L)).thenReturn(person);
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
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, false);
        when(appResponsibleRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appResponsibleServiceFacadeBean.update(99L, inputDTO));

        assertEquals(Constants.ERR_APPRESPONSIBLE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_responsibleTypeRequiresPersonalCaib_syncFlipsPersonToNonCaib_throwsBusinessRuleException() {
        ResponsibleType type = responsibleType(20L, "Responsable de la informacio");
        type.setRequiresPersonalCaib(true);
        activeAppResponsible.setResponsibleType(type);
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 10L, null, null, null, null, 20L, null, null, false);
        Person person = new Person();
        person.setId(10L);
        person.setPersonalCaib(true);
        person.setCompany(new Company());
        when(appResponsibleRepository.findById(1L)).thenReturn(activeAppResponsible);
        when(personRepository.findById(10L)).thenReturn(person);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appResponsibleServiceFacadeBean.update(1L, inputDTO));

        assertEquals(Constants.ERR_APPRESPONSIBLE_REQUIRES_PERSONAL_CAIB, ex.getMessage());
        verify(appResponsibleRepository, never()).update(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        AppResponsibleInputDTO inputDTO = new AppResponsibleInputDTO(40L, 11L, null, null, null, null, 21L, null, null, false);
        AppResponsible updated = new AppResponsible();
        AppResponsibleOutputDTO response = new AppResponsibleOutputDTO();
        when(appResponsibleRepository.findById(1L)).thenReturn(activeAppResponsible);
        when(appResponsibleRepository.update(activeAppResponsible, 1L)).thenReturn(updated);
        when(appResponsibleMapper.toResponse(updated)).thenReturn(response);

        AppResponsibleOutputDTO result = appResponsibleServiceFacadeBean.update(1L, inputDTO);

        verify(appResponsibleMapper).updateModelFromInput(inputDTO, activeAppResponsible);
        assertEquals(response, result);
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
