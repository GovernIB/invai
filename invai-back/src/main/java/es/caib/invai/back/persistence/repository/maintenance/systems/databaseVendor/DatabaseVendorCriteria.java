package es.caib.invai.back.persistence.repository.maintenance.systems.databaseVendor;

import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object (DTO) capturing search filters and evaluation metrics used
 * to build dynamic database queries targeting database vendor/type catalog assets within the
 * infrastructure environments ecosystem.
 * <p>
 * Evaluates basic functional property tokens, connectivity default port metrics, and maps composite
 * global keyword filtering definitions across isolated catalog columns.
 * </p>
 *
 * @author CAIB Architecture Team
 * @version 1.0.0
 * @since 2026-07-28
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseVendorCriteria{

    /**
     * Exact or partial functional catalog identifier token filter (e.g., "Oracle", "PostgreSQL").
     */
    private String name;

    /**
     * Exact numeric connectivity default port filter parameter used for precise vendor lookups.
     */
    private Integer defaultPort;

    /**
     * Master registry identity indicator managing active vs logical soft-deleted
     * runtime ecosystem state lifecycles.
     *
     * @see StatusEnum
     */
    private Long statusId;

    /**
     * Global text lookup variable cross-referencing keywords across functional
     * columns ({@code name}) inside the programmatic criteria context.
     */
    private String search;
}
