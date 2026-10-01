package es.caib.invai.back.interna.application.responsibleAuthorized.dir3.DTO;

import es.caib.invai.back.service.model.catalog.dir3Status.Dir3ValidationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Outbound representation of a {@code Dir3Validation} record, returned by the manual-validation
 * endpoint. The same record is also embedded (as the domain model) on
 * {@code AppResponsibleOutputDTO}/{@code AppAuthorizedOutputDTO}.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Dir3ValidationOutputDTO {

    /** Unique identifier. */
    private Long id;

    /** Current DIR3 validation state. */
    private Dir3ValidationStatus dir3Status;

    /** Timestamp of the manual DIR3 validation, populated only when {@link #dir3Status} is MANUAL. */
    private LocalDateTime manualValidatedAt;

    /** User who performed the manual DIR3 validation. */
    private String manualValidatedBy;

    /** Free-text reason given when manually validating, populated only when {@link #dir3Status} is MANUAL. */
    private String reason;
}
