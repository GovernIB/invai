package es.caib.invai.back.interna.catalog.appSection.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for an application
 * section lookup entry.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppSectionOutputDTO {

    /** Unique identifier of the section entry. */
    private Long id;

    /** Fixed technical code of the section (e.g. {@code "SECURITY"}). */
    private String code;

    /** Display label of the section (Catalan). */
    private String name;

    /** Display label of the section in Spanish. */
    private String nameEs;
}
