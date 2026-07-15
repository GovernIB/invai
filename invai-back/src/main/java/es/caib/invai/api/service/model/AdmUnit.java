package es.caib.invai.api.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Business domain representation of an Administrative Unit ({@code Unitat Administrativa}).
 * Contains core organizational metadata and regionalized nomenclature variants.
 *
 * @since 1.0.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdmUnit {

    /** Unique persistent sequencing index record identity. */
    private Long id;

    /** Official administrative code assignment string tracker. */
    private String code;

    /** Primary regionalized naming string parameter (typically Catalan). */
    private String name;

    /** Alternate corporate naming string mapped explicitly to Spanish. */
    private String nameEs;

    /** Timestamp marking the instance's execution path logic deactivation point. */
    private LocalDateTime deletedAt;

    /** User-land corporate audit tracker index pinpointing who triggered soft-deletion. */
    private String deletedBy;
}