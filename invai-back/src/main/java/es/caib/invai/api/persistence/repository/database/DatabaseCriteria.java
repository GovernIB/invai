package es.caib.invai.api.persistence.repository.database;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object (DTO) capturing search filters and evaluation metrics used
 * to build dynamic database queries targeting system database assets within the
 * infrastructure inventory ecosystem.
 * <p>
 * Evaluates basic functional property tokens, host server relationship references, and maps
 * composite global keyword filtering definitions across relevant descriptive columns.
 * </p>
 *
 * @since 1.0.2
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseCriteria {

    /**
     * Exact host server primary identifier filter matching the linked {@code ServerEntity}.
     */
    private Long serverId;

    /**
     * Partial functional service schema identifier token filter (e.g., "invai_db").
     */
    private String service;

    /**
     * Exact database vendor/type primary identifier filter matching the linked {@code DatabaseVendorEntity}.
     */
    private Long databaseTypeId;

    /**
     * Master registry identity indicator managing active vs logical soft-deleted
     * runtime ecosystem state lifecycles.
     *
     * @see es.caib.invai.api.service.model.StatusEnum
     */
    private Long statusId;

    /**
     * Global text lookup variable cross-referencing keywords concurrently across all functional
     * columns ({@code service} and {@code description}) inside the programmatic criteria context.
     */
    private String search;
}
