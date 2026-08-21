package es.caib.invai.back.persistence.repository.maintenance.responsible.company;

import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object (DTO) capturing search filters and evaluation metrics used
 * to build dynamic database queries targeting the Company registry ecosystem.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCriteria {

    /** Company name filter used for partial, case-insensitive matching. */
    private String name;

    /**
     * Master registry identity indicator managing active vs logical soft-deleted runtime ecosystem state lifecycles.
     *
     * @see StatusEnum
     */
    private Long statusId;

    /**
     * Global text lookup variable cross-referencing keywords against the {@code name} column.
     */
    private String search;
}
