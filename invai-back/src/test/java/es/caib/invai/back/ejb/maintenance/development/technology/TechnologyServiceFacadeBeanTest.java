package es.caib.invai.back.ejb.maintenance.development.technology;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.development.technology.DTO.TechnologyInputDTO;
import es.caib.invai.back.interna.maintenance.development.technology.DTO.TechnologyOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.technology.AppTechnologyRepository;
import es.caib.invai.back.persistence.repository.maintenance.development.technology.TechnologyCriteria;
import es.caib.invai.back.persistence.repository.maintenance.development.technology.TechnologyRepository;
import es.caib.invai.back.service.mapper.maintenance.development.technology.TechnologyMapper;
import es.caib.invai.back.service.model.maintenance.development.technology.Technology;
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
 * Unit tests for {@link TechnologyServiceFacadeBean}, exercising every branch of its business
 * rules with the {@link TechnologyRepository}, {@link TechnologyMapper}, and
 * {@link AppTechnologyRepository} collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class TechnologyServiceFacadeBeanTest {

    @Mock
    private TechnologyMapper technologyMapper;

    @Mock
    private TechnologyRepository technologyRepository;

    @Mock
    private AppTechnologyRepository appTechnologyRepository;

    @InjectMocks
    private TechnologyServiceFacadeBean technologyServiceFacadeBean;

    private Technology activeTechnology;

    @BeforeEach
    void setUp() {
        activeTechnology = new Technology();
        activeTechnology.setId(1L);
        activeTechnology.setName("Spring Boot");
    }

    @Test
    void getById_found_returnsMappedResponse() {
        TechnologyOutputDTO expected = new TechnologyOutputDTO();
        when(technologyRepository.findById(1L)).thenReturn(activeTechnology);
        when(technologyMapper.toResponse(activeTechnology)).thenReturn(expected);

        TechnologyOutputDTO result = technologyServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(technologyRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> technologyServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_TECHNOLOGYCATALOG_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        TechnologyCriteria criteria = new TechnologyCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<Technology> domainPage = new PageImpl<>(List.of(activeTechnology));
        TechnologyOutputDTO mapped = new TechnologyOutputDTO();
        when(technologyRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(technologyMapper.toResponse(activeTechnology)).thenReturn(mapped);

        Page<TechnologyOutputDTO> result = technologyServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueName_persistsAndReturnsResponse() {
        TechnologyInputDTO input = new TechnologyInputDTO("React", 2L);
        Technology model = new Technology();
        Technology saved = new Technology();
        TechnologyOutputDTO response = new TechnologyOutputDTO();
        when(technologyRepository.existsByNameAndDeletedAtIsNull("React")).thenReturn(false);
        when(technologyMapper.toModelFromInput(input)).thenReturn(model);
        when(technologyRepository.create(model)).thenReturn(saved);
        when(technologyMapper.toResponse(saved)).thenReturn(response);

        TechnologyOutputDTO result = technologyServiceFacadeBean.create(input);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateName_throwsBusinessRuleException() {
        TechnologyInputDTO input = new TechnologyInputDTO("Spring Boot", 2L);
        when(technologyRepository.existsByNameAndDeletedAtIsNull("Spring Boot")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> technologyServiceFacadeBean.create(input));
        assertEquals(Constants.ERR_TECHNOLOGYCATALOG_DUPLICATED, ex.getMessage());
        verify(technologyRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        TechnologyInputDTO input = new TechnologyInputDTO("X", 2L);
        when(technologyRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> technologyServiceFacadeBean.update(99L, input));
        assertEquals(Constants.ERR_TECHNOLOGYCATALOG_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_duplicateName_throwsBusinessRuleException() {
        TechnologyInputDTO input = new TechnologyInputDTO("Taken", 2L);
        when(technologyRepository.findById(1L)).thenReturn(activeTechnology);
        when(technologyRepository.existsByNameAndIdNotAndDeletedAtIsNull("Taken", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> technologyServiceFacadeBean.update(1L, input));
        assertEquals(Constants.ERR_TECHNOLOGYCATALOG_DUPLICATED, ex.getMessage());
        verify(technologyMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        TechnologyInputDTO input = new TechnologyInputDTO("Updated", 3L);
        Technology updated = new Technology();
        TechnologyOutputDTO response = new TechnologyOutputDTO();
        when(technologyRepository.findById(1L)).thenReturn(activeTechnology);
        when(technologyRepository.existsByNameAndIdNotAndDeletedAtIsNull("Updated", 1L)).thenReturn(false);
        when(technologyRepository.update(activeTechnology, 1L)).thenReturn(updated);
        when(technologyMapper.toResponse(updated)).thenReturn(response);

        TechnologyOutputDTO result = technologyServiceFacadeBean.update(1L, input);

        verify(technologyMapper).updateModelFromInput(input, activeTechnology);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(technologyRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> technologyServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_TECHNOLOGYCATALOG_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeTechnology.setDeletedAt(LocalDateTime.now());
        when(technologyRepository.findById(1L)).thenReturn(activeTechnology);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> technologyServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_TECHNOLOGYCATALOG_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_hasDependencies_throwsBusinessRuleException() {
        when(technologyRepository.findById(1L)).thenReturn(activeTechnology);
        when(appTechnologyRepository.existsByTechnologyId(1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> technologyServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_TECHNOLOGYCATALOG_DELETE_HAS_DEPENDENCIES, ex.getMessage());
        verify(technologyRepository, never()).delete(any());
    }

    @Test
    void delete_valid_softDeletesTechnology() {
        when(technologyRepository.findById(1L)).thenReturn(activeTechnology);
        when(appTechnologyRepository.existsByTechnologyId(1L)).thenReturn(false);

        technologyServiceFacadeBean.delete(1L);

        assertNotNull(activeTechnology.getDeletedAt());
        verify(technologyRepository, times(1)).delete(activeTechnology);
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(technologyRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> technologyServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_TECHNOLOGYCATALOG_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(technologyRepository.findById(1L)).thenReturn(activeTechnology);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> technologyServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_TECHNOLOGYCATALOG_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesTechnology() {
        activeTechnology.setDeletedAt(LocalDateTime.now());
        activeTechnology.setDeletedBy("someone");
        Technology reactivated = new Technology();
        TechnologyOutputDTO response = new TechnologyOutputDTO();
        when(technologyRepository.findById(1L)).thenReturn(activeTechnology);
        when(technologyRepository.update(eq(activeTechnology), eq(1L))).thenReturn(reactivated);
        when(technologyMapper.toResponse(reactivated)).thenReturn(response);

        TechnologyOutputDTO result = technologyServiceFacadeBean.reactivate(1L);

        assertNull(activeTechnology.getDeletedAt());
        assertNull(activeTechnology.getDeletedBy());
        assertEquals(response, result);
    }
}
