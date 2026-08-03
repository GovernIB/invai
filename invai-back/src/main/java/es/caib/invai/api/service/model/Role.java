package es.caib.invai.api.service.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Domain entity model configuring provider roles assignable within application development modules.
 *
 * @since 1.0.2
 */
@Getter
@Setter
public class Role {

    /** Unique database role index identifier key. */
    private Long id;

    /** Primary descriptive role title label string (typically Catalan). */
    private String name;

    /** Secondary descriptive role title label string matching Spanish locales. */
    private String nameEs;

    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
}
