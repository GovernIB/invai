package es.caib.invai.back.service.model.application.development.technology;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import es.caib.invai.back.service.model.maintenance.development.layer.Layer;
import es.caib.invai.back.service.model.maintenance.development.technology.Technology;

/**
 * Domain aggregate model representing a technology stack entry (layer, technology, version,
 * architecture) assigned to a specific {@link AppDevelopment} module.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppTechnology {
    /** Primary key of this technology stack entry. */
    private Long id;
    /** Parent development module this technology entry belongs to. */
    private AppDevelopment appDevelopment;
    /** Architecture layer catalog entry. */
    private Layer layer;
    /** Technology catalog entry. */
    private Technology technology;
    /** Version release details. */
    private String version;
    /** Architectural model description. */
    private String architecture;
    /** Timestamp when the record was created. */
    private LocalDateTime createdAt;
    /** Username of the user who created the record. */
    private String createdBy;
    /** Timestamp of the last update to the record. */
    private LocalDateTime updatedAt;
    /** Username of the user who last updated the record. */
    private String updatedBy;
    /** Timestamp when the record was logically deleted, or null if still active. */
    private LocalDateTime deletedAt;
    /** Username of the user who logically deleted the record. */
    private String deletedBy;
}
