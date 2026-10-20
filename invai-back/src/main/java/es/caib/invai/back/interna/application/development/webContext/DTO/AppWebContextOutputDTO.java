package es.caib.invai.back.interna.application.development.webContext.DTO;

import es.caib.invai.back.interna.application.development.core.DTO.DevelopmentOutputDTO;
import es.caib.invai.back.interna.application.security.core.DTO.AppSecurityOutputDTO;
import es.caib.invai.back.interna.maintenance.security.webContext.DTO.WebContextOutputDTO;
import es.caib.invai.back.interna.maintenance.general.field.DTO.FieldOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Outbound transport payload detailing technical context mappings of the target
 * application web context entity definitions.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppWebContextOutputDTO {

    /** Unique identification pointer of this application-web context association. */
    private Long id;
    /** Security anchor this association belongs to. */
    private AppSecurityOutputDTO appSecurity;
    /** Development anchor this association belongs to - the only anchor CRUD is exposed from. */
    private DevelopmentOutputDTO appDevelopment;
    /** Web context catalog entry assigned to the security anchor. */
    private WebContextOutputDTO webContext;
    /** Functional field/scope within which the web context is assigned. */
    private FieldOutputDTO field;
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
    /** Timestamp at which this record was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}
