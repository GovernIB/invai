package es.caib.invai.api.persistence.repository.application;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) capturing search filters and evaluation metrics used
 * to build dynamic database queries targeting application registry assets.
 * Evaluates basic property properties, foreign identity indexes, and composite full-text searches.
 *
 * @since 1.0.0
 */
@Getter
@Setter
public class ApplicationCriteria {

    private String prefix;
    private String applicationName;
    private Long categoryId;
    private Long systemTypeId;
    private String fieldId;
    private Long commissionId;
    private Long admUnitId;
    private String statusId;
    private String description;
    private Boolean incomplete;

    /** Alphanumeric text token applied across multiple structural string fields during comprehensive global queries. */
    private String quickSearch;
}