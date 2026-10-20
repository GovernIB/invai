package es.caib.invai.back.ejb.application.security.webContext;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.development.webContext.DTO.AppWebContextOutputDTO;
import es.caib.invai.back.interna.application.security.webContext.DTO.AppWebContextValidateInputDTO;
import es.caib.invai.back.persistence.repository.application.security.webContext.AppWebContextCriteria;
import es.caib.invai.back.persistence.repository.application.security.webContext.AppWebContextRepository;
import es.caib.invai.back.service.mapper.application.security.webContext.AppWebContextMapper;
import es.caib.invai.back.service.model.application.security.webContext.AppWebContext;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppWebContextServiceFacadeBean} (Security's view): a read-only listing plus
 * the one-way validation action, with the {@link AppWebContextRepository} and
 * {@link AppWebContextMapper} collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class AppWebContextServiceFacadeBeanTest {

    @Mock
    private AppWebContextMapper appWebContextMapper;

    @Mock
    private AppWebContextRepository appWebContextRepository;

    @InjectMocks
    private AppWebContextServiceFacadeBean appWebContextServiceFacadeBean;

    private AppWebContext activeModel;

    @BeforeEach
    void setUp() {
        activeModel = new AppWebContext();
        activeModel.setId(1L);
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        AppWebContextCriteria criteria = new AppWebContextCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppWebContext> domainPage = new PageImpl<>(List.of(activeModel));
        AppWebContextOutputDTO mapped = new AppWebContextOutputDTO();
        when(appWebContextRepository.findAllByAppSecurityId(10L, criteria, pageable)).thenReturn(domainPage);
        when(appWebContextMapper.toResponse(activeModel)).thenReturn(mapped);

        Page<AppWebContextOutputDTO> result = appWebContextServiceFacadeBean.getAll(10L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void validate_notFound_throwsBusinessRuleException() {
        AppWebContextValidateInputDTO inputDTO = new AppWebContextValidateInputDTO("looks fine");
        when(appWebContextRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appWebContextServiceFacadeBean.validate(99L, inputDTO));
        assertEquals(Constants.ERR_APP_WEB_CONTEXT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void validate_alreadyValidated_throwsBusinessRuleException() {
        activeModel.setValidated(true);
        AppWebContextValidateInputDTO inputDTO = new AppWebContextValidateInputDTO("looks fine");
        when(appWebContextRepository.findById(1L)).thenReturn(activeModel);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appWebContextServiceFacadeBean.validate(1L, inputDTO));
        assertEquals(Constants.ERR_APP_WEB_CONTEXT_ALREADY_VALIDATED, ex.getMessage());
    }

    @Test
    void validate_valid_marksModelAsValidatedAndPersists() {
        AppWebContextValidateInputDTO inputDTO = new AppWebContextValidateInputDTO("looks fine");
        when(appWebContextRepository.findById(1L)).thenReturn(activeModel);

        appWebContextServiceFacadeBean.validate(1L, inputDTO);

        assertNotNull(activeModel.getValidatedAt());
        assertEquals("looks fine", activeModel.getValidatedReason());
        verify(appWebContextRepository).update(activeModel, 1L);
        verify(appWebContextMapper, never()).toResponse(any(AppWebContext.class));
    }
}
