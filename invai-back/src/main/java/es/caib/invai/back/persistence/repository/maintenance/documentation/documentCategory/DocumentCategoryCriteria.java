package es.caib.invai.back.persistence.repository.maintenance.documentation.documentCategory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Search filters used to build the dynamic query that lists the documentation categories (the
 * records without a parent). A category is found when it, or any of its subcategories, fulfils
 * the {@code name} or {@code search} text. The status applies to the subcategories too, and each
 * category comes with the subcategories that match the text, or with all of them when the category
 * itself matches.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentCategoryCriteria {

    /**
     * Name filter used for partial, case-insensitive matching on the Catalan or Spanish name of the
     * category or of any of its subcategories.
     */
    private String name;

    /** Active vs logically deleted state of the categories to return, {@code null} for both. */
    private Long statusId;

    /**
     * Global text lookup matching the name (Catalan or Spanish) of the category or of any of its
     * subcategories, partial and case-insensitive.
     */
    private String search;
}
