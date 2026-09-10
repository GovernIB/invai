package es.caib.invai.back.ejb.maintenance.security.personalDataProcessing;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.security.personalDataProcessing.DTO.PersonalDataProcessingInputDTO;
import es.caib.invai.back.interna.maintenance.security.personalDataProcessing.DTO.PersonalDataProcessingOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.security.personalDataProcessing.PersonalDataProcessingCriteria;
import es.caib.invai.back.persistence.repository.maintenance.security.personalDataProcessing.PersonalDataProcessingRepository;
import es.caib.invai.back.service.mapper.maintenance.security.personalDataProcessing.PersonalDataProcessingMapper;
import es.caib.invai.back.service.model.maintenance.security.personalDataProcessing.PersonalDataProcessing;
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
 * Unit tests for {@link PersonalDataProcessingServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link PersonalDataProcessingRepository} and {@link PersonalDataProcessingMapper} collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class PersonalDataProcessingServiceFacadeBeanTest {

    @Mock
    private PersonalDataProcessingMapper personalDataProcessingMapper;

    @Mock
    private PersonalDataProcessingRepository personalDataProcessingRepository;

    @InjectMocks
    private PersonalDataProcessingServiceFacadeBean personalDataProcessingServiceFacadeBean;

    private PersonalDataProcessing activePersonalDataProcessing;

    @BeforeEach
    void setUp() {
        activePersonalDataProcessing = new PersonalDataProcessing();
        activePersonalDataProcessing.setId(1L);
        activePersonalDataProcessing.setName("Tractament de nomines");
    }

    @Test
    void getById_found_returnsMappedResponse() {
        PersonalDataProcessingOutputDTO expected = new PersonalDataProcessingOutputDTO();
        when(personalDataProcessingRepository.findById(1L)).thenReturn(activePersonalDataProcessing);
        when(personalDataProcessingMapper.toResponse(activePersonalDataProcessing)).thenReturn(expected);

        PersonalDataProcessingOutputDTO result = personalDataProcessingServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(personalDataProcessingRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> personalDataProcessingServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_PERSONALDATAPROCESSING_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        PersonalDataProcessingCriteria criteria = new PersonalDataProcessingCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<PersonalDataProcessing> domainPage = new PageImpl<>(List.of(activePersonalDataProcessing));
        PersonalDataProcessingOutputDTO mapped = new PersonalDataProcessingOutputDTO();
        when(personalDataProcessingRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(personalDataProcessingMapper.toResponse(activePersonalDataProcessing)).thenReturn(mapped);

        Page<PersonalDataProcessingOutputDTO> result = personalDataProcessingServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueName_persistsAndReturnsResponse() {
        PersonalDataProcessingInputDTO inputDTO = new PersonalDataProcessingInputDTO("Tractament de videovigilancia", "Tractament de videovigilancia");
        PersonalDataProcessing model = new PersonalDataProcessing();
        PersonalDataProcessing saved = new PersonalDataProcessing();
        PersonalDataProcessingOutputDTO response = new PersonalDataProcessingOutputDTO();
        when(personalDataProcessingRepository.existsByNameAndDeletedAtIsNull("Tractament de videovigilancia")).thenReturn(false);
        when(personalDataProcessingMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(personalDataProcessingRepository.create(model)).thenReturn(saved);
        when(personalDataProcessingMapper.toResponse(saved)).thenReturn(response);

        PersonalDataProcessingOutputDTO result = personalDataProcessingServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateName_throwsBusinessRuleException() {
        PersonalDataProcessingInputDTO inputDTO = new PersonalDataProcessingInputDTO("Tractament de nomines", "Tractament de nomines");
        when(personalDataProcessingRepository.existsByNameAndDeletedAtIsNull("Tractament de nomines")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> personalDataProcessingServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_PERSONALDATAPROCESSING_DUPLICATED, ex.getMessage());
        verify(personalDataProcessingRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        PersonalDataProcessingInputDTO inputDTO = new PersonalDataProcessingInputDTO("X", "X ES");
        when(personalDataProcessingRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> personalDataProcessingServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_PERSONALDATAPROCESSING_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_duplicateName_throwsBusinessRuleException() {
        PersonalDataProcessingInputDTO inputDTO = new PersonalDataProcessingInputDTO("Taken", "Taken ES");
        when(personalDataProcessingRepository.findById(1L)).thenReturn(activePersonalDataProcessing);
        when(personalDataProcessingRepository.existsByNameAndIdNotAndDeletedAtIsNull("Taken", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> personalDataProcessingServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_PERSONALDATAPROCESSING_DUPLICATED, ex.getMessage());
        verify(personalDataProcessingMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        PersonalDataProcessingInputDTO inputDTO = new PersonalDataProcessingInputDTO("Updated", "Updated ES");
        PersonalDataProcessing updated = new PersonalDataProcessing();
        PersonalDataProcessingOutputDTO response = new PersonalDataProcessingOutputDTO();
        when(personalDataProcessingRepository.findById(1L)).thenReturn(activePersonalDataProcessing);
        when(personalDataProcessingRepository.existsByNameAndIdNotAndDeletedAtIsNull("Updated", 1L)).thenReturn(false);
        when(personalDataProcessingRepository.update(activePersonalDataProcessing, 1L)).thenReturn(updated);
        when(personalDataProcessingMapper.toResponse(updated)).thenReturn(response);

        PersonalDataProcessingOutputDTO result = personalDataProcessingServiceFacadeBean.update(1L, inputDTO);

        verify(personalDataProcessingMapper).updateModelFromInput(inputDTO, activePersonalDataProcessing);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(personalDataProcessingRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> personalDataProcessingServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_PERSONALDATAPROCESSING_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activePersonalDataProcessing.setDeletedAt(LocalDateTime.now());
        when(personalDataProcessingRepository.findById(1L)).thenReturn(activePersonalDataProcessing);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> personalDataProcessingServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_PERSONALDATAPROCESSING_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesPersonalDataProcessing() {
        when(personalDataProcessingRepository.findById(1L)).thenReturn(activePersonalDataProcessing);

        personalDataProcessingServiceFacadeBean.delete(1L);

        assertNotNull(activePersonalDataProcessing.getDeletedAt());
        verify(personalDataProcessingRepository, times(1)).delete(activePersonalDataProcessing);
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(personalDataProcessingRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> personalDataProcessingServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_PERSONALDATAPROCESSING_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(personalDataProcessingRepository.findById(1L)).thenReturn(activePersonalDataProcessing);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> personalDataProcessingServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_PERSONALDATAPROCESSING_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesPersonalDataProcessing() {
        activePersonalDataProcessing.setDeletedAt(LocalDateTime.now());
        activePersonalDataProcessing.setDeletedBy("someone");
        PersonalDataProcessing reactivated = new PersonalDataProcessing();
        PersonalDataProcessingOutputDTO response = new PersonalDataProcessingOutputDTO();
        when(personalDataProcessingRepository.findById(1L)).thenReturn(activePersonalDataProcessing);
        when(personalDataProcessingRepository.update(eq(activePersonalDataProcessing), eq(1L))).thenReturn(reactivated);
        when(personalDataProcessingMapper.toResponse(reactivated)).thenReturn(response);

        PersonalDataProcessingOutputDTO result = personalDataProcessingServiceFacadeBean.reactivate(1L);

        assertNull(activePersonalDataProcessing.getDeletedAt());
        assertNull(activePersonalDataProcessing.getDeletedBy());
        assertEquals(response, result);
    }
}
