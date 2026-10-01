package es.caib.invai.back.persistence.model.application.security.webContext;

import lombok.*;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.application.development.core.AppDevelopmentEntity;
import es.caib.invai.back.persistence.model.application.security.core.AppSecurityEntity;
import es.caib.invai.back.persistence.model.maintenance.security.webContext.WebContextEntity;
import es.caib.invai.back.persistence.model.maintenance.general.field.FieldEntity;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * Persistent entity mapping an {@link AppSecurityEntity} to a {@link WebContextEntity}
 * assigned to it within a given functional {@link FieldEntity} scope. Dually anchored: it also
 * belongs to the owning application's {@link AppDevelopmentEntity} ("Desenvolupament" tab), which
 * is the only anchor from which the record can be created/edited/deleted - Security is read-only
 * plus the one-way {@code validated} flag below.
 *
 * @since 1.0.4
 */
@Entity
@Table(name = "INV_APP_WEB_CONTEXT")
@Getter
@Setter
public class AppWebContextEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identification pointer of this application-web context association. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_WEB_CONTEXT_SEQ")
    @SequenceGenerator(name = "INV_APP_WEB_CONTEXT_SEQ", sequenceName = "INV_APP_WEB_CONTEXT_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Security anchor this association belongs to. Read-only from Security's own controller. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_SECURITY_ID", nullable = false)
    private AppSecurityEntity appSecurity;

    /** Development anchor this association belongs to - the only anchor CRUD is exposed from. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_DEVELOPMENT_ID", nullable = false)
    private AppDevelopmentEntity appDevelopment;

    /** Web context catalog entry assigned to the security anchor. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WEB_CONTEXT_ID", nullable = false)
    private WebContextEntity webContext;

    /** Functional field/scope within which the web context is assigned. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FIELD_ID", nullable = false)
    private FieldEntity field;

    /** Free-text observation notes attached to this association. */
    @Lob
    @Column(name = "OBSERVATION")
    private String observation;

    /** URL of the web context. */
    @Column(name = "URL")
    private String url;

    /** Whether Security has validated this web context. One-way: never reset back to {@code false}. */
    @Column(name = "VALIDATED", nullable = false)
    private boolean validated;

    /** Timestamp at which Security validated this web context, or {@code null} if not yet validated. */
    @Column(name = "VALIDATED_AT")
    private LocalDateTime validatedAt;

    /** Identifier of the user who validated this web context, or {@code null} if not yet validated. */
    @Column(name = "VALIDATED_BY", length = 64)
    private String validatedBy;

    /** Free-text justification given by Security at validation time, or {@code null} if not yet validated. */
    @Lob
    @Column(name = "VALIDATED_REASON")
    private String validatedReason;
}
