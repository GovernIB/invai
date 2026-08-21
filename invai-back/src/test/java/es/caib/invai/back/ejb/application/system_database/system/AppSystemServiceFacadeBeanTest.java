package es.caib.invai.back.ejb.application.system_database.system;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.system_database.system.DTO.AppSystemInputDTO;
import es.caib.invai.back.interna.application.system_database.system.DTO.AppSystemOutputDTO;
import es.caib.invai.back.persistence.repository.application.system_database.system.AppSystemCriteria;
import es.caib.invai.back.persistence.repository.application.system_database.system.AppSystemRepository;
import es.caib.invai.back.service.mapper.application.system_database.system.AppSystemMapper;
import es.caib.invai.back.service.model.application.system_database.system.AppSystem;
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
import es.caib.invai.back.ejb.application.system_database.system.AppSystemServiceFacadeBean;

/**
 * Unit tests for {@link AppSystemServiceFacadeBean}, exercising every branch of its business
 * rules with the {@link AppSystemRepository} and {@link AppSystemMapper} collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class AppSystemServiceFacadeBeanTest {

    @Mock
    private AppSystemMapper appSystemMapper;

    @Mock
    private AppSystemRepository appSystemRepository;

    @InjectMocks
    private AppSystemServiceFacadeBean appSystemServiceFacadeBean;

    private AppSystem activeModel;

    @BeforeEach
    void setUp() {
        activeModel = new AppSystem();
        activeModel.setId(1L);
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        AppSystemCriteria criteria = new AppSystemCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppSystem> domainPage = new PageImpl<>(List.of(activeModel));
        AppSystemOutputDTO mapped = new AppSystemOutputDTO();
        when(appSystemRepository.findAll(10L, criteria, pageable)).thenReturn(domainPage);
        when(appSystemMapper.toResponse(activeModel)).thenReturn(mapped);

        Page<AppSystemOutputDTO> result = appSystemServiceFacadeBean.getAll(10L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueLink_persistsAndReturnsResponse() {
        AppSystemInputDTO inputDTO = new AppSystemInputDTO(5L, 7L);
        AppSystem model = new AppSystem();
        AppSystem saved = new AppSystem();
        AppSystemOutputDTO response = new AppSystemOutputDTO();
        when(appSystemRepository.existsByInformationSystemDbAndSystem(5L, 7L)).thenReturn(false);
        when(appSystemMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appSystemRepository.create(model)).thenReturn(saved);
        when(appSystemMapper.toResponse(saved)).thenReturn(response);

        AppSystemOutputDTO result = appSystemServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateLink_throwsBusinessRuleException() {
        AppSystemInputDTO inputDTO = new AppSystemInputDTO(5L, 7L);
        when(appSystemRepository.existsByInformationSystemDbAndSystem(5L, 7L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appSystemServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_APPLICATIONSYSTEM_LINK_DUPLICATED, ex.getMessage());
        verify(appSystemRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        AppSystemInputDTO inputDTO = new AppSystemInputDTO(5L, 7L);
        when(appSystemRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appSystemServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_APPLICATIONSYSTEM_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_duplicateLink_throwsBusinessRuleException() {
        AppSystemInputDTO inputDTO = new AppSystemInputDTO(5L, 7L);
        when(appSystemRepository.findById(1L)).thenReturn(activeModel);
        when(appSystemRepository.existsByInformationSystemDbAndSystemAndIdNot(5L, 7L, 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appSystemServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_APPLICATIONSYSTEM_LINK_OWNED_BY_OTHER, ex.getMessage());
        verify(appSystemMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        AppSystemInputDTO inputDTO = new AppSystemInputDTO(5L, 7L);
        AppSystem updated = new AppSystem();
        AppSystemOutputDTO response = new AppSystemOutputDTO();
        when(appSystemRepository.findById(1L)).thenReturn(activeModel);
        when(appSystemRepository.existsByInformationSystemDbAndSystemAndIdNot(5L, 7L, 1L)).thenReturn(false);
        when(appSystemRepository.update(activeModel, 1L)).thenReturn(updated);
        when(appSystemMapper.toResponse(updated)).thenReturn(response);

        AppSystemOutputDTO result = appSystemServiceFacadeBean.update(1L, inputDTO);

        verify(appSystemMapper).updateModelFromInput(inputDTO, activeModel);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(appSystemRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appSystemServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_APPLICATIONSYSTEM_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeModel.setDeletedAt(LocalDateTime.now());
        when(appSystemRepository.findById(1L)).thenReturn(activeModel);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appSystemServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_APPLICATIONSYSTEM_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesModel() {
        when(appSystemRepository.findById(1L)).thenReturn(activeModel);

        appSystemServiceFacadeBean.delete(1L);

        assertNotNull(activeModel.getDeletedAt());
        verify(appSystemRepository).delete(activeModel);
    }
}
