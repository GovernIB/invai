package es.caib.invai.api.service.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Domain business model tracking standard system architectural types, engine patterns,
 * or technology runtime tiers.
 *
 * @since 1.0.2
 */
@Getter
@Setter
public class SystemType {

    /** Unique persistent sequence structural index row key mapping technology configurations. */
    private Long id;

    /** System category architecture definition designator label string (typically Catalan). */
    private String name;

    /** System category architecture alternate variant definition designator layout string matching Spanish. */
    private String nameEs;

    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
}