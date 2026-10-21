package es.caib.invai.back.interna.catalog.documentStatus.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for a
 * document status lookup entry.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentStatusOutputDTO {

    /** Unique identifier of the state entry. */
    private Long id;

    /** Fixed technical code of the state (e.g. {@code "ACTIVE"}). */
    private String code;

    /** Display label of the state (Catalan). */
    private String name;

    /** Display label of the state in Spanish. */
    private String nameEs;
}
