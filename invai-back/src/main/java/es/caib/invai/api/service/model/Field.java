package es.caib.invai.api.service.model;

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

    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
}