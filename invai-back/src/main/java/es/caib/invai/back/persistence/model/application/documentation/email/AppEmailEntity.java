package es.caib.invai.back.persistence.model.application.documentation.email;

import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA persistent entity representing an e-mail sent from the Documentació tab of an application. It
 * is created when the e-mail is sent and never changed or deleted: it is the history of the sendings.
 *
 * @since 1.0.7
 */
@Entity
@Table(name = "INV_APP_DOC_EMAIL")
@Getter
@Setter
public class AppEmailEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique auto-generated database identifier of the e-mail. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_DOC_EMAIL_SEQ")
    @SequenceGenerator(name = "INV_APP_DOC_EMAIL_SEQ", sequenceName = "INV_APP_DOC_EMAIL_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Application the documentation was sent from. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APPLICATION_ID", nullable = false)
    private ApplicationEntity application;

    /** E-mail address of the user who sent it, used as the Reply-To of the message. */
    @Column(name = "SENDER_EMAIL", nullable = false, length = 320)
    private String senderEmail;

    /** Subject of the message. */
    @Column(name = "SUBJECT", nullable = false, length = 250)
    private String subject;

    /** Optional text the user wrote in the message. */
    @Column(name = "MESSAGE", length = 4000)
    private String message;

    /** Addresses the message was sent to. */
    @OneToMany(mappedBy = "appEmail", cascade = CascadeType.ALL)
    private List<AppEmailRecipientEntity> recipients = new ArrayList<>();

    /** Attachments the message carries. */
    @OneToMany(mappedBy = "appEmail", cascade = CascadeType.ALL)
    private List<AppEmailItemEntity> items = new ArrayList<>();
}
