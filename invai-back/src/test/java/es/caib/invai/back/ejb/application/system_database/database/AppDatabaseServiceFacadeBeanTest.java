package es.caib.invai.back.ejb.application.system_database.database;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.system_database.database.DTO.AppDatabaseInputDTO;
import es.caib.invai.back.interna.application.system_database.database.DTO.AppDatabaseOutputDTO;
import es.caib.invai.back.persistence.repository.application.system_database.database.AppDatabaseCriteria;
import es.caib.invai.back.persistence.repository.application.system_database.database.AppDatabaseRepository;
import es.caib.invai.back.service.mapper.application.system_database.database.AppDatabaseMapper;
import es.caib.invai.back.service.model.application.system_database.database.AppDatabase;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import es.caib.invai.back.ejb.application.system_database.database.AppDatabaseServiceFacadeBean;

/**
 * Unit tests for {@link AppDatabaseServiceFacadeBean}, exercising every branch of its business
 * rules with the {@link AppDatabaseRepository} and {@link AppDatabaseMapper} collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class AppDatabaseServiceFacadeBeanTest {

    @Mock
    private AppDatabaseMapper appDatabaseMapper;

    @Mock
    private AppDatabaseRepository appDatabaseRepository;

    @InjectMocks
    private AppDatabaseServiceFacadeBean appDatabaseServiceFacadeBean;

    private AppDatabase activeModel;

    @BeforeEach
    void setUp() {
        activeModel = new AppDatabase();
        activeModel.setId(1L);
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        AppDatabaseCriteria criteria = new AppDatabaseCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppDatabase> domainPage = new PageImpl<>(List.of(activeModel));
        AppDatabaseOutputDTO mapped = new AppDatabaseOutputDTO();
        when(appDatabaseRepository.findAll(10L, criteria, pageable)).thenReturn(domainPage);
        when(appDatabaseMapper.toResponse(activeModel)).thenReturn(mapped);

        Page<AppDatabaseOutputDTO> result = appDatabaseServiceFacadeBean.getAll(10L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueLink_persistsAndReturnsResponse() {
        AppDatabaseInputDTO inputDTO = new AppDatabaseInputDTO(5L, 7L);
        AppDatabase model = new AppDatabase();
        AppDatabase saved = new AppDatabase();
        AppDatabaseOutputDTO response = new AppDatabaseOutputDTO();
        when(appDatabaseRepository.existsByInformationSystemDbIdAndDatabaseId(5L, 7L)).thenReturn(false);
        when(appDatabaseMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appDatabaseRepository.create(model)).thenReturn(saved);
        when(appDatabaseMapper.toResponse(saved)).thenReturn(response);

        AppDatabaseOutputDTO result = appDatabaseServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateLink_throwsBusinessRuleException() {
        AppDatabaseInputDTO inputDTO = new AppDatabaseInputDTO(5L, 7L);
        when(appDatabaseRepository.existsByInformationSystemDbIdAndDatabaseId(5L, 7L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appDatabaseServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_APP_DATABASE_DUPLICATED, ex.getMessage());
        verify(appDatabaseRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        AppDatabaseInputDTO inputDTO = new AppDatabaseInputDTO(5L, 7L);
        when(appDatabaseRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appDatabaseServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_APP_DATABASE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_duplicateLink_throwsBusinessRuleException() {
        AppDatabaseInputDTO inputDTO = new AppDatabaseInputDTO(5L, 7L);
        when(appDatabaseRepository.findById(1L)).thenReturn(activeModel);
        when(appDatabaseRepository.existsByUniqueCombinationExcludingId(5L, 7L, 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appDatabaseServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_APP_DATABASE_DUPLICATED, ex.getMessage());
        verify(appDatabaseMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        AppDatabaseInputDTO inputDTO = new AppDatabaseInputDTO(5L, 7L);
        AppDatabase updated = new AppDatabase();
        AppDatabaseOutputDTO response = new AppDatabaseOutputDTO();
        when(appDatabaseRepository.findById(1L)).thenReturn(activeModel);
        when(appDatabaseRepository.existsByUniqueCombinationExcludingId(5L, 7L, 1L)).thenReturn(false);
        when(appDatabaseRepository.update(activeModel, 1L)).thenReturn(updated);
        when(appDatabaseMapper.toResponse(updated)).thenReturn(response);

        AppDatabaseOutputDTO result = appDatabaseServiceFacadeBean.update(1L, inputDTO);

        verify(appDatabaseMapper).updateModelFromInput(inputDTO, activeModel);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(appDatabaseRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appDatabaseServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_APP_DATABASE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeModel.setDeletedAt(LocalDateTime.now());
        when(appDatabaseRepository.findById(1L)).thenReturn(activeModel);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appDatabaseServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_APP_DATABASE_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesModel() {
        when(appDatabaseRepository.findById(1L)).thenReturn(activeModel);

        appDatabaseServiceFacadeBean.delete(1L);

        assertNotNull(activeModel.getDeletedAt());
        verify(appDatabaseRepository).delete(activeModel);
    }
}
