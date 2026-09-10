package es.caib.invai.back.interna.application.security.core.DTO;

import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Outbound transport payload detailing technical context mappings of the target
 * application security anchor entity definitions.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppSecurityOutputDTO {

    /** Unique identification pointer of this security anchor. */
    private Long id;
    /** Corporate application this security anchor belongs to. */
    private ApplicationOutputDTO application;
    /** Free-text observation notes attached to this security anchor. */
    private String observation;
    /** Timestamp at which this record was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}
