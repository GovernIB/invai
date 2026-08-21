package es.caib.invai.back.ejb.application.responsibleAuthorized.authorized;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedDeleteDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedOutputDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedCriteria;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.type.AppAuthorizedTypeLinkRepository;
import es.caib.invai.back.persistence.repository.maintenance.responsible.authorizationType.AuthorizationTypeRepository;
import es.caib.invai.back.persistence.repository.maintenance.responsible.person.PersonRepository;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.authorized.AppAuthorizedMapper;
import es.caib.invai.back.service.mapper.maintenance.responsible.authorizationType.AuthorizationTypeMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.type.AppAuthorizedTypeLink;
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
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
    private PersonRepository personRepository;

    @InjectMocks
    private AppAuthorizedServiceFacadeBean appAuthorizedServiceFacadeBean;

    private AppAuthorized activeModel;

    @BeforeEach
    void setUp() {
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(10L).build();
        Person person = new Person();
        person.setId(7L);
        activeModel = AppAuthorized.builder().id(1L).appResponsibleAuthorized(anchor).person(person).build();
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPageWithAuthorizationTypes() {
        AppAuthorizedCriteria criteria = new AppAuthorizedCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppAuthorized> domainPage = new PageImpl<>(List.of(activeModel));
        AppAuthorizedOutputDTO mapped = AppAuthorizedOutputDTO.builder().id(1L).build();
        when(appAuthorizedRepository.findAll(10L, criteria, pageable)).thenReturn(domainPage);
        when(appAuthorizedMapper.toResponse(activeModel)).thenReturn(mapped);
        when(appAuthorizedTypeLinkRepository.findAllByAppAuthorizedId(1L)).thenReturn(List.of());

        Page<AppAuthorizedOutputDTO> result = appAuthorizedServiceFacadeBean.getAll(10L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
        assertEquals(List.of(), mapped.getAuthorizationTypes());
    }

    // ------------------------------------------------------------------
    // create
    // ------------------------------------------------------------------

    @Test
    void create_noExistingHolder_persistsAttachesTypesAndReturnsResponse() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, 7L, null, null, null, null, List.of(2L, 3L), null, false);
        AppAuthorized model = new AppAuthorized();
        AppAuthorized saved = AppAuthorized.builder().id(1L).build();
        AppAuthorizedOutputDTO response = AppAuthorizedOutputDTO.builder().id(1L).build();
        when(appAuthorizedRepository.findActiveByAppResponsibleAuthorizedAndPerson(10L, 7L)).thenReturn(null);
        when(appAuthorizedMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appAuthorizedRepository.create(model)).thenReturn(saved);
        when(appAuthorizedTypeLinkRepository.findAllByAppAuthorizedId(1L)).thenReturn(List.of());
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
    }

    @Test
    void create_existingAuthorizedHolder_deactivatesOldAndCreatesNew() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, 7L, null, null, null, null, List.of(2L), null, false);
        Person oldAuthorizedPerson = new Person();
        oldAuthorizedPerson.setId(7L);
        AppAuthorized existingAuthorizedHolder = AppAuthorized.builder().id(2L).person(oldAuthorizedPerson).build();
        AppAuthorized model = new AppAuthorized();
        AppAuthorized saved = AppAuthorized.builder().id(1L).build();
        AppAuthorizedOutputDTO response = AppAuthorizedOutputDTO.builder().id(1L).build();
        when(appAuthorizedRepository.findActiveByAppResponsibleAuthorizedAndPerson(10L, 7L)).thenReturn(existingAuthorizedHolder);
        when(appAuthorizedMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appAuthorizedRepository.create(model)).thenReturn(saved);
        when(appAuthorizedTypeLinkRepository.findAllByAppAuthorizedId(1L)).thenReturn(List.of());
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
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, null, null, null, null, null, List.of(2L), null, true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appAuthorizedServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_APPAUTHORIZED_PERSON_DATA_REQUIRED, ex.getMessage());
        verify(appAuthorizedRepository, never()).create(any());
    }

    @Test
    void create_noPersonIdWithExistingEmail_reusesExistingPersonAndSkipsCreation() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, null, "Joan", "Fuster", "joan@caib.es", null, List.of(2L), null, true);
        Person existingPerson = new Person();
        existingPerson.setId(99L);
        existingPerson.setPersonalCaib(true);
        AppAuthorized model = new AppAuthorized();
        AppAuthorized saved = AppAuthorized.builder().id(1L).build();
        AppAuthorizedOutputDTO response = AppAuthorizedOutputDTO.builder().id(1L).build();
        when(personRepository.findByEmail("joan@caib.es")).thenReturn(existingPerson);
        when(appAuthorizedRepository.findActiveByAppResponsibleAuthorizedAndPerson(10L, 99L)).thenReturn(null);
        when(appAuthorizedMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appAuthorizedRepository.create(model)).thenReturn(saved);
        when(appAuthorizedTypeLinkRepository.findAllByAppAuthorizedId(1L)).thenReturn(List.of());
        when(appAuthorizedMapper.toResponse(saved)).thenReturn(response);

        appAuthorizedServiceFacadeBean.create(inputDTO);

        assertEquals(99L, inputDTO.getPersonId());
        verify(personRepository, never()).create(any());
    }

    @Test
    void create_noPersonIdNoExistingEmailPersonalCaib_createsNewCaibPerson() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, null, "Joan", "Fuster", "joan@caib.es", null, List.of(2L), null, true);
        AppAuthorized model = new AppAuthorized();
        AppAuthorized saved = AppAuthorized.builder().id(1L).build();
        AppAuthorizedOutputDTO response = AppAuthorizedOutputDTO.builder().id(1L).build();
        Person createdPerson = new Person();
        createdPerson.setId(55L);
        when(personRepository.findByEmail("joan@caib.es")).thenReturn(null);
        when(personRepository.create(any())).thenReturn(createdPerson);
        when(appAuthorizedRepository.findActiveByAppResponsibleAuthorizedAndPerson(10L, 55L)).thenReturn(null);
        when(appAuthorizedMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appAuthorizedRepository.create(model)).thenReturn(saved);
        when(appAuthorizedTypeLinkRepository.findAllByAppAuthorizedId(1L)).thenReturn(List.of());
        when(appAuthorizedMapper.toResponse(saved)).thenReturn(response);

        appAuthorizedServiceFacadeBean.create(inputDTO);

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
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, null, "Maria", "Puig", "maria@extern.es", null, List.of(2L), null, false);
        when(personRepository.findByEmail("maria@extern.es")).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appAuthorizedServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_PERSON_COMPANY_REQUIRED_WHEN_NOT_CAIB, ex.getMessage());
        verify(personRepository, never()).create(any());
    }

    @Test
    void create_noPersonIdExternalWithCompany_createsNewExternalPersonWithCompany() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, null, "Maria", "Puig", "maria@extern.es", 5L, List.of(2L), null, false);
        AppAuthorized model = new AppAuthorized();
        AppAuthorized saved = AppAuthorized.builder().id(1L).build();
        AppAuthorizedOutputDTO response = AppAuthorizedOutputDTO.builder().id(1L).build();
        Person createdPerson = new Person();
        createdPerson.setId(56L);
        when(personRepository.findByEmail("maria@extern.es")).thenReturn(null);
        when(personRepository.create(any())).thenReturn(createdPerson);
        when(appAuthorizedRepository.findActiveByAppResponsibleAuthorizedAndPerson(10L, 56L)).thenReturn(null);
        when(appAuthorizedMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appAuthorizedRepository.create(model)).thenReturn(saved);
        when(appAuthorizedTypeLinkRepository.findAllByAppAuthorizedId(1L)).thenReturn(List.of());
        when(appAuthorizedMapper.toResponse(saved)).thenReturn(response);

        appAuthorizedServiceFacadeBean.create(inputDTO);

        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).create(captor.capture());
        assertFalse(captor.getValue().isPersonalCaib());
        assertEquals(5L, captor.getValue().getCompany().getId());
        assertEquals(56L, inputDTO.getPersonId());
    }

    @Test
    void create_personalCaibDiffersFromStoredValue_updatesPerson() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, 7L, null, null, null, null, List.of(2L), null, true);
        Person person = new Person();
        person.setId(7L);
        person.setPersonalCaib(false);
        AppAuthorized model = new AppAuthorized();
        AppAuthorized saved = AppAuthorized.builder().id(1L).build();
        AppAuthorizedOutputDTO response = AppAuthorizedOutputDTO.builder().id(1L).build();
        when(personRepository.findById(7L)).thenReturn(person);
        when(appAuthorizedRepository.findActiveByAppResponsibleAuthorizedAndPerson(10L, 7L)).thenReturn(null);
        when(appAuthorizedMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appAuthorizedRepository.create(model)).thenReturn(saved);
        when(appAuthorizedTypeLinkRepository.findAllByAppAuthorizedId(1L)).thenReturn(List.of());
        when(appAuthorizedMapper.toResponse(saved)).thenReturn(response);

        appAuthorizedServiceFacadeBean.create(inputDTO);

        assertTrue(person.isPersonalCaib());
        verify(personRepository).update(person, 7L);
    }

    @Test
    void create_settingNonCaibWithoutCompany_throwsBusinessRuleException() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, 7L, null, null, null, null, List.of(2L), null, false);
        Person person = new Person();
        person.setId(7L);
        person.setPersonalCaib(true);
        person.setCompany(null);
        when(personRepository.findById(7L)).thenReturn(person);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appAuthorizedServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_PERSON_COMPANY_REQUIRED_WHEN_NOT_CAIB, ex.getMessage());
        verify(appAuthorizedRepository, never()).create(any());
    }

    @Test
    void create_settingNonCaibWithCompany_updatesPerson() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, 7L, null, null, null, null, List.of(2L), null, false);
        Person person = new Person();
        person.setId(7L);
        person.setPersonalCaib(true);
        person.setCompany(new Company());
        AppAuthorized model = new AppAuthorized();
        AppAuthorized saved = AppAuthorized.builder().id(1L).build();
        AppAuthorizedOutputDTO response = AppAuthorizedOutputDTO.builder().id(1L).build();
        when(personRepository.findById(7L)).thenReturn(person);
        when(appAuthorizedRepository.findActiveByAppResponsibleAuthorizedAndPerson(10L, 7L)).thenReturn(null);
        when(appAuthorizedMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appAuthorizedRepository.create(model)).thenReturn(saved);
        when(appAuthorizedTypeLinkRepository.findAllByAppAuthorizedId(1L)).thenReturn(List.of());
        when(appAuthorizedMapper.toResponse(saved)).thenReturn(response);

        appAuthorizedServiceFacadeBean.create(inputDTO);

        assertFalse(person.isPersonalCaib());
        verify(personRepository).update(person, 7L);
    }

    // ------------------------------------------------------------------
    // update
    // ------------------------------------------------------------------

    @Test
    void update_notFound_throwsBusinessRuleException() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, 7L, null, null, null, null, List.of(2L), null, false);
        when(appAuthorizedRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appAuthorizedServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_APPAUTHORIZED_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_valid_reconcilesAuthorizationTypesAddingAndRemoving() {
        AppAuthorizedInputDTO inputDTO = new AppAuthorizedInputDTO(10L, 7L, null, null, null, null, List.of(2L, 3L), "Nova observacio", false);
        AppAuthorized updated = AppAuthorized.builder().id(1L).build();
        AppAuthorizedOutputDTO response = AppAuthorizedOutputDTO.builder().id(1L).build();
        AppAuthorizedTypeLink existingLink1 = AppAuthorizedTypeLink.builder().id(100L).appAuthorizedId(1L).authorizationTypeId(1L).build();
        AppAuthorizedTypeLink existingLink2 = AppAuthorizedTypeLink.builder().id(101L).appAuthorizedId(1L).authorizationTypeId(2L).build();
        when(appAuthorizedRepository.findById(1L)).thenReturn(activeModel);
        when(appAuthorizedRepository.update(activeModel, 1L)).thenReturn(updated);
        when(appAuthorizedTypeLinkRepository.findAllByAppAuthorizedId(1L)).thenReturn(List.of(existingLink1, existingLink2));
        when(appAuthorizedMapper.toResponse(updated)).thenReturn(response);

        AppAuthorizedOutputDTO result = appAuthorizedServiceFacadeBean.update(1L, inputDTO);

        verify(appAuthorizedMapper).updateModelFromInput(inputDTO, activeModel);
        verify(appAuthorizedRepository, never()).existsByAppResponsibleAuthorizedAndPersonAndIdNot(any(), any(), any());
        verify(personRepository, never()).findById(any());
        verify(appAuthorizedTypeLinkRepository).delete(existingLink1);
        verify(appAuthorizedTypeLinkRepository, never()).delete(existingLink2);
        ArgumentCaptor<AppAuthorizedTypeLink> captor = ArgumentCaptor.forClass(AppAuthorizedTypeLink.class);
        verify(appAuthorizedTypeLinkRepository).create(captor.capture());
        assertEquals(3L, captor.getValue().getAuthorizationTypeId());
        assertEquals(response, result);
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
        when(appAuthorizedTypeLinkRepository.findAllByAppAuthorizedId(1L)).thenReturn(List.of());
        when(appAuthorizedMapper.toResponse(updated)).thenReturn(response);

        AppAuthorizedOutputDTO result = appAuthorizedServiceFacadeBean.reactivate(1L);

        assertNull(activeModel.getDeletedAt());
        assertNull(activeModel.getDeletedBy());
        assertEquals(response, result);
    }
}
