package es.caib.invai.back.ejb.application.development.technology;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.development.technology.DTO.AppTechnologyInputDTO;
import es.caib.invai.back.interna.application.development.technology.DTO.AppTechnologyOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.technology.AppTechnologyCriteria;
import es.caib.invai.back.persistence.repository.application.development.technology.AppTechnologyRepository;
import es.caib.invai.back.service.mapper.application.development.technology.AppTechnologyMapper;
import es.caib.invai.back.service.model.application.development.technology.AppTechnology;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppTechnologyServiceFacadeBean}, exercising every branch of its business
 * rules with the {@link AppTechnologyRepository} and {@link AppTechnologyMapper} collaborators
 * fully mocked. This is a child "link" entity scoped to a parent {@code Development}: it has no
 * {@code getById} or {@code reactivate} operations, only {@code getAll}/{@code create}/{@code
 * update}/{@code delete}.
 */
@ExtendWith(MockitoExtension.class)
class AppTechnologyServiceFacadeBeanTest {

    @Mock
    private AppTechnologyMapper appTechnologyMapper;

    @Mock
    private AppTechnologyRepository appTechnologyRepository;

    @InjectMocks
    private AppTechnologyServiceFacadeBean appTechnologyServiceFacadeBean;

    private AppTechnology activeTechnology;

    @BeforeEach
    void setUp() {
        activeTechnology = new AppTechnology();
        activeTechnology.setId(1L);
        activeTechnology.setVersion("1.0.0");
        activeTechnology.setArchitecture("x86_64");
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        AppTechnologyCriteria criteria = new AppTechnologyCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppTechnology> domainPage = new PageImpl<>(List.of(activeTechnology));
        AppTechnologyOutputDTO mapped = new AppTechnologyOutputDTO();
        when(appTechnologyRepository.findAll(10L, criteria, pageable)).thenReturn(domainPage);
        when(appTechnologyMapper.toResponse(activeTechnology)).thenReturn(mapped);

        Page<AppTechnologyOutputDTO> result = appTechnologyServiceFacadeBean.getAll(10L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_valid_persistsAndReturnsResponse() {
        AppTechnologyInputDTO inputDTO = new AppTechnologyInputDTO(2L, 3L, 4L, "  2.1.0  ", "  Microservices  ");
        AppTechnology model = new AppTechnology();
        AppTechnology saved = new AppTechnology();
        AppTechnologyOutputDTO response = new AppTechnologyOutputDTO();
        when(appTechnologyMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appTechnologyRepository.create(model)).thenReturn(saved);
        when(appTechnologyMapper.toResponse(saved)).thenReturn(response);

        AppTechnologyOutputDTO result = appTechnologyServiceFacadeBean.create(inputDTO);

        assertEquals("2.1.0", inputDTO.getVersion());
        assertEquals("Microservices", inputDTO.getArchitecture());
        assertEquals(response, result);
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        AppTechnologyInputDTO inputDTO = new AppTechnologyInputDTO(2L, 3L, 4L, "1.0", "arch");
        when(appTechnologyRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appTechnologyServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_TECHNOLOGY_NOT_FOUND, ex.getMessage());
        verify(appTechnologyMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        AppTechnologyInputDTO inputDTO = new AppTechnologyInputDTO(2L, 3L, 4L, " 3.0.0 ", " ARM ");
        AppTechnology updated = new AppTechnology();
        AppTechnologyOutputDTO response = new AppTechnologyOutputDTO();
        when(appTechnologyRepository.findById(1L)).thenReturn(activeTechnology);
        when(appTechnologyRepository.update(activeTechnology, 1L)).thenReturn(updated);
        when(appTechnologyMapper.toResponse(updated)).thenReturn(response);

        AppTechnologyOutputDTO result = appTechnologyServiceFacadeBean.update(1L, inputDTO);

        assertEquals("3.0.0", inputDTO.getVersion());
        assertEquals("ARM", inputDTO.getArchitecture());
        verify(appTechnologyMapper).updateModelFromInput(inputDTO, activeTechnology);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(appTechnologyRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appTechnologyServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_TECHNOLOGY_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeTechnology.setDeletedAt(LocalDateTime.now());
        when(appTechnologyRepository.findById(1L)).thenReturn(activeTechnology);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appTechnologyServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_TECHNOLOGY_NOT_ACTIVE, ex.getMessage());
        verify(appTechnologyRepository, never()).delete(any());
    }

    @Test
    void delete_valid_softDeletesTechnology() {
        when(appTechnologyRepository.findById(1L)).thenReturn(activeTechnology);

        appTechnologyServiceFacadeBean.delete(1L);

        assertNotNull(activeTechnology.getDeletedAt());
        verify(appTechnologyRepository, times(1)).delete(activeTechnology);
    }
}
