package es.caib.invai.back.service.model.catalog.appSection;

import lombok.Getter;
import lombok.Setter;

/**
 * Domain model representing one entry of the fixed lookup of the application sections (tabs) a
 * document or link can be filed under.
 *
 * @since 1.0.6
 */
@Getter
@Setter
public class AppSection {

    /** Unique identifier of the section entry. */
    private Long id;

    /** Fixed technical code of the section (e.g. {@code "SECURITY"}). */
    private String code;

    /** Display label of the section (Catalan). */
    private String name;

    /** Display label of the section in Spanish. */
    private String nameEs;
}
