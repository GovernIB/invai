package es.caib.invai.back.interna.maintenance.documentation.category.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for a documentation
 * category or subcategory.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentCategoryOutputDTO {

    /** The unique data storage database auto-incremented primary key. */
    private Long id;

    /** Name of the category or subcategory (Catalan). */
    private String name;

    /** Name of the category or subcategory in Spanish. */
    private String nameEs;

    /** Identifier of the category this record belongs to, or {@code null} if it is itself a category. */
    private Long parentId;

    /**
     * Subcategories of a category (active and logically deleted ones, told apart by their
     * {@code deletedAt}), sorted by name. Empty for a subcategory.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<DocumentCategoryOutputDTO> subcategories;

    /** Timestamp at which the record was logically deleted, or {@code null} if it is active. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime deletedAt;
}
