package es.caib.invai.back.interna.application.documentation.link.DTO;

import es.caib.invai.back.interna.catalog.appSection.DTO.AppSectionOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) modeling a link of an application: one row of the "Enllaços" list.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppDocumentationLinkOutputDTO {

    /** Unique identifier of the link. */
    private Long id;

    /** Title of the link. */
    private String title;

    /** Description of the link, or {@code null} if it has none. */
    private String description;

    /** Section of the application the link belongs to. */
    private AppSectionOutputDTO section;

    /** Address the link points to. */
    private String url;

    /** Timestamp at which the link was logically deleted, or {@code null} if it is not deleted. */
    private LocalDateTime deletedAt;
}
