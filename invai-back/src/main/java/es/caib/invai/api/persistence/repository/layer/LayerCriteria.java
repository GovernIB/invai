package es.caib.invai.api.persistence.repository.layer;

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

    private String name;

    /**
     * @see es.caib.invai.api.service.model.StatusEnum
     */
    private Long statusId;

    private String search;
}
