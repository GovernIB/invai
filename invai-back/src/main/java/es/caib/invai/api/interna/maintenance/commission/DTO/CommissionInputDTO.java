package es.caib.invai.api.interna.maintenance.commission.DTO;

import es.caib.invai.api.service.model.CommissionType;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Getter
@Setter
public class CommissionInputDTO {

    @NotBlank(message = "{validation.commission.name.required}")
    @Size(max = 100, message = "{validation.commission.name.size}")
    private String name;

    @NotBlank(message = "{validation.commission.nameEs.required}")
    @Size(max = 100, message = "{validation.commission.nameEs.size}")
    private String nameEs;

    @NotBlank(message = "{validation.commission.expedientNumber.required}")
    @Size(max = 50, message = "{validation.commission.expedientNumber.size}")
    private String expedientNumber;

    @NotNull(message = "{validation.commission.approvalDate.required}")
    private LocalDate approvalDate;

    /**
     * Strongly typed classification level of the commission panel.
     */
    @NotNull(message = "{validation.commission.commissionType.required}")
    private CommissionType commissionType;
}