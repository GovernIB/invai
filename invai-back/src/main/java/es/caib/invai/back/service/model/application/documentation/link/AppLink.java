package es.caib.invai.back.service.model.application.documentation.link;

import es.caib.invai.back.service.model.catalog.appSection.AppSection;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Domain model representing a link of an application (the "Enllaços" list of the Documentació tab).
 *
 * @since 1.0.6
 */
@Getter
@Setter
public class AppLink {

    /** Unique database identifier key. */
    private Long id;

    /** Identifier of the application the link belongs to. */
    private Long applicationId;

    /** Title of the link. */
    private String title;

    /** Optional description of the link. */
    private String description;

    /** Section of the application the link belongs to. */
    private AppSection section;

    /** Address the link points to. */
    private String url;

    /** Timestamp at which the record was created. */
    private LocalDateTime createdAt;
    /** Username of the user who created the record. */
    private String createdBy;
    /** Timestamp at which the record was last updated. */
    private LocalDateTime updatedAt;
    /** Username of the user who last updated the record. */
    private String updatedBy;
    /** Timestamp at which the record was logically deleted, or {@code null} if it is active. */
    private LocalDateTime deletedAt;
    /** Username of the user who logically deleted the record. */
    private String deletedBy;
}
