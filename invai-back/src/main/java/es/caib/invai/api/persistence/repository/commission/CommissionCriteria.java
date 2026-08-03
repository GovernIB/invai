package es.caib.invai.api.persistence.repository.commission;

import es.caib.invai.api.service.model.CommissionType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) capturing search filters and evaluation metrics used
 * to build dynamic database queries targeting application registry assets.
 * Evaluates basic property properties, foreign identity indexes, and composite full-text searches.
 *
 * @since 1.0.1
 */
@Getter
@Setter
public class CommissionCriteria {

    private String name;
    private String nameEs;
    private String expedientNumber;
    private CommissionType commissionType;
    private LocalDate approvalDate;
    private Long statusId;

    private String search;
}