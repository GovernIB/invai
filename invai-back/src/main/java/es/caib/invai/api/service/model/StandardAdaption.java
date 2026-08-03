package es.caib.invai.api.service.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Rich domain dictionary object tracking structural entity metadata labels and descriptions
 * matching distinct GOIB standards compliance level lookup records.
 *
 * @since 1.0.2
 */
@Getter
@Setter
public class StandardAdaption {

    /** Relational database compliance lookup table index configuration sequence target. */
    private Long id;

    /** Regionalized structural baseline label matching the compliance level (typically Catalan). */
    private String name;

    /** Alternate localized baseline label matching the compliance level for Spanish. */
    private String nameEs;
}
