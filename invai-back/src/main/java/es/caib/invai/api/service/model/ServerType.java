package es.caib.invai.api.service.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Rich domain dictionary object tracking structural entity metadata labels and descriptions
 * matching distinct server type lookup records.
 *
 * @since 1.0.2
 */
@Getter
@Setter
public class ServerType {

    /** Relational database server type lookup table index configuration sequence target. */
    private Long id;

    /** Stable technical discriminator code for the server type (e.g., DATABASE, APPLICATION). */
    private String code;

    /** Regionalized structural baseline label matching the server type (typically Catalan). */
    private String name;

    /** Alternate localized baseline label matching the server type for Spanish. */
    private String nameEs;
}
