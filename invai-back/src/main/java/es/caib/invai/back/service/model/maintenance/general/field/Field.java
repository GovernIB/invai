package es.caib.invai.back.service.model.maintenance.general.field;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Domain model structuring distinct functional field boundaries and operational spheres
 * across public sector assets.
 *
 * @since 1.0.1
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Field {

    /** Unique operational business field perimeter layout track index. */
    private Long id;

    /** Functional sector name mapping description parameters (typically Catalan). */
    private String name;

    /** Functional sector alternate string mapping description explicitly matching Spanish locales. */
    private String nameEs;

    /** Timestamp when this record was created. */
    private LocalDateTime createdAt;
    /** User who created this record. */
    private String createdBy;
    /** Timestamp of the last update to this record. */
    private LocalDateTime updatedAt;
    /** User who last updated this record. */
    private String updatedBy;
    /** Timestamp when this record was soft-deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
    /** User who soft-deleted this record. */
    private String deletedBy;
}