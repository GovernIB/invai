package es.caib.invai.back.ejb.application.documentation;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkInputDTO;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkOutputDTO;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkUpdateInputDTO;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.persistence.repository.application.documentation.link.AppDocumentationLinkCriteria;
import es.caib.invai.back.persistence.repository.application.documentation.link.AppLinkRepository;
import es.caib.invai.back.persistence.repository.catalog.appSection.AppSectionRepository;
import es.caib.invai.back.service.mapper.application.documentation.link.AppLinkMapper;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.documentation.link.AppLink;
import es.caib.invai.back.service.model.catalog.appSection.AppSection;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppDocumentationLinkServiceFacadeBean}, with every collaborator mocked.
 */
@ExtendWith(MockitoExtension.class)
class AppDocumentationLinkServiceFacadeBeanTest {

    private static final String URL = "https://intranet.caib.es/manual";

    @Mock
    private AppLinkMapper appLinkMapper;
    @Mock
    private AppLinkRepository appLinkRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private AppSectionRepository appSectionRepository;

    @InjectMocks
    private AppDocumentationLinkServiceFacadeBean facade;

    private AppLink stored;

    @BeforeEach
    void setUp() {
        stored = new AppLink();
        stored.setId(10L);
        stored.setApplicationId(3L);
        stored.setTitle("Manual");
        stored.setSection(section(5L));
        stored.setUrl(URL);
    }

    private static AppSection section(Long id) {
        AppSection section = new AppSection();
        section.setId(id);
        return section;
    }

    private static AppDocumentationLinkInputDTO createInput() {
        return new AppDocumentationLinkInputDTO(3L, "  Manual  ", "desc", 5L, "  " + URL + "  ");
    }

    private static AppDocumentationLinkUpdateInputDTO updateInput() {
        return new AppDocumentationLinkUpdateInputDTO("  Manual v2  ", null, 6L, URL);
    }

    // ------------------------------------------------------------------
    // getAll
    // ------------------------------------------------------------------

    @Test
    void getAll_mapsEveryLinkOfThePage() {
        AppDocumentationLinkCriteria filter = new AppDocumentationLinkCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        AppDocumentationLinkOutputDTO dto = new AppDocumentationLinkOutputDTO();
        when(appLinkRepository.findAll(filter, pageable)).thenReturn(new PageImpl<>(List.of(stored)));
        when(appLinkMapper.toResponse(stored)).thenReturn(dto);

        Page<AppDocumentationLinkOutputDTO> result = facade.getAll(filter, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(dto, result.getContent().get(0));
    }

    // ------------------------------------------------------------------
    // create
    // ------------------------------------------------------------------

    @Test
    void create_storesTheTrimmedLink() {
        AppDocumentationLinkInputDTO inputDTO = createInput();
        AppLink toSave = new AppLink();
        AppLink saved = new AppLink();
        AppDocumentationLinkOutputDTO dto = new AppDocumentationLinkOutputDTO();
        when(applicationRepository.findById(3L)).thenReturn(new Application());
        when(appSectionRepository.findById(5L)).thenReturn(section(5L));
        when(appLinkRepository.existsActiveDuplicate(3L, "Manual", 5L, URL)).thenReturn(false);
        when(appLinkMapper.toModelFromInput(inputDTO)).thenReturn(toSave);
        when(appLinkRepository.create(toSave)).thenReturn(saved);
        when(appLinkMapper.toResponse(saved)).thenReturn(dto);

        AppDocumentationLinkOutputDTO result = facade.create(inputDTO);

        assertSame(dto, result);
        assertEquals("Manual", inputDTO.getTitle());
        assertEquals(URL, inputDTO.getUrl());
    }

    @Test
    void create_applicationNotFound_throwsAndStoresNothing() {
        when(applicationRepository.findById(3L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.create(createInput()));

        assertEquals(Constants.ERR_APP_NOT_FOUND, ex.getMessage());
        verify(appLinkRepository, never()).create(any());
    }

    @Test
    void create_sectionNotFound_throwsAndStoresNothing() {
        when(applicationRepository.findById(3L)).thenReturn(new Application());
        when(appSectionRepository.findById(5L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.create(createInput()));

        assertEquals(Constants.ERR_LINK_SECTION_NOT_FOUND, ex.getMessage());
        verify(appLinkRepository, never()).create(any());
    }

    @Test
    void create_aLinkWithTheSameTitleSectionAndUrl_throwsAndStoresNothing() {
        when(applicationRepository.findById(3L)).thenReturn(new Application());
        when(appSectionRepository.findById(5L)).thenReturn(section(5L));
        when(appLinkRepository.existsActiveDuplicate(3L, "Manual", 5L, URL)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.create(createInput()));

        assertEquals(Constants.ERR_LINK_DUPLICATED, ex.getMessage());
        verify(appLinkRepository, never()).create(any());
    }

    // ------------------------------------------------------------------
    // update
    // ------------------------------------------------------------------

    @Test
    void update_appliesTheTrimmedDataAndSavesIt() {
        AppDocumentationLinkUpdateInputDTO inputDTO = updateInput();
        AppDocumentationLinkOutputDTO dto = new AppDocumentationLinkOutputDTO();
        when(appLinkRepository.findById(10L)).thenReturn(stored);
        when(appSectionRepository.findById(6L)).thenReturn(section(6L));
        when(appLinkRepository.existsActiveDuplicateExcluding(3L, "Manual v2", 6L, URL, 10L)).thenReturn(false);
        when(appLinkRepository.update(stored, 10L)).thenReturn(stored);
        when(appLinkMapper.toResponse(stored)).thenReturn(dto);

        AppDocumentationLinkOutputDTO result = facade.update(10L, inputDTO);

        assertSame(dto, result);
        assertEquals("Manual v2", inputDTO.getTitle());
        verify(appLinkMapper).updateModelFromInput(inputDTO, stored);
    }

    @Test
    void update_notFound_throws() {
        when(appLinkRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.update(99L, updateInput()));

        assertEquals(Constants.ERR_LINK_NOT_FOUND, ex.getMessage());
        verify(appLinkRepository, never()).update(any(), anyLong());
    }

    @Test
    void update_deletedLink_throws() {
        stored.setDeletedAt(LocalDateTime.now());
        when(appLinkRepository.findById(10L)).thenReturn(stored);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.update(10L, updateInput()));

        assertEquals(Constants.ERR_LINK_NOT_ACTIVE, ex.getMessage());
        verify(appLinkRepository, never()).update(any(), anyLong());
    }

    @Test
    void update_sectionNotFound_throws() {
        when(appLinkRepository.findById(10L)).thenReturn(stored);
        when(appSectionRepository.findById(6L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.update(10L, updateInput()));

        assertEquals(Constants.ERR_LINK_SECTION_NOT_FOUND, ex.getMessage());
        verify(appLinkRepository, never()).update(any(), anyLong());
    }

    @Test
    void update_anotherLinkWithTheSameTitleSectionAndUrl_throwsAndChangesNothing() {
        when(appLinkRepository.findById(10L)).thenReturn(stored);
        when(appSectionRepository.findById(6L)).thenReturn(section(6L));
        when(appLinkRepository.existsActiveDuplicateExcluding(3L, "Manual v2", 6L, URL, 10L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.update(10L, updateInput()));

        assertEquals(Constants.ERR_LINK_DUPLICATED, ex.getMessage());
        verify(appLinkRepository, never()).update(any(), anyLong());
    }

    @Test
    void update_neverLooksForDuplicatesAmongTheDeletedOrOfOtherApplications() {
        when(appLinkRepository.findById(10L)).thenReturn(stored);
        when(appSectionRepository.findById(6L)).thenReturn(section(6L));
        when(appLinkRepository.update(any(), anyLong())).thenReturn(stored);

        facade.update(10L, updateInput());

        verify(appLinkRepository).existsActiveDuplicateExcluding(3L, "Manual v2", 6L, URL, 10L);
        verify(appLinkRepository, never()).existsActiveDuplicate(anyLong(), anyString(), anyLong(), anyString());
    }

    // ------------------------------------------------------------------
    // delete
    // ------------------------------------------------------------------

    @Test
    void delete_marksTheLinkAsDeleted() {
        when(appLinkRepository.findById(10L)).thenReturn(stored);

        facade.delete(10L);

        assertNotNull(stored.getDeletedAt());
        assertNotNull(stored.getDeletedBy());
        verify(appLinkRepository).delete(stored);
    }

    @Test
    void delete_notFound_throws() {
        when(appLinkRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.delete(99L));

        assertEquals(Constants.ERR_LINK_NOT_FOUND, ex.getMessage());
        verify(appLinkRepository, never()).delete(any());
    }

    @Test
    void delete_alreadyDeleted_throws() {
        stored.setDeletedAt(LocalDateTime.now());
        when(appLinkRepository.findById(10L)).thenReturn(stored);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.delete(10L));

        assertEquals(Constants.ERR_LINK_NOT_ACTIVE, ex.getMessage());
        verify(appLinkRepository, never()).delete(any());
    }
}
