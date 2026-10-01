package es.caib.invai.back.ejb.application.responsibleAuthorized.dir3;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.responsibleAuthorized.dir3.DTO.Dir3ManualValidateInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.dir3.DTO.Dir3ValidationOutputDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.dir3.Dir3ValidationRepository;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.dir3.Dir3ValidationMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.dir3.Dir3Validation;
import es.caib.invai.back.service.model.catalog.dir3Status.Dir3ValidationStatus;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link Dir3ValidationServiceFacadeBean}, covering the guard against validating
 * anything other than a {@code NOT_VALIDATED} record and the resulting state after a successful
 * manual validation.
 */
@ExtendWith(MockitoExtension.class)
class Dir3ValidationServiceFacadeBeanTest {

    @Mock
    private Dir3ValidationRepository dir3ValidationRepository;

    @Mock
    private Dir3ValidationMapper dir3ValidationMapper;

    @InjectMocks
    private Dir3ValidationServiceFacadeBean dir3ValidationServiceFacadeBean;

    private Dir3Validation noValidado;

    @BeforeEach
    void setUp() {
        noValidado = Dir3Validation.builder().id(500L).dir3Status(Dir3ValidationStatus.NOT_VALIDATED).build();
    }

    @Test
    void validateManually_notFound_throwsBusinessRuleException() {
        when(dir3ValidationRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> dir3ValidationServiceFacadeBean.validateManually(99L, new Dir3ManualValidateInputDTO("Motiu")));
        assertEquals(Constants.ERR_DIR3VALIDATION_NOT_FOUND, ex.getMessage());
    }

    @Test
    void validateManually_alreadyValidado_throwsBusinessRuleException() {
        noValidado.setDir3Status(Dir3ValidationStatus.VALIDATED);
        when(dir3ValidationRepository.findById(500L)).thenReturn(noValidado);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> dir3ValidationServiceFacadeBean.validateManually(500L, new Dir3ManualValidateInputDTO("Motiu")));
        assertEquals(Constants.ERR_DIR3VALIDATION_MANUAL_VALIDATION_NOT_ALLOWED, ex.getMessage());
        verify(dir3ValidationRepository, never()).update(any(), any());
    }

    @Test
    void validateManually_alreadyManual_throwsBusinessRuleException() {
        noValidado.setDir3Status(Dir3ValidationStatus.MANUAL);
        when(dir3ValidationRepository.findById(500L)).thenReturn(noValidado);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> dir3ValidationServiceFacadeBean.validateManually(500L, new Dir3ManualValidateInputDTO("Motiu")));
        assertEquals(Constants.ERR_DIR3VALIDATION_MANUAL_VALIDATION_NOT_ALLOWED, ex.getMessage());
        verify(dir3ValidationRepository, never()).update(any(), any());
    }

    @Test
    void validateManually_alreadyNoAplica_throwsBusinessRuleException() {
        noValidado.setDir3Status(Dir3ValidationStatus.NOT_APPLY);
        when(dir3ValidationRepository.findById(500L)).thenReturn(noValidado);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> dir3ValidationServiceFacadeBean.validateManually(500L, new Dir3ManualValidateInputDTO("Motiu")));
        assertEquals(Constants.ERR_DIR3VALIDATION_MANUAL_VALIDATION_NOT_ALLOWED, ex.getMessage());
    }

    @Test
    void validateManually_noValidado_promotesToManualAndReturnsMappedResponse() {
        Dir3ValidationOutputDTO response = new Dir3ValidationOutputDTO();
        when(dir3ValidationRepository.findById(500L)).thenReturn(noValidado);
        when(dir3ValidationRepository.update(noValidado, 500L)).thenReturn(noValidado);
        when(dir3ValidationMapper.toResponse(noValidado)).thenReturn(response);

        Dir3ValidationOutputDTO result = dir3ValidationServiceFacadeBean.validateManually(500L,
                new Dir3ManualValidateInputDTO("El DIR3 de Soffid esta desactualitzat, s'ha comprovat manualment"));

        ArgumentCaptor<Dir3Validation> captor = ArgumentCaptor.forClass(Dir3Validation.class);
        verify(dir3ValidationRepository).update(captor.capture(), eq(500L));
        assertEquals(Dir3ValidationStatus.MANUAL, captor.getValue().getDir3Status());
        assertNotNull(captor.getValue().getManualValidatedAt());
        assertEquals("El DIR3 de Soffid esta desactualitzat, s'ha comprovat manualment", captor.getValue().getReason());
        assertSame(response, result);
    }
}
