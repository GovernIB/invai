package es.caib.invai.back.persistence.repository.application.documentation.attachment;

import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentVersionAudEntity;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentVersionEntity;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationAttachmentVersion;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentEntity;
import es.caib.invai.back.persistence.model.catalog.mimeType.LkupMimeTypeEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the access registration of {@link AppDocumentationAttachmentVersionRepositoryAdapter}:
 * a download or a view leaves a row in the audit history of the attachment and changes nothing
 * else.
 */
@ExtendWith(MockitoExtension.class)
class AppDocumentationAttachmentVersionRepositoryAdapterTest {

    @Mock
    private EntityManager entityManager;
    @Mock
    private AppDocumentationAttachmentVersionJPARepository appDocumentationAttachmentVersionJPARepository;
    @Mock
    private AppDocumentationAttachmentVersionAudJPARepository appDocumentationAttachmentVersionAudJPARepository;

    @InjectMocks
    private AppDocumentationAttachmentVersionRepositoryAdapter adapter;

    private AppDocumentationAttachmentVersionEntity attachment;

    @BeforeEach
    void setUp() {
        AppDocumentationAttachmentEntity document = new AppDocumentationAttachmentEntity();
        document.setId(10L);
        LkupMimeTypeEntity pdf = new LkupMimeTypeEntity();
        pdf.setId(1L);
        attachment = new AppDocumentationAttachmentVersionEntity();
        attachment.setId(100L);
        attachment.setAppDocumentationAttachment(document);
        attachment.setMimeType(pdf);
        attachment.setVersionLabel("2.1");
        attachment.setFileName("manual.pdf");
        attachment.setFileSize(2048L);
        attachment.setChecksumSha256("a".repeat(64));
        attachment.setCurrent(true);
        attachment.setCreatedAt(LocalDateTime.of(2025, 11, 18, 10, 0));
        attachment.setCreatedBy("jgarcia@gov.com");
        when(appDocumentationAttachmentVersionJPARepository.getReferenceById(100L)).thenReturn(attachment);
        lenient().when(appDocumentationAttachmentVersionJPARepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private AppDocumentationAttachmentVersionAudEntity registered() {
        ArgumentCaptor<AppDocumentationAttachmentVersionAudEntity> captor = ArgumentCaptor.forClass(AppDocumentationAttachmentVersionAudEntity.class);
        verify(appDocumentationAttachmentVersionAudJPARepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void registerDownload_aDownload_leavesADownloadRowWithTheStateOfTheAttachment() {
        adapter.registerDownload(100L, false);

        AppDocumentationAttachmentVersionAudEntity aud = registered();
        assertEquals("DOWNLOAD", aud.getAudAction());
        assertEquals(100L, aud.getAppDocumentationAttachmentVersionId());
        assertEquals(10L, aud.getAppDocumentationAttachmentId());
        assertEquals(1L, aud.getMimeTypeId());
        assertEquals("2.1", aud.getVersionLabel());
        assertEquals("manual.pdf", aud.getFileName());
        assertNotNull(aud.getAuditDate());
        assertNotNull(aud.getAuditUser());
    }

    @Test
    void registerDownload_aViewInTheBrowser_leavesAViewRow() {
        adapter.registerDownload(100L, true);

        assertEquals("VIEW", registered().getAudAction());
    }

    @Test
    void registerDownload_doesNotModifyTheAttachment() {
        adapter.registerDownload(100L, false);

        verify(appDocumentationAttachmentVersionJPARepository, never()).save(any());
        verify(appDocumentationAttachmentVersionJPARepository, never()).saveAndFlush(any());
    }

    @Test
    void delete_softDeletesTheAttachmentAndItStopsBeingTheCurrentOne() {
        AppDocumentationAttachmentVersion model = new AppDocumentationAttachmentVersion();
        model.setId(100L);
        model.setDeletedAt(LocalDateTime.of(2026, 10, 20, 12, 0));
        model.setDeletedBy("admin");

        adapter.delete(model);

        assertEquals(LocalDateTime.of(2026, 10, 20, 12, 0), attachment.getDeletedAt());
        assertEquals("admin", attachment.getDeletedBy());
        assertFalse(attachment.isCurrent());
        assertEquals("DELETE", registered().getAudAction());
    }

    @Test
    void markCurrent_makesTheAttachmentTheCurrentOneAndAuditsTheChange() {
        attachment.setCurrent(false);

        adapter.markCurrent(100L);

        assertTrue(attachment.isCurrent());
        AppDocumentationAttachmentVersionAudEntity aud = registered();
        assertEquals("UPDATE", aud.getAudAction());
        assertEquals(100L, aud.getAppDocumentationAttachmentVersionId());
    }
}
