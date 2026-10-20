package es.caib.invai.back.interna.application.integration.core.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for an integration
 * anchor. Its rows (see {@code AppIntegrationConnectionController}) are fetched separately, paginated by
 * {@code appIntegrationId}, the same way {@code AppEnsClassification} rows are fetched separately
 * from their {@code AppSecurity} anchor.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppIntegrationOutputDTO {

    /** The unique data storage database auto-incremented primary key. */
    private Long id;

    /** Identifier of the application this anchor belongs to. */
    private Long applicationId;

    /** Free-text observations about this application's integrations. */
    private String observation;

    /** Timestamp at which this record was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}
