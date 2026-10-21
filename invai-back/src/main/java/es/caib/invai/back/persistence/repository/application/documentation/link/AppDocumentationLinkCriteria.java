package es.caib.invai.back.persistence.repository.application.documentation.link;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Search filters used to build the dynamic query that lists the links of an application, deleted
 * ones included unless the {@code deleted} filter says otherwise. Every filter is optional except
 * {@code applicationId}, which scopes the listing.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppDocumentationLinkCriteria {

    /** Identifier of the application whose links are listed. */
    private Long applicationId;

    /** Partial, case-insensitive filter on the title of the link. */
    private String title;

    /** Partial, case-insensitive filter on the description of the link. */
    private String description;

    /** Identifier of the section the links belong to. */
    private Long sectionId;

    /** Partial, case-insensitive filter on the address of the link. */
    private String url;

    /**
     * Global text lookup, partial and case-insensitive, matching the title, the description, the
     * address or the name of the section of the link.
     */
    private String quickSearch;

    /**
     * Logical deletion of the links: {@code true} lists only the deleted ones, {@code false} only
     * the ones that are not deleted, and {@code null} (the default) lists all of them.
     */
    private Boolean deleted;
}
