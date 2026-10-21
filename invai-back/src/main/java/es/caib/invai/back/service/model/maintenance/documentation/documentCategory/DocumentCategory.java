package es.caib.invai.back.service.model.maintenance.documentation.documentCategory;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Domain model representing a documentation category or subcategory. A category has no parent; a
 * subcategory belongs to exactly one category, so the hierarchy has two levels at most.
 *
 * @since 1.0.6
 */
@Getter
@Setter
public class DocumentCategory {

    /** Unique database identifier key. */
    private Long id;

    /** Name of the category or subcategory (Catalan). */
    private String name;

    /** Name of the category or subcategory in Spanish. */
    private String nameEs;

    /** Identifier of the category this record belongs to, or {@code null} if it is itself a category. */
    private Long parentId;

    /**
     * Subcategories of this category (active and logically deleted ones), sorted by name. Only
     * filled in by the repository when this record is a category; always empty for a subcategory.
     */
    private List<DocumentCategory> subcategories = new ArrayList<>();

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

    /**
     * Tells whether this record is a category (it has no parent) rather than a subcategory.
     *
     * @return {@code true} if it is a category
     */
    public boolean isCategory() {
        return Objects.isNull(parentId);
    }
}
