package es.caib.invai.back.interna.maintenance.general.commission.DTO;

import es.caib.invai.back.service.model.maintenance.general.commission.CommissionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for a Commission
 * catalog record.
 *
 * @since 1.0.1
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommissionOutputDTO {

    /** Unique identifier of the commission. */
    private Long id;
    /** Descriptive name of the commission, typically in Catalan. */
    private String name;
    /** Descriptive name of the commission in Spanish. */
    private String nameEs;
    /** Unique administrative dossier tracking code identifying the commission. */
    private String expedientNumber;
    /** Date on which the commission was formally approved. */
    private LocalDate approvalDate;
    /** Classification level of the commission panel. */
    private CommissionType commissionType;
    /** Timestamp marking when the commission was soft-deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}