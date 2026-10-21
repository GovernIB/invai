package es.caib.invai.back.persistence.repository.application.documentation.link;

import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.application.documentation.link.AppLinkAudEntity;
import es.caib.invai.back.persistence.model.application.documentation.link.AppLinkEntity;
import es.caib.invai.back.persistence.model.catalog.appSection.LkupAppSectionEntity;
import es.caib.invai.back.service.mapper.application.documentation.link.AppLinkMapper;
import es.caib.invai.back.service.model.application.documentation.link.AppLink;
import es.caib.invai.back.service.model.catalog.appSection.AppSection;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppLinkRepositoryAdapter}: the listing only goes through its own JPA
 * repository with the sort left to the specification, the duplicates are looked up among the
 * active links, and every change is audited.
 */
@ExtendWith(MockitoExtension.class)
class AppLinkRepositoryAdapterTest {

    private static final LocalDateTime DELETED_AT = LocalDateTime.of(2026, 10, 21, 12, 0);

    @Mock
    private EntityManager entityManager;
    @Mock
    private AppLinkJPARepository appLinkJPARepository;
    @Mock
    private AppLinkAudJPARepository appLinkAudJPARepository;
    @Mock
    private AppLinkMapper appLinkMapper;

    @InjectMocks
    private AppLinkRepositoryAdapter adapter;

    private ApplicationEntity application;
    private LkupAppSectionEntity sectionEntity;
    private AppLinkEntity entity;
    private AppLink model;

    @BeforeEach
    void setUp() {
        application = new ApplicationEntity();
        application.setId(3L);
        sectionEntity = new LkupAppSectionEntity();
        sectionEntity.setId(5L);
        entity = new AppLinkEntity();
        entity.setId(10L);
        entity.setApplication(application);
        entity.setSection(sectionEntity);
        entity.setTitle("Manual");
        entity.setDescription("desc");
        entity.setUrl("https://intranet.caib.es/manual");
        AppSection section = new AppSection();
        section.setId(5L);
        model = new AppLink();
        model.setId(10L);
        model.setApplicationId(3L);
        model.setSection(section);
    }

    private void stubReferences() {
        when(appLinkMapper.toEntity(model)).thenReturn(entity);
        when(entityManager.getReference(ApplicationEntity.class, 3L)).thenReturn(application);
        when(entityManager.getReference(LkupAppSectionEntity.class, 5L)).thenReturn(sectionEntity);
    }

    @Test
    void findById_mapsTheLink() {
        when(appLinkJPARepository.findById(10L)).thenReturn(Optional.of(entity));
        when(appLinkMapper.toModel(entity)).thenReturn(model);

        assertSame(model, adapter.findById(10L));
    }

    @Test
    void findById_returnsNullWhenThereIsNone() {
        when(appLinkJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_mapsEveryLinkOfThePage() {
        when(appLinkJPARepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));
        when(appLinkMapper.toModel(entity)).thenReturn(model);

        Page<AppLink> result = adapter.findAll(new AppDocumentationLinkCriteria(), PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void findAll_runsTheQueryUnsorted_becauseTheSpecificationAppliesTheSort() {
        when(appLinkJPARepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        adapter.findAll(new AppDocumentationLinkCriteria(), PageRequest.of(2, 5, Sort.by("section").descending()));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(appLinkJPARepository).findAll(any(Specification.class), captor.capture());
        assertEquals(2, captor.getValue().getPageNumber());
        assertEquals(5, captor.getValue().getPageSize());
        assertTrue(captor.getValue().getSort().isUnsorted());
    }

    @Test
    void existsActiveDuplicate_asksForTheActiveLinksOfTheApplication() {
        when(appLinkJPARepository.existsByApplicationIdAndTitleAndSectionIdAndUrlAndDeletedAtIsNull(3L, "Manual", 5L, "u")).thenReturn(true);

        assertTrue(adapter.existsActiveDuplicate(3L, "Manual", 5L, "u"));
        assertFalse(adapter.existsActiveDuplicate(3L, "Other", 5L, "u"));
    }

    @Test
    void existsActiveDuplicateExcluding_leavesOutTheGivenLink() {
        when(appLinkJPARepository.existsByApplicationIdAndTitleAndSectionIdAndUrlAndIdNotAndDeletedAtIsNull(3L, "Manual", 5L, "u", 10L)).thenReturn(true);

        assertTrue(adapter.existsActiveDuplicateExcluding(3L, "Manual", 5L, "u", 10L));
        assertFalse(adapter.existsActiveDuplicateExcluding(3L, "Manual", 5L, "u", 11L));
    }

    @Test
    void create_savesTheLinkAndAuditsItAsAnInsert() {
        entity.setId(null);
        stubReferences();
        when(appLinkJPARepository.saveAndFlush(entity)).thenAnswer(invocation -> {
            entity.setId(10L);
            return entity;
        });
        when(appLinkMapper.toModel(entity)).thenReturn(model);

        AppLink result = adapter.create(model);

        assertSame(model, result);
        ArgumentCaptor<AppLinkAudEntity> captor = ArgumentCaptor.forClass(AppLinkAudEntity.class);
        verify(appLinkAudJPARepository).save(captor.capture());
        AppLinkAudEntity aud = captor.getValue();
        assertEquals("INSERT", aud.getAudAction());
        assertEquals(10L, aud.getAppLinkId());
        assertEquals(3L, aud.getApplicationId());
        assertEquals(5L, aud.getSectionId());
        assertEquals("Manual", aud.getTitle());
        assertEquals("desc", aud.getDescription());
        assertEquals("https://intranet.caib.es/manual", aud.getUrl());
        assertNotNull(aud.getAuditDate());
    }

    @Test
    void update_savesTheLinkWithItsIdentifierAndAuditsItAsAnUpdate() {
        stubReferences();
        when(appLinkJPARepository.save(entity)).thenReturn(entity);
        when(appLinkMapper.toModel(entity)).thenReturn(model);

        AppLink result = adapter.update(model, 10L);

        assertSame(model, result);
        assertEquals(10L, entity.getId());
        ArgumentCaptor<AppLinkAudEntity> captor = ArgumentCaptor.forClass(AppLinkAudEntity.class);
        verify(appLinkAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_savesTheDeletionAndAuditsItAsADelete() {
        model.setDeletedAt(DELETED_AT);
        model.setDeletedBy("admin");
        entity.setDeletedAt(DELETED_AT);
        entity.setDeletedBy("admin");
        stubReferences();
        when(appLinkJPARepository.save(entity)).thenReturn(entity);

        adapter.delete(model);

        ArgumentCaptor<AppLinkAudEntity> captor = ArgumentCaptor.forClass(AppLinkAudEntity.class);
        verify(appLinkAudJPARepository).save(captor.capture());
        AppLinkAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertEquals(DELETED_AT, aud.getDeletedAt());
        assertEquals("admin", aud.getDeletedBy());
    }
}
