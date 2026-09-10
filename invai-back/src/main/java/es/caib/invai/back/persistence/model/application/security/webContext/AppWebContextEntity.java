package es.caib.invai.back.persistence.model.application.security.webContext;

import lombok.*;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.application.security.core.AppSecurityEntity;
import es.caib.invai.back.persistence.model.maintenance.security.webContext.WebContextEntity;
import es.caib.invai.back.persistence.model.maintenance.general.field.FieldEntity;

import java.io.Serial;

/**
 * Persistent entity mapping an {@link AppSecurityEntity} to a {@link WebContextEntity}
 * assigned to it within a given functional {@link FieldEntity} scope.
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

    /** Security anchor this association belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_SECURITY_ID", nullable = false)
    private AppSecurityEntity appSecurity;

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
}
