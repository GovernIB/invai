package es.caib.invai.back.ejb.application.development.provider;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.development.provider.DTO.AppProviderInputDTO;
import es.caib.invai.back.interna.application.development.provider.DTO.AppProviderOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.provider.AppProviderCriteria;
import es.caib.invai.back.persistence.repository.application.development.provider.AppProviderRepository;
import es.caib.invai.back.service.mapper.application.development.provider.AppProviderMapper;
import es.caib.invai.back.service.model.application.development.provider.AppProvider;
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
import es.caib.invai.back.ejb.application.development.provider.AppProviderServiceFacadeBean;

/**
 * Unit tests for {@link AppProviderServiceFacadeBean}, exercising every branch of its business
 * rules with the {@link AppProviderRepository} and {@link AppProviderMapper} collaborators fully
 * mocked. This is a child "link" entity scoped to a parent {@code Development}: it has no
 * {@code getById} or {@code reactivate} operations, only {@code getAll}/{@code create}/{@code
 * update}/{@code delete}.
 */
@ExtendWith(MockitoExtension.class)
class AppProviderServiceFacadeBeanTest {

    @Mock
    private AppProviderMapper appProviderMapper;

    @Mock
    private AppProviderRepository appProviderRepository;

    @InjectMocks
    private AppProviderServiceFacadeBean appProviderServiceFacadeBean;

    private AppProvider activeProvider;

    @BeforeEach
    void setUp() {
        activeProvider = new AppProvider();
        activeProvider.setId(1L);
        activeProvider.setCompanyName("Acme Corp");
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        AppProviderCriteria criteria = new AppProviderCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppProvider> domainPage = new PageImpl<>(List.of(activeProvider));
        AppProviderOutputDTO mapped = new AppProviderOutputDTO();
        when(appProviderRepository.findAll(10L, criteria, pageable)).thenReturn(domainPage);
        when(appProviderMapper.toResponse(activeProvider)).thenReturn(mapped);

        Page<AppProviderOutputDTO> result = appProviderServiceFacadeBean.getAll(10L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_valid_persistsAndReturnsResponse() {
        AppProviderInputDTO inputDTO = new AppProviderInputDTO(2L, "  New Provider  ", 5L, null, null);
        AppProvider model = new AppProvider();
        AppProvider saved = new AppProvider();
        AppProviderOutputDTO response = new AppProviderOutputDTO();
        when(appProviderMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appProviderRepository.create(model)).thenReturn(saved);
        when(appProviderMapper.toResponse(saved)).thenReturn(response);

        AppProviderOutputDTO result = appProviderServiceFacadeBean.create(inputDTO);

        assertEquals("New Provider", inputDTO.getCompanyName());
        assertEquals(response, result);
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        AppProviderInputDTO inputDTO = new AppProviderInputDTO(2L, "X", null, null, null);
        when(appProviderRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appProviderServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_PROVIDER_NOT_FOUND, ex.getMessage());
        verify(appProviderMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        AppProviderInputDTO inputDTO = new AppProviderInputDTO(2L, " Updated Provider ", 5L, null, null);
        AppProvider updated = new AppProvider();
        AppProviderOutputDTO response = new AppProviderOutputDTO();
        when(appProviderRepository.findById(1L)).thenReturn(activeProvider);
        when(appProviderRepository.update(activeProvider, 1L)).thenReturn(updated);
        when(appProviderMapper.toResponse(updated)).thenReturn(response);

        AppProviderOutputDTO result = appProviderServiceFacadeBean.update(1L, inputDTO);

        assertEquals("Updated Provider", inputDTO.getCompanyName());
        verify(appProviderMapper).updateModelFromInput(inputDTO, activeProvider);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(appProviderRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appProviderServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_PROVIDER_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeProvider.setDeletedAt(LocalDateTime.now());
        when(appProviderRepository.findById(1L)).thenReturn(activeProvider);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appProviderServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_PROVIDER_NOT_ACTIVE, ex.getMessage());
        verify(appProviderRepository, never()).delete(any());
    }

    @Test
    void delete_valid_softDeletesProvider() {
        when(appProviderRepository.findById(1L)).thenReturn(activeProvider);

        appProviderServiceFacadeBean.delete(1L);

        assertNotNull(activeProvider.getDeletedAt());
        verify(appProviderRepository, times(1)).delete(activeProvider);
    }
}
