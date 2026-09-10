package es.caib.invai.back.ejb.application.security.ensClassification;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.security.ensClassification.DTO.AppEnsClassificationInputDTO;
import es.caib.invai.back.interna.application.security.ensClassification.DTO.AppEnsClassificationOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.ensClassification.AppEnsClassificationCriteria;
import es.caib.invai.back.persistence.repository.application.security.ensClassification.AppEnsClassificationRepository;
import es.caib.invai.back.service.mapper.application.security.ensClassification.AppEnsClassificationMapper;
import es.caib.invai.back.service.model.application.security.ensClassification.AppEnsClassification;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppEnsClassificationServiceFacadeBean}, exercising every branch of its business
 * rules with the {@link AppEnsClassificationRepository} and {@link AppEnsClassificationMapper} collaborators
 * fully mocked. Unlike sibling entities, ENS classification has no duplicate/uniqueness business rule.
 */
@ExtendWith(MockitoExtension.class)
class AppEnsClassificationServiceFacadeBeanTest {

    @Mock
    private AppEnsClassificationMapper appEnsClassificationMapper;

    @Mock
    private AppEnsClassificationRepository appEnsClassificationRepository;

    @InjectMocks
    private AppEnsClassificationServiceFacadeBean appEnsClassificationServiceFacadeBean;

    private AppEnsClassification activeModel;

    @BeforeEach
    void setUp() {
        activeModel = new AppEnsClassification();
        activeModel.setId(1L);
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        AppEnsClassificationCriteria criteria = new AppEnsClassificationCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppEnsClassification> domainPage = new PageImpl<>(List.of(activeModel));
        AppEnsClassificationOutputDTO mapped = new AppEnsClassificationOutputDTO();
        when(appEnsClassificationRepository.findAll(10L, criteria, pageable)).thenReturn(domainPage);
        when(appEnsClassificationMapper.toResponse(activeModel)).thenReturn(mapped);

        Page<AppEnsClassificationOutputDTO> result = appEnsClassificationServiceFacadeBean.getAll(10L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_valid_persistsAndReturnsResponse() {
        AppEnsClassificationInputDTO inputDTO = new AppEnsClassificationInputDTO();
        inputDTO.setAppSecurityId(5L);
        AppEnsClassification model = new AppEnsClassification();
        AppEnsClassification saved = new AppEnsClassification();
        AppEnsClassificationOutputDTO response = new AppEnsClassificationOutputDTO();
        when(appEnsClassificationMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appEnsClassificationRepository.create(model)).thenReturn(saved);
        when(appEnsClassificationMapper.toResponse(saved)).thenReturn(response);

        AppEnsClassificationOutputDTO result = appEnsClassificationServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        AppEnsClassificationInputDTO inputDTO = new AppEnsClassificationInputDTO();
        inputDTO.setAppSecurityId(5L);
        when(appEnsClassificationRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appEnsClassificationServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_APP_ENS_CLASSIFICATION_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        AppEnsClassificationInputDTO inputDTO = new AppEnsClassificationInputDTO();
        inputDTO.setAppSecurityId(5L);
        AppEnsClassification updated = new AppEnsClassification();
        AppEnsClassificationOutputDTO response = new AppEnsClassificationOutputDTO();
        when(appEnsClassificationRepository.findById(1L)).thenReturn(activeModel);
        when(appEnsClassificationRepository.update(activeModel, 1L)).thenReturn(updated);
        when(appEnsClassificationMapper.toResponse(updated)).thenReturn(response);

        AppEnsClassificationOutputDTO result = appEnsClassificationServiceFacadeBean.update(1L, inputDTO);

        verify(appEnsClassificationMapper).updateModelFromInput(inputDTO, activeModel);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(appEnsClassificationRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appEnsClassificationServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_APP_ENS_CLASSIFICATION_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeModel.setDeletedAt(LocalDateTime.now());
        when(appEnsClassificationRepository.findById(1L)).thenReturn(activeModel);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appEnsClassificationServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_APP_ENS_CLASSIFICATION_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesModel() {
        when(appEnsClassificationRepository.findById(1L)).thenReturn(activeModel);

        appEnsClassificationServiceFacadeBean.delete(1L);

        assertNotNull(activeModel.getDeletedAt());
        verify(appEnsClassificationRepository).delete(activeModel);
    }
}
