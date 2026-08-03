package es.caib.invai.api.service.model;

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

    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
}