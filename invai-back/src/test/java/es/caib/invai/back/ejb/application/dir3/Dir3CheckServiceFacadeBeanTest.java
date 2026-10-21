package es.caib.invai.back.ejb.application.dir3;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.exception.SoffidTimeoutException;
import es.caib.invai.back.interna.application.dir3.DTO.ApplicationDir3MismatchOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonDir3CheckOutputDTO;
import es.caib.invai.back.persistence.repository.application.core.ApplicationCriteria;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.core.AppResponsibleAuthorizedRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.dir3.Dir3ValidationRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleRepository;
import es.caib.invai.back.service.facade.maintenance.responsible.person.PersonService;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.dir3.PrimaryGroupDir3;
import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.dir3.Dir3Validation;
import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import es.caib.invai.back.service.model.catalog.validationStatus.ValidationStatusEnum;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link Dir3CheckServiceFacadeBean}, exercising every branch of the DIR3 audit of the
 * applications with the {@link ApplicationRepository}, the responsible/authorized repositories and the
 * {@link PersonService} collaborators fully mocked.
 *
 * @since 1.0.6
 */
@ExtendWith(MockitoExtension.class)
class Dir3CheckServiceFacadeBeanTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private AppResponsibleAuthorizedRepository appResponsibleAuthorizedRepository;

    @Mock
    private AppResponsibleRepository appResponsibleRepository;

    @Mock
    private AppAuthorizedRepository appAuthorizedRepository;

    @Mock
    private PersonService personService;

    @Mock
    private Dir3ValidationRepository dir3ValidationRepository;

    @InjectMocks
    private Dir3CheckServiceFacadeBean dir3CheckServiceFacadeBean;

    // ------------------------------------------------------------------
    // checkDir3MismatchForAllApplications
    // ------------------------------------------------------------------

    private Application applicationWithAdmUnitCode(Long id, String name, String admUnitCode) {
        Application application = new Application();
        application.setId(id);
        application.setName(name);
        application.setAdmUnitCode(admUnitCode);
        return application;
    }

    private void stubAllApplications(Application... applications) {
        when(applicationRepository.findAll(any(ApplicationCriteria.class), eq(Pageable.unpaged())))
                .thenReturn(new PageImpl<>(List.of(applications)));
    }

    private AppResponsible buildAppResponsibleWithDir3Validation(Long personId, boolean personalCaib, ValidationStatusEnum currentStatus) {
        Person person = new Person();
        person.setId(personId);
        person.setEmail("person" + personId + "@caib.es");
        person.setPersonalCaib(personalCaib);
        AppResponsible responsible = new AppResponsible();
        responsible.setId(1L);
        responsible.setPerson(person);
        responsible.setDir3Validation(Dir3Validation.builder().id(700L).dir3Status(currentStatus).build());
        return responsible;
    }

    @Test
    void checkDir3MismatchForAllApplications_blankAdmUnitCode_isSkippedEntirely() {
        stubAllApplications(applicationWithAdmUnitCode(1L, "App One", null));

        List<ApplicationDir3MismatchOutputDTO> result = dir3CheckServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertEquals(0, result.size());
        verify(appResponsibleAuthorizedRepository, never()).findByApplicationId(any());
    }

    @Test
    void checkDir3MismatchForAllApplications_noAnchor_isSkipped() {
        stubAllApplications(applicationWithAdmUnitCode(1L, "App One", "A04026919"));
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(null);

        List<ApplicationDir3MismatchOutputDTO> result = dir3CheckServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertEquals(0, result.size());
        verify(appResponsibleRepository, never()).findAll(any(), any(), any());
    }

    @Test
    void checkDir3MismatchForAllApplications_nonPersonalCaibPerson_neverChecksSoffidAndNeverMismatches() {
        stubAllApplications(applicationWithAdmUnitCode(1L, "App One", "A04026919"));
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(60L, false, ValidationStatusEnum.NOT_APPLY);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());

        List<ApplicationDir3MismatchOutputDTO> result = dir3CheckServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertEquals(0, result.size());
        verify(personService, never()).checkDir3(any(), any());
        verify(dir3ValidationRepository, never()).update(any(), any());
    }

    @Test
    void checkDir3MismatchForAllApplications_matchingResponsible_isNotFlaggedOrTouched() {
        stubAllApplications(applicationWithAdmUnitCode(1L, "App One", "A04026919"));
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, ValidationStatusEnum.VALIDATED);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "A04026919", "A04026919", true));

        List<ApplicationDir3MismatchOutputDTO> result = dir3CheckServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertEquals(0, result.size());
        assertEquals(ValidationStatusEnum.VALIDATED, responsible.getDir3Validation().getDir3Status());
        verify(dir3ValidationRepository, never()).update(any(), any());
    }

    @Test
    void checkDir3MismatchForAllApplications_mismatchedResponsible_flagsApplicationAndDowngradesToNoValidado() {
        stubAllApplications(applicationWithAdmUnitCode(1L, "App One", "A04026919"));
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, ValidationStatusEnum.VALIDATED);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "OTHER_UNIT", "A04026919", false));

        List<ApplicationDir3MismatchOutputDTO> result = dir3CheckServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("App One", result.get(0).getName());
        assertTrue(result.get(0).isDir3Mismatch());
        assertEquals(ValidationStatusEnum.NOT_VALIDATED, responsible.getDir3Validation().getDir3Status());
        verify(dir3ValidationRepository).update(responsible.getDir3Validation(), 700L);
    }

    @Test
    void checkDir3MismatchForAllApplications_manualAssignmentStillMismatching_isCheckedButProtectedFromDowngrade() {
        stubAllApplications(applicationWithAdmUnitCode(1L, "App One", "A04026919"));
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, ValidationStatusEnum.MANUAL);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "OTHER_UNIT", "A04026919", false));

        List<ApplicationDir3MismatchOutputDTO> result = dir3CheckServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertEquals(0, result.size());
        verify(personService).checkDir3("person50@caib.es", "A04026919");
        assertEquals(ValidationStatusEnum.MANUAL, responsible.getDir3Validation().getDir3Status());
        verify(dir3ValidationRepository, never()).update(any(), any());
    }

    @Test
    void checkDir3MismatchForAllApplications_manualAssignmentNowMatching_isPromotedToValidadoButNotFlagged() {
        stubAllApplications(applicationWithAdmUnitCode(1L, "App One", "A04026919"));
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, ValidationStatusEnum.MANUAL);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "A04026919", "A04026919", true));

        List<ApplicationDir3MismatchOutputDTO> result = dir3CheckServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertEquals(0, result.size());
        verify(personService).checkDir3("person50@caib.es", "A04026919");
        assertEquals(ValidationStatusEnum.VALIDATED, responsible.getDir3Validation().getDir3Status());
        verify(dir3ValidationRepository).update(responsible.getDir3Validation(), 700L);
    }

    @Test
    void checkDir3MismatchForAllApplications_noAplicaAssignment_isNeverCheckedOrChanged() {
        stubAllApplications(applicationWithAdmUnitCode(1L, "App One", "A04026919"));
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, ValidationStatusEnum.NOT_APPLY);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());

        List<ApplicationDir3MismatchOutputDTO> result = dir3CheckServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertEquals(0, result.size());
        verify(personService, never()).checkDir3(any(), any());
        assertEquals(ValidationStatusEnum.NOT_APPLY, responsible.getDir3Validation().getDir3Status());
        verify(dir3ValidationRepository, never()).update(any(), any());
    }

    @Test
    void checkDir3MismatchForAllApplications_alreadyNoValidadoStillMismatching_isIncludedButDir3ValidationNotRewritten() {
        stubAllApplications(applicationWithAdmUnitCode(1L, "App One", "A04026919"));
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, ValidationStatusEnum.NOT_VALIDATED);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "OTHER_UNIT", "A04026919", false));

        List<ApplicationDir3MismatchOutputDTO> result = dir3CheckServiceFacadeBean.checkDir3MismatchForAllApplications();

        // Unlike mismatchFound in the old design, the returned list now reports every application
        // currently mismatching - not just newly-discovered downgrades - so a person who was
        // already NOT_VALIDATED before this run, and still doesn't match, is included here too.
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(personService).checkDir3("person50@caib.es", "A04026919");
        assertEquals(ValidationStatusEnum.NOT_VALIDATED, responsible.getDir3Validation().getDir3Status());
        // The Dir3Validation status itself didn't change (it was already NOT_VALIDATED), so no need
        // to write it again - only the resulting status matters for the list/application flag.
        verify(dir3ValidationRepository, never()).update(any(), any());
    }

    @Test
    void checkDir3MismatchForAllApplications_alreadyNoValidadoNowMatching_isPromotedToValidadoButNotFlagged() {
        stubAllApplications(applicationWithAdmUnitCode(1L, "App One", "A04026919"));
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, ValidationStatusEnum.NOT_VALIDATED);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "A04026919", "A04026919", true));

        List<ApplicationDir3MismatchOutputDTO> result = dir3CheckServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertEquals(0, result.size());
        verify(personService).checkDir3("person50@caib.es", "A04026919");
        assertEquals(ValidationStatusEnum.VALIDATED, responsible.getDir3Validation().getDir3Status());
        verify(dir3ValidationRepository).update(responsible.getDir3Validation(), 700L);
    }

    @Test
    void checkDir3MismatchForAllApplications_mismatchedAuthorized_flagsApplicationAndDowngradesToNoValidado() {
        stubAllApplications(applicationWithAdmUnitCode(1L, "App One", "A04026919"));
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of()));
        Person person = new Person();
        person.setId(55L);
        person.setEmail("person55@caib.es");
        person.setPersonalCaib(true);
        AppAuthorized authorized = AppAuthorized.builder()
                .id(2L)
                .person(person)
                .dir3Validation(Dir3Validation.builder().id(701L).dir3Status(ValidationStatusEnum.VALIDATED).build())
                .build();
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of(authorized));
        when(personService.checkDir3("person55@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "OTHER_UNIT", "A04026919", false));

        List<ApplicationDir3MismatchOutputDTO> result = dir3CheckServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertEquals(1, result.size());
        assertEquals(ValidationStatusEnum.NOT_VALIDATED, authorized.getDir3Validation().getDir3Status());
        verify(dir3ValidationRepository).update(authorized.getDir3Validation(), 701L);
    }

    @Test
    void checkDir3MismatchForAllApplications_onlyMismatchedApplicationsAreReturned() {
        Application cleanApp = applicationWithAdmUnitCode(1L, "Clean App", "A04026919");
        Application mismatchedApp = applicationWithAdmUnitCode(2L, "Mismatched App", "A04026919");
        stubAllApplications(cleanApp, mismatchedApp);
        AppResponsibleAuthorized anchor1 = AppResponsibleAuthorized.builder().id(9L).build();
        AppResponsibleAuthorized anchor2 = AppResponsibleAuthorized.builder().id(10L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor1);
        when(appResponsibleAuthorizedRepository.findByApplicationId(2L)).thenReturn(anchor2);
        AppResponsible matching = buildAppResponsibleWithDir3Validation(50L, true, ValidationStatusEnum.VALIDATED);
        AppResponsible mismatched = buildAppResponsibleWithDir3Validation(51L, true, ValidationStatusEnum.VALIDATED);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(matching)));
        when(appResponsibleRepository.findAll(eq(10L), any(), any())).thenReturn(new PageImpl<>(List.of(mismatched)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(any())).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "A04026919", "A04026919", true));
        when(personService.checkDir3("person51@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "OTHER_UNIT", "A04026919", false));

        List<ApplicationDir3MismatchOutputDTO> result = dir3CheckServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
        assertEquals("Mismatched App", result.get(0).getName());
    }

    @Test
    void checkDir3MismatchForAllApplications_currentMismatch_persistsTrueOnApplication() {
        Application application = applicationWithAdmUnitCode(1L, "App One", "A04026919");
        stubAllApplications(application);
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, ValidationStatusEnum.VALIDATED);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "OTHER_UNIT", "A04026919", false));

        dir3CheckServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertTrue(application.isDir3Mismatch());
        verify(applicationRepository).update(application, 1L);
    }

    @Test
    void checkDir3MismatchForAllApplications_onlyManualMismatching_doesNotPersistDir3MismatchTrue() {
        Application application = applicationWithAdmUnitCode(1L, "App One", "A04026919");
        stubAllApplications(application);
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, ValidationStatusEnum.MANUAL);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "OTHER_UNIT", "A04026919", false));

        dir3CheckServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertFalse(application.isDir3Mismatch());
        verify(applicationRepository, never()).update(any(), any());
    }

    @Test
    void checkDir3MismatchForAllApplications_flagAlreadyCurrent_doesNotCallUpdateAgain() {
        Application application = applicationWithAdmUnitCode(1L, "App One", "A04026919");
        application.setDir3Mismatch(true);
        stubAllApplications(application);
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, ValidationStatusEnum.NOT_VALIDATED);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "OTHER_UNIT", "A04026919", false));

        dir3CheckServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertTrue(application.isDir3Mismatch());
        verify(applicationRepository, never()).update(any(), any());
    }

    @Test
    void checkDir3MismatchForAllApplications_blankAdmUnitCode_clearsStaleMismatchFlag() {
        Application application = applicationWithAdmUnitCode(1L, "App One", null);
        application.setDir3Mismatch(true);
        stubAllApplications(application);

        dir3CheckServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertFalse(application.isDir3Mismatch());
        verify(applicationRepository).update(application, 1L);
    }

    @Test
    void checkDir3MismatchForAllApplications_noAnchor_clearsStaleMismatchFlag() {
        Application application = applicationWithAdmUnitCode(1L, "App One", "A04026919");
        application.setDir3Mismatch(true);
        stubAllApplications(application);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(null);

        dir3CheckServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertFalse(application.isDir3Mismatch());
        verify(applicationRepository).update(application, 1L);
    }

    // ------------------------------------------------------------------
    // checkDir3Mismatch (single application)
    // ------------------------------------------------------------------

    @Test
    void checkDir3Mismatch_soffidFailure_propagatesAndIsNeverReadAsAMismatch() {
        Application application = applicationWithAdmUnitCode(1L, "App One", "A04026919");
        when(applicationRepository.findById(1L)).thenReturn(application);
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, ValidationStatusEnum.VALIDATED);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenThrow(new SoffidTimeoutException());

        assertThrows(SoffidTimeoutException.class, () -> dir3CheckServiceFacadeBean.checkDir3Mismatch(1L));

        assertEquals(ValidationStatusEnum.VALIDATED, responsible.getDir3Validation().getDir3Status());
        verify(dir3ValidationRepository, never()).update(any(), any());
        verify(applicationRepository, never()).update(any(), any());
    }

    @Test
    void checkDir3Mismatch_unknownApplication_throwsBusinessRuleException() {
        when(applicationRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> dir3CheckServiceFacadeBean.checkDir3Mismatch(99L));

        assertEquals(Constants.ERR_APP_NOT_FOUND, ex.getMessage());
        verify(appResponsibleAuthorizedRepository, never()).findByApplicationId(any());
    }

    @Test
    void checkDir3Mismatch_mismatchedResponsible_returnsMismatchAndDowngradesOnlyThatApplication() {
        Application application = applicationWithAdmUnitCode(1L, "App One", "A04026919");
        when(applicationRepository.findById(1L)).thenReturn(application);
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, ValidationStatusEnum.VALIDATED);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "OTHER_UNIT", "A04026919", false));

        ApplicationDir3MismatchOutputDTO result = dir3CheckServiceFacadeBean.checkDir3Mismatch(1L);

        assertEquals(1L, result.getId());
        assertEquals("App One", result.getName());
        assertTrue(result.isDir3Mismatch());
        assertEquals(ValidationStatusEnum.NOT_VALIDATED, responsible.getDir3Validation().getDir3Status());
        assertTrue(application.isDir3Mismatch());
        verify(dir3ValidationRepository).update(responsible.getDir3Validation(), 700L);
        verify(applicationRepository).update(application, 1L);
        verify(applicationRepository, never()).findAll(any(ApplicationCriteria.class), any());
    }

    @Test
    void checkDir3Mismatch_mismatchedAuthorized_returnsMismatch() {
        Application application = applicationWithAdmUnitCode(1L, "App One", "A04026919");
        when(applicationRepository.findById(1L)).thenReturn(application);
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of()));
        Person person = new Person();
        person.setId(51L);
        person.setEmail("person51@caib.es");
        person.setPersonalCaib(true);
        AppAuthorized authorized = AppAuthorized.builder()
                .id(80L)
                .person(person)
                .dir3Validation(Dir3Validation.builder().id(701L).dir3Status(ValidationStatusEnum.VALIDATED).build())
                .build();
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of(authorized));
        when(personService.checkDir3("person51@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "OTHER_UNIT", "A04026919", false));

        ApplicationDir3MismatchOutputDTO result = dir3CheckServiceFacadeBean.checkDir3Mismatch(1L);

        assertTrue(result.isDir3Mismatch());
        assertEquals(ValidationStatusEnum.NOT_VALIDATED, authorized.getDir3Validation().getDir3Status());
    }

    @Test
    void checkDir3Mismatch_everyAssignmentMatches_returnsNoMismatchAndClearsStaleFlag() {
        Application application = applicationWithAdmUnitCode(1L, "App One", "A04026919");
        application.setDir3Mismatch(true);
        when(applicationRepository.findById(1L)).thenReturn(application);
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, ValidationStatusEnum.NOT_VALIDATED);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "A04026919", "A04026919", true));

        ApplicationDir3MismatchOutputDTO result = dir3CheckServiceFacadeBean.checkDir3Mismatch(1L);

        assertEquals(1L, result.getId());
        assertFalse(result.isDir3Mismatch());
        assertEquals(ValidationStatusEnum.VALIDATED, responsible.getDir3Validation().getDir3Status());
        assertFalse(application.isDir3Mismatch());
        verify(applicationRepository).update(application, 1L);
    }

    @Test
    void checkDir3Mismatch_manualAssignmentStillMismatching_isProtectedAndNotCounted() {
        Application application = applicationWithAdmUnitCode(1L, "App One", "A04026919");
        when(applicationRepository.findById(1L)).thenReturn(application);
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, ValidationStatusEnum.MANUAL);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "OTHER_UNIT", "A04026919", false));

        ApplicationDir3MismatchOutputDTO result = dir3CheckServiceFacadeBean.checkDir3Mismatch(1L);

        assertFalse(result.isDir3Mismatch());
        assertEquals(ValidationStatusEnum.MANUAL, responsible.getDir3Validation().getDir3Status());
        verify(dir3ValidationRepository, never()).update(any(), any());
    }

    @Test
    void checkDir3Mismatch_blankAdmUnitCode_returnsNoMismatchWithoutLookingUpAssignments() {
        Application application = applicationWithAdmUnitCode(1L, "App One", null);
        when(applicationRepository.findById(1L)).thenReturn(application);

        ApplicationDir3MismatchOutputDTO result = dir3CheckServiceFacadeBean.checkDir3Mismatch(1L);

        assertEquals(1L, result.getId());
        assertFalse(result.isDir3Mismatch());
        verify(appResponsibleAuthorizedRepository, never()).findByApplicationId(any());
        verify(personService, never()).checkDir3(any(), any());
    }

    // ------------------------------------------------------------------
    // checkDir3Mismatch with a DIR3 primaryGroupDir3 / getAssignedPersonEmails
    // ------------------------------------------------------------------

    @Test
    void checkDir3MismatchWithPrimaryGroupDir3_mismatchedResponsible_downgradesUsingThePrimaryGroupDir3WithoutCallingSoffid() {
        Application application = applicationWithAdmUnitCode(1L, "App One", "A04026919");
        when(applicationRepository.findById(1L)).thenReturn(application);
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, ValidationStatusEnum.VALIDATED);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        PrimaryGroupDir3 primaryGroupDir3 = new PrimaryGroupDir3(java.util.Map.of("person50@caib.es", "dgtic"),
                java.util.Map.of("dgtic", "A04003003"));

        ApplicationDir3MismatchOutputDTO result = dir3CheckServiceFacadeBean.checkDir3Mismatch(1L, primaryGroupDir3);

        assertTrue(result.isDir3Mismatch());
        assertEquals(ValidationStatusEnum.NOT_VALIDATED, responsible.getDir3Validation().getDir3Status());
        verify(dir3ValidationRepository).update(responsible.getDir3Validation(), 700L);
        verify(personService, never()).checkDir3(any(), any());
    }

    @Test
    void checkDir3MismatchWithPrimaryGroupDir3_matchingResponsible_isPromotedAndNotFlagged() {
        Application application = applicationWithAdmUnitCode(1L, "App One", "A04026919");
        application.setDir3Mismatch(true);
        when(applicationRepository.findById(1L)).thenReturn(application);
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, ValidationStatusEnum.NOT_VALIDATED);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        PrimaryGroupDir3 primaryGroupDir3 = new PrimaryGroupDir3(java.util.Map.of("person50@caib.es", "sgaip"),
                java.util.Map.of("sgaip", "A04026919"));

        ApplicationDir3MismatchOutputDTO result = dir3CheckServiceFacadeBean.checkDir3Mismatch(1L, primaryGroupDir3);

        assertFalse(result.isDir3Mismatch());
        assertEquals(ValidationStatusEnum.VALIDATED, responsible.getDir3Validation().getDir3Status());
        assertFalse(application.isDir3Mismatch());
        verify(personService, never()).checkDir3(any(), any());
    }

    @Test
    void checkDir3MismatchWithPrimaryGroupDir3_personMissingFromThePrimaryGroupDir3_isReadAsAMismatchLikeAnUnknownPersonInSoffid() {
        Application application = applicationWithAdmUnitCode(1L, "App One", "A04026919");
        when(applicationRepository.findById(1L)).thenReturn(application);
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, ValidationStatusEnum.VALIDATED);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());

        ApplicationDir3MismatchOutputDTO result = dir3CheckServiceFacadeBean.checkDir3Mismatch(1L,
                new PrimaryGroupDir3(java.util.Map.of(), java.util.Map.of()));

        assertTrue(result.isDir3Mismatch());
        assertEquals(ValidationStatusEnum.NOT_VALIDATED, responsible.getDir3Validation().getDir3Status());
    }

    @Test
    void checkDir3MismatchWithPrimaryGroupDir3_unknownApplication_throwsBusinessRuleException() {
        when(applicationRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> dir3CheckServiceFacadeBean.checkDir3Mismatch(99L, new PrimaryGroupDir3(java.util.Map.of(), java.util.Map.of())));

        assertEquals(Constants.ERR_APP_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAssignedPersonEmails_mergesResponsiblesAndAuthorizedWithoutRepeats() {
        when(appResponsibleRepository.findAllActivePersonalCaibEmails()).thenReturn(List.of("ana@caib.es", "joan@caib.es"));
        when(appAuthorizedRepository.findAllActivePersonalCaibEmails()).thenReturn(List.of("joan@caib.es", "pere@caib.es"));

        assertEquals(List.of("ana@caib.es", "joan@caib.es", "pere@caib.es"), dir3CheckServiceFacadeBean.getAssignedPersonEmails());
    }
}
