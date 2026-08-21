package es.caib.invai.back.service.model.maintenance.admUnit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Business domain representation of an Administrative Unit ({@code Unitat Administrativa}).
 * Contains core organizational metadata and regionalized nomenclature variants.
 *
 * @since 1.0.1
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

    /** Timestamp marking when the administrative unit record was created. */
    private LocalDateTime createdAt;
    /** Username of the user who created the administrative unit record. */
    private String createdBy;
    /** Timestamp marking the last modification made to the administrative unit record. */
    private LocalDateTime updatedAt;
    /** Username of the user who last modified the administrative unit record. */
    private String updatedBy;
    /** Timestamp marking when the administrative unit record was logically soft-deleted, or {@code null} if active. */
    private LocalDateTime deletedAt;
    /** Username of the user who logically soft-deleted the administrative unit record, or {@code null} if active. */
    private String deletedBy;
}