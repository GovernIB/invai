package es.caib.invai.back.ejb.maintenance.security.webContext;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.security.webContext.DTO.WebContextInputDTO;
import es.caib.invai.back.interna.maintenance.security.webContext.DTO.WebContextOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.security.webContext.WebContextCriteria;
import es.caib.invai.back.persistence.repository.maintenance.security.webContext.WebContextRepository;
import es.caib.invai.back.service.mapper.maintenance.security.webContext.WebContextMapper;
import es.caib.invai.back.service.model.maintenance.security.webContext.WebContext;
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
 * Unit tests for {@link WebContextServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link WebContextRepository} and {@link WebContextMapper} collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class WebContextServiceFacadeBeanTest {

    @Mock
    private WebContextMapper webContextMapper;

    @Mock
    private WebContextRepository webContextRepository;

    @InjectMocks
    private WebContextServiceFacadeBean webContextServiceFacadeBean;

    private WebContext activeWebContext;

    @BeforeEach
    void setUp() {
        activeWebContext = new WebContext();
        activeWebContext.setId(1L);
        activeWebContext.setName("Intranet corporativa");
    }

    @Test
    void getById_found_returnsMappedResponse() {
        WebContextOutputDTO expected = new WebContextOutputDTO();
        when(webContextRepository.findById(1L)).thenReturn(activeWebContext);
        when(webContextMapper.toResponse(activeWebContext)).thenReturn(expected);

        WebContextOutputDTO result = webContextServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(webContextRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> webContextServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_WEBCONTEXT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        WebContextCriteria criteria = new WebContextCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<WebContext> domainPage = new PageImpl<>(List.of(activeWebContext));
        WebContextOutputDTO mapped = new WebContextOutputDTO();
        when(webContextRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(webContextMapper.toResponse(activeWebContext)).thenReturn(mapped);

        Page<WebContextOutputDTO> result = webContextServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueName_persistsAndReturnsResponse() {
        WebContextInputDTO inputDTO = new WebContextInputDTO("Portal ciutada", "Portal ciutada");
        WebContext model = new WebContext();
        WebContext saved = new WebContext();
        WebContextOutputDTO response = new WebContextOutputDTO();
        when(webContextRepository.existsByNameAndDeletedAtIsNull("Portal ciutada")).thenReturn(false);
        when(webContextMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(webContextRepository.create(model)).thenReturn(saved);
        when(webContextMapper.toResponse(saved)).thenReturn(response);

        WebContextOutputDTO result = webContextServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateName_throwsBusinessRuleException() {
        WebContextInputDTO inputDTO = new WebContextInputDTO("Intranet corporativa", "Intranet corporativa");
        when(webContextRepository.existsByNameAndDeletedAtIsNull("Intranet corporativa")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> webContextServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_WEBCONTEXT_DUPLICATED, ex.getMessage());
        verify(webContextRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        WebContextInputDTO inputDTO = new WebContextInputDTO("X", "X ES");
        when(webContextRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> webContextServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_WEBCONTEXT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_duplicateName_throwsBusinessRuleException() {
        WebContextInputDTO inputDTO = new WebContextInputDTO("Taken", "Taken ES");
        when(webContextRepository.findById(1L)).thenReturn(activeWebContext);
        when(webContextRepository.existsByNameAndIdNotAndDeletedAtIsNull("Taken", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> webContextServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_WEBCONTEXT_DUPLICATED, ex.getMessage());
        verify(webContextMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        WebContextInputDTO inputDTO = new WebContextInputDTO("Updated", "Updated ES");
        WebContext updated = new WebContext();
        WebContextOutputDTO response = new WebContextOutputDTO();
        when(webContextRepository.findById(1L)).thenReturn(activeWebContext);
        when(webContextRepository.existsByNameAndIdNotAndDeletedAtIsNull("Updated", 1L)).thenReturn(false);
        when(webContextRepository.update(activeWebContext, 1L)).thenReturn(updated);
        when(webContextMapper.toResponse(updated)).thenReturn(response);

        WebContextOutputDTO result = webContextServiceFacadeBean.update(1L, inputDTO);

        verify(webContextMapper).updateModelFromInput(inputDTO, activeWebContext);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(webContextRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> webContextServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_WEBCONTEXT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeWebContext.setDeletedAt(LocalDateTime.now());
        when(webContextRepository.findById(1L)).thenReturn(activeWebContext);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> webContextServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_WEBCONTEXT_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesWebContext() {
        when(webContextRepository.findById(1L)).thenReturn(activeWebContext);

        webContextServiceFacadeBean.delete(1L);

        assertNotNull(activeWebContext.getDeletedAt());
        verify(webContextRepository, times(1)).delete(activeWebContext);
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(webContextRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> webContextServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_WEBCONTEXT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(webContextRepository.findById(1L)).thenReturn(activeWebContext);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> webContextServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_WEBCONTEXT_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesWebContext() {
        activeWebContext.setDeletedAt(LocalDateTime.now());
        activeWebContext.setDeletedBy("someone");
        WebContext reactivated = new WebContext();
        WebContextOutputDTO response = new WebContextOutputDTO();
        when(webContextRepository.findById(1L)).thenReturn(activeWebContext);
        when(webContextRepository.update(eq(activeWebContext), eq(1L))).thenReturn(reactivated);
        when(webContextMapper.toResponse(reactivated)).thenReturn(response);

        WebContextOutputDTO result = webContextServiceFacadeBean.reactivate(1L);

        assertNull(activeWebContext.getDeletedAt());
        assertNull(activeWebContext.getDeletedBy());
        assertEquals(response, result);
    }
}
