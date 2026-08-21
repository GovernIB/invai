package es.caib.invai.back.persistence.repository.maintenance.development.technology;

import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object (DTO) capturing search filters and evaluation metrics used
 * to build dynamic database queries targeting the catalog Technology registry ecosystem.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TechnologyCriteria {

    /** Name filter matched as a case-insensitive partial match against the technology name. */
    private String name;

    /** Filter restricting results to technologies belonging to this architecture layer. */
    private Long layerId;

    /**
     * @see StatusEnum
     */
    private Long statusId;

    /** Free-text search term matched as a case-insensitive partial match against the technology name. */
    private String search;
}
