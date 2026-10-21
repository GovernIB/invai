package es.caib.invai.back.service.mapper.application.documentation.attachment;

import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationMetadataInputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationMetadataOutputDTO;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentVersionEntity;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationMetadataEntity;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link AppDocumentationMetadataMapper}: how a metadata of an attachment moves
 * between its entity, its model and the DTOs the endpoints send and receive.
 */
class AppDocumentationMetadataMapperTest {

    private AppDocumentationMetadataMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AppDocumentationMetadataMapperImpl();
    }

    private static AppDocumentationMetadataEntity entity() {
        AppDocumentationAttachmentVersionEntity attachment = new AppDocumentationAttachmentVersionEntity();
        attachment.setId(100L);
        AppDocumentationMetadataEntity entity = new AppDocumentationMetadataEntity();
        entity.setId(1L);
        entity.setAppDocumentationAttachmentVersion(attachment);
        entity.setMetaKey("eni.clase");
        entity.setMetaValue("Manual");
        return entity;
    }

    @Test
    void toModel_keepsTheIdentifierOfItsAttachment() {
        AppDocumentationMetadata model = mapper.toModel(entity());

        assertEquals(100L, model.getAppDocumentationAttachmentVersionId());
        assertEquals("eni.clase", model.getMetaKey());
        assertEquals("Manual", model.getMetaValue());
    }

    @Test
    void toEntity_leavesTheAttachmentToTheRepository() {
        AppDocumentationMetadataEntity back = mapper.toEntity(mapper.toModel(entity()));

        assertNull(back.getAppDocumentationAttachmentVersion());
        assertEquals("eni.clase", back.getMetaKey());
        assertEquals("Manual", back.getMetaValue());
    }

    @Test
    void toResponse_showsOnlyTheKeyAndTheValue() {
        AppDocumentationMetadataOutputDTO dto = mapper.toResponse(mapper.toModel(entity()));

        assertEquals("eni.clase", dto.getKey());
        assertEquals("Manual", dto.getValue());
    }

    @Test
    void toModelFromInput_belongsToTheGivenAttachment() {
        AppDocumentationMetadata model = mapper.toModelFromInput(new AppDocumentationMetadataInputDTO("k", "v"), 100L);

        assertNull(model.getId());
        assertEquals(100L, model.getAppDocumentationAttachmentVersionId());
        assertEquals("k", model.getMetaKey());
        assertEquals("v", model.getMetaValue());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void lists_areConvertedElementByElement() {
        List<AppDocumentationMetadata> models = mapper.toModelList(List.of(entity(), entity()));
        List<AppDocumentationMetadataOutputDTO> dtos = mapper.toResponseList(models);

        assertEquals(2, models.size());
        assertEquals(2, dtos.size());
        assertEquals("eni.clase", dtos.get(1).getKey());
    }
}
