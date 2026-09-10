package es.caib.invai.back.ejb.application.system_database.core;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.system_database.core.DTO.AppInformationSystemDbInputDTO;
import es.caib.invai.back.interna.application.system_database.core.DTO.AppInformationSystemDbOutputDTO;
import es.caib.invai.back.persistence.repository.application.system_database.core.AppInformationSystemDbRepository;
import es.caib.invai.back.service.mapper.application.system_database.core.AppInformationSystemDbMapper;
import es.caib.invai.back.service.model.application.system_database.core.AppInformationSystemDb;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppInformationSystemDbServiceFacadeBean}, exercising every branch of its
 * business rules with the {@link AppInformationSystemDbRepository} and
 * {@link AppInformationSystemDbMapper} collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class AppInformationSystemDbServiceFacadeBeanTest {

    @Mock
    private AppInformationSystemDbMapper appInformationSystemDbMapper;

    @Mock
    private AppInformationSystemDbRepository appInformationSystemDbRepository;

    @InjectMocks
    private AppInformationSystemDbServiceFacadeBean appInformationSystemDbServiceFacadeBean;

    private AppInformationSystemDb activeModel;

    @BeforeEach
    void setUp() {
        activeModel = new AppInformationSystemDb();
        activeModel.setId(1L);
        activeModel.setObservation("Core grouping");
    }

    @Test
    void getById_delegatesToRepositoryAndMapsResponse() {
        AppInformationSystemDbOutputDTO mapped = new AppInformationSystemDbOutputDTO();
        when(appInformationSystemDbRepository.findById(1L)).thenReturn(activeModel);
        when(appInformationSystemDbMapper.toResponse(activeModel)).thenReturn(mapped);

        AppInformationSystemDbOutputDTO result = appInformationSystemDbServiceFacadeBean.getById(1L);

        assertEquals(mapped, result);
    }

    @Test
    void create_valid_persistsAndReturnsResponse() {
        AppInformationSystemDbInputDTO inputDTO = new AppInformationSystemDbInputDTO(10L, "Notes");
        AppInformationSystemDb model = new AppInformationSystemDb();
        AppInformationSystemDb saved = new AppInformationSystemDb();
        AppInformationSystemDbOutputDTO response = new AppInformationSystemDbOutputDTO();
        when(appInformationSystemDbMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appInformationSystemDbRepository.create(model)).thenReturn(saved);
        when(appInformationSystemDbMapper.toResponse(saved)).thenReturn(response);

        AppInformationSystemDbOutputDTO result = appInformationSystemDbServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
        assertEquals("Notes", inputDTO.getObservation());
    }

    @Test
    void create_sanitizesInputBeforeMapping() {
        AppInformationSystemDbInputDTO inputDTO = new AppInformationSystemDbInputDTO(10L, "  Notes  ");
        when(appInformationSystemDbMapper.toModelFromInput(inputDTO)).thenReturn(new AppInformationSystemDb());
        when(appInformationSystemDbRepository.create(org.mockito.ArgumentMatchers.any())).thenReturn(new AppInformationSystemDb());
        when(appInformationSystemDbMapper.toResponse(org.mockito.ArgumentMatchers.any())).thenReturn(new AppInformationSystemDbOutputDTO());

        appInformationSystemDbServiceFacadeBean.create(inputDTO);

        assertEquals("Notes", inputDTO.getObservation());
    }

    @Test
    void create_applicationAlreadyHasActiveGrouping_throwsBusinessRuleException() {
        AppInformationSystemDbInputDTO inputDTO = new AppInformationSystemDbInputDTO(10L, "Notes");
        when(appInformationSystemDbRepository.findByApplicationId(10L)).thenReturn(activeModel);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appInformationSystemDbServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_APP_INFORMATION_SYSTEM_DB_ALREADY_EXISTS, ex.getMessage());
        verify(appInformationSystemDbRepository, org.mockito.Mockito.never()).create(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void create_applicationHasOnlySoftDeletedGrouping_persistsNewRecord() {
        activeModel.setDeletedAt(LocalDateTime.now());
        AppInformationSystemDbInputDTO inputDTO = new AppInformationSystemDbInputDTO(10L, "Notes");
        AppInformationSystemDb model = new AppInformationSystemDb();
        AppInformationSystemDb saved = new AppInformationSystemDb();
        AppInformationSystemDbOutputDTO response = new AppInformationSystemDbOutputDTO();
        when(appInformationSystemDbRepository.findByApplicationId(10L)).thenReturn(activeModel);
        when(appInformationSystemDbMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appInformationSystemDbRepository.create(model)).thenReturn(saved);
        when(appInformationSystemDbMapper.toResponse(saved)).thenReturn(response);

        AppInformationSystemDbOutputDTO result = appInformationSystemDbServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        AppInformationSystemDbInputDTO inputDTO = new AppInformationSystemDbInputDTO(10L, "Notes");
        when(appInformationSystemDbRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appInformationSystemDbServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_APP_INFORMATION_SYSTEM_DB_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        AppInformationSystemDbInputDTO inputDTO = new AppInformationSystemDbInputDTO(10L, "Updated");
        AppInformationSystemDb updated = new AppInformationSystemDb();
        AppInformationSystemDbOutputDTO response = new AppInformationSystemDbOutputDTO();
        when(appInformationSystemDbRepository.findById(1L)).thenReturn(activeModel);
        when(appInformationSystemDbRepository.update(activeModel, 1L)).thenReturn(updated);
        when(appInformationSystemDbMapper.toResponse(updated)).thenReturn(response);

        AppInformationSystemDbOutputDTO result = appInformationSystemDbServiceFacadeBean.update(1L, inputDTO);

        verify(appInformationSystemDbMapper).updateModelFromInput(inputDTO, activeModel);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(appInformationSystemDbRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appInformationSystemDbServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_APP_INFORMATION_SYSTEM_DB_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeModel.setDeletedAt(LocalDateTime.now());
        when(appInformationSystemDbRepository.findById(1L)).thenReturn(activeModel);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appInformationSystemDbServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_APP_INFORMATION_SYSTEM_DB_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesModel() {
        when(appInformationSystemDbRepository.findById(1L)).thenReturn(activeModel);

        appInformationSystemDbServiceFacadeBean.delete(1L);

        assertNotNull(activeModel.getDeletedAt());
        verify(appInformationSystemDbRepository).delete(activeModel);
    }
}
