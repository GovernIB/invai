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
import es.caib.invai.back.service.facade.application.documentation.email.AppDocumentationEmailService;
import es.caib.invai.back.service.facade.maintenance.documentation.documentRolePermission.DocumentRolePermissionService;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationAttachment;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationAttachmentVersion;
import es.caib.invai.back.service.model.application.documentation.email.AppEmail;
import es.caib.invai.back.service.model.application.documentation.email.AppEmailItem;
import es.caib.invai.back.service.model.application.documentation.email.ChosenAttachment;
import es.caib.invai.back.service.model.application.documentation.email.MailAttachment;
import es.caib.invai.back.service.model.maintenance.documentation.documentRolePermission.DocumentPermission;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Facade service implementation for sending by e-mail documentation of an application: the files,
 * a specific version of each, chosen in the Documentació tab. The message is sent
 * from the address of the application, with the user who asks for it as Reply-To, synchronously,
 * and every sending is kept in the history together with what it carried.
 *
 * @since 1.0.7
 */
@Service
@Slf4j
@Transactional
public class AppDocumentationEmailServiceFacadeBean implements AppDocumentationEmailService {

    /** Repository port used to check that the application exists. */
    @Autowired
    private ApplicationRepository applicationRepository;

    /** Repository port used to find the documents to attach. */
    @Autowired
    private AppDocumentationAttachmentRepository appDocumentationAttachmentRepository;

    /** Repository port used to find the versions of the documents and read their files. */
    @Autowired
    private AppDocumentationAttachmentVersionRepository appDocumentationAttachmentVersionRepository;

    /** Repository port used to keep the sendings in the history. */
    @Autowired
    private AppEmailRepository appEmailRepository;

    /** Component that hands the messages to the mail server. */
    @Autowired
    private AppMailSender appMailSender;

    /** Service used to check that the logged user can read the categories of the documents to send. */
    @Autowired
    private DocumentRolePermissionService documentRolePermissionService;

    /**
     * Sends by e-mail the chosen attachments of an application and keeps the sending in
     * the history. The history is written before the message is sent, in the same transaction, which is
     * rolled back if the sending fails, so a sending that fails leaves no trace in it.
     *
     * @param inputDTO the recipients, subject, optional message, and the attachments to send
     * @throws BusinessRuleException if the application, a document or a version doesn't exist or
     *                               doesn't belong to the application, is deleted, nothing is selected,
     *                               the attachments are too large, or the address of the sender is unknown
     * @throws AccessDeniedException if the user can't read the category of a document
     * @throws EmailSendException    if the message can't be handed to the mail server; the history is rolled back
     */
    @Override
    @Transactional(rollbackFor = EmailSendException.class)
    public void send(AppDocumentationEmailInputDTO inputDTO) {
        log.debug("Facade: Sending by e-mail documentation of application ID: {}", inputDTO.getApplicationId());

        Utils.sanitize(inputDTO);

        Long applicationId = inputDTO.getApplicationId();
        if (Objects.isNull(applicationRepository.findById(applicationId))) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_FOUND);
        }
        List<AppDocumentationEmailAttachmentInputDTO> attachmentInputs = Objects.requireNonNullElse(inputDTO.getAttachments(), List.of());
        if (attachmentInputs.isEmpty()) {
            throw new BusinessRuleException(Constants.ERR_EMAIL_NO_ITEMS);
        }

        AppEmail email = new AppEmail();
        email.setApplicationId(applicationId);
        email.setSubject(inputDTO.getSubject());
        email.setMessage(StringUtils.isBlank(inputDTO.getMessage()) ? null : inputDTO.getMessage());
        email.setRecipients(parseRecipients(inputDTO.getRecipients()));

        List<ChosenAttachment> chosenAttachments = findAttachments(applicationId, attachmentInputs);
        chosenAttachments.forEach(chosen -> email.getItems().add(chosen.item()));

        email.setSenderEmail(resolveSenderEmail());

        List<MailAttachment> mailAttachments = readFiles(chosenAttachments);
        appEmailRepository.create(email);
        appMailSender.send(email.getSenderEmail(), email.getRecipients(), email.getSubject(), StringUtils.defaultString(email.getMessage()), mailAttachments);
    }

    /**
     * Finds the chosen versions of the documents, checking that each document is an active one of
     * the application and each version an active one of its document, and that the files together
     * are not too large to be sent. A version chosen more than once counts once. The user needs
     * the read permission on the category of every document, as to download it.
     *
     * @param applicationId identifier of the application
     * @param inputs        the chosen documents and versions
     * @return the chosen versions with their item of the history, in the order they were chosen
     * @throws BusinessRuleException if a document or version is not found, deleted, or of another application or document,
     *                               or the files exceed the maximum total size
     * @throws AccessDeniedException if the user can't read the category of a document
     */
    private List<ChosenAttachment> findAttachments(Long applicationId, List<AppDocumentationEmailAttachmentInputDTO> inputs) {
        List<ChosenAttachment> chosen = new ArrayList<>();
        Set<Long> versionIds = new HashSet<>();
        long totalSize = 0;

        for (AppDocumentationEmailAttachmentInputDTO input : inputs) {
            if (!versionIds.add(input.getVersionId())) {
                continue;
            }

            AppDocumentationAttachment document = appDocumentationAttachmentRepository.findById(input.getDocumentId());
            if (Objects.isNull(document) || Objects.nonNull(document.getDeletedAt()) || !Objects.equals(document.getApplicationId(), applicationId)) {
                throw new BusinessRuleException(Constants.ERR_DOCUMENTATION_NOT_FOUND);
            }
            documentRolePermissionService.requirePermission(DocumentPermission.READ, document.getCategory().getId());
            AppDocumentationAttachmentVersion version = appDocumentationAttachmentVersionRepository.findById(input.getVersionId());
            if (Objects.isNull(version) || Objects.nonNull(version.getDeletedAt()) || !Objects.equals(version.getAppDocumentationAttachmentId(), document.getId())) {
                throw new BusinessRuleException(Constants.ERR_DOCUMENTATION_ATTACHMENT_NOT_FOUND);
            }
            totalSize += version.getFileSize();

            if (totalSize > Constants.EMAIL_MAX_ATTACHMENTS_SIZE_BYTES) {
                throw new BusinessRuleException(Constants.ERR_EMAIL_ATTACHMENTS_TOO_LARGE);
            }

            AppEmailItem item = new AppEmailItem();
            item.setVersionId(version.getId());
            item.setTitle(document.getTitle());
            item.setVersionLabel(version.getVersionLabel());
            item.setFileName(version.getFileName());
            chosen.add(new ChosenAttachment(item, version));
        }
        return chosen;
    }

    /**
     * Reads the files of the chosen versions. It runs once the total size is known to be allowed, so
     * a request that is too large never loads them.
     *
     * @param chosenAttachments the chosen versions
     * @return the files, ready to be attached
     */
    private List<MailAttachment> readFiles(List<ChosenAttachment> chosenAttachments) {
        List<MailAttachment> files = new ArrayList<>();
        for (ChosenAttachment chosen : chosenAttachments) {
            AppDocumentationAttachmentVersion version = chosen.version();
            files.add(new MailAttachment(version.getFileName(), version.getMimeType().getMimeType(),
                    appDocumentationAttachmentVersionRepository.findContentById(version.getId())));
        }
        return files;
    }

    /**
     * Removes the repeated addresses, ignoring case: every address is kept trimmed and in lower case.
     *
     * @param recipients the addresses typed in
     * @return the addresses without repetitions
     */
    private Set<String> parseRecipients(List<String> recipients) {
        return recipients.stream()
                .map(recipient -> recipient.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    /**
     * Finds the e-mail address of the user who sends the message: the one in their session.
     *
     * @return the address of the sender
     * @throws BusinessRuleException if the session carries none
     */
    private String resolveSenderEmail() {
        String senderEmail = Utils.resolveCurrentUserEmail();
        if (StringUtils.isBlank(senderEmail)) {
            throw new BusinessRuleException(Constants.ERR_EMAIL_SENDER_NOT_FOUND);
        }
        return senderEmail;
    }
}
