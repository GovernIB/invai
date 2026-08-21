package es.caib.invai.back.ejb.maintenance.responsible.authorizationType;

import es.caib.invai.back.ejb.maintenance.responsible.authorizationType.AuthorizationTypeServiceFacadeBean;
import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO.AuthorizationTypeInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO.AuthorizationTypeOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.responsible.authorizationType.AuthorizationTypeCriteria;
import es.caib.invai.back.persistence.repository.maintenance.responsible.authorizationType.AuthorizationTypeRepository;
import es.caib.invai.back.service.mapper.maintenance.responsible.authorizationType.AuthorizationTypeMapper;
import es.caib.invai.back.service.model.maintenance.responsible.authorizationType.AuthorizationType;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthorizationTypeServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link AuthorizationTypeRepository} and {@link AuthorizationTypeMapper} collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class AuthorizationTypeServiceFacadeBeanTest {

    @Mock
    private AuthorizationTypeMapper authorizationTypeMapper;

    @Mock
    private AuthorizationTypeRepository authorizationTypeRepository;

    @InjectMocks
    private AuthorizationTypeServiceFacadeBean authorizationTypeServiceFacadeBean;

    private AuthorizationType activeAuthorizationType;

    @BeforeEach
    void setUp() {
        activeAuthorizationType = new AuthorizationType();
        activeAuthorizationType.setId(1L);
        activeAuthorizationType.setName("Firmar peticiones");
    }

    @Test
    void getById_found_returnsMappedResponse() {
        AuthorizationTypeOutputDTO expected = new AuthorizationTypeOutputDTO();
        when(authorizationTypeRepository.findById(1L)).thenReturn(activeAuthorizationType);
        when(authorizationTypeMapper.toResponse(activeAuthorizationType)).thenReturn(expected);

        AuthorizationTypeOutputDTO result = authorizationTypeServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(authorizationTypeRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> authorizationTypeServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_AUTHORIZATIONTYPE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        AuthorizationTypeCriteria criteria = new AuthorizationTypeCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AuthorizationType> domainPage = new PageImpl<>(List.of(activeAuthorizationType));
        AuthorizationTypeOutputDTO mapped = new AuthorizationTypeOutputDTO();
        when(authorizationTypeRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(authorizationTypeMapper.toResponse(activeAuthorizationType)).thenReturn(mapped);

        Page<AuthorizationTypeOutputDTO> result = authorizationTypeServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueName_persistsAndReturnsResponse() {
        AuthorizationTypeInputDTO inputDTO = new AuthorizationTypeInputDTO("Acceso a los logs", "Acceso a los logs");
        AuthorizationType model = new AuthorizationType();
        AuthorizationType saved = new AuthorizationType();
        AuthorizationTypeOutputDTO response = new AuthorizationTypeOutputDTO();
        when(authorizationTypeRepository.existsByNameAndDeletedAtIsNull("Acceso a los logs")).thenReturn(false);
        when(authorizationTypeMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(authorizationTypeRepository.create(model)).thenReturn(saved);
        when(authorizationTypeMapper.toResponse(saved)).thenReturn(response);

        AuthorizationTypeOutputDTO result = authorizationTypeServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateName_throwsBusinessRuleException() {
        AuthorizationTypeInputDTO inputDTO = new AuthorizationTypeInputDTO("Firmar peticiones", "Firmar peticiones");
        when(authorizationTypeRepository.existsByNameAndDeletedAtIsNull("Firmar peticiones")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> authorizationTypeServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_AUTHORIZATIONTYPE_DUPLICATED, ex.getMessage());
        verify(authorizationTypeRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        AuthorizationTypeInputDTO inputDTO = new AuthorizationTypeInputDTO("X", "X ES");
        when(authorizationTypeRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> authorizationTypeServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_AUTHORIZATIONTYPE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_duplicateName_throwsBusinessRuleException() {
        AuthorizationTypeInputDTO inputDTO = new AuthorizationTypeInputDTO("Taken", "Taken ES");
        when(authorizationTypeRepository.findById(1L)).thenReturn(activeAuthorizationType);
        when(authorizationTypeRepository.existsByNameAndIdNotAndDeletedAtIsNull("Taken", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> authorizationTypeServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_AUTHORIZATIONTYPE_DUPLICATED, ex.getMessage());
        verify(authorizationTypeMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        AuthorizationTypeInputDTO inputDTO = new AuthorizationTypeInputDTO("Updated", "Updated ES");
        AuthorizationType updated = new AuthorizationType();
        AuthorizationTypeOutputDTO response = new AuthorizationTypeOutputDTO();
        when(authorizationTypeRepository.findById(1L)).thenReturn(activeAuthorizationType);
        when(authorizationTypeRepository.existsByNameAndIdNotAndDeletedAtIsNull("Updated", 1L)).thenReturn(false);
        when(authorizationTypeRepository.update(activeAuthorizationType, 1L)).thenReturn(updated);
        when(authorizationTypeMapper.toResponse(updated)).thenReturn(response);

        AuthorizationTypeOutputDTO result = authorizationTypeServiceFacadeBean.update(1L, inputDTO);

        verify(authorizationTypeMapper).updateModelFromInput(inputDTO, activeAuthorizationType);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(authorizationTypeRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> authorizationTypeServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_AUTHORIZATIONTYPE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeAuthorizationType.setDeletedAt(LocalDateTime.now());
        when(authorizationTypeRepository.findById(1L)).thenReturn(activeAuthorizationType);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> authorizationTypeServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_AUTHORIZATIONTYPE_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesAuthorizationType() {
        when(authorizationTypeRepository.findById(1L)).thenReturn(activeAuthorizationType);

        authorizationTypeServiceFacadeBean.delete(1L);

        assertEquals(true, activeAuthorizationType.getDeletedAt() != null);
        verify(authorizationTypeRepository, times(1)).delete(activeAuthorizationType);
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(authorizationTypeRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> authorizationTypeServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_AUTHORIZATIONTYPE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(authorizationTypeRepository.findById(1L)).thenReturn(activeAuthorizationType);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> authorizationTypeServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_AUTHORIZATIONTYPE_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesAuthorizationType() {
        activeAuthorizationType.setDeletedAt(LocalDateTime.now());
        activeAuthorizationType.setDeletedBy("someone");
        AuthorizationType reactivated = new AuthorizationType();
        AuthorizationTypeOutputDTO response = new AuthorizationTypeOutputDTO();
        when(authorizationTypeRepository.findById(1L)).thenReturn(activeAuthorizationType);
        when(authorizationTypeRepository.update(eq(activeAuthorizationType), eq(1L))).thenReturn(reactivated);
        when(authorizationTypeMapper.toResponse(reactivated)).thenReturn(response);

        AuthorizationTypeOutputDTO result = authorizationTypeServiceFacadeBean.reactivate(1L);

        assertNull(activeAuthorizationType.getDeletedAt());
        assertNull(activeAuthorizationType.getDeletedBy());
        assertEquals(response, result);
    }
}
