package es.caib.invai.back.ejb.application.documentation;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.exception.EmailSendException;
import es.caib.invai.back.interna.application.documentation.email.DTO.AppDocumentationEmailAttachmentInputDTO;
import es.caib.invai.back.interna.application.documentation.email.DTO.AppDocumentationEmailInputDTO;
import es.caib.invai.back.mail.AppMailSender;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.persistence.repository.application.documentation.attachment.AppDocumentationAttachmentRepository;
import es.caib.invai.back.persistence.repository.application.documentation.attachment.AppDocumentationAttachmentVersionRepository;
import es.caib.invai.back.persistence.repository.application.documentation.email.AppEmailRepository;
import es.caib.invai.back.service.facade.maintenance.documentation.documentRolePermission.DocumentRolePermissionService;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationAttachment;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationAttachmentVersion;
import es.caib.invai.back.service.model.application.documentation.email.AppEmail;
import es.caib.invai.back.service.model.application.documentation.email.AppEmailItem;
import es.caib.invai.back.service.model.application.documentation.email.MailAttachment;
import es.caib.invai.back.service.model.catalog.mimeType.MimeType;
import es.caib.invai.back.service.model.maintenance.documentation.documentCategory.DocumentCategory;
import es.caib.invai.back.service.model.maintenance.documentation.documentRolePermission.DocumentPermission;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppDocumentationEmailServiceFacadeBean}, with every collaborator mocked.
 */
@ExtendWith(MockitoExtension.class)
class AppDocumentationEmailServiceFacadeBeanTest {

    private static final Long APP_ID = 3L;
    private static final Long CATEGORY_ID = 5L;
    private static final String SENDER = "sender@caib.es";

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private AppDocumentationAttachmentRepository attachmentRepository;
    @Mock
    private AppDocumentationAttachmentVersionRepository versionRepository;
    @Mock
    private AppEmailRepository appEmailRepository;
    @Mock
    private AppMailSender appMailSender;
    @Mock
    private DocumentRolePermissionService documentRolePermissionService;

    @Captor
    private ArgumentCaptor<List<MailAttachment>> filesCaptor;

    @InjectMocks
    private AppDocumentationEmailServiceFacadeBean facade;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ------------------------------------------------------------------
    // fixtures
    // ------------------------------------------------------------------

    private static void loginAs(String username, String email) {
        OidcIdToken idToken = new OidcIdToken("token", Instant.now(), Instant.now().plusSeconds(60), Map.of("sub", "1"));
        Map<String, Object> claims = new HashMap<>();
        claims.put("preferred_username", username);
        if (email != null) {
            claims.put("email", email);
        }
        DefaultOidcUser user = new DefaultOidcUser(List.of(new SimpleGrantedAuthority("ROLE_USER")), idToken, new OidcUserInfo(claims));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    private static AppDocumentationEmailInputDTO input(List<AppDocumentationEmailAttachmentInputDTO> attachments) {
        return new AppDocumentationEmailInputDTO(APP_ID, new ArrayList<>(List.of("  A@Caib.es ", "a@caib.es", "b@caib.es")),
                "  Documentacio  ", "  Hola  ", attachments);
    }

    private static AppDocumentationAttachment document(Long id, Long applicationId) {
        AppDocumentationAttachment document = new AppDocumentationAttachment();
        document.setId(id);
        document.setApplicationId(applicationId);
        document.setTitle("Manual " + id);
        DocumentCategory category = new DocumentCategory();
        category.setId(CATEGORY_ID);
        document.setCategory(category);
        return document;
    }

    private static AppDocumentationAttachmentVersion version(Long id, Long documentId, long size) {
        AppDocumentationAttachmentVersion version = new AppDocumentationAttachmentVersion();
        version.setId(id);
        version.setAppDocumentationAttachmentId(documentId);
        version.setVersionLabel("1." + id);
        version.setFileName("manual" + id + ".pdf");
        version.setFileSize(size);
        MimeType mimeType = new MimeType();
        mimeType.setMimeType("application/pdf");
        version.setMimeType(mimeType);
        return version;
    }

    private static List<AppDocumentationEmailAttachmentInputDTO> attachments(Long documentId, Long versionId) {
        return List.of(new AppDocumentationEmailAttachmentInputDTO(documentId, versionId));
    }

    private void applicationExists() {
        when(applicationRepository.findById(APP_ID)).thenReturn(new Application());
    }

    private void documentWithVersion(Long documentId, Long versionId, long size) {
        when(attachmentRepository.findById(documentId)).thenReturn(document(documentId, APP_ID));
        when(versionRepository.findById(versionId)).thenReturn(version(versionId, documentId, size));
    }

    // ------------------------------------------------------------------
    // send: happy paths
    // ------------------------------------------------------------------

    @Test
    void send_attachment_keepsHistoryThenSendsFromTheLoggedUser() {
        loginAs("u1", SENDER);
        applicationExists();
        documentWithVersion(7L, 70L, 1024);
        when(versionRepository.findContentById(70L)).thenReturn(new byte[]{1, 2, 3});

        facade.send(input(attachments(7L, 70L)));

        verify(documentRolePermissionService).requirePermission(DocumentPermission.READ, CATEGORY_ID);
        ArgumentCaptor<AppEmail> emailCaptor = ArgumentCaptor.forClass(AppEmail.class);
        InOrder order = inOrder(appEmailRepository, appMailSender);
        order.verify(appEmailRepository).create(emailCaptor.capture());
        order.verify(appMailSender).send(eq(SENDER), anySet(), anyString(), anyString(), filesCaptor.capture());

        AppEmail email = emailCaptor.getValue();
        assertEquals(APP_ID, email.getApplicationId());
        assertEquals(SENDER, email.getSenderEmail());
        assertEquals("Documentacio", email.getSubject());
        assertEquals("Hola", email.getMessage());
        assertEquals(Set.of("a@caib.es", "b@caib.es"), email.getRecipients());
        assertEquals(1, email.getItems().size());

        AppEmailItem attachmentItem = email.getItems().get(0);
        assertEquals(70L, attachmentItem.getVersionId());
        assertEquals("Manual 7", attachmentItem.getTitle());
        assertEquals("1.70", attachmentItem.getVersionLabel());
        assertEquals("manual70.pdf", attachmentItem.getFileName());

        List<MailAttachment> files = filesCaptor.getValue();
        assertEquals(1, files.size());
        assertEquals("manual70.pdf", files.get(0).fileName());
        assertEquals("application/pdf", files.get(0).mimeType());
        assertEquals(3, files.get(0).content().length);
    }

    @Test
    void send_noMessage_bodyIsEmptyAndMessageIsNotKept() {
        loginAs("u1", SENDER);
        applicationExists();
        documentWithVersion(7L, 70L, 10);
        when(versionRepository.findContentById(70L)).thenReturn(new byte[]{1});
        AppDocumentationEmailInputDTO dto = input(attachments(7L, 70L));
        dto.setMessage("   ");

        facade.send(dto);

        ArgumentCaptor<AppEmail> email = ArgumentCaptor.forClass(AppEmail.class);
        verify(appEmailRepository).create(email.capture());
        assertNull(email.getValue().getMessage());
        verify(appMailSender).send(any(), anySet(), any(), eq(""), anyList());
    }

    @Test
    void send_sameVersionRepeated_countsOnce() {
        loginAs("u1", SENDER);
        applicationExists();
        documentWithVersion(7L, 70L, 10);
        when(versionRepository.findContentById(70L)).thenReturn(new byte[]{1});
        List<AppDocumentationEmailAttachmentInputDTO> twice = List.of(
                new AppDocumentationEmailAttachmentInputDTO(7L, 70L), new AppDocumentationEmailAttachmentInputDTO(7L, 70L));

        facade.send(input(twice));

        ArgumentCaptor<AppEmail> email = ArgumentCaptor.forClass(AppEmail.class);
        verify(appEmailRepository).create(email.capture());
        assertEquals(1, email.getValue().getItems().size());
        verify(attachmentRepository).findById(7L);
    }

    // ------------------------------------------------------------------
    // send: sender address
    // ------------------------------------------------------------------

    @Test
    void send_noAddressInSession_throwsAndSendsNothing() {
        loginAs("u1", null);
        applicationExists();
        documentWithVersion(7L, 70L, 10);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> facade.send(input(attachments(7L, 70L))));

        assertEquals(Constants.ERR_EMAIL_SENDER_NOT_FOUND, ex.getMessage());
        verifyNoInteractions(appEmailRepository, appMailSender);
    }

    // ------------------------------------------------------------------
    // send: validations
    // ------------------------------------------------------------------

    @Test
    void send_applicationNotFound_throws() {
        when(applicationRepository.findById(APP_ID)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> facade.send(input(attachments(7L, 70L))));

        assertEquals(Constants.ERR_APP_NOT_FOUND, ex.getMessage());
        verifyNoInteractions(appEmailRepository, appMailSender);
    }

    @Test
    void send_nothingSelected_throws() {
        applicationExists();

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> facade.send(input(List.of())));

        assertEquals(Constants.ERR_EMAIL_NO_ITEMS, ex.getMessage());
        verifyNoInteractions(appEmailRepository, appMailSender);
    }

    @Test
    void send_documentNotFound_throws() {
        applicationExists();
        when(attachmentRepository.findById(7L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> facade.send(input(attachments(7L, 70L))));

        assertEquals(Constants.ERR_DOCUMENTATION_NOT_FOUND, ex.getMessage());
        verifyNoInteractions(appEmailRepository, appMailSender);
    }

    @Test
    void send_documentDeleted_throws() {
        applicationExists();
        AppDocumentationAttachment deleted = document(7L, APP_ID);
        deleted.setDeletedAt(LocalDateTime.now());
        when(attachmentRepository.findById(7L)).thenReturn(deleted);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> facade.send(input(attachments(7L, 70L))));

        assertEquals(Constants.ERR_DOCUMENTATION_NOT_FOUND, ex.getMessage());
    }

    @Test
    void send_documentOfAnotherApplication_throws() {
        applicationExists();
        when(attachmentRepository.findById(7L)).thenReturn(document(7L, 99L));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> facade.send(input(attachments(7L, 70L))));

        assertEquals(Constants.ERR_DOCUMENTATION_NOT_FOUND, ex.getMessage());
        verifyNoInteractions(appEmailRepository, appMailSender);
    }

    @Test
    void send_noReadPermissionOnTheCategory_throwsAndSendsNothing() {
        applicationExists();
        when(attachmentRepository.findById(7L)).thenReturn(document(7L, APP_ID));
        doThrow(new AccessDeniedException("denied")).when(documentRolePermissionService)
                .requirePermission(DocumentPermission.READ, CATEGORY_ID);

        assertThrows(AccessDeniedException.class, () -> facade.send(input(attachments(7L, 70L))));

        verify(versionRepository, never()).findById(any());
        verifyNoInteractions(appEmailRepository, appMailSender);
    }

    @Test
    void send_documentsOfAnotherApplication_dontReachThePermissionCheck() {
        applicationExists();
        when(attachmentRepository.findById(7L)).thenReturn(document(7L, 99L));

        assertThrows(BusinessRuleException.class, () -> facade.send(input(attachments(7L, 70L))));

        verifyNoInteractions(documentRolePermissionService);
    }

    @Test
    void send_versionNotFound_throws() {
        applicationExists();
        when(attachmentRepository.findById(7L)).thenReturn(document(7L, APP_ID));
        when(versionRepository.findById(70L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> facade.send(input(attachments(7L, 70L))));

        assertEquals(Constants.ERR_DOCUMENTATION_ATTACHMENT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void send_versionDeleted_throws() {
        applicationExists();
        when(attachmentRepository.findById(7L)).thenReturn(document(7L, APP_ID));
        AppDocumentationAttachmentVersion deleted = version(70L, 7L, 10);
        deleted.setDeletedAt(LocalDateTime.now());
        when(versionRepository.findById(70L)).thenReturn(deleted);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> facade.send(input(attachments(7L, 70L))));

        assertEquals(Constants.ERR_DOCUMENTATION_ATTACHMENT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void send_versionOfAnotherDocument_throws() {
        applicationExists();
        when(attachmentRepository.findById(7L)).thenReturn(document(7L, APP_ID));
        when(versionRepository.findById(70L)).thenReturn(version(70L, 8L, 10));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> facade.send(input(attachments(7L, 70L))));

        assertEquals(Constants.ERR_DOCUMENTATION_ATTACHMENT_NOT_FOUND, ex.getMessage());
        verifyNoInteractions(appEmailRepository, appMailSender);
    }

    @Test
    void send_filesTooLargeTogether_throwsWithoutReadingThem() {
        applicationExists();
        long eightMb = 8L * 1024 * 1024;
        documentWithVersion(7L, 70L, eightMb);
        documentWithVersion(8L, 80L, eightMb);
        List<AppDocumentationEmailAttachmentInputDTO> both = List.of(
                new AppDocumentationEmailAttachmentInputDTO(7L, 70L), new AppDocumentationEmailAttachmentInputDTO(8L, 80L));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> facade.send(input(both)));

        assertEquals(Constants.ERR_EMAIL_ATTACHMENTS_TOO_LARGE, ex.getMessage());
        verify(versionRepository, never()).findContentById(any());
        verifyNoInteractions(appEmailRepository, appMailSender);
    }

    // ------------------------------------------------------------------
    // send: mail failure
    // ------------------------------------------------------------------

    @Test
    void send_mailServerFails_propagatesTheFailure() {
        loginAs("u1", SENDER);
        applicationExists();
        documentWithVersion(7L, 70L, 10);
        when(versionRepository.findContentById(70L)).thenReturn(new byte[]{1});
        EmailSendException failure = new EmailSendException(new IllegalStateException("smtp down"));
        doThrow(failure).when(appMailSender).send(any(), anySet(), any(), any(), anyList());

        EmailSendException thrown = assertThrows(EmailSendException.class,
                () -> facade.send(input(attachments(7L, 70L))));

        assertSame(failure, thrown);
    }

    @Test
    void send_isTransactionalAndRollsBackWhenTheMailCannotBeSent() throws NoSuchMethodException {
        Transactional transactional = AppDocumentationEmailServiceFacadeBean.class
                .getMethod("send", AppDocumentationEmailInputDTO.class).getAnnotation(Transactional.class);

        assertNotNull(transactional);
        assertTrue(Arrays.asList(transactional.rollbackFor()).contains(EmailSendException.class));
    }
}
