package es.caib.invai.back.interna.maintenance.documentation.category.DTO;

import es.caib.invai.back.utils.Constants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) defining the inbound API request payload structure for registering
 * or modifying a documentation category or subcategory.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentCategoryInputDTO {

    /** Name of the category or subcategory (Catalan). */
    @NotBlank(message = "{" + Constants.VALIDATION_DOCUMENTCATEGORY_NAME_REQUIRED + "}")
    @Size(max = 150, message = "{" + Constants.VALIDATION_DOCUMENTCATEGORY_NAME_SIZE + "}")
    private String name;

    /** Name of the category or subcategory in Spanish. */
    @NotBlank(message = "{" + Constants.VALIDATION_DOCUMENTCATEGORY_NAME_ES_REQUIRED + "}")
    @Size(max = 150, message = "{" + Constants.VALIDATION_DOCUMENTCATEGORY_NAME_ES_SIZE + "}")
    private String nameEs;

    /**
     * Identifier of the category this record belongs to: {@code null} to register a category,
     * the identifier of an active category to register a subcategory of it.
     */
    private Long parentId;
}
