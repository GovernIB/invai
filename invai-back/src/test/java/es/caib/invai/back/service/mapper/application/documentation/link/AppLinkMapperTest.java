package es.caib.invai.back.service.mapper.application.documentation.link;

import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkInputDTO;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkOutputDTO;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkUpdateInputDTO;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.application.documentation.link.AppLinkEntity;
import es.caib.invai.back.persistence.model.catalog.appSection.LkupAppSectionEntity;
import es.caib.invai.back.service.mapper.catalog.appSection.AppSectionMapperImpl;
import es.caib.invai.back.service.model.application.documentation.link.AppLink;
import es.caib.invai.back.service.model.catalog.appSection.AppSection;
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
        ReflectionTestUtils.setField(impl, "appSectionMapper", new AppSectionMapperImpl());
        mapper = impl;
    }

    private static AppLinkEntity entity() {
        ApplicationEntity application = new ApplicationEntity();
        application.setId(3L);
        LkupAppSectionEntity section = new LkupAppSectionEntity();
        section.setId(5L);
        section.setCode("GENERAL");
        section.setName("General");
        section.setNameEs("General es");
        AppLinkEntity entity = new AppLinkEntity();
        entity.setId(10L);
        entity.setApplication(application);
        entity.setTitle("Manual");
        entity.setDescription("desc");
        entity.setSection(section);
        entity.setUrl("https://intranet.caib.es/manual");
        return entity;
    }

    @Test
    void toModel_keepsTheIdentifierOfItsApplicationAndItsSection() {
        AppLink model = mapper.toModel(entity());

        assertEquals(10L, model.getId());
        assertEquals(3L, model.getApplicationId());
        assertEquals("Manual", model.getTitle());
        assertEquals("desc", model.getDescription());
        assertEquals(5L, model.getSection().getId());
        assertEquals("https://intranet.caib.es/manual", model.getUrl());
    }

    @Test
    void toEntity_leavesTheApplicationAndTheSectionToTheRepository() {
        AppLinkEntity back = mapper.toEntity(mapper.toModel(entity()));

        assertNull(back.getApplication());
        assertNull(back.getSection());
        assertEquals("Manual", back.getTitle());
        assertEquals("https://intranet.caib.es/manual", back.getUrl());
    }

    @Test
    void toResponse_showsTheSectionAndTheDeletionTimestamp() {
        AppLink model = mapper.toModel(entity());
        LocalDateTime deletedAt = LocalDateTime.of(2026, 10, 21, 10, 0);
        model.setDeletedAt(deletedAt);

        AppDocumentationLinkOutputDTO dto = mapper.toResponse(model);

        assertEquals(10L, dto.getId());
        assertEquals("Manual", dto.getTitle());
        assertEquals("desc", dto.getDescription());
        assertEquals("https://intranet.caib.es/manual", dto.getUrl());
        assertEquals(5L, dto.getSection().getId());
        assertEquals("GENERAL", dto.getSection().getCode());
        assertEquals("General", dto.getSection().getName());
        assertEquals("General es", dto.getSection().getNameEs());
        assertEquals(deletedAt, dto.getDeletedAt());
    }

    @Test
    void toModelFromInput_buildsTheLinkWithTheSectionOnlyIdentified() {
        AppLink model = mapper.toModelFromInput(new AppDocumentationLinkInputDTO(3L, "Manual", "desc", 5L, "https://intranet.caib.es/manual"));

        assertNull(model.getId());
        assertEquals(3L, model.getApplicationId());
        assertEquals("Manual", model.getTitle());
        assertEquals("desc", model.getDescription());
        assertEquals(5L, model.getSection().getId());
        assertNull(model.getSection().getCode());
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
        assertEquals(6L, model.getSection().getId());
        assertEquals("https://intranet.caib.es/v2", model.getUrl());
    }

    @Test
    void newSection_holdsOnlyTheIdentifier() {
        AppSection section = mapper.newSection(7L);

        assertEquals(7L, section.getId());
        assertNull(section.getName());
    }
}
