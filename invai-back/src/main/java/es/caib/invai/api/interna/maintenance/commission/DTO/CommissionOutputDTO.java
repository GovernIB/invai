package es.caib.invai.api.interna.maintenance.commission.DTO;

import es.caib.invai.api.service.model.CommissionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommissionOutputDTO {

    private Long id;
    private String name;
    private String nameEs;
    private String expedientNumber;
    private LocalDate approvalDate;
    private CommissionType commissionType;
    private LocalDateTime deletedAt;
}