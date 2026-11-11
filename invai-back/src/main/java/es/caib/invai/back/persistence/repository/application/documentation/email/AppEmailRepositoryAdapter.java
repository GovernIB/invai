package es.caib.invai.back.persistence.repository.application.documentation.email;

import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentVersionEntity;
import es.caib.invai.back.persistence.model.application.documentation.email.AppEmailEntity;
import es.caib.invai.back.persistence.model.application.documentation.email.AppEmailItemEntity;
import es.caib.invai.back.persistence.model.application.documentation.email.AppEmailRecipientEntity;
import es.caib.invai.back.service.model.application.documentation.email.AppEmail;
import es.caib.invai.back.service.model.application.documentation.email.AppEmailItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

/**
 * Infrastructure repository adapter implementing the outbound port {@link AppEmailRepository}.
 * The history is written once and never read back by the application, so there is no mapper: the
 * entities are built straight from the domain model.
 *
 * @since 1.0.7
 */
@Repository
@Slf4j
public class AppEmailRepositoryAdapter implements AppEmailRepository {

    /** Entity manager used to build the references to the application, and the versions. */
    @PersistenceContext
    private EntityManager entityManager;

    /** Spring Data JPA repository used to persist {@link AppEmailEntity} records. */
    @Autowired
    private AppEmailJPARepository appEmailJPARepository;

    @Override
    public AppEmail create(AppEmail email) {
        log.debug("Repository: Persisting sent e-mail of application ID: {} into the history", email.getApplicationId());
        try {
            AppEmailEntity entity = new AppEmailEntity();
            entity.setApplication(entityManager.getReference(ApplicationEntity.class, email.getApplicationId()));
            entity.setSenderEmail(email.getSenderEmail());
            entity.setSubject(email.getSubject());
            entity.setMessage(email.getMessage());
            for (String address : email.getRecipients()) {
                AppEmailRecipientEntity recipient = new AppEmailRecipientEntity();
                recipient.setAppEmail(entity);
                recipient.setRecipientEmail(address);
                entity.getRecipients().add(recipient);
            }
            for (AppEmailItem item : email.getItems()) {
                entity.getItems().add(toItemEntity(item, entity));
            }
            entity = appEmailJPARepository.saveAndFlush(entity);
            email.setId(entity.getId());
            return email;
        } catch (DataAccessException e) {
            log.error("Repository error: Failure persisting sent e-mail of application ID: {} into the history", email.getApplicationId(), e);
            throw e;
        }
    }

    /**
     * Builds the entity of an attachment of the e-mail.
     *
     * @param item  the attachment
     * @param email the entity of the e-mail it belongs to
     * @return the entity of the item
     */
    private AppEmailItemEntity toItemEntity(AppEmailItem item, AppEmailEntity email) {
        AppEmailItemEntity entity = new AppEmailItemEntity();
        entity.setAppEmail(email);
        entity.setVersion(entityManager.getReference(AppDocumentationAttachmentVersionEntity.class, item.getVersionId()));
        entity.setTitle(item.getTitle());
        entity.setVersionLabel(item.getVersionLabel());
        entity.setFileName(item.getFileName());
        return entity;
    }
}
