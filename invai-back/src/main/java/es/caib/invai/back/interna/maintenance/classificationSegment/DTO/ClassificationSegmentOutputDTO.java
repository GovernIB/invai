package es.caib.invai.back.interna.maintenance.classificationSegment.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for a ClassificationSegment resource.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClassificationSegmentOutputDTO {

    /** The unique data storage database auto-incremented primary key. */
    private Long id;

    /** Classification segment descriptive label string. */
    private String name;

    /** The translated Spanish localized descriptive name of the classification segment entry. */
    private String nameEs;

    /** Timestamp when the classification segment entry was logically deleted, or {@code null} if it is still active. */
    private LocalDateTime deletedAt;
}
