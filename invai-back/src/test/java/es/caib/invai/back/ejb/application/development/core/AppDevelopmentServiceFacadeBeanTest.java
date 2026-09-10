package es.caib.invai.back.ejb.application.development.core;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.development.core.DTO.DevelopmentInputDTO;
import es.caib.invai.back.interna.application.development.core.DTO.DevelopmentOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.core.AppDevelopmentRepository;
import es.caib.invai.back.service.mapper.application.development.core.AppDevelopmentMapper;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppDevelopmentServiceFacadeBean}, exercising every branch of its business
 * rules with the {@link AppDevelopmentRepository} and {@link AppDevelopmentMapper} collaborators fully
 * mocked. This is a 1:1 child entity scoped to a parent {@code Application}, addressed by its own
 * record ID via {@code getById}/{@code create}/{@code update}/{@code delete}; it has no
 * {@code reactivate} operation.
 */
@ExtendWith(MockitoExtension.class)
class AppDevelopmentServiceFacadeBeanTest {

    @Mock
    private AppDevelopmentMapper appDevelopmentMapper;

    @Mock
    private AppDevelopmentRepository appDevelopmentRepository;

    @InjectMocks
    private AppDevelopmentServiceFacadeBean developmentServiceFacadeBean;

    private AppDevelopment activeDevelopment;

    @BeforeEach
    void setUp() {
        activeDevelopment = new AppDevelopment();
        activeDevelopment.setId(1L);
        activeDevelopment.setCode("https://git.example.com/repo.git");
    }

    @Test
    void getById_delegatesToRepositoryAndMapsResponse() {
        DevelopmentOutputDTO mapped = new DevelopmentOutputDTO();
        when(appDevelopmentRepository.findById(1L)).thenReturn(activeDevelopment);
        when(appDevelopmentMapper.toResponse(activeDevelopment)).thenReturn(mapped);

        DevelopmentOutputDTO result = developmentServiceFacadeBean.getById(1L);

        assertEquals(mapped, result);
    }

    @Test
    void create_valid_persistsAndReturnsResponse() {
        DevelopmentInputDTO inputDTO = new DevelopmentInputDTO(2L, 3L, 4L, "  https://repo  ", 5L,
                LocalDateTime.now(), "  Some observation  ");
        AppDevelopment model = new AppDevelopment();
        AppDevelopment saved = new AppDevelopment();
        DevelopmentOutputDTO response = new DevelopmentOutputDTO();
        when(appDevelopmentMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appDevelopmentRepository.create(model)).thenReturn(saved);
        when(appDevelopmentMapper.toResponse(saved)).thenReturn(response);

        DevelopmentOutputDTO result = developmentServiceFacadeBean.create(inputDTO);

        assertEquals("https://repo", inputDTO.getCode());
        assertEquals("Some observation", inputDTO.getObservation());
        assertEquals(response, result);
    }

    @Test
    void create_applicationAlreadyHasDevelopment_throwsBusinessRuleException() {
        DevelopmentInputDTO inputDTO = new DevelopmentInputDTO(2L, 3L, 4L, "https://repo", 5L,
                LocalDateTime.now(), "obs");
        when(appDevelopmentRepository.findByApplicationId(2L)).thenReturn(activeDevelopment);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> developmentServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_DEVELOPMENT_ALREADY_EXISTS, ex.getMessage());
        verify(appDevelopmentRepository, never()).create(any());
    }

    @Test
    void create_applicationHasOnlySoftDeletedDevelopment_persistsNewRecord() {
        activeDevelopment.setDeletedAt(LocalDateTime.now());
        DevelopmentInputDTO inputDTO = new DevelopmentInputDTO(2L, 3L, 4L, "https://repo", 5L,
                LocalDateTime.now(), "obs");
        AppDevelopment model = new AppDevelopment();
        AppDevelopment saved = new AppDevelopment();
        DevelopmentOutputDTO response = new DevelopmentOutputDTO();
        when(appDevelopmentRepository.findByApplicationId(2L)).thenReturn(activeDevelopment);
        when(appDevelopmentMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appDevelopmentRepository.create(model)).thenReturn(saved);
        when(appDevelopmentMapper.toResponse(saved)).thenReturn(response);

        DevelopmentOutputDTO result = developmentServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
        verify(appDevelopmentRepository).create(model);
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        DevelopmentInputDTO inputDTO = new DevelopmentInputDTO(2L, 3L, 4L, "code", 5L, LocalDateTime.now(), "obs");
        when(appDevelopmentRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> developmentServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_DEVELOPMENT_NOT_FOUND, ex.getMessage());
        verify(appDevelopmentMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        DevelopmentInputDTO inputDTO = new DevelopmentInputDTO(2L, 3L, 4L, " newcode ", 5L, LocalDateTime.now(),
                " new observation ");
        AppDevelopment updated = new AppDevelopment();
        DevelopmentOutputDTO response = new DevelopmentOutputDTO();
        when(appDevelopmentRepository.findById(1L)).thenReturn(activeDevelopment);
        when(appDevelopmentRepository.update(activeDevelopment, 1L)).thenReturn(updated);
        when(appDevelopmentMapper.toResponse(updated)).thenReturn(response);

        DevelopmentOutputDTO result = developmentServiceFacadeBean.update(1L, inputDTO);

        assertEquals("newcode", inputDTO.getCode());
        assertEquals("new observation", inputDTO.getObservation());
        verify(appDevelopmentMapper).updateModelFromInput(inputDTO, activeDevelopment);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(appDevelopmentRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> developmentServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_DEVELOPMENT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeDevelopment.setDeletedAt(LocalDateTime.now());
        when(appDevelopmentRepository.findById(1L)).thenReturn(activeDevelopment);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> developmentServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_DEVELOPMENT_NOT_ACTIVE, ex.getMessage());
        verify(appDevelopmentRepository, never()).delete(any());
    }

    @Test
    void delete_valid_softDeletesDevelopment() {
        when(appDevelopmentRepository.findById(1L)).thenReturn(activeDevelopment);

        developmentServiceFacadeBean.delete(1L);

        assertNotNull(activeDevelopment.getDeletedAt());
        verify(appDevelopmentRepository, times(1)).delete(activeDevelopment);
    }
}
