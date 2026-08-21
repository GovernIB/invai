package es.caib.invai.back.ejb.maintenance.systems.environment;

import es.caib.invai.back.ejb.maintenance.systems.environment.EnvironmentServiceFacadeBean;
import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.systems.environment.DTO.EnvironmentInputDTO;
import es.caib.invai.back.interna.maintenance.systems.environment.DTO.EnvironmentOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.systems.environment.EnvironmentCriteria;
import es.caib.invai.back.persistence.repository.maintenance.systems.environment.EnvironmentRepository;
import es.caib.invai.back.service.mapper.maintenance.systems.environment.EnvironmentMapper;
import es.caib.invai.back.service.model.maintenance.systems.environment.Environment;
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
 * Unit tests for {@link EnvironmentServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link EnvironmentRepository} and {@link EnvironmentMapper} collaborators fully mocked.
 * <p>
 * Note: {@code EnvironmentServiceFacadeBean} has no dependency-check repository, so {@code delete}
 * does not verify downstream references before soft-deleting, and its "already inactive" branch
 * reuses {@code Constants.ERR_ENVIRONMENT_NOT_FOUND} (there is no dedicated "not active" constant
 * for this module) - both behaviors are asserted here exactly as implemented.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class EnvironmentServiceFacadeBeanTest {

    @Mock
    private EnvironmentMapper environmentMapper;

    @Mock
    private EnvironmentRepository environmentRepository;

    @InjectMocks
    private EnvironmentServiceFacadeBean environmentServiceFacadeBean;

    private Environment activeEnvironment;

    @BeforeEach
    void setUp() {
        activeEnvironment = new Environment();
        activeEnvironment.setId(1L);
        activeEnvironment.setCode("PRO");
        activeEnvironment.setName("Produccio");
        activeEnvironment.setNameEs("Produccion");
    }

    @Test
    void getById_found_returnsMappedResponse() {
        EnvironmentOutputDTO expected = new EnvironmentOutputDTO();
        when(environmentRepository.findById(1L)).thenReturn(activeEnvironment);
        when(environmentMapper.toResponse(activeEnvironment)).thenReturn(expected);

        EnvironmentOutputDTO result = environmentServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(environmentRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> environmentServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_ENVIRONMENT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        EnvironmentCriteria criteria = new EnvironmentCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<Environment> domainPage = new PageImpl<>(List.of(activeEnvironment));
        EnvironmentOutputDTO mapped = new EnvironmentOutputDTO();
        when(environmentRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(environmentMapper.toResponse(activeEnvironment)).thenReturn(mapped);

        Page<EnvironmentOutputDTO> result = environmentServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueCode_persistsAndReturnsResponse() {
        EnvironmentInputDTO inputDTO = new EnvironmentInputDTO();
        inputDTO.setCode("DEV");
        inputDTO.setName("Desenvolupament");
        inputDTO.setNameEs("Desarrollo");
        Environment model = new Environment();
        Environment saved = new Environment();
        EnvironmentOutputDTO response = new EnvironmentOutputDTO();
        when(environmentRepository.existsByCode("DEV")).thenReturn(false);
        when(environmentMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(environmentRepository.create(model)).thenReturn(saved);
        when(environmentMapper.toResponse(saved)).thenReturn(response);

        EnvironmentOutputDTO result = environmentServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateCode_throwsBusinessRuleException() {
        EnvironmentInputDTO inputDTO = new EnvironmentInputDTO();
        inputDTO.setCode("PRO");
        inputDTO.setName("Produccio");
        inputDTO.setNameEs("Produccion");
        when(environmentRepository.existsByCode("PRO")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> environmentServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_ENVIRONMENT_DUPLICATED, ex.getMessage());
        verify(environmentRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        EnvironmentInputDTO inputDTO = new EnvironmentInputDTO();
        inputDTO.setCode("X");
        inputDTO.setName("X");
        inputDTO.setNameEs("X");
        when(environmentRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> environmentServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_ENVIRONMENT_NOT_FOUND, ex.getMessage());
    }

    /**
     * Real behavior note: {@code EnvironmentServiceFacadeBean.update} throws
     * {@code Constants.ERR_DATABASE_DUPLICATED} (not {@code ERR_ENVIRONMENT_DUPLICATED}) when the
     * code collides with another active environment - this looks like a copy/paste bug in production
     * code, but the test asserts the actual current behavior.
     */
    @Test
    void update_duplicateCode_throwsBusinessRuleExceptionWithDatabaseDuplicatedConstant() {
        EnvironmentInputDTO inputDTO = new EnvironmentInputDTO();
        inputDTO.setCode("Taken");
        inputDTO.setName("Nom");
        inputDTO.setNameEs("Nombre");
        when(environmentRepository.findById(1L)).thenReturn(activeEnvironment);
        when(environmentRepository.existsByCodeAndIdNot("Taken", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> environmentServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_DATABASE_DUPLICATED, ex.getMessage());
        verify(environmentMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        EnvironmentInputDTO inputDTO = new EnvironmentInputDTO();
        inputDTO.setCode("Updated");
        inputDTO.setName("Actualitzat");
        inputDTO.setNameEs("Actualizado");
        Environment updated = new Environment();
        EnvironmentOutputDTO response = new EnvironmentOutputDTO();
        when(environmentRepository.findById(1L)).thenReturn(activeEnvironment);
        when(environmentRepository.existsByCodeAndIdNot("Updated", 1L)).thenReturn(false);
        when(environmentRepository.update(activeEnvironment, 1L)).thenReturn(updated);
        when(environmentMapper.toResponse(updated)).thenReturn(response);

        EnvironmentOutputDTO result = environmentServiceFacadeBean.update(1L, inputDTO);

        verify(environmentMapper).updateModelFromInput(inputDTO, activeEnvironment);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(environmentRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> environmentServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_ENVIRONMENT_NOT_FOUND, ex.getMessage());
    }

    /**
     * Real behavior note: the "already inactive" branch reuses {@code ERR_ENVIRONMENT_NOT_FOUND}
     * since there is no dedicated "not active" constant defined for Environment in {@code Constants}.
     */
    @Test
    void delete_alreadyInactive_throwsBusinessRuleExceptionWithNotFoundConstant() {
        activeEnvironment.setDeletedAt(LocalDateTime.now());
        when(environmentRepository.findById(1L)).thenReturn(activeEnvironment);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> environmentServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_ENVIRONMENT_NOT_FOUND, ex.getMessage());
        verify(environmentRepository, never()).delete(any());
    }

    @Test
    void delete_valid_softDeletesEnvironment() {
        when(environmentRepository.findById(1L)).thenReturn(activeEnvironment);

        environmentServiceFacadeBean.delete(1L);

        assertEquals(activeEnvironment.getDeletedAt() != null, true);
        verify(environmentRepository, times(1)).delete(activeEnvironment);
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(environmentRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> environmentServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_ENVIRONMENT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(environmentRepository.findById(1L)).thenReturn(activeEnvironment);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> environmentServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_ENVIRONMENT_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesEnvironment() {
        activeEnvironment.setDeletedAt(LocalDateTime.now());
        activeEnvironment.setDeletedBy("someone");
        Environment reactivated = new Environment();
        EnvironmentOutputDTO response = new EnvironmentOutputDTO();
        when(environmentRepository.findById(1L)).thenReturn(activeEnvironment);
        when(environmentRepository.update(eq(activeEnvironment), eq(1L))).thenReturn(reactivated);
        when(environmentMapper.toResponse(reactivated)).thenReturn(response);

        EnvironmentOutputDTO result = environmentServiceFacadeBean.reactivate(1L);

        assertNull(activeEnvironment.getDeletedAt());
        assertNull(activeEnvironment.getDeletedBy());
        assertEquals(response, result);
    }
}
