package es.caib.invai.back.service.model.maintenance.general.category;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Domain entity model configuring taxonomic catalog classifications
 * applied across business application assets.
 *
 * @since 1.0.1
 */
@Getter
@Setter
public class Category {

    /** Unique database classification category index identifier key. */
    private Long id;

    /** Primary descriptive classification title label string (typically Catalan). */
    private String name;

    /** Secondary descriptive classification title label string matching Spanish locales. */
    private String nameEs;

    /** Timestamp marking when the category record was created. */
    private LocalDateTime createdAt;
    /** Username of the user who created the category record. */
    private String createdBy;
    /** Timestamp marking the last modification made to the category record. */
    private LocalDateTime updatedAt;
    /** Username of the user who last modified the category record. */
    private String updatedBy;
    /** Timestamp at which the category was logically soft-deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
    /** Username of the user who logically soft-deleted the category record. */
    private String deletedBy;
}