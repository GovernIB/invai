package es.caib.invai.back.persistence.repository.application.documentation.email;

import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentVersionEntity;
import es.caib.invai.back.persistence.model.application.documentation.email.AppEmailEntity;
import es.caib.invai.back.persistence.model.application.documentation.email.AppEmailItemEntity;
import es.caib.invai.back.service.model.application.documentation.email.AppEmail;
import es.caib.invai.back.service.model.application.documentation.email.AppEmailItem;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppEmailRepositoryAdapter}: a sending is kept in a single save with its
 * recipients and attachments, and the related rows are only referenced, never loaded.
 */
@ExtendWith(MockitoExtension.class)
class AppEmailRepositoryAdapterTest {

    @Mock
    private EntityManager entityManager;
    @Mock
    private AppEmailJPARepository appEmailJPARepository;

    @InjectMocks
    private AppEmailRepositoryAdapter adapter;

    private static AppEmailItem attachmentItem() {
        AppEmailItem item = new AppEmailItem();
        item.setVersionId(70L);
        item.setTitle("Manual");
        item.setVersionLabel("2.1");
        item.setFileName("manual.pdf");
        return item;
    }

    @Test
    void create_savesTheSendingWithRecipientsAndAttachmentsAndReturnsTheGeneratedId() {
        ApplicationEntity application = new ApplicationEntity();
        AppDocumentationAttachmentVersionEntity version = new AppDocumentationAttachmentVersionEntity();
        when(entityManager.getReference(ApplicationEntity.class, 3L)).thenReturn(application);
        when(entityManager.getReference(AppDocumentationAttachmentVersionEntity.class, 70L)).thenReturn(version);
        when(appEmailJPARepository.saveAndFlush(any(AppEmailEntity.class))).thenAnswer(invocation -> {
            AppEmailEntity saved = invocation.getArgument(0);
            saved.setId(55L);
            return saved;
        });
        AppEmail email = new AppEmail();
        email.setApplicationId(3L);
        email.setSenderEmail("sender@caib.es");
        email.setSubject("Documentacio");
        email.setMessage("Hola");
        email.setRecipients(new LinkedHashSet<>(List.of("a@caib.es", "b@caib.es")));
        email.getItems().add(attachmentItem());

        AppEmail result = adapter.create(email);

        assertSame(email, result);
        assertEquals(55L, result.getId());
        ArgumentCaptor<AppEmailEntity> captor = ArgumentCaptor.forClass(AppEmailEntity.class);
        verify(appEmailJPARepository).saveAndFlush(captor.capture());
        AppEmailEntity entity = captor.getValue();
        assertSame(application, entity.getApplication());
        assertEquals("sender@caib.es", entity.getSenderEmail());
        assertEquals("Documentacio", entity.getSubject());
        assertEquals("Hola", entity.getMessage());

        assertEquals(2, entity.getRecipients().size());
        assertEquals("a@caib.es", entity.getRecipients().get(0).getRecipientEmail());
        assertEquals("b@caib.es", entity.getRecipients().get(1).getRecipientEmail());
        entity.getRecipients().forEach(recipient -> assertSame(entity, recipient.getAppEmail()));

        assertEquals(1, entity.getItems().size());
        AppEmailItemEntity attachment = entity.getItems().get(0);
        assertSame(entity, attachment.getAppEmail());
        assertSame(version, attachment.getVersion());
        assertEquals("Manual", attachment.getTitle());
        assertEquals("2.1", attachment.getVersionLabel());
        assertEquals("manual.pdf", attachment.getFileName());
    }

    @Test
    void create_databaseFailure_isPropagated() {
        when(entityManager.getReference(ApplicationEntity.class, 3L)).thenReturn(new ApplicationEntity());
        DataAccessException failure = new DataAccessResourceFailureException("database down");
        when(appEmailJPARepository.saveAndFlush(any(AppEmailEntity.class))).thenThrow(failure);
        AppEmail email = new AppEmail();
        email.setApplicationId(3L);
        email.setRecipients(new LinkedHashSet<>(List.of("a@caib.es")));

        assertSame(failure, assertThrows(DataAccessException.class, () -> adapter.create(email)));
    }

    @Test
    void create_withoutMessageNorItems_savesJustTheSending() {
        when(entityManager.getReference(ApplicationEntity.class, 3L)).thenReturn(new ApplicationEntity());
        when(appEmailJPARepository.saveAndFlush(any(AppEmailEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AppEmail email = new AppEmail();
        email.setApplicationId(3L);
        email.setSenderEmail("sender@caib.es");
        email.setSubject("Assumpte");
        email.setRecipients(new LinkedHashSet<>(List.of("a@caib.es")));

        adapter.create(email);

        ArgumentCaptor<AppEmailEntity> captor = ArgumentCaptor.forClass(AppEmailEntity.class);
        verify(appEmailJPARepository).saveAndFlush(captor.capture());
        assertNull(captor.getValue().getMessage());
        assertEquals(1, captor.getValue().getRecipients().size());
        assertEquals(0, captor.getValue().getItems().size());
    }
}
