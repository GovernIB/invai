package es.caib.invai.back.interna.maintenance.general.commission.DTO;

import es.caib.invai.back.utils.Constants;

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
    @NotBlank(message = "{" + Constants.VALIDATION_COMMISSION_NAME_REQUIRED + "}")
    @Size(max = 100, message = "{" + Constants.VALIDATION_COMMISSION_NAME_SIZE + "}")
    private String name;

    /** Descriptive name of the commission in Spanish. */
    @NotBlank(message = "{" + Constants.VALIDATION_COMMISSION_NAME_ES_REQUIRED + "}")
    @Size(max = 100, message = "{" + Constants.VALIDATION_COMMISSION_NAME_ES_SIZE + "}")
    private String nameEs;

    /** Unique administrative dossier tracking code identifying the commission. */
    @NotBlank(message = "{" + Constants.VALIDATION_COMMISSION_EXPEDIENT_NUMBER_REQUIRED + "}")
    @Size(max = 50, message = "{" + Constants.VALIDATION_COMMISSION_EXPEDIENT_NUMBER_SIZE + "}")
    private String expedientNumber;

    /** Date on which the commission was formally approved. */
    @NotNull(message = "{" + Constants.VALIDATION_COMMISSION_APPROVAL_DATE_REQUIRED + "}")
    private LocalDate approvalDate;

    /**
     * Strongly typed classification level of the commission panel.
     */
    @NotNull(message = "{" + Constants.VALIDATION_COMMISSION_TYPE_REQUIRED + "}")
    private CommissionType commissionType;
}