package es.caib.invai.back.persistence.model.application.documentation.email;

import es.caib.invai.back.persistence.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * JPA persistent entity representing one address an e-mail of the history was sent to.
 *
 * @since 1.0.7
 */
@Entity
@Table(name = "INV_APP_DOC_EMAIL_RECIPIENT")
@Getter
@Setter
public class AppEmailRecipientEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique auto-generated database identifier of the recipient. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_DOC_EML_RCPT_SEQ")
    @SequenceGenerator(name = "INV_APP_DOC_EML_RCPT_SEQ", sequenceName = "INV_APP_DOC_EML_RCPT_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Sent e-mail the address belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_DOC_EMAIL_ID", nullable = false)
    private AppEmailEntity appEmail;

    /** E-mail address the message was sent to. */
    @Column(name = "RECIPIENT_EMAIL", nullable = false, length = 320)
    private String recipientEmail;
}
