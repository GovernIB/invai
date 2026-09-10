package es.caib.invai.back.ejb.maintenance.security.identityProvider;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.security.identityProvider.DTO.IdentityProviderInputDTO;
import es.caib.invai.back.interna.maintenance.security.identityProvider.DTO.IdentityProviderOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.security.identityProvider.IdentityProviderCriteria;
import es.caib.invai.back.persistence.repository.maintenance.security.identityProvider.IdentityProviderRepository;
import es.caib.invai.back.service.mapper.maintenance.security.identityProvider.IdentityProviderMapper;
import es.caib.invai.back.service.model.maintenance.security.identityProvider.IdentityProvider;
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
 * Unit tests for {@link IdentityProviderServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link IdentityProviderRepository} and {@link IdentityProviderMapper} collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class IdentityProviderServiceFacadeBeanTest {

    @Mock
    private IdentityProviderMapper identityProviderMapper;

    @Mock
    private IdentityProviderRepository identityProviderRepository;

    @InjectMocks
    private IdentityProviderServiceFacadeBean identityProviderServiceFacadeBean;

    private IdentityProvider activeIdentityProvider;

    @BeforeEach
    void setUp() {
        activeIdentityProvider = new IdentityProvider();
        activeIdentityProvider.setId(1L);
        activeIdentityProvider.setName("Cl@ve");
    }

    @Test
    void getById_found_returnsMappedResponse() {
        IdentityProviderOutputDTO expected = new IdentityProviderOutputDTO();
        when(identityProviderRepository.findById(1L)).thenReturn(activeIdentityProvider);
        when(identityProviderMapper.toResponse(activeIdentityProvider)).thenReturn(expected);

        IdentityProviderOutputDTO result = identityProviderServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(identityProviderRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> identityProviderServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_IDENTITYPROVIDER_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        IdentityProviderCriteria criteria = new IdentityProviderCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<IdentityProvider> domainPage = new PageImpl<>(List.of(activeIdentityProvider));
        IdentityProviderOutputDTO mapped = new IdentityProviderOutputDTO();
        when(identityProviderRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(identityProviderMapper.toResponse(activeIdentityProvider)).thenReturn(mapped);

        Page<IdentityProviderOutputDTO> result = identityProviderServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueName_persistsAndReturnsResponse() {
        IdentityProviderInputDTO inputDTO = new IdentityProviderInputDTO("Keycloak CAIB");
        IdentityProvider model = new IdentityProvider();
        IdentityProvider saved = new IdentityProvider();
        IdentityProviderOutputDTO response = new IdentityProviderOutputDTO();
        when(identityProviderRepository.existsByNameAndDeletedAtIsNull("Keycloak CAIB")).thenReturn(false);
        when(identityProviderMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(identityProviderRepository.create(model)).thenReturn(saved);
        when(identityProviderMapper.toResponse(saved)).thenReturn(response);

        IdentityProviderOutputDTO result = identityProviderServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateName_throwsBusinessRuleException() {
        IdentityProviderInputDTO inputDTO = new IdentityProviderInputDTO("Cl@ve");
        when(identityProviderRepository.existsByNameAndDeletedAtIsNull("Cl@ve")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> identityProviderServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_IDENTITYPROVIDER_DUPLICATED, ex.getMessage());
        verify(identityProviderRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        IdentityProviderInputDTO inputDTO = new IdentityProviderInputDTO("X");
        when(identityProviderRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> identityProviderServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_IDENTITYPROVIDER_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_duplicateName_throwsBusinessRuleException() {
        IdentityProviderInputDTO inputDTO = new IdentityProviderInputDTO("Taken");
        when(identityProviderRepository.findById(1L)).thenReturn(activeIdentityProvider);
        when(identityProviderRepository.existsByNameAndIdNotAndDeletedAtIsNull("Taken", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> identityProviderServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_IDENTITYPROVIDER_DUPLICATED, ex.getMessage());
        verify(identityProviderMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        IdentityProviderInputDTO inputDTO = new IdentityProviderInputDTO("Updated");
        IdentityProvider updated = new IdentityProvider();
        IdentityProviderOutputDTO response = new IdentityProviderOutputDTO();
        when(identityProviderRepository.findById(1L)).thenReturn(activeIdentityProvider);
        when(identityProviderRepository.existsByNameAndIdNotAndDeletedAtIsNull("Updated", 1L)).thenReturn(false);
        when(identityProviderRepository.update(activeIdentityProvider, 1L)).thenReturn(updated);
        when(identityProviderMapper.toResponse(updated)).thenReturn(response);

        IdentityProviderOutputDTO result = identityProviderServiceFacadeBean.update(1L, inputDTO);

        verify(identityProviderMapper).updateModelFromInput(inputDTO, activeIdentityProvider);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(identityProviderRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> identityProviderServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_IDENTITYPROVIDER_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeIdentityProvider.setDeletedAt(LocalDateTime.now());
        when(identityProviderRepository.findById(1L)).thenReturn(activeIdentityProvider);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> identityProviderServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_IDENTITYPROVIDER_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesIdentityProvider() {
        when(identityProviderRepository.findById(1L)).thenReturn(activeIdentityProvider);

        identityProviderServiceFacadeBean.delete(1L);

        assertNotNull(activeIdentityProvider.getDeletedAt());
        verify(identityProviderRepository, times(1)).delete(activeIdentityProvider);
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(identityProviderRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> identityProviderServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_IDENTITYPROVIDER_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(identityProviderRepository.findById(1L)).thenReturn(activeIdentityProvider);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> identityProviderServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_IDENTITYPROVIDER_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesIdentityProvider() {
        activeIdentityProvider.setDeletedAt(LocalDateTime.now());
        activeIdentityProvider.setDeletedBy("someone");
        IdentityProvider reactivated = new IdentityProvider();
        IdentityProviderOutputDTO response = new IdentityProviderOutputDTO();
        when(identityProviderRepository.findById(1L)).thenReturn(activeIdentityProvider);
        when(identityProviderRepository.update(eq(activeIdentityProvider), eq(1L))).thenReturn(reactivated);
        when(identityProviderMapper.toResponse(reactivated)).thenReturn(response);

        IdentityProviderOutputDTO result = identityProviderServiceFacadeBean.reactivate(1L);

        assertNull(activeIdentityProvider.getDeletedAt());
        assertNull(activeIdentityProvider.getDeletedBy());
        assertEquals(response, result);
    }
}
