package es.caib.invai.back.persistence.repository.maintenance.general.commission;

import es.caib.invai.back.service.model.maintenance.general.commission.CommissionType;
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

    /** Filters results by a partial, case-insensitive match on the commission's name. */
    private String name;
    /** Filters results by a partial, case-insensitive match on the commission's Spanish name. */
    private String nameEs;
    /** Filters results by an exact match on the commission's expedient dossier tracking code. */
    private String expedientNumber;
    /** Filters results by the commission's classification type. */
    private CommissionType commissionType;
    /** Filters results by an exact match on the commission's approval date. */
    private LocalDate approvalDate;
    /** Filters results by active/inactive status, based on the {@link es.caib.invai.back.service.model.catalog.status.StatusEnum} identifier. */
    private Long statusId;

    /** Free-text keyword applied across name, Spanish name, and expedient number fields. */
    private String search;
}