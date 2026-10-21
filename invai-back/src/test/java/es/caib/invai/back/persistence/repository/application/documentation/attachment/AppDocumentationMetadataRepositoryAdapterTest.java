package es.caib.invai.back.persistence.repository.application.documentation.attachment;

import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentVersionEntity;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationMetadataAudEntity;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationMetadataEntity;
import es.caib.invai.back.service.mapper.application.documentation.attachment.AppDocumentationMetadataMapper;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationMetadata;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppDocumentationMetadataRepositoryAdapter}: the metadata is read and
 * logically deleted by attachments, and every deletion is audited.
 */
@ExtendWith(MockitoExtension.class)
class AppDocumentationMetadataRepositoryAdapterTest {

    private static final LocalDateTime DELETED_AT = LocalDateTime.of(2026, 10, 20, 12, 0);

    @Mock
    private EntityManager entityManager;
    @Mock
    private AppDocumentationMetadataJPARepository appDocumentationMetadataJPARepository;
    @Mock
    private AppDocumentationMetadataAudJPARepository appDocumentationMetadataAudJPARepository;
    @Mock
    private AppDocumentationMetadataMapper appDocumentationMetadataMapper;

    @InjectMocks
    private AppDocumentationMetadataRepositoryAdapter adapter;

    private AppDocumentationMetadataEntity entity;
    private AppDocumentationMetadata model;

    @BeforeEach
    void setUp() {
        AppDocumentationAttachmentVersionEntity attachment = new AppDocumentationAttachmentVersionEntity();
        attachment.setId(100L);
        entity = new AppDocumentationMetadataEntity();
        entity.setId(1L);
        entity.setAppDocumentationAttachmentVersion(attachment);
        entity.setMetaKey("key1");
        entity.setMetaValue("value1");
        model = new AppDocumentationMetadata();
        model.setId(1L);
        model.setAppDocumentationAttachmentVersionId(100L);
        lenient().when(appDocumentationMetadataJPARepository.getReferenceById(1L)).thenReturn(entity);
        lenient().when(appDocumentationMetadataJPARepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void deleteAllByAttachmentIds_deletesTheActiveMetadataOfThoseAttachmentsAndAuditsIt() {
        List<AppDocumentationMetadataEntity> found = List.of(entity);
        when(appDocumentationMetadataJPARepository.findByAppDocumentationAttachmentVersionIdInAndDeletedAtIsNullOrderByMetaKeyAscIdAsc(List.of(100L))).thenReturn(found);
        when(appDocumentationMetadataMapper.toModelList(found)).thenReturn(List.of(model));

        adapter.deleteAllByAttachmentIds(List.of(100L), DELETED_AT, "admin");

        assertEquals(DELETED_AT, entity.getDeletedAt());
        assertEquals("admin", entity.getDeletedBy());
        ArgumentCaptor<AppDocumentationMetadataAudEntity> captor = ArgumentCaptor.forClass(AppDocumentationMetadataAudEntity.class);
        verify(appDocumentationMetadataAudJPARepository).save(captor.capture());
        AppDocumentationMetadataAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertEquals(1L, aud.getAppDocumentationMetadataId());
        assertEquals(100L, aud.getAppDocumentationAttachmentVersionId());
        assertEquals("key1", aud.getMetaKey());
        assertEquals(DELETED_AT, aud.getDeletedAt());
        assertNotNull(aud.getAuditDate());
    }

    @Test
    void deleteAllByAttachmentIds_withNoAttachments_deletesNothingAndAsksNothing() {
        adapter.deleteAllByAttachmentIds(List.of(), DELETED_AT, "admin");

        verify(appDocumentationMetadataJPARepository, never()).findByAppDocumentationAttachmentVersionIdInAndDeletedAtIsNullOrderByMetaKeyAscIdAsc(any());
        verify(appDocumentationMetadataAudJPARepository, never()).save(any());
    }

    @Test
    void findActiveByAttachmentIds_withNoAttachments_returnsNothingWithoutQuerying() {
        assertTrue(adapter.findActiveByAttachmentIds(List.of()).isEmpty());

        verify(appDocumentationMetadataJPARepository, never()).findByAppDocumentationAttachmentVersionIdInAndDeletedAtIsNullOrderByMetaKeyAscIdAsc(any());
    }

    @Test
    void findDeletedWithDocumentationAttachment_asksForTheMetadataDeletedAtThatMoment() {
        List<AppDocumentationMetadataEntity> found = List.of(entity);
        when(appDocumentationMetadataJPARepository.findByAppDocumentationAttachmentVersionIdInAndDeletedAtOrderByMetaKeyAscIdAsc(List.of(100L), DELETED_AT)).thenReturn(found);
        when(appDocumentationMetadataMapper.toModelList(found)).thenReturn(List.of(model));

        assertEquals(List.of(model), adapter.findDeletedWithDocumentationAttachment(List.of(100L), DELETED_AT));
    }

    @Test
    void findDeletedWithDocumentationAttachment_withNoAttachments_returnsNothingWithoutQuerying() {
        assertTrue(adapter.findDeletedWithDocumentationAttachment(List.of(), DELETED_AT).isEmpty());

        verify(appDocumentationMetadataJPARepository, never()).findByAppDocumentationAttachmentVersionIdInAndDeletedAtOrderByMetaKeyAscIdAsc(any(), any());
    }
}
