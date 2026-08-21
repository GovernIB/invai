package es.caib.invai.back.ejb.maintenance.responsible.person;

import es.caib.invai.back.ejb.maintenance.responsible.person.PersonServiceFacadeBean;
import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.responsible.person.PersonCriteria;
import es.caib.invai.back.persistence.repository.maintenance.responsible.person.PersonRepository;
import es.caib.invai.back.service.mapper.maintenance.responsible.person.PersonMapper;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

        Page<PersonOutputDTO> result = personServiceFacadeBean.getAll(criteria, pageable);

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

        assertEquals(true, activePerson.getDeletedAt() != null);
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
}
