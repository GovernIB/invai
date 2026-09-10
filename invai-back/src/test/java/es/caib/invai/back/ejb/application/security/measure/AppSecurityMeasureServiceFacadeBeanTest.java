package es.caib.invai.back.ejb.application.security.measure;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.security.measure.DTO.AppSecurityMeasureInputDTO;
import es.caib.invai.back.interna.application.security.measure.DTO.AppSecurityMeasureOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.measure.AppSecurityMeasureCriteria;
import es.caib.invai.back.persistence.repository.application.security.measure.AppSecurityMeasureRepository;
import es.caib.invai.back.service.mapper.application.security.measure.AppSecurityMeasureMapper;
import es.caib.invai.back.service.model.application.security.measure.AppSecurityMeasure;
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
 * Unit tests for {@link AppSecurityMeasureServiceFacadeBean}, exercising every branch of its
 * business rules with the {@link AppSecurityMeasureRepository} and {@link AppSecurityMeasureMapper}
 * collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class AppSecurityMeasureServiceFacadeBeanTest {

    @Mock
    private AppSecurityMeasureMapper appSecurityMeasureMapper;

    @Mock
    private AppSecurityMeasureRepository appSecurityMeasureRepository;

    @InjectMocks
    private AppSecurityMeasureServiceFacadeBean appSecurityMeasureServiceFacadeBean;

    private AppSecurityMeasure activeModel;

    @BeforeEach
    void setUp() {
        activeModel = new AppSecurityMeasure();
        activeModel.setId(1L);
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        AppSecurityMeasureCriteria criteria = new AppSecurityMeasureCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppSecurityMeasure> domainPage = new PageImpl<>(List.of(activeModel));
        AppSecurityMeasureOutputDTO mapped = new AppSecurityMeasureOutputDTO();
        when(appSecurityMeasureRepository.findAll(10L, criteria, pageable)).thenReturn(domainPage);
        when(appSecurityMeasureMapper.toResponse(activeModel)).thenReturn(mapped);

        Page<AppSecurityMeasureOutputDTO> result = appSecurityMeasureServiceFacadeBean.getAll(10L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_valid_persistsAndReturnsResponse() {
        AppSecurityMeasureInputDTO inputDTO = new AppSecurityMeasureInputDTO(5L, 7L, 8L, "desc");
        AppSecurityMeasure model = new AppSecurityMeasure();
        AppSecurityMeasure saved = new AppSecurityMeasure();
        AppSecurityMeasureOutputDTO response = new AppSecurityMeasureOutputDTO();
        when(appSecurityMeasureMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appSecurityMeasureRepository.create(model)).thenReturn(saved);
        when(appSecurityMeasureMapper.toResponse(saved)).thenReturn(response);

        AppSecurityMeasureOutputDTO result = appSecurityMeasureServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        AppSecurityMeasureInputDTO inputDTO = new AppSecurityMeasureInputDTO(5L, 7L, 8L, "desc");
        when(appSecurityMeasureRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appSecurityMeasureServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_APP_SECURITY_MEASURE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        AppSecurityMeasureInputDTO inputDTO = new AppSecurityMeasureInputDTO(5L, 7L, 8L, "desc");
        AppSecurityMeasure updated = new AppSecurityMeasure();
        AppSecurityMeasureOutputDTO response = new AppSecurityMeasureOutputDTO();
        when(appSecurityMeasureRepository.findById(1L)).thenReturn(activeModel);
        when(appSecurityMeasureRepository.update(activeModel, 1L)).thenReturn(updated);
        when(appSecurityMeasureMapper.toResponse(updated)).thenReturn(response);

        AppSecurityMeasureOutputDTO result = appSecurityMeasureServiceFacadeBean.update(1L, inputDTO);

        verify(appSecurityMeasureMapper).updateModelFromInput(inputDTO, activeModel);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(appSecurityMeasureRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appSecurityMeasureServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_APP_SECURITY_MEASURE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeModel.setDeletedAt(LocalDateTime.now());
        when(appSecurityMeasureRepository.findById(1L)).thenReturn(activeModel);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appSecurityMeasureServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_APP_SECURITY_MEASURE_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesModel() {
        when(appSecurityMeasureRepository.findById(1L)).thenReturn(activeModel);

        appSecurityMeasureServiceFacadeBean.delete(1L);

        assertNotNull(activeModel.getDeletedAt());
        verify(appSecurityMeasureRepository).delete(activeModel);
    }
}
