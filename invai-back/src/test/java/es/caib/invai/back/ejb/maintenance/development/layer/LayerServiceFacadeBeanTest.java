package es.caib.invai.back.ejb.maintenance.development.layer;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.development.layer.DTO.LayerInputDTO;
import es.caib.invai.back.interna.maintenance.development.layer.DTO.LayerOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.technology.AppTechnologyRepository;
import es.caib.invai.back.persistence.repository.maintenance.development.layer.LayerCriteria;
import es.caib.invai.back.persistence.repository.maintenance.development.layer.LayerRepository;
import es.caib.invai.back.persistence.repository.maintenance.development.technology.TechnologyRepository;
import es.caib.invai.back.service.mapper.maintenance.development.layer.LayerMapper;
import es.caib.invai.back.service.model.maintenance.development.layer.Layer;
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
 * Unit tests for {@link LayerServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link LayerRepository}, {@link LayerMapper}, {@link TechnologyRepository}, and
 * {@link AppTechnologyRepository} collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class LayerServiceFacadeBeanTest {

    @Mock
    private LayerMapper layerMapper;

    @Mock
    private LayerRepository layerRepository;

    @Mock
    private TechnologyRepository technologyRepository;

    @Mock
    private AppTechnologyRepository appTechnologyRepository;

    @InjectMocks
    private LayerServiceFacadeBean layerServiceFacadeBean;

    private Layer activeLayer;

    @BeforeEach
    void setUp() {
        activeLayer = new Layer();
        activeLayer.setId(1L);
        activeLayer.setName("Presentation");
    }

    @Test
    void getById_found_returnsMappedResponse() {
        LayerOutputDTO expected = new LayerOutputDTO();
        when(layerRepository.findById(1L)).thenReturn(activeLayer);
        when(layerMapper.toResponse(activeLayer)).thenReturn(expected);

        LayerOutputDTO result = layerServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(layerRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> layerServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_LAYER_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        LayerCriteria criteria = new LayerCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<Layer> domainPage = new PageImpl<>(List.of(activeLayer));
        LayerOutputDTO mapped = new LayerOutputDTO();
        when(layerRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(layerMapper.toResponse(activeLayer)).thenReturn(mapped);

        Page<LayerOutputDTO> result = layerServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueName_persistsAndReturnsResponse() {
        LayerInputDTO input = new LayerInputDTO("New Layer");
        Layer model = new Layer();
        Layer saved = new Layer();
        LayerOutputDTO response = new LayerOutputDTO();
        when(layerRepository.existsByNameAndDeletedAtIsNull("New Layer")).thenReturn(false);
        when(layerMapper.toModelFromInput(input)).thenReturn(model);
        when(layerRepository.create(model)).thenReturn(saved);
        when(layerMapper.toResponse(saved)).thenReturn(response);

        LayerOutputDTO result = layerServiceFacadeBean.create(input);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateName_throwsBusinessRuleException() {
        LayerInputDTO input = new LayerInputDTO("Presentation");
        when(layerRepository.existsByNameAndDeletedAtIsNull("Presentation")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> layerServiceFacadeBean.create(input));
        assertEquals(Constants.ERR_LAYER_DUPLICATED, ex.getMessage());
        verify(layerRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        LayerInputDTO input = new LayerInputDTO("X");
        when(layerRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> layerServiceFacadeBean.update(99L, input));
        assertEquals(Constants.ERR_LAYER_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_duplicateName_throwsBusinessRuleException() {
        LayerInputDTO input = new LayerInputDTO("Taken");
        when(layerRepository.findById(1L)).thenReturn(activeLayer);
        when(layerRepository.existsByNameAndIdNotAndDeletedAtIsNull("Taken", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> layerServiceFacadeBean.update(1L, input));
        assertEquals(Constants.ERR_LAYER_DUPLICATED, ex.getMessage());
        verify(layerMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        LayerInputDTO input = new LayerInputDTO("Updated");
        Layer updated = new Layer();
        LayerOutputDTO response = new LayerOutputDTO();
        when(layerRepository.findById(1L)).thenReturn(activeLayer);
        when(layerRepository.existsByNameAndIdNotAndDeletedAtIsNull("Updated", 1L)).thenReturn(false);
        when(layerRepository.update(activeLayer, 1L)).thenReturn(updated);
        when(layerMapper.toResponse(updated)).thenReturn(response);

        LayerOutputDTO result = layerServiceFacadeBean.update(1L, input);

        verify(layerMapper).updateModelFromInput(input, activeLayer);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(layerRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> layerServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_LAYER_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeLayer.setDeletedAt(LocalDateTime.now());
        when(layerRepository.findById(1L)).thenReturn(activeLayer);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> layerServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_LAYER_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_hasTechnologyDependencies_throwsBusinessRuleException() {
        when(layerRepository.findById(1L)).thenReturn(activeLayer);
        when(technologyRepository.existsByLayerId(1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> layerServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_LAYER_DELETE_HAS_DEPENDENCIES, ex.getMessage());
        verify(layerRepository, never()).delete(any());
    }

    @Test
    void delete_hasAppTechnologyDependencies_throwsBusinessRuleException() {
        when(layerRepository.findById(1L)).thenReturn(activeLayer);
        when(technologyRepository.existsByLayerId(1L)).thenReturn(false);
        when(appTechnologyRepository.existsByLayerId(1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> layerServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_LAYER_DELETE_HAS_DEPENDENCIES, ex.getMessage());
        verify(layerRepository, never()).delete(any());
    }

    @Test
    void delete_valid_softDeletesLayer() {
        when(layerRepository.findById(1L)).thenReturn(activeLayer);
        when(technologyRepository.existsByLayerId(1L)).thenReturn(false);
        when(appTechnologyRepository.existsByLayerId(1L)).thenReturn(false);

        layerServiceFacadeBean.delete(1L);

        assertNotNull(activeLayer.getDeletedAt());
        verify(layerRepository, times(1)).delete(activeLayer);
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(layerRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> layerServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_LAYER_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(layerRepository.findById(1L)).thenReturn(activeLayer);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> layerServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_LAYER_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesLayer() {
        activeLayer.setDeletedAt(LocalDateTime.now());
        activeLayer.setDeletedBy("someone");
        Layer reactivated = new Layer();
        LayerOutputDTO response = new LayerOutputDTO();
        when(layerRepository.findById(1L)).thenReturn(activeLayer);
        when(layerRepository.update(eq(activeLayer), eq(1L))).thenReturn(reactivated);
        when(layerMapper.toResponse(reactivated)).thenReturn(response);

        LayerOutputDTO result = layerServiceFacadeBean.reactivate(1L);

        assertNull(activeLayer.getDeletedAt());
        assertNull(activeLayer.getDeletedBy());
        assertEquals(response, result);
    }
}
