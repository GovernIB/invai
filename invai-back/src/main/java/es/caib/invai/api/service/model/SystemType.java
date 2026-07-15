package es.caib.invai.api.service.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Domain business model tracking standard system architectural types, engine patterns,
 * or technology runtime tiers.
 *
 * @since 1.0.0
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

    /** Audit validation tracking clock metric indexing instance soft-deletion points. */
    private LocalDateTime deletedAt;

    /** Corporate actor tracking key string documenting active system deletion authors. */
    private String deletedBy;
}