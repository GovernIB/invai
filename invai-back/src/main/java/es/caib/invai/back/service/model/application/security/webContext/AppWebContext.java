package es.caib.invai.back.service.model.application.security.webContext;

import lombok.*;

import java.time.LocalDateTime;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
import es.caib.invai.back.service.model.maintenance.security.webContext.WebContext;
import es.caib.invai.back.service.model.maintenance.general.field.Field;

/**
 * Domain model representing the association between an {@link AppSecurity} anchor
 * and a {@link WebContext} assigned to it within a given {@link Field} scope.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppWebContext {
    /** Unique identification pointer of this application-web context association. */
    private Long id;
    /** Security anchor this association belongs to. */
    private AppSecurity appSecurity;
    /** Development anchor this association belongs to - the only anchor CRUD is exposed from. */
    private AppDevelopment appDevelopment;
    /** Web context catalog entry assigned to the security anchor. */
    private WebContext webContext;
    /** Functional field/scope within which the web context is assigned. */
    private Field field;
    /** Free-text observation notes attached to this association. */
    private String observation;
    /** URL of the web context. */
    private String url;
    /** Whether Security has validated this web context. One-way: never reset back to {@code false}. */
    private boolean validated;
    /** Timestamp at which Security validated this web context, or {@code null} if not yet validated. */
    private LocalDateTime validatedAt;
    /** Identifier of the user who validated this web context, or {@code null} if not yet validated. */
    private String validatedBy;
    /** Free-text justification given by Security at validation time, or {@code null} if not yet validated. */
    private String validatedReason;
    /** Timestamp at which this record was created. */
    private LocalDateTime createdAt;
    /** Identifier of the user who created this record. */
    private String createdBy;
    /** Timestamp at which this record was last updated. */
    private LocalDateTime updatedAt;
    /** Identifier of the user who last updated this record. */
    private String updatedBy;
    /** Timestamp at which this record was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
    /** Identifier of the user who logically deleted this record, or {@code null} if still active. */
    private String deletedBy;
}
