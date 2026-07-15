package es.caib.invai.api.service.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Rich domain dictionary object tracking structural entity metadata labels and descriptions
 * matching distinct lifecycle state records.
 *
 * @since 1.0.0
 */
@Getter
@Setter
public class Status {

    /** Relational database lifecycle status table index configuration sequence target. */
    private Long id;

    /** Regionalized structural baseline label matching current active states (typically Catalan). */
    private String name;

    /** Alternate localized baseline label matching current active states target definitions for Spanish. */
    private String nameEs;
}