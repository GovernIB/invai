package es.caib.invai.back.interna.maintenance.general.commission.DTO;

import es.caib.invai.back.service.model.maintenance.general.commission.CommissionType;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) defining the inbound API request payload structure for
 * registering or modifying a Commission catalog record.
 *
 * @since 1.0.1
 */
@Getter
@Setter
public class CommissionInputDTO {

    /** Descriptive name of the commission, typically in Catalan. */
    @NotBlank(message = "{validation.commission.name.required}")
    @Size(max = 100, message = "{validation.commission.name.size}")
    private String name;

    /** Descriptive name of the commission in Spanish. */
    @NotBlank(message = "{validation.commission.nameEs.required}")
    @Size(max = 100, message = "{validation.commission.nameEs.size}")
    private String nameEs;

    /** Unique administrative dossier tracking code identifying the commission. */
    @NotBlank(message = "{validation.commission.expedientNumber.required}")
    @Size(max = 50, message = "{validation.commission.expedientNumber.size}")
    private String expedientNumber;

    /** Date on which the commission was formally approved. */
    @NotNull(message = "{validation.commission.approvalDate.required}")
    private LocalDate approvalDate;

    /**
     * Strongly typed classification level of the commission panel.
     */
    @NotNull(message = "{validation.commission.commissionType.required}")
    private CommissionType commissionType;
}