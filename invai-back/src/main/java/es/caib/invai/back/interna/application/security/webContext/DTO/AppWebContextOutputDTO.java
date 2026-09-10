package es.caib.invai.back.interna.application.security.webContext.DTO;

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
    /** Web context catalog entry assigned to the security anchor. */
    private WebContextOutputDTO webContext;
    /** Functional field/scope within which the web context is assigned. */
    private FieldOutputDTO field;
    /** Free-text observation notes attached to this association. */
    private String observation;
    /** Timestamp at which this record was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}
