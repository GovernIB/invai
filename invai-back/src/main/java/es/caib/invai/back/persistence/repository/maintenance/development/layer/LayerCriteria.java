package es.caib.invai.back.persistence.repository.maintenance.development.layer;

import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object (DTO) capturing search filters and evaluation metrics used
 * to build dynamic database queries targeting the architecture Layer registry ecosystem.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LayerCriteria {

    /** Partial layer name filter. */
    private String name;

    /**
     * @see StatusEnum
     */
    private Long statusId;

    /** Global text lookup filter matching the {@code name} column. */
    private String search;
}
