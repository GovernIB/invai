package es.caib.invai.back.service.mapper.application.documentation.attachment;

import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentInputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentOutputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentSimpleOutputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentUpdateInputDTO;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentEntity;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentVersionEntity;
import es.caib.invai.back.persistence.model.catalog.appSection.LkupAppSectionEntity;
import es.caib.invai.back.persistence.model.catalog.documentStatus.LkupDocumentStatusEntity;
import es.caib.invai.back.persistence.model.catalog.mimeType.LkupMimeTypeEntity;
import es.caib.invai.back.persistence.model.maintenance.documentation.documentCategory.DocumentCategoryEntity;
import es.caib.invai.back.service.mapper.catalog.appSection.AppSectionMapperImpl;
import es.caib.invai.back.service.mapper.catalog.documentStatus.DocumentStatusMapperImpl;
import es.caib.invai.back.service.mapper.catalog.mimeType.MimeTypeMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.documentation.documentCategory.DocumentCategoryMapperImpl;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationAttachment;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationAttachmentVersion;
import es.caib.invai.back.service.model.catalog.mimeType.MimeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link AppDocumentationAttachmentMapper}, locking in how the stored category is split into the
 * category and the subcategory the user sees, that a document never shows the subcategories nor
 * the deletion timestamp of its categories, and what each inbound DTO writes onto the model.
 */
class AppDocumentationAttachmentMapperTest {

    private AppDocumentationAttachmentMapper mapper;

    @BeforeEach
    void setUp() {
        AppDocumentationAttachmentVersionMapperImpl versionMapper = new AppDocumentationAttachmentVersionMapperImpl();
        ReflectionTestUtils.setField(versionMapper, "mimeTypeMapper", new MimeTypeMapperImpl());
        ReflectionTestUtils.setField(versionMapper, "appDocumentationMetadataMapper", new AppDocumentationMetadataMapperImpl());
        AppDocumentationAttachmentMapperImpl impl = new AppDocumentationAttachmentMapperImpl();
        ReflectionTestUtils.setField(impl, "appSectionMapper", new AppSectionMapperImpl());
        ReflectionTestUtils.setField(impl, "documentStatusMapper", new DocumentStatusMapperImpl());
        ReflectionTestUtils.setField(impl, "documentCategoryMapper", new DocumentCategoryMapperImpl());
        ReflectionTestUtils.setField(impl, "appDocumentationAttachmentVersionMapper", versionMapper);
        mapper = impl;
    }

    private static DocumentCategoryEntity category(Long id, String name, DocumentCategoryEntity parent) {
        DocumentCategoryEntity entity = new DocumentCategoryEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setNameEs(name + " ES");
        entity.setParent(parent);
        return entity;
    }

    private static AppDocumentationAttachmentEntity entity(DocumentCategoryEntity documentCategory) {
        ApplicationEntity application = new ApplicationEntity();
        application.setId(3L);
        LkupAppSectionEntity section = new LkupAppSectionEntity();
        section.setId(7L);
        section.setCode("SECURITY");
        section.setName("Seguretat");
        section.setNameEs("Seguridad");
        LkupDocumentStatusEntity status = new LkupDocumentStatusEntity();
        status.setId(9L);
        status.setCode("ACTIVE");
        status.setName("Vigent");
        status.setNameEs("Vigente");

        AppDocumentationAttachmentEntity entity = new AppDocumentationAttachmentEntity();
        entity.setId(10L);
        entity.setApplication(application);
        entity.setTitle("Manual API v2");
        entity.setSection(section);
        entity.setDocumentCategory(documentCategory);
        entity.setStatus(status);
        return entity;
    }

    private static AppDocumentationAttachmentVersion currentAttachment() {
        MimeType pdf = new MimeType();
        pdf.setId(1L);
        pdf.setExtension("pdf");
        pdf.setMimeType("application/pdf");
        pdf.setName("PDF");
        AppDocumentationAttachmentVersion version = new AppDocumentationAttachmentVersion();
        version.setId(100L);
        version.setAppDocumentationAttachmentId(10L);
        version.setVersionLabel("2.1");
        version.setFileName("manual.pdf");
        version.setMimeType(pdf);
        version.setFileSize(2048L);
        version.setCurrent(true);
        version.setCreatedAt(LocalDateTime.of(2025, 11, 18, 10, 0));
        version.setCreatedBy("jgarcia@gov.com");
        return version;
    }

    @Test
    void toModel_aSubcategory_keepsItAsStoredAndExposesItsParentAsTheCategory() {
        DocumentCategoryEntity technical = category(1L, "Tècnica", null);
        DocumentCategoryEntity api = category(2L, "API", technical);

        AppDocumentationAttachment model = mapper.toModel(entity(api));

        assertEquals(3L, model.getApplicationId());
        assertEquals(2L, model.getDocumentCategory().getId());
        assertEquals("Tècnica", model.getCategory().getName());
        assertEquals("API", model.getSubcategory().getName());
        assertEquals("Seguretat", model.getSection().getName());
        assertEquals("ACTIVE", model.getStatus().getCode());
    }

    @Test
    void toModel_aCategory_hasNoSubcategory() {
        AppDocumentationAttachment model = mapper.toModel(entity(category(1L, "Tècnica", null)));

        assertEquals("Tècnica", model.getCategory().getName());
        assertNull(model.getSubcategory());
    }

    @Test
    void toSimpleResponse_takesTheVersionDataFromTheCurrentAttachment() {
        AppDocumentationAttachment model = mapper.toModel(entity(category(2L, "API", category(1L, "Tècnica", null))));
        model.setCurrentAttachment(currentAttachment());

        AppDocumentationAttachmentSimpleOutputDTO dto = mapper.toSimpleResponse(model);

        assertEquals(10L, dto.getId());
        assertEquals("2.1", dto.getVersionLabel());
        assertEquals(100L, dto.getCurrentAttachmentId());
        assertEquals("PDF", dto.getMimeType().getName());
        assertEquals("Tècnica", dto.getCategory().getName());
        assertNull(dto.getCategory().getParentId());
        assertEquals("API", dto.getSubcategory().getName());
        assertEquals(1L, dto.getSubcategory().getParentId());
        assertEquals("SECURITY", dto.getSection().getCode());
        assertEquals("Vigente", dto.getStatus().getNameEs());
    }

    @Test
    void toResponse_ofADeletedDocument_carriesItsDeletionTimestamp() {
        AppDocumentationAttachment model = mapper.toModel(entity(category(1L, "T\u00e8cnica", null)));
        assertNull(mapper.toResponse(model).getDeletedAt());

        model.setDeletedAt(LocalDateTime.of(2026, 10, 20, 12, 0));

        assertEquals(LocalDateTime.of(2026, 10, 20, 12, 0), mapper.toResponse(model).getDeletedAt());
    }

    @Test
    void toSimpleResponse_tellsADeletedDocumentApartThroughItsDeletionTimestamp() {
        AppDocumentationAttachment model = mapper.toModel(entity(category(1L, "T\u00e8cnica", null)));
        assertNull(mapper.toSimpleResponse(model).getDeletedAt());

        model.setDeletedAt(LocalDateTime.of(2026, 10, 20, 12, 0));

        assertEquals(LocalDateTime.of(2026, 10, 20, 12, 0), mapper.toSimpleResponse(model).getDeletedAt());
    }

    @Test
    void toSimpleResponse_withoutACurrentAttachment_leavesItsDataEmpty() {
        AppDocumentationAttachmentSimpleOutputDTO dto = mapper.toSimpleResponse(mapper.toModel(entity(category(1L, "Tècnica", null))));

        assertNull(dto.getMimeType());
        assertNull(dto.getVersionLabel());
        assertNull(dto.getCurrentAttachmentId());
    }

    @Test
    void categoriesOfADocument_neverShowTheirSubcategoriesNorTheirDeletionTimestamp() {
        DocumentCategoryEntity technical = category(1L, "Tècnica", null);
        technical.setDeletedAt(LocalDateTime.now());
        AppDocumentationAttachment model = mapper.toModel(entity(category(2L, "API", technical)));
        model.getCategory().setSubcategories(List.of(new es.caib.invai.back.service.model.maintenance.documentation.documentCategory.DocumentCategory()));

        AppDocumentationAttachmentSimpleOutputDTO simple = mapper.toSimpleResponse(model);
        AppDocumentationAttachmentOutputDTO detail = mapper.toResponse(model);

        assertNull(simple.getCategory().getSubcategories());
        assertNull(simple.getCategory().getDeletedAt());
        assertNull(detail.getCategory().getSubcategories());
        assertNull(detail.getCategory().getDeletedAt());
        assertNull(detail.getSubcategory().getSubcategories());
    }

    @Test
    void toResponse_mapsEveryVersionWithItsUploadDateAndAuthor() {
        AppDocumentationAttachment model = mapper.toModel(entity(category(1L, "Tècnica", null)));
        model.setAttachments(List.of(currentAttachment()));

        AppDocumentationAttachmentOutputDTO dto = mapper.toResponse(model);

        assertEquals(3L, dto.getApplicationId());
        assertEquals(1, dto.getAttachments().size());
        assertEquals(LocalDateTime.of(2025, 11, 18, 10, 0), dto.getAttachments().get(0).getUploadedAt());
        assertEquals("jgarcia@gov.com", dto.getAttachments().get(0).getUploadedBy());
        assertEquals("manual.pdf", dto.getAttachments().get(0).getFileName());
        assertEquals(2048L, dto.getAttachments().get(0).getFileSize());
    }

    @Test
    void toModelFromInput_carriesOnlyTheIdentifiersOfTheRelatedRecords() {
        AppDocumentationAttachmentInputDTO inputDTO = new AppDocumentationAttachmentInputDTO(3L, "Manual", 7L, 5L, 9L, "1.0", "note", null);

        AppDocumentationAttachment model = mapper.toModelFromInput(inputDTO);

        assertNull(model.getId());
        assertEquals(3L, model.getApplicationId());
        assertEquals("Manual", model.getTitle());
        assertEquals(7L, model.getSection().getId());
        assertNull(model.getSection().getName());
        assertEquals(5L, model.getDocumentCategory().getId());
        assertEquals(9L, model.getStatus().getId());
        assertNull(model.getCreatedAt());
    }

    @Test
    void updateModelFromInput_changesTheMainRecordAndKeepsTheApplicationAndAudit() {
        AppDocumentationAttachment model = mapper.toModel(entity(category(1L, "Tècnica", null)));
        model.setCreatedBy("creator");

        mapper.updateModelFromInput(new AppDocumentationAttachmentUpdateInputDTO("New title", 8L, 6L, 11L), model);

        assertEquals("New title", model.getTitle());
        assertEquals(8L, model.getSection().getId());
        assertEquals(6L, model.getDocumentCategory().getId());
        assertEquals(11L, model.getStatus().getId());
        assertEquals(3L, model.getApplicationId());
        assertEquals(10L, model.getId());
        assertEquals("creator", model.getCreatedBy());
    }

    @Test
    void version_toModelAndBack_neverTouchesTheContent() {
        AppDocumentationAttachmentVersionEntity entity = new AppDocumentationAttachmentVersionEntity();
        entity.setId(100L);
        AppDocumentationAttachmentEntity parent = new AppDocumentationAttachmentEntity();
        parent.setId(10L);
        entity.setAppDocumentationAttachment(parent);
        LkupMimeTypeEntity pdf = new LkupMimeTypeEntity();
        pdf.setId(1L);
        pdf.setName("PDF");
        entity.setMimeType(pdf);
        entity.setVersionLabel("2.1");
        entity.setCurrent(true);

        AppDocumentationAttachmentVersionMapperImpl versionMapper = new AppDocumentationAttachmentVersionMapperImpl();
        ReflectionTestUtils.setField(versionMapper, "mimeTypeMapper", new MimeTypeMapperImpl());
        ReflectionTestUtils.setField(versionMapper, "appDocumentationMetadataMapper", new AppDocumentationMetadataMapperImpl());
        AppDocumentationAttachmentVersion model = versionMapper.toModel(entity);
        AppDocumentationAttachmentVersionEntity back = versionMapper.toEntity(model);

        assertEquals(10L, model.getAppDocumentationAttachmentId());
        assertEquals("PDF", model.getMimeType().getName());
        assertEquals(true, model.isCurrent());
        assertNull(back.getContent());
        assertNull(back.getAppDocumentationAttachment());
        assertNotNull(back.getVersionLabel());
    }
}
