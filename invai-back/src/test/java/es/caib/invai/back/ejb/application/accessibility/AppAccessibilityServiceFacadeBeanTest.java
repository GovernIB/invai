package es.caib.invai.back.ejb.application.accessibility;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.accessibility.DTO.AppAccessibilityInputDTO;
import es.caib.invai.back.interna.application.accessibility.DTO.AppAccessibilityOutputDTO;
import es.caib.invai.back.persistence.repository.application.accessibility.AppAccessibilityRepository;
import es.caib.invai.back.service.mapper.application.accessibility.AppAccessibilityMapper;
import es.caib.invai.back.service.model.application.accessibility.AppAccessibility;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppAccessibilityServiceFacadeBean}, exercising every branch of its
 * business rules with the {@link AppAccessibilityRepository} and
 * {@link AppAccessibilityMapper} collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class AppAccessibilityServiceFacadeBeanTest {

    @Mock
    private AppAccessibilityMapper appAccessibilityMapper;

    @Mock
    private AppAccessibilityRepository appAccessibilityRepository;

    @InjectMocks
    private AppAccessibilityServiceFacadeBean appAccessibilityServiceFacadeBean;

    private AppAccessibility activeModel;

    @BeforeEach
    void setUp() {
        activeModel = new AppAccessibility();
        activeModel.setId(1L);
    }

    private AppAccessibilityInputDTO buildInputDTO() {
        return new AppAccessibilityInputDTO(10L, 400L, 401L, "https://invai.example.test",
                Boolean.TRUE, "APP INVAI Android", "PDF antic", "Observacio",
                LocalDateTime.of(2026, 1, 1, 0, 0));
    }

    @Test
    void getById_delegatesToRepositoryAndMapsResponse() {
        AppAccessibilityOutputDTO mapped = new AppAccessibilityOutputDTO();
        when(appAccessibilityRepository.findById(1L)).thenReturn(activeModel);
        when(appAccessibilityMapper.toResponse(activeModel)).thenReturn(mapped);

        AppAccessibilityOutputDTO result = appAccessibilityServiceFacadeBean.getById(1L);

        assertEquals(mapped, result);
    }

    @Test
    void create_valid_persistsAndReturnsResponse() {
        AppAccessibilityInputDTO inputDTO = buildInputDTO();
        AppAccessibility model = new AppAccessibility();
        AppAccessibility saved = new AppAccessibility();
        AppAccessibilityOutputDTO response = new AppAccessibilityOutputDTO();
        when(appAccessibilityMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appAccessibilityRepository.create(model)).thenReturn(saved);
        when(appAccessibilityMapper.toResponse(saved)).thenReturn(response);

        AppAccessibilityOutputDTO result = appAccessibilityServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_sanitizesInputBeforeMapping() {
        AppAccessibilityInputDTO inputDTO = new AppAccessibilityInputDTO(10L, 400L, null, null,
                null, null, null, "  Notes  ", null);
        when(appAccessibilityMapper.toModelFromInput(inputDTO)).thenReturn(new AppAccessibility());
        when(appAccessibilityRepository.create(any())).thenReturn(new AppAccessibility());
        when(appAccessibilityMapper.toResponse(any())).thenReturn(new AppAccessibilityOutputDTO());

        appAccessibilityServiceFacadeBean.create(inputDTO);

        assertEquals("Notes", inputDTO.getObservations());
    }

    @Test
    void create_applicationAlreadyHasActiveAccessibilityAnchor_throwsBusinessRuleException() {
        AppAccessibilityInputDTO inputDTO = buildInputDTO();
        when(appAccessibilityRepository.findByApplicationId(10L)).thenReturn(activeModel);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appAccessibilityServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_APP_ACCESSIBILITY_ALREADY_EXISTS, ex.getMessage());
        verify(appAccessibilityRepository, org.mockito.Mockito.never()).create(any());
    }

    @Test
    void create_applicationHasOnlySoftDeletedAccessibilityAnchor_persistsNewRecord() {
        activeModel.setDeletedAt(LocalDateTime.now());
        AppAccessibilityInputDTO inputDTO = buildInputDTO();
        AppAccessibility model = new AppAccessibility();
        AppAccessibility saved = new AppAccessibility();
        AppAccessibilityOutputDTO response = new AppAccessibilityOutputDTO();
        when(appAccessibilityRepository.findByApplicationId(10L)).thenReturn(activeModel);
        when(appAccessibilityMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appAccessibilityRepository.create(model)).thenReturn(saved);
        when(appAccessibilityMapper.toResponse(saved)).thenReturn(response);

        AppAccessibilityOutputDTO result = appAccessibilityServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        AppAccessibilityInputDTO inputDTO = buildInputDTO();
        when(appAccessibilityRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appAccessibilityServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_APP_ACCESSIBILITY_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        AppAccessibilityInputDTO inputDTO = buildInputDTO();
        AppAccessibility updated = new AppAccessibility();
        AppAccessibilityOutputDTO response = new AppAccessibilityOutputDTO();
        when(appAccessibilityRepository.findById(1L)).thenReturn(activeModel);
        when(appAccessibilityRepository.update(activeModel, 1L)).thenReturn(updated);
        when(appAccessibilityMapper.toResponse(updated)).thenReturn(response);

        AppAccessibilityOutputDTO result = appAccessibilityServiceFacadeBean.update(1L, inputDTO);

        verify(appAccessibilityMapper).updateModelFromInput(inputDTO, activeModel);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(appAccessibilityRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appAccessibilityServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_APP_ACCESSIBILITY_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeModel.setDeletedAt(LocalDateTime.now());
        when(appAccessibilityRepository.findById(1L)).thenReturn(activeModel);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appAccessibilityServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_APP_ACCESSIBILITY_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesModel() {
        when(appAccessibilityRepository.findById(1L)).thenReturn(activeModel);

        appAccessibilityServiceFacadeBean.delete(1L);

        assertNotNull(activeModel.getDeletedAt());
        verify(appAccessibilityRepository).delete(activeModel);
    }
}
