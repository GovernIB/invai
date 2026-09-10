package es.caib.invai.back.interna.maintenance.security.personalDataProcessing.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for an PersonalDataProcessing resource.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PersonalDataProcessingOutputDTO {

    /** The unique data storage database auto-incremented primary key. */
    private Long id;

    /** Personal data processing entry descriptive label string. */
    private String name;

    /** The translated Spanish localized descriptive name of the personal data processing entry. */
    private String nameEs;

    /** Timestamp when the personal data processing entry was logically deleted, or {@code null} if it is still active. */
    private LocalDateTime deletedAt;
}
