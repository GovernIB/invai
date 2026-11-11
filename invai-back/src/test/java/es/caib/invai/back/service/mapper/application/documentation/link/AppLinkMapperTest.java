package es.caib.invai.back.service.mapper.application.documentation.link;

import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkInputDTO;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkOutputDTO;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkUpdateInputDTO;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.application.documentation.link.AppLinkEntity;
import es.caib.invai.back.persistence.model.maintenance.documentation.documentCategory.DocumentCategoryEntity;
import es.caib.invai.back.service.mapper.maintenance.documentation.documentCategory.DocumentCategoryMapperImpl;
import es.caib.invai.back.service.model.application.documentation.link.AppLink;
import es.caib.invai.back.service.model.maintenance.documentation.documentCategory.DocumentCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link AppLinkMapper}: how a link moves between its entity, its model and the
 * DTOs the endpoints send and receive.
 */
class AppLinkMapperTest {

    private AppLinkMapper mapper;

    @BeforeEach
    void setUp() {
        AppLinkMapperImpl impl = new AppLinkMapperImpl();
        ReflectionTestUtils.setField(impl, "documentCategoryMapper", new DocumentCategoryMapperImpl());
        mapper = impl;
    }

    private static AppLinkEntity entity() {
        ApplicationEntity application = new ApplicationEntity();
        application.setId(3L);
        DocumentCategoryEntity category = new DocumentCategoryEntity();
        category.setId(5L);
        category.setName("General");
        category.setNameEs("General es");
        AppLinkEntity entity = new AppLinkEntity();
        entity.setId(10L);
        entity.setApplication(application);
        entity.setTitle("Manual");
        entity.setDescription("desc");
        entity.setCategory(category);
        entity.setUrl("https://intranet.caib.es/manual");
        return entity;
    }

    @Test
    void toModel_keepsTheIdentifierOfItsApplicationAndItsCategory() {
        AppLink model = mapper.toModel(entity());

        assertEquals(10L, model.getId());
        assertEquals(3L, model.getApplicationId());
        assertEquals("Manual", model.getTitle());
        assertEquals("desc", model.getDescription());
        assertEquals(5L, model.getCategory().getId());
        assertEquals("https://intranet.caib.es/manual", model.getUrl());
    }

    @Test
    void toEntity_leavesTheApplicationAndTheCategoryToTheRepository() {
        AppLinkEntity back = mapper.toEntity(mapper.toModel(entity()));

        assertNull(back.getApplication());
        assertNull(back.getCategory());
        assertEquals("Manual", back.getTitle());
        assertEquals("https://intranet.caib.es/manual", back.getUrl());
    }

    @Test
    void toResponse_showsTheCategoryAndTheDeletionTimestamp() {
        AppLink model = mapper.toModel(entity());
        LocalDateTime deletedAt = LocalDateTime.of(2026, 10, 21, 10, 0);
        model.setDeletedAt(deletedAt);

        AppDocumentationLinkOutputDTO dto = mapper.toResponse(model);

        assertEquals(10L, dto.getId());
        assertEquals("Manual", dto.getTitle());
        assertEquals("desc", dto.getDescription());
        assertEquals("https://intranet.caib.es/manual", dto.getUrl());
        assertEquals(5L, dto.getCategory().getId());
        assertEquals("General", dto.getCategory().getName());
        assertEquals("General es", dto.getCategory().getNameEs());
        assertEquals(deletedAt, dto.getDeletedAt());
    }

    @Test
    void toResponse_showsTheCategoryWithoutItsSubcategoriesNorItsDeletionTimestamp() {
        AppLink model = mapper.toModel(entity());
        model.getCategory().setDeletedAt(LocalDateTime.of(2026, 10, 21, 10, 0));

        AppDocumentationLinkOutputDTO dto = mapper.toResponse(model);

        assertNull(dto.getCategory().getSubcategories());
        assertNull(dto.getCategory().getDeletedAt());
    }

    @Test
    void toModelFromInput_buildsTheLinkWithTheCategoryOnlyIdentified() {
        AppLink model = mapper.toModelFromInput(new AppDocumentationLinkInputDTO(3L, "Manual", "desc", 5L, "https://intranet.caib.es/manual"));

        assertNull(model.getId());
        assertEquals(3L, model.getApplicationId());
        assertEquals("Manual", model.getTitle());
        assertEquals("desc", model.getDescription());
        assertEquals(5L, model.getCategory().getId());
        assertNull(model.getCategory().getName());
        assertEquals("https://intranet.caib.es/manual", model.getUrl());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void updateModelFromInput_changesTheDataButNeverTheApplicationNorTheIdentity() {
        AppLink model = mapper.toModel(entity());
        model.setCreatedBy("someone");

        mapper.updateModelFromInput(new AppDocumentationLinkUpdateInputDTO("Manual v2", null, 6L, "https://intranet.caib.es/v2"), model);

        assertEquals(10L, model.getId());
        assertEquals(3L, model.getApplicationId());
        assertEquals("someone", model.getCreatedBy());
        assertEquals("Manual v2", model.getTitle());
        assertNull(model.getDescription());
        assertEquals(6L, model.getCategory().getId());
        assertEquals("https://intranet.caib.es/v2", model.getUrl());
    }

    @Test
    void newCategory_holdsOnlyTheIdentifier() {
        DocumentCategory category = mapper.newCategory(7L);

        assertEquals(7L, category.getId());
        assertNull(category.getName());
    }
}
