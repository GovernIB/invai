package es.caib.invai.back.persistence.repository.application.documentation.attachment;

import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentAudEntity;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentEntity;
import es.caib.invai.back.persistence.model.catalog.documentStatus.LkupDocumentStatusEntity;
import es.caib.invai.back.persistence.model.maintenance.documentation.documentCategory.DocumentCategoryEntity;
import es.caib.invai.back.service.mapper.application.documentation.attachment.AppDocumentationAttachmentMapper;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationAttachment;
import es.caib.invai.back.service.model.catalog.documentStatus.DocumentStatus;
import es.caib.invai.back.service.model.maintenance.documentation.documentCategory.DocumentCategory;
import jakarta.persistence.EntityManager;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppDocumentationAttachmentRepositoryAdapter}: the listing only goes through
 * its own JPA repository, with the sort left to the specification instead of the pageable, and the
 * writes link the category and the optional subcategory through references.
 */
@ExtendWith(MockitoExtension.class)
class AppDocumentationAttachmentRepositoryAdapterTest {

    @Mock
    private EntityManager entityManager;
    @Mock
    private AppDocumentationAttachmentJPARepository appDocumentationAttachmentJPARepository;
    @Mock
    private AppDocumentationAttachmentAudJPARepository appDocumentationAttachmentAudJPARepository;
    @Mock
    private AppDocumentationAttachmentMapper appDocumentationAttachmentMapper;

    @InjectMocks
    private AppDocumentationAttachmentRepositoryAdapter adapter;

    private static AppDocumentationAttachment model(Long id) {
        AppDocumentationAttachment model = new AppDocumentationAttachment();
        model.setId(id);
        return model;
    }

    private void stubPage(Long... ids) {
        List<AppDocumentationAttachmentEntity> entities = new ArrayList<>();
        for (Long id : ids) {
            AppDocumentationAttachmentEntity entity = new AppDocumentationAttachmentEntity();
            entity.setId(id);
            entities.add(entity);
            when(appDocumentationAttachmentMapper.toModel(entity)).thenReturn(model(id));
        }
        when(appDocumentationAttachmentJPARepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(entities));
    }

    @Test
    void findAll_mapsEveryDocumentOfThePage() {
        stubPage(10L, 11L);

        Page<AppDocumentationAttachment> result = adapter.findAll(new AppDocumentationAttachmentCriteria(), null, PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        assertEquals(10L, result.getContent().get(0).getId());
        assertEquals(11L, result.getContent().get(1).getId());
    }

    @Test
    void findAll_runsTheQueryUnsorted_becauseTheSpecificationAppliesTheSort() {
        stubPage(10L);

        adapter.findAll(new AppDocumentationAttachmentCriteria(), null, PageRequest.of(2, 5, Sort.by("versionLabel").descending()));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(appDocumentationAttachmentJPARepository).findAll(any(Specification.class), captor.capture());
        assertEquals(2, captor.getValue().getPageNumber());
        assertEquals(5, captor.getValue().getPageSize());
        assertTrue(captor.getValue().getSort().isUnsorted());
    }

    // ------------------------------------------------------------------
    // create / update / delete: the related records are linked through references
    // ------------------------------------------------------------------

    private static DocumentCategory category(Long id) {
        DocumentCategory category = new DocumentCategory();
        category.setId(id);
        return category;
    }

    private static AppDocumentationAttachment documentToSave(Long subcategoryId) {
        AppDocumentationAttachment model = model(10L);
        model.setApplicationId(3L);
        model.setCategory(category(5L));
        model.setSubcategory(Objects.isNull(subcategoryId) ? null : category(subcategoryId));
        DocumentStatus status = new DocumentStatus();
        status.setId(9L);
        model.setStatus(status);
        return model;
    }

    private ApplicationEntity stubReferences(AppDocumentationAttachment model, AppDocumentationAttachmentEntity entity) {
        ApplicationEntity application = new ApplicationEntity();
        application.setId(3L);
        DocumentCategoryEntity category = new DocumentCategoryEntity();
        category.setId(5L);
        LkupDocumentStatusEntity status = new LkupDocumentStatusEntity();
        status.setId(9L);
        when(appDocumentationAttachmentMapper.toEntity(model)).thenReturn(entity);
        when(entityManager.getReference(ApplicationEntity.class, 3L)).thenReturn(application);
        when(entityManager.getReference(DocumentCategoryEntity.class, 5L)).thenReturn(category);
        when(entityManager.getReference(LkupDocumentStatusEntity.class, 9L)).thenReturn(status);
        return application;
    }

    private AppDocumentationAttachmentAudEntity audited() {
        ArgumentCaptor<AppDocumentationAttachmentAudEntity> captor = ArgumentCaptor.forClass(AppDocumentationAttachmentAudEntity.class);
        verify(appDocumentationAttachmentAudJPARepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void create_linksTheApplicationTheCategoryTheSubcategoryAndTheStatusAndAuditsThem() {
        AppDocumentationAttachment model = documentToSave(7L);
        AppDocumentationAttachmentEntity entity = new AppDocumentationAttachmentEntity();
        entity.setId(10L);
        stubReferences(model, entity);
        DocumentCategoryEntity subcategory = new DocumentCategoryEntity();
        subcategory.setId(7L);
        when(entityManager.getReference(DocumentCategoryEntity.class, 7L)).thenReturn(subcategory);
        when(appDocumentationAttachmentJPARepository.saveAndFlush(entity)).thenReturn(entity);
        when(appDocumentationAttachmentMapper.toModel(entity)).thenReturn(model);

        AppDocumentationAttachment result = adapter.create(model);

        assertSame(model, result);
        assertEquals(5L, entity.getCategory().getId());
        assertSame(subcategory, entity.getSubcategory());
        assertEquals(9L, entity.getStatus().getId());
        AppDocumentationAttachmentAudEntity aud = audited();
        assertEquals("INSERT", aud.getAudAction());
        assertEquals(3L, aud.getApplicationId());
        assertEquals(5L, aud.getCategoryId());
        assertEquals(7L, aud.getSubcategoryId());
        assertEquals(9L, aud.getStatusId());
    }

    @Test
    void create_withoutSubcategory_linksNoneAndAuditsNone() {
        AppDocumentationAttachment model = documentToSave(null);
        AppDocumentationAttachmentEntity entity = new AppDocumentationAttachmentEntity();
        entity.setId(10L);
        stubReferences(model, entity);
        when(appDocumentationAttachmentJPARepository.saveAndFlush(entity)).thenReturn(entity);
        when(appDocumentationAttachmentMapper.toModel(entity)).thenReturn(model);

        adapter.create(model);

        assertNull(entity.getSubcategory());
        assertNull(audited().getSubcategoryId());
        verify(entityManager, times(3)).getReference(any(Class.class), any());
    }

    @Test
    void update_savesTheDocumentUnderTheGivenIdentifierWithItsNewCategoryAndSubcategory() {
        AppDocumentationAttachment model = documentToSave(7L);
        AppDocumentationAttachmentEntity entity = new AppDocumentationAttachmentEntity();
        stubReferences(model, entity);
        DocumentCategoryEntity subcategory = new DocumentCategoryEntity();
        subcategory.setId(7L);
        when(entityManager.getReference(DocumentCategoryEntity.class, 7L)).thenReturn(subcategory);
        when(appDocumentationAttachmentJPARepository.save(entity)).thenReturn(entity);
        when(appDocumentationAttachmentMapper.toModel(entity)).thenReturn(model);

        adapter.update(model, 10L);

        assertEquals(10L, entity.getId());
        assertEquals(5L, entity.getCategory().getId());
        assertSame(subcategory, entity.getSubcategory());
        AppDocumentationAttachmentAudEntity aud = audited();
        assertEquals("UPDATE", aud.getAudAction());
        assertEquals(10L, aud.getAppDocumentationAttachmentId());
        assertEquals(5L, aud.getCategoryId());
        assertEquals(7L, aud.getSubcategoryId());
    }

    @Test
    void delete_savesTheDeletionAndAuditsTheCategoryAndTheSubcategoryTheDocumentHad() {
        AppDocumentationAttachment model = documentToSave(7L);
        model.setDeletedAt(LocalDateTime.of(2026, 10, 20, 12, 0));
        AppDocumentationAttachmentEntity entity = new AppDocumentationAttachmentEntity();
        entity.setDeletedAt(model.getDeletedAt());
        stubReferences(model, entity);
        DocumentCategoryEntity subcategory = new DocumentCategoryEntity();
        subcategory.setId(7L);
        when(entityManager.getReference(DocumentCategoryEntity.class, 7L)).thenReturn(subcategory);
        when(appDocumentationAttachmentJPARepository.save(entity)).thenReturn(entity);

        adapter.delete(model);

        assertEquals(10L, entity.getId());
        AppDocumentationAttachmentAudEntity aud = audited();
        assertEquals("DELETE", aud.getAudAction());
        assertEquals(5L, aud.getCategoryId());
        assertEquals(7L, aud.getSubcategoryId());
        assertEquals(model.getDeletedAt(), aud.getDeletedAt());
    }
}
