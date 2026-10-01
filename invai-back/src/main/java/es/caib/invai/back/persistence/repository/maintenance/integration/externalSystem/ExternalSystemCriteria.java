package es.caib.invai.back.persistence.repository.maintenance.integration.externalSystem;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object (DTO) capturing search filters and evaluation metrics used
 * to build dynamic database queries targeting the External System registry ecosystem.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExternalSystemCriteria {

    /** External system name filter used for partial, case-insensitive matching. */
    private String name;

    /** Identifier of the responsible company to filter by. */
    private Long companyId;

    /**
     * Master registry identity indicator managing active vs logical soft-deleted runtime ecosystem state lifecycles.
     */
    private Long statusId;

    /**
     * Global text lookup variable cross-referencing keywords against the {@code name} column.
     */
    private String search;
}
