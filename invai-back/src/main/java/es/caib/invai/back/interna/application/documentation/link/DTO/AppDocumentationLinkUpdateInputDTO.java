package es.caib.invai.back.interna.application.documentation.link.DTO;

import es.caib.invai.back.utils.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) modeling the inbound payload to update a link of an application. The
 * application a link belongs to never changes.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppDocumentationLinkUpdateInputDTO {

    /** Title of the link. */
    @NotBlank(message = "{" + Constants.VALIDATION_LINK_TITLE_REQUIRED + "}")
    @Size(max = 250, message = "{" + Constants.VALIDATION_LINK_TITLE_SIZE + "}")
    private String title;

    /** Optional description of the link. */
    @Size(max = 1000, message = "{" + Constants.VALIDATION_LINK_DESCRIPTION_SIZE + "}")
    private String description;

    /** Identifier of the section of the application the link belongs to. */
    @NotNull(message = "{" + Constants.VALIDATION_LINK_SECTION_REQUIRED + "}")
    private Long sectionId;

    /** Address the link points to: an absolute {@code http} or {@code https} URL. */
    @NotBlank(message = "{" + Constants.VALIDATION_LINK_URL_REQUIRED + "}")
    @Size(max = 500, message = "{" + Constants.VALIDATION_LINK_URL_SIZE + "}")
    @Pattern(regexp = "^\\s*https?://\\S+\\s*$", message = "{" + Constants.VALIDATION_LINK_URL_INVALID + "}")
    private String url;
}
