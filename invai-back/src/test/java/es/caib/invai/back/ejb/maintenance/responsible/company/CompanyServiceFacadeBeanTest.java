package es.caib.invai.back.ejb.maintenance.responsible.company;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.responsible.company.DTO.CompanyInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.company.DTO.CompanyOutputDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleRepository;
import es.caib.invai.back.persistence.repository.maintenance.responsible.company.CompanyCriteria;
import es.caib.invai.back.persistence.repository.maintenance.responsible.company.CompanyRepository;
import es.caib.invai.back.persistence.repository.maintenance.responsible.person.PersonRepository;
import es.caib.invai.back.service.mapper.maintenance.responsible.company.CompanyMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import es.caib.invai.back.service.model.maintenance.responsible.company.Company;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CompanyServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link CompanyRepository} and {@link CompanyMapper} collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class CompanyServiceFacadeBeanTest {

    @Mock
    private CompanyMapper companyMapper;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private AppResponsibleRepository appResponsibleRepository;

    @Mock
    private AppAuthorizedRepository appAuthorizedRepository;

    @InjectMocks
    private CompanyServiceFacadeBean companyServiceFacadeBean;

    private Company activeCompany;

    @BeforeEach
    void setUp() {
        activeCompany = new Company();
        activeCompany.setId(1L);
        activeCompany.setName("Plexus Tech");
    }

    @Test
    void getById_found_returnsMappedResponse() {
        CompanyOutputDTO expected = new CompanyOutputDTO();
        when(companyRepository.findById(1L)).thenReturn(activeCompany);
        when(companyMapper.toResponse(activeCompany)).thenReturn(expected);

        CompanyOutputDTO result = companyServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(companyRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> companyServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_COMPANY_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        CompanyCriteria criteria = new CompanyCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<Company> domainPage = new PageImpl<>(List.of(activeCompany));
        CompanyOutputDTO mapped = new CompanyOutputDTO();
        when(companyRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(companyMapper.toResponse(activeCompany)).thenReturn(mapped);

        Page<CompanyOutputDTO> result = companyServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueName_persistsAndReturnsResponse() {
        CompanyInputDTO inputDTO = new CompanyInputDTO("New Company");
        Company model = new Company();
        Company saved = new Company();
        CompanyOutputDTO response = new CompanyOutputDTO();
        when(companyRepository.existsByNameAndDeletedAtIsNull("New Company")).thenReturn(false);
        when(companyMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(companyRepository.create(model)).thenReturn(saved);
        when(companyMapper.toResponse(saved)).thenReturn(response);

        CompanyOutputDTO result = companyServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateName_throwsBusinessRuleException() {
        CompanyInputDTO inputDTO = new CompanyInputDTO("Plexus Tech");
        when(companyRepository.existsByNameAndDeletedAtIsNull("Plexus Tech")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> companyServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_COMPANY_DUPLICATED, ex.getMessage());
        verify(companyRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        CompanyInputDTO inputDTO = new CompanyInputDTO("X");
        when(companyRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> companyServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_COMPANY_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_duplicateName_throwsBusinessRuleException() {
        CompanyInputDTO inputDTO = new CompanyInputDTO("Taken");
        when(companyRepository.findById(1L)).thenReturn(activeCompany);
        when(companyRepository.existsByNameAndIdNotAndDeletedAtIsNull("Taken", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> companyServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_COMPANY_DUPLICATED, ex.getMessage());
        verify(companyMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        CompanyInputDTO inputDTO = new CompanyInputDTO("Updated");
        Company updated = new Company();
        CompanyOutputDTO response = new CompanyOutputDTO();
        when(companyRepository.findById(1L)).thenReturn(activeCompany);
        when(companyRepository.existsByNameAndIdNotAndDeletedAtIsNull("Updated", 1L)).thenReturn(false);
        when(companyRepository.update(activeCompany, 1L)).thenReturn(updated);
        when(companyMapper.toResponse(updated)).thenReturn(response);

        CompanyOutputDTO result = companyServiceFacadeBean.update(1L, inputDTO);

        verify(companyMapper).updateModelFromInput(inputDTO, activeCompany);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(companyRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> companyServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_COMPANY_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeCompany.setDeletedAt(LocalDateTime.now());
        when(companyRepository.findById(1L)).thenReturn(activeCompany);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> companyServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_COMPANY_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_noPersons_softDeletesCompany() {
        when(companyRepository.findById(1L)).thenReturn(activeCompany);
        when(personRepository.findAllActiveByCompanyId(1L)).thenReturn(List.of());

        companyServiceFacadeBean.delete(1L);

        assertNotNull(activeCompany.getDeletedAt());
        verify(companyRepository, times(1)).delete(activeCompany);
    }

    @Test
    void delete_personHasActiveResponsibilityOnActiveApplication_throwsBusinessRuleException() {
        Person person = person(10L, 1L, false);
        AppResponsibleAuthorized activeAnchor = AppResponsibleAuthorized.builder().id(40L).build();
        AppResponsible responsible = AppResponsible.builder().id(100L).appResponsibleAuthorized(activeAnchor).build();
        when(companyRepository.findById(1L)).thenReturn(activeCompany);
        when(personRepository.findAllActiveByCompanyId(1L)).thenReturn(List.of(person));
        when(appResponsibleRepository.findAllActiveByPersonId(10L)).thenReturn(List.of(responsible));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> companyServiceFacadeBean.delete(1L));

        assertEquals(Constants.ERR_COMPANY_HAS_RESPONSIBLE, ex.getMessage());
        verify(companyRepository, never()).delete(any());
        verify(personRepository, never()).delete(any());
    }

    @Test
    void delete_personHasActiveResponsibilityOnInactiveApplication_doesNotBlockAndCascadesCloseOut() {
        Person person = person(10L, 1L, false);
        AppResponsibleAuthorized inactiveAnchor = AppResponsibleAuthorized.builder().id(40L).deletedAt(LocalDateTime.now()).build();
        AppResponsible responsible = AppResponsible.builder().id(100L).appResponsibleAuthorized(inactiveAnchor).build();
        when(companyRepository.findById(1L)).thenReturn(activeCompany);
        when(personRepository.findAllActiveByCompanyId(1L)).thenReturn(List.of(person));
        when(appResponsibleRepository.findAllActiveByPersonId(10L)).thenReturn(List.of(responsible));
        when(appAuthorizedRepository.findAllActiveByPersonId(10L)).thenReturn(List.of());

        companyServiceFacadeBean.delete(1L);

        assertNotNull(responsible.getDeletedAt());
        verify(appResponsibleRepository).delete(responsible);
        verify(personRepository).delete(person);
        verify(companyRepository).delete(activeCompany);
    }

    @Test
    void delete_personHasActiveAuthorizationOnActiveApplicationWithNoExternalBackup_throwsBusinessRuleException() {
        Person person = person(10L, 1L, false);
        AppResponsibleAuthorized activeAnchor = AppResponsibleAuthorized.builder().id(40L).build();
        AppAuthorized authorized = AppAuthorized.builder().id(200L).appResponsibleAuthorized(activeAnchor).person(person).build();
        when(companyRepository.findById(1L)).thenReturn(activeCompany);
        when(personRepository.findAllActiveByCompanyId(1L)).thenReturn(List.of(person));
        when(appResponsibleRepository.findAllActiveByPersonId(10L)).thenReturn(List.of());
        when(appAuthorizedRepository.findAllActiveByPersonId(10L)).thenReturn(List.of(authorized));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(40L)).thenReturn(List.of(authorized));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> companyServiceFacadeBean.delete(1L));

        assertEquals(Constants.ERR_COMPANY_HAS_AUTHORIZED, ex.getMessage());
        verify(companyRepository, never()).delete(any());
        verify(personRepository, never()).delete(any());
    }

    @Test
    void delete_personHasActiveAuthorizationOnActiveApplicationWithExternalBackup_allowsDeletionAndCascadesDeactivatesIt() {
        Person person = person(10L, 1L, false);
        Person externalPerson = person(20L, 2L, false);
        AppResponsibleAuthorized activeAnchor = AppResponsibleAuthorized.builder().id(40L).build();
        AppAuthorized authorized = AppAuthorized.builder().id(200L).appResponsibleAuthorized(activeAnchor).person(person).build();
        AppAuthorized externalAuthorized = AppAuthorized.builder().id(201L).appResponsibleAuthorized(activeAnchor).person(externalPerson).build();
        when(companyRepository.findById(1L)).thenReturn(activeCompany);
        when(personRepository.findAllActiveByCompanyId(1L)).thenReturn(List.of(person));
        when(appResponsibleRepository.findAllActiveByPersonId(10L)).thenReturn(List.of());
        when(appAuthorizedRepository.findAllActiveByPersonId(10L)).thenReturn(List.of(authorized));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(40L)).thenReturn(List.of(authorized, externalAuthorized));

        companyServiceFacadeBean.delete(1L);

        assertNotNull(authorized.getDeletedAt());
        verify(appAuthorizedRepository).delete(authorized);
        verify(appAuthorizedRepository, never()).delete(externalAuthorized);
        verify(personRepository).delete(person);
        verify(companyRepository).delete(activeCompany);
    }

    @Test
    void delete_personHasActiveAuthorizationBackedByCaibPerson_allowsDeletion() {
        Person person = person(10L, 1L, false);
        Person caibPerson = new Person();
        caibPerson.setId(30L);
        caibPerson.setPersonalCaib(true);
        AppResponsibleAuthorized activeAnchor = AppResponsibleAuthorized.builder().id(40L).build();
        AppAuthorized authorized = AppAuthorized.builder().id(200L).appResponsibleAuthorized(activeAnchor).person(person).build();
        AppAuthorized caibAuthorized = AppAuthorized.builder().id(202L).appResponsibleAuthorized(activeAnchor).person(caibPerson).build();
        when(companyRepository.findById(1L)).thenReturn(activeCompany);
        when(personRepository.findAllActiveByCompanyId(1L)).thenReturn(List.of(person));
        when(appResponsibleRepository.findAllActiveByPersonId(10L)).thenReturn(List.of());
        when(appAuthorizedRepository.findAllActiveByPersonId(10L)).thenReturn(List.of(authorized));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(40L)).thenReturn(List.of(authorized, caibAuthorized));

        companyServiceFacadeBean.delete(1L);

        verify(companyRepository).delete(activeCompany);
    }

    private static Person person(Long id, Long companyId, boolean personalCaib) {
        Person person = new Person();
        person.setId(id);
        person.setPersonalCaib(personalCaib);
        Company company = new Company();
        company.setId(companyId);
        person.setCompany(company);
        return person;
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(companyRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> companyServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_COMPANY_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(companyRepository.findById(1L)).thenReturn(activeCompany);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> companyServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_COMPANY_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesCompany() {
        activeCompany.setDeletedAt(LocalDateTime.now());
        activeCompany.setDeletedBy("someone");
        Company reactivated = new Company();
        CompanyOutputDTO response = new CompanyOutputDTO();
        when(companyRepository.findById(1L)).thenReturn(activeCompany);
        when(companyRepository.update(eq(activeCompany), eq(1L))).thenReturn(reactivated);
        when(companyMapper.toResponse(reactivated)).thenReturn(response);

        CompanyOutputDTO result = companyServiceFacadeBean.reactivate(1L);

        assertNull(activeCompany.getDeletedAt());
        assertNull(activeCompany.getDeletedBy());
        assertEquals(response, result);
    }
}
