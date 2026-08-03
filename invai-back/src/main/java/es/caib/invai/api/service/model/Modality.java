package es.caib.invai.api.service.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Rich domain dictionary object tracking structural entity metadata labels and descriptions
 * matching distinct development modality lookup records.
 *
 * @since 1.0.2
 */
@Getter
@Setter
public class Modality {

    /** Relational database modality lookup table index configuration sequence target. */
    private Long id;

    /** Regionalized structural baseline label matching the modality (typically Catalan). */
    private String name;

    /** Alternate localized baseline label matching the modality for Spanish. */
    private String nameEs;
}
