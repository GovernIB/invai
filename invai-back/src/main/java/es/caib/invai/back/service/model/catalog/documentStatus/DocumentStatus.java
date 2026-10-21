package es.caib.invai.back.service.model.catalog.documentStatus;

import lombok.Getter;
import lombok.Setter;

/**
 * Domain model representing one entry of the fixed lookup of the states of a document.
 *
 * @since 1.0.6
 */
@Getter
@Setter
public class DocumentStatus {

    /** Unique identifier of the state entry. */
    private Long id;

    /** Fixed technical code of the state (e.g. {@code "ACTIVE"}). */
    private String code;

    /** Display label of the state (Catalan). */
    private String name;

    /** Display label of the state in Spanish. */
    private String nameEs;
}
