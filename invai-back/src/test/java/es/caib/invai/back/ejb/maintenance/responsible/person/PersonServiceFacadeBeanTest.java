package es.caib.invai.back.ejb.maintenance.responsible.person;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.exception.SoffidClientException;
import es.caib.invai.back.exception.SoffidTimeoutException;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonCombinedSearchOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.responsible.person.PersonCriteria;
import es.caib.invai.back.persistence.repository.maintenance.responsible.person.PersonRepository;
import es.caib.invai.back.rest.soffid.SoffidClient;
import es.caib.invai.back.rest.soffid.SoffidUser;
import es.caib.invai.back.service.mapper.maintenance.responsible.person.PersonMapper;
import es.caib.invai.back.service.model.maintenance.responsible.company.Company;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
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
import org.springframework.data.domain.PageRequest;
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
 * Unit tests for {@link PersonServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link PersonRepository} and {@link PersonMapper} collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class PersonServiceFacadeBeanTest {

    @Mock
    private PersonMapper personMapper;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private SoffidClient soffidClient;

    @InjectMocks
    private PersonServiceFacadeBean personServiceFacadeBean;

    private Person activePerson;

    @BeforeEach
    void setUp() {
        activePerson = new Person();
        activePerson.setId(1L);
        activePerson.setFirstName("Joan");
        activePerson.setLastName("Puig");
        activePerson.setEmail("joan.puig@example.com");
    }

    @Test
    void getById_found_returnsMappedResponse() {
        PersonOutputDTO expected = new PersonOutputDTO();
        when(personRepository.findById(1L)).thenReturn(activePerson);
        when(personMapper.toResponse(activePerson)).thenReturn(expected);

        PersonOutputDTO result = personServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(personRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> personServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_PERSON_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        PersonCriteria criteria = new PersonCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<Person> domainPage = new PageImpl<>(List.of(activePerson));
        PersonOutputDTO mapped = new PersonOutputDTO();
        when(personRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(personMapper.toResponse(activePerson)).thenReturn(mapped);

        Page<PersonOutputDTO> result = personServiceFacadeBean.searchDatabase(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueEmail_persistsAndReturnsResponse() {
        PersonInputDTO inputDTO = new PersonInputDTO();
        inputDTO.setCompanyId(1L);
        inputDTO.setFirstName("New");
        inputDTO.setLastName("Person");
        inputDTO.setEmail("new.person@example.com");
        Person model = new Person();
        Person saved = new Person();
        PersonOutputDTO response = new PersonOutputDTO();
        when(personRepository.existsByEmail("new.person@example.com")).thenReturn(false);
        when(personMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(personRepository.create(model)).thenReturn(saved);
        when(personMapper.toResponse(saved)).thenReturn(response);

        PersonOutputDTO result = personServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateEmail_throwsBusinessRuleException() {
        PersonInputDTO inputDTO = new PersonInputDTO();
        inputDTO.setCompanyId(1L);
        inputDTO.setFirstName("Taken");
        inputDTO.setLastName("Taken");
        inputDTO.setEmail("joan.puig@example.com");
        when(personRepository.existsByEmail("joan.puig@example.com")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> personServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_PERSON_DUPLICATED, ex.getMessage());
        verify(personRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        PersonInputDTO inputDTO = new PersonInputDTO();
        inputDTO.setCompanyId(1L);
        inputDTO.setFirstName("X");
        inputDTO.setLastName("Y");
        inputDTO.setEmail("x@example.com");
        when(personRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> personServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_PERSON_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_duplicateEmail_throwsBusinessRuleException() {
        PersonInputDTO inputDTO = new PersonInputDTO();
        inputDTO.setCompanyId(1L);
        inputDTO.setFirstName("Taken");
        inputDTO.setLastName("Taken");
        inputDTO.setEmail("taken@example.com");
        when(personRepository.findById(1L)).thenReturn(activePerson);
        when(personRepository.existsByEmailAndIdNot("taken@example.com", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> personServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_PERSON_OWNED_BY_OTHER, ex.getMessage());
        verify(personMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        PersonInputDTO inputDTO = new PersonInputDTO();
        inputDTO.setCompanyId(1L);
        inputDTO.setFirstName("Updated");
        inputDTO.setLastName("Person");
        inputDTO.setEmail("updated@example.com");
        Person updated = new Person();
        PersonOutputDTO response = new PersonOutputDTO();
        when(personRepository.findById(1L)).thenReturn(activePerson);
        when(personRepository.existsByEmailAndIdNot("updated@example.com", 1L)).thenReturn(false);
        when(personRepository.update(activePerson, 1L)).thenReturn(updated);
        when(personMapper.toResponse(updated)).thenReturn(response);

        PersonOutputDTO result = personServiceFacadeBean.update(1L, inputDTO);

        verify(personMapper).updateModelFromInput(inputDTO, activePerson);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(personRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> personServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_PERSON_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activePerson.setDeletedAt(LocalDateTime.now());
        when(personRepository.findById(1L)).thenReturn(activePerson);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> personServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_PERSON_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesPerson() {
        when(personRepository.findById(1L)).thenReturn(activePerson);

        personServiceFacadeBean.delete(1L);

        assertNotNull(activePerson.getDeletedAt());
        verify(personRepository, times(1)).delete(activePerson);
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(personRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> personServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_PERSON_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(personRepository.findById(1L)).thenReturn(activePerson);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> personServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_PERSON_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesPerson() {
        activePerson.setDeletedAt(LocalDateTime.now());
        activePerson.setDeletedBy("someone");
        Person reactivated = new Person();
        PersonOutputDTO response = new PersonOutputDTO();
        when(personRepository.findById(1L)).thenReturn(activePerson);
        when(personRepository.update(eq(activePerson), eq(1L))).thenReturn(reactivated);
        when(personMapper.toResponse(reactivated)).thenReturn(response);

        PersonOutputDTO result = personServiceFacadeBean.reactivate(1L);

        assertNull(activePerson.getDeletedAt());
        assertNull(activePerson.getDeletedBy());
        assertEquals(response, result);
    }

    @Test
    void searchSoffid_delegatesToSoffidClientAndMapsResults() {
        SoffidUser soffidUser = new SoffidUser();
        soffidUser.setFirstName("Joan");
        soffidUser.setLastName("Puig");
        soffidUser.setEmailAddress("joan.puig@caib.es");
        soffidUser.setActive(true);
        Pageable pageable = PageRequest.of(0, 20);
        when(soffidClient.searchUsers("Joan", pageable)).thenReturn(new PageImpl<>(List.of(soffidUser), pageable, 1));

        Page<PersonOutputDTO> result = personServiceFacadeBean.searchSoffid("Joan", pageable);

        assertEquals(1, result.getTotalElements());
        PersonOutputDTO first = result.getContent().get(0);
        assertNull(first.getId());
        assertNull(first.getCompany());
        assertNull(first.getDeletedAt());
        assertTrue(first.isPersonalCaib());
        assertEquals("Joan", first.getFirstName());
        assertEquals("Puig", first.getLastName());
        assertEquals("joan.puig@caib.es", first.getEmail());
    }

    @Test
    void searchSoffid_blankFullName_stillDelegatesAsUnfilteredListing() {
        Pageable pageable = PageRequest.of(0, 20);
        when(soffidClient.searchUsers(null, pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<PersonOutputDTO> result = personServiceFacadeBean.searchSoffid(null, pageable);

        assertTrue(result.getContent().isEmpty());
        verify(soffidClient).searchUsers(null, pageable);
    }

    @Test
    void searchSoffid_noResults_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(soffidClient.searchUsers("Nobody", pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<PersonOutputDTO> result = personServiceFacadeBean.searchSoffid("Nobody", pageable);

        assertTrue(result.getContent().isEmpty());
    }

    // ------------------------------------------------------------------
    // searchCombined
    // ------------------------------------------------------------------

    @Test
    void searchCombined_blankSearch_alwaysQueriesBothSources() {
        Pageable pageable = PageRequest.of(0, 10);
        PersonOutputDTO localDto = new PersonOutputDTO();
        localDto.setFirstName("Local");
        when(personRepository.findAll(any(PersonCriteria.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(activePerson), pageable, 1));
        when(personMapper.toResponse(activePerson)).thenReturn(localDto);

        SoffidUser soffidUser = new SoffidUser();
        soffidUser.setFirstName("Remote");
        soffidUser.setEmailAddress("remote@caib.es");
        when(soffidClient.searchUsers(null, pageable)).thenReturn(new PageImpl<>(List.of(soffidUser), pageable, 1));

        PersonCombinedSearchOutputDTO result = personServiceFacadeBean.searchCombined(null, pageable);

        assertEquals(1, result.getDatabase().getTotalElements());
        assertEquals("Local", result.getDatabase().getContent().get(0).getFirstName());
        assertEquals(1, result.getSoffid().getTotalElements());
        assertEquals("Remote", result.getSoffid().getContent().get(0).getFirstName());
    }

    @Test
    void searchCombined_searchTermWithLocalMatches_doesNotQuerySoffid() {
        Pageable pageable = PageRequest.of(0, 10);
        PersonOutputDTO localDto = new PersonOutputDTO();
        localDto.setFirstName("Joan");
        when(personRepository.findAll(any(PersonCriteria.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(activePerson), pageable, 1));
        when(personMapper.toResponse(activePerson)).thenReturn(localDto);

        PersonCombinedSearchOutputDTO result = personServiceFacadeBean.searchCombined("Joan", pageable);

        assertEquals(1, result.getDatabase().getTotalElements());
        assertTrue(result.getSoffid().getContent().isEmpty());
        verify(soffidClient, never()).searchUsers(any(), any());
    }

    @Test
    void searchCombined_searchTermWithNoLocalMatches_fallsBackToSoffid() {
        Pageable pageable = PageRequest.of(0, 10);
        when(personRepository.findAll(any(PersonCriteria.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        SoffidUser soffidUser = new SoffidUser();
        soffidUser.setFirstName("Joan");
        soffidUser.setEmailAddress("joan@caib.es");
        when(soffidClient.searchUsers("Joan", pageable)).thenReturn(new PageImpl<>(List.of(soffidUser), pageable, 1));

        PersonCombinedSearchOutputDTO result = personServiceFacadeBean.searchCombined("Joan", pageable);

        assertTrue(result.getDatabase().getContent().isEmpty());
        assertEquals(1, result.getSoffid().getTotalElements());
        assertEquals("Joan", result.getSoffid().getContent().get(0).getFirstName());
    }

    @Test
    void searchCombined_searchTermWithNoMatchesAnywhere_returnsEmptyBothSides() {
        Pageable pageable = PageRequest.of(0, 10);
        when(personRepository.findAll(any(PersonCriteria.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));
        when(soffidClient.searchUsers("Nobody", pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        PersonCombinedSearchOutputDTO result = personServiceFacadeBean.searchCombined("Nobody", pageable);

        assertTrue(result.getDatabase().getContent().isEmpty());
        assertTrue(result.getSoffid().getContent().isEmpty());
    }

    // ------------------------------------------------------------------
    // getOrCreatePerson
    // ------------------------------------------------------------------

    @Test
    void getOrCreatePerson_existingEmail_reusesExistingPersonAndSkipsCreation() {
        Person existing = new Person();
        existing.setId(99L);
        when(personRepository.findByEmail("joan@caib.es")).thenReturn(existing);

        Person result = personServiceFacadeBean.getOrCreatePerson("Joan", "Fuster", "joan@caib.es", true, null, "u8443");

        assertSame(existing, result);
        verify(personRepository, never()).create(any());
    }

    @Test
    void getOrCreatePerson_noExistingEmailPersonalCaib_createsNewCaibPersonWithUserName() {
        Person created = new Person();
        created.setId(55L);
        when(personRepository.findByEmail("joan@caib.es")).thenReturn(null);
        when(personRepository.create(any())).thenReturn(created);

        Person result = personServiceFacadeBean.getOrCreatePerson("Joan", "Fuster", "joan@caib.es", true, null, "u8443");

        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).create(captor.capture());
        assertEquals("Joan", captor.getValue().getFirstName());
        assertEquals("Fuster", captor.getValue().getLastName());
        assertEquals("joan@caib.es", captor.getValue().getEmail());
        assertTrue(captor.getValue().isPersonalCaib());
        assertEquals("u8443", captor.getValue().getUserName());
        assertNull(captor.getValue().getCompany());
        assertSame(created, result);
    }

    @Test
    void getOrCreatePerson_noExistingEmailExternalWithoutCompany_throwsBusinessRuleException() {
        when(personRepository.findByEmail("maria@extern.es")).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> personServiceFacadeBean.getOrCreatePerson("Maria", "Puig", "maria@extern.es", false, null, null));

        assertEquals(Constants.ERR_PERSON_COMPANY_REQUIRED_WHEN_NOT_CAIB, ex.getMessage());
        verify(personRepository, never()).create(any());
    }

    @Test
    void getOrCreatePerson_noExistingEmailExternalWithCompany_createsNewExternalPersonWithCompany() {
        Person created = new Person();
        created.setId(56L);
        when(personRepository.findByEmail("maria@extern.es")).thenReturn(null);
        when(personRepository.create(any())).thenReturn(created);

        Person result = personServiceFacadeBean.getOrCreatePerson("Maria", "Puig", "maria@extern.es", false, 5L, null);

        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).create(captor.capture());
        assertFalse(captor.getValue().isPersonalCaib());
        assertEquals(5L, captor.getValue().getCompany().getId());
        assertSame(created, result);
    }

    // ------------------------------------------------------------------
    // updatePersonalCaibToPerson
    // ------------------------------------------------------------------

    @Test
    void updatePersonalCaib_ToPerson_personNotFound_isNoOp() {
        when(personRepository.findById(10L)).thenReturn(null);

        personServiceFacadeBean.updatePersonalCaibToPerson(10L, true);

        verify(personRepository, never()).update(any(), any());
    }

    @Test
    void updatePersonalCaib_ToPerson_matchesStoredValue_doesNotTouchPerson() {
        Person person = new Person();
        person.setId(10L);
        person.setPersonalCaib(true);
        when(personRepository.findById(10L)).thenReturn(person);

        personServiceFacadeBean.updatePersonalCaibToPerson(10L, true);

        verify(personRepository, never()).update(any(), any());
    }

    @Test
    void updatePersonalCaib_ToPerson_differsFromStoredValue_updatesPerson() {
        Person person = new Person();
        person.setId(10L);
        person.setPersonalCaib(false);
        when(personRepository.findById(10L)).thenReturn(person);

        personServiceFacadeBean.updatePersonalCaibToPerson(10L, true);

        assertTrue(person.isPersonalCaib());
        verify(personRepository).update(person, 10L);
    }

    @Test
    void updatePersonalCaib_settingNonCaibToPersonWithoutCompany_throwsBusinessRuleException() {
        Person person = new Person();
        person.setId(10L);
        person.setPersonalCaib(true);
        person.setCompany(null);
        when(personRepository.findById(10L)).thenReturn(person);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> personServiceFacadeBean.updatePersonalCaibToPerson(10L, false));

        assertEquals(Constants.ERR_PERSON_COMPANY_REQUIRED_WHEN_NOT_CAIB, ex.getMessage());
        verify(personRepository, never()).update(any(), any());
    }

    @Test
    void updatePersonalCaib_settingNonCaibToPersonWithCompany_updatesPerson() {
        Person person = new Person();
        person.setId(10L);
        person.setPersonalCaib(true);
        person.setCompany(new Company());
        when(personRepository.findById(10L)).thenReturn(person);

        personServiceFacadeBean.updatePersonalCaibToPerson(10L, false);

        assertFalse(person.isPersonalCaib());
        verify(personRepository).update(person, 10L);
    }

    @Test
    void checkDir3_personNotFoundLocally_stillChecksSoffidByEmail() {
        when(personRepository.findByEmail("unregistered@example.com")).thenReturn(null);
        SoffidUser soffidUser = new SoffidUser();
        soffidUser.setPrimaryGroup("sgaip");
        when(soffidClient.searchByEmail("unregistered@example.com")).thenReturn(soffidUser);
        when(soffidClient.resolveGroupDir3("sgaip")).thenReturn("A04027054");

        var result = personServiceFacadeBean.checkDir3("unregistered@example.com", "A04027054");

        assertTrue(result.isMatches());
        assertEquals("A04027054", result.getGroupDir3());
    }

    @Test
    void checkDir3_personNotPersonalCaib_returnsNullPersonDir3WithoutCallingSoffid() {
        activePerson.setPersonalCaib(false);
        when(personRepository.findByEmail("joan.puig@example.com")).thenReturn(activePerson);

        var result = personServiceFacadeBean.checkDir3("joan.puig@example.com", "A04027054");

        assertFalse(result.isMatches());
        assertNull(result.getGroupDir3());
        verify(soffidClient, never()).searchByEmail(any());
    }

    @Test
    void checkDir3_blankAdmUnitCode_returnsNullPersonDir3WithoutCallingSoffid() {
        activePerson.setPersonalCaib(true);
        when(personRepository.findByEmail("joan.puig@example.com")).thenReturn(activePerson);

        var result = personServiceFacadeBean.checkDir3("joan.puig@example.com", "  ");

        assertNull(result.getGroupDir3());
        verify(soffidClient, never()).searchByEmail(any());
    }

    @Test
    void checkDir3_noMatchingSoffidUser_returnsNullPersonDir3() {
        activePerson.setPersonalCaib(true);
        when(personRepository.findByEmail("joan.puig@example.com")).thenReturn(activePerson);
        when(soffidClient.searchByEmail("joan.puig@example.com")).thenReturn(null);

        var result = personServiceFacadeBean.checkDir3("joan.puig@example.com", "A04027054");

        assertNull(result.getGroupDir3());
        assertFalse(result.isMatches());
    }

    @Test
    void checkDir3_primaryGroupHasDir3AndMatches_returnsMatch() {
        activePerson.setPersonalCaib(true);
        when(personRepository.findByEmail("joan.puig@example.com")).thenReturn(activePerson);
        SoffidUser soffidUser = new SoffidUser();
        soffidUser.setPrimaryGroup("sgaip");
        when(soffidClient.searchByEmail("joan.puig@example.com")).thenReturn(soffidUser);
        when(soffidClient.resolveGroupDir3("sgaip")).thenReturn("A04027054");

        var result = personServiceFacadeBean.checkDir3("joan.puig@example.com", "A04027054");

        assertTrue(result.isMatches());
        assertEquals("A04027054", result.getGroupDir3());
    }

    @Test
    void checkDir3_primaryGroupDir3Differs_returnsMismatch() {
        activePerson.setPersonalCaib(true);
        when(personRepository.findByEmail("joan.puig@example.com")).thenReturn(activePerson);
        SoffidUser soffidUser = new SoffidUser();
        soffidUser.setPrimaryGroup("sgaip");
        when(soffidClient.searchByEmail("joan.puig@example.com")).thenReturn(soffidUser);
        when(soffidClient.resolveGroupDir3("sgaip")).thenReturn("A04027054");

        var result = personServiceFacadeBean.checkDir3("joan.puig@example.com", "A04000000");

        assertFalse(result.isMatches());
        assertEquals("A04027054", result.getGroupDir3());
    }

    @Test
    void checkDir3_noGroupHasDir3_returnsNullPersonDir3() {
        activePerson.setPersonalCaib(true);
        when(personRepository.findByEmail("joan.puig@example.com")).thenReturn(activePerson);
        SoffidUser soffidUser = new SoffidUser();
        soffidUser.setPrimaryGroup("ncadmin");
        when(soffidClient.searchByEmail("joan.puig@example.com")).thenReturn(soffidUser);
        when(soffidClient.resolveGroupDir3("ncadmin")).thenReturn(null);

        var result = personServiceFacadeBean.checkDir3("joan.puig@example.com", "A04027054");

        assertNull(result.getGroupDir3());
    }

    @Test
    void checkDir3_soffidClientError_propagatesInsteadOfBeingSwallowed() {
        activePerson.setPersonalCaib(true);
        when(personRepository.findByEmail("joan.puig@example.com")).thenReturn(activePerson);
        when(soffidClient.searchByEmail("joan.puig@example.com"))
                .thenThrow(new SoffidClientException("500"));

        assertThrows(SoffidClientException.class,
                () -> personServiceFacadeBean.checkDir3("joan.puig@example.com", "A04027054"));
    }

    @Test
    void checkDir3_soffidTimeout_propagatesInsteadOfBeingSwallowed() {
        activePerson.setPersonalCaib(true);
        when(personRepository.findByEmail("joan.puig@example.com")).thenReturn(activePerson);
        when(soffidClient.searchByEmail("joan.puig@example.com"))
                .thenThrow(new SoffidTimeoutException());

        assertThrows(SoffidTimeoutException.class,
                () -> personServiceFacadeBean.checkDir3("joan.puig@example.com", "A04027054"));
    }
}
